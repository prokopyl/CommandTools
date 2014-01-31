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
    
    ItemStack itemTool = event.getItem();
    
    CommandTool tool = toolStore.getTool(NBTUtils.getCommandToolOwner(itemTool), NBTUtils.getCommandToolID(itemTool));
    
    if(tool == null)
    {
        event.getPlayer().sendMessage("§4This tool does not exist in the Tool Database. This may indicate a corrupted savefile or tool database .");
        //event.getPlayer().sendMessage("§4You can safely use §c/ctool delete §4to delete this tool.");
        return;
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
    
    
    //newTool.notify("Tool successfuly created. Use /ctool edit to assign commands to this tool.");
    
}

public void renameTool(Player player, String[] args)
{
    CommandTool tool = getToolInHand(player);
    if(tool == null) return;
    
    String sNewName = "";
    
    if(args.length < 2)
    {
        player.sendMessage("$cYou must give a name to your tool.");
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

public void runVirtualPlayerCommands(List<String> sCommands, String sPlayerName, Location hLocation)
{
    toolStore.runVirtualPlayerCommands(sCommands, sPlayerName, hLocation);
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
    toolStore.setPlayersToolFresh(event.getPlayer().getName(), true);
}

}
