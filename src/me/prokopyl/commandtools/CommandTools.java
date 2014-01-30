package me.prokopyl.commandtools;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;


public class CommandTools extends JavaPlugin implements Listener 
{

private final ToolStore toolStore;

public CommandTools() 
{
    toolStore = new ToolStore(this);
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
    
    tool.use(event.getPlayer());
}

@EventHandler(priority=EventPriority.HIGH)
public void onItemDrop(PlayerDropItemEvent event)
{
    if(CommandTool.isCommandTool(event.getItemDrop().getItemStack()))
    {
        event.getPlayer().sendMessage("You can't drop a CommandTool !");
        event.setCancelled(true);
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
        
        CommandTool tool = toolStore.getTool(NBTUtils.getCommandToolOwner(itemTool), NBTUtils.getCommandToolID(itemTool));
        sender.setItemInHand(ToolEditor.createEditedCommandTool(tool));
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
    
    CommandTool newTool = new CommandTool("Tool", itemInHand.getType(), player.getName());
    toolStore.addTool(newTool);
    
    playerInventory.setItem(firstEmptySlot, itemInHand);
    player.setItemInHand(newTool.createItem());
    
    newTool.notify("Tool successfuly created. Use /ctool edit to assign commands to this tool.");
    
}

}
