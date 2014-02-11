package me.prokopyl.commandtools;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;


public class CommandTools extends JavaPlugin implements Listener 
{
static private CommandTools plugin;
private final ToolStore toolStore;


public CommandTools() 
{
    plugin = this;
    toolStore = new ToolStore();
}

static public CommandTools getPlugin()
{
    return plugin;
}


@Override
public void onEnable()
{
    Bukkit.getPluginManager().registerEvents(this, this);
    Bukkit.getPluginManager().registerEvents(new ToolEditor(this), this);
    
}

@Override
public void onDisable()
{
    toolStore.save();
}

@Override
public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
{
    if(!cmd.getName().equalsIgnoreCase("ctool")) return false;
    if(!(sender instanceof Player))
    {
        sender.sendMessage("§cYou must be a player to use Command Tools.");
        return true;
    }
    if(args.length <= 0)
    {
        sender.sendMessage("§cNot enough arguments.");
        return true;
    }
    
    onCToolCommand((Player)sender, args);
    
    return true;
}

@EventHandler(priority=EventPriority.HIGH)
public void onPlayerUse(PlayerInteractEvent event)
{
    if(event.getAction() != Action.RIGHT_CLICK_AIR) return;
    if(!CommandTool.isCommandTool(event.getItem())) return;
    
    if(!event.getPlayer().hasPermission("commandtools.ctools"))
    {
        event.getPlayer().sendMessage("§cYou are not allowed to use Command Tools.");
        return;
    }
    
    ItemStack itemTool = event.getItem();
    
    CommandTool tool = toolStore.getTool(itemTool);
    
    if(tool == null)
    {
        event.getPlayer().sendMessage("§4This tool does not exist in the Tool Database. This may indicate a corrupted savefile or tool database.");
        event.getPlayer().sendMessage("§4You can safely use §c/ctool delete §4to delete this tool.");
        return;
    }
    
    if(!(tool.getOwnerName().equals(event.getPlayer().getName())))
    {
        tool = toolStore.cloneTool(itemTool, event.getPlayer().getName());
        event.getPlayer().setItemInHand(tool.createItem());
    }
    
    tool.use(event.getPlayer());
    event.setCancelled(true);
}

@EventHandler(priority=EventPriority.HIGH)
public void onItemDrop(PlayerDropItemEvent event)
{
    ItemStack itemTool = event.getItemDrop().getItemStack();
    if(CommandTool.isCommandTool(itemTool))
    {
        //Must override NBT Data before dropping the item !
        ItemStack newItem = toolStore.getTool(NBTUtils.getCommandToolOwner(itemTool), NBTUtils.getCommandToolID(itemTool)).createItem();
        event.getItemDrop().setItemStack(newItem);
    }
}

public void onCToolCommand(Player sender, String[] args)
{try{
    
    if(args[0].equalsIgnoreCase("new"))
    {
        newCommandTool(sender);
    }
    else if(args[0].equalsIgnoreCase("edit"))
    {
        ItemStack itemTool = sender.getItemInHand();
        
        if(!CommandTool.isCommandTool(itemTool)) return;
        
        CommandTool tool = toolStore.getTool(itemTool);
        sender.setItemInHand(ToolEditor.createEditedCommandTool(tool));
    }
    else if(args[0].equalsIgnoreCase("rename"))
    {
        renameTool(sender, args);
    }
    else if(args[0].equalsIgnoreCase("get"))
    {
        if(args.length < 2)
        {
            return;
        }
        giveTool(sender, args[1]);
    }
    else if(args[0].equalsIgnoreCase("list"))
    {
        listTools(sender);
    }
    else if(args[0].equalsIgnoreCase("delete"))
    {
        deleteTool(sender, sender.getItemInHand());
    }
    else
    {
        sender.sendMessage("§cUnknown action.");
    }
} catch(Exception e) {
    Logger.getLogger(CommandTool.class.getName()).log(Level.SEVERE, null, e);
    sender.sendMessage("§4An error occured while executing the requested action. See server log for more information.");
}}

public CommandTool getTool(String playerName, String toolId)
{
    return toolStore.getTool(playerName, toolId);
}

private void newCommandTool(Player player)
{
    Inventory playerInventory = player.getInventory();
    ItemStack itemInHand = player.getItemInHand();
    if(itemInHand.getType() == Material.AIR)
    {
        player.sendMessage("§cYou must have an item in hand in order to get a tool.");
        return;
    }
    int firstEmptySlot = playerInventory.firstEmpty();
    if(firstEmptySlot < 0)
    {
        player.sendMessage("§cYour inventory is full ! You must have some space left in order to get a tool.");
        return;
    }
    
    CommandTool newTool = new CommandTool(toolStore.getNextAvailableToolID("Tool", player.getName()), "Tool", itemInHand.getType(), player.getName());
    toolStore.addTool(newTool);
    
    playerInventory.setItem(firstEmptySlot, itemInHand);
    player.setItemInHand(newTool.createItem());
    
}

private void giveTool(Player player, String toolName)
{
    if(player.getInventory().firstEmpty() < 0)
    {
        player.sendMessage("§cYour inventory is full ! You must have some space left in order to get a tool.");
        return;
    }
    
    CommandTool tool = toolStore.getTool(player.getName(), toolName);
    
    if(tool == null)
    {
        player.sendMessage("§cThis tool does not exist.");
        return;
    }
    
    player.getInventory().addItem(tool.createItem());
}

private void listTools(Player player)
{
    List<CommandTool> toolList = toolStore.getToolList(player.getName());
    if(toolList == null|| toolList.isEmpty())
    {
        player.sendMessage("§7No tool found.");
        return;
    }
    
    player.sendMessage("§7" + toolList.size() + " tools found.");
    
    String sToolList = toolList.get(0).getId();
    for(int i = 1; i < toolList.size(); i++)
    {
        sToolList += "§7, §r" + toolList.get(i).getId();
    }
    player.sendMessage(sToolList);
}

private void deleteTool(Player player, ItemStack item)
{
    if(!CommandTool.isCommandTool(item))
    {
        player.sendMessage("§4This is not a valid command tool.");
        return;
    }
    CommandTool tool = toolStore.getTool(item);
    
    if(tool != null) toolStore.deleteTool(tool);
    
    player.setItemInHand(new ItemStack(Material.AIR));
    player.sendMessage("§7Tool successfully deleted.");
}

public void renameTool(Player player, String[] args)
{
    CommandTool tool = getToolInHand(player);
    if(tool == null) return;
    
    String sNewName = "";
    
    if(args.length < 2)
    {
        player.sendMessage("§cYou must give a name to your tool.");
        return;
    }
    
    for(int i = 1; i < args.length; i++)
    {
        sNewName += args[i] + " ";
    }
    sNewName = sNewName.trim();
    
    tool.rename(toolStore.getNextAvailableToolID(sNewName, player.getName()), sNewName);
    
    player.setItemInHand(tool.createItem());
    
}

public void runVirtualPlayerCommands(List<String> sCommands, String sPlayerName, Location hLocation, CommandTool tool)
{
    toolStore.runVirtualPlayerCommands(sCommands, sPlayerName, hLocation, tool);
}

private CommandTool getToolInHand(Player player)
{
    ItemStack itemInHand = player.getItemInHand();
    if(itemInHand.getType() == Material.AIR || !CommandTool.isCommandTool(itemInHand))
    {
        player.sendMessage("§cYou must have a tool in hand.");
        return null;
    }
    
    CommandTool tool = toolStore.getTool(itemInHand);
    if(tool == null)
    {
        player.sendMessage("§4This tool does not exist in the Tool Database. This may indicate a corrupted savefile or tool database .");
        //player.sendMessage("§4You can safely use §c/ctool delete §4to delete this tool.");
        return null;
    }
    
    return tool;
    
}

@EventHandler(priority=EventPriority.HIGH)
public void fixNBTInventoryEventWTF(InventoryClickEvent event)
{
    if(!(event.getWhoClicked() instanceof Player)) return;
    Player player = (Player)event.getWhoClicked();
    
    if(!(event.getAction() == InventoryAction.PLACE_ALL && event.getClick() == ClickType.CREATIVE && event.getSlotType() == SlotType.QUICKBAR))
    {
        return;
    }
    
    if(CommandTool.isCommandTool(event.getCurrentItem()))
    {
        CommandTool tool = toolStore.getTool(event.getCurrentItem());
        if(tool != null)
        {
            if(tool.getFreshTool())
            {
                event.setCancelled(true);
                tool.setFreshTool(false);
            }
        }
    }
    
}
@EventHandler(priority=EventPriority.HIGH)
public void playerLogin(PlayerLoginEvent event)
{
    if(!event.getPlayer().hasPermission("commandtools.ctools")) return;
    toolStore.setPlayersToolFresh(event.getPlayer().getName(), true);
}

}
