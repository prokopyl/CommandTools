package me.prokopyl.commandtools;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;


public class CommandTools extends JavaPlugin implements Listener 
{
static private CommandTools plugin;

public CommandTools() 
{
    plugin = this;
}

static public CommandTools getPlugin()
{
    return plugin;
}

@Override
public void onEnable()
{
    Bukkit.getPluginManager().registerEvents(this, this);
    Bukkit.getPluginManager().registerEvents(new ToolEditor(), this);
}

@Override
public void onDisable()
{
    ToolManager.save();
}

@Override
public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
{
    if(!cmd.getName().equalsIgnoreCase("ctool")) return false;
    CommandExecutor.Execute(sender, args);
    return true;
}

@EventHandler(priority=EventPriority.HIGH)
public void onPlayerUse(PlayerInteractEvent event)
{
    if(event.getAction() != Action.RIGHT_CLICK_AIR) return;
    if(!CommandTool.isCommandTool(event.getItem())) return;
    event.setCancelled(true);
    
    if(!event.getPlayer().hasPermission("commandtools.ctools"))
    {
        event.getPlayer().sendMessage("§cYou are not allowed to use Command Tools.");
        return;
    }
    
    ItemStack itemTool = event.getItem();
    
    CommandTool tool = ToolManager.getTool(itemTool);
    
    if(tool == null)
    {
        event.getPlayer().sendMessage("§4This tool does not exist in the Tool Database. This may indicate a corrupted savefile or tool database.");
        event.getPlayer().sendMessage("§4You can safely use §c/ctool delete §4to delete this tool.");
        return;
    }
    
    if(!(tool.getOwnerUUID().equals(event.getPlayer().getUniqueId())))
    {
        tool = ToolManager.cloneTool(itemTool, event.getPlayer().getUniqueId());
        event.getPlayer().setItemInHand(tool.createItem());
    }
    
    tool.use(event.getPlayer());
}


@EventHandler(priority=EventPriority.HIGH)
public void onWorldSave(WorldSaveEvent event)
{
    ToolManager.save();
}

}
