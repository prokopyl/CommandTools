/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package me.prokopyl.commandtools.commands;

import me.prokopyl.commandtools.CommandTool;
import me.prokopyl.commandtools.ToolManager;
import me.prokopyl.commandtools.commands.CommandException.Reason;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

abstract public class Command 
{
    protected final Commands commandGroup;
    protected final String commandName;
    protected final String usageParameters;
    
    protected CommandSender sender;
    protected String[] args;
    
    abstract protected void run() throws CommandException;
    
    public Command(Commands commandGroup)
    {
        this.commandGroup = commandGroup;
        
        CommandInfo commandInfo = this.getClass().getAnnotation(CommandInfo.class);
        if(commandInfo == null) 
            throw new IllegalArgumentException("Command has no CommandInfo annotation");
        
        commandName = commandInfo.name().toLowerCase();
        usageParameters = commandInfo.usageParameters();
    }
    
    public void execute(CommandSender sender, String[] args)
    {
        this.sender = sender; this.args = args;
        try
        {
            run();
        }
        catch(CommandException ex)
        {
            sender.sendMessage(ex.getReasonString());
        }
        this.sender = null; this.args = null;
    }
    
    public String getUsageString()
    {
        return "/" + commandGroup.getUsualName() + " " + commandName + " " + usageParameters;
    }
    
    public String getName()
    {
        return commandName;
    }
    
    public boolean matches(String name)
    {
        return commandName.equals(name.toLowerCase());
    }
    
    protected Player playerSender() throws CommandException
    {
        if(!(sender instanceof Player)) throw new CommandException(Reason.COMMANDSENDER_EXPECTED_PLAYER);
        return (Player)sender;
    }
    
    static protected CommandTool getToolInHand(Player player)
    {
        ItemStack itemInHand = player.getItemInHand();
        if(itemInHand.getType() == Material.AIR || !CommandTool.isCommandTool(itemInHand))
        {
            player.sendMessage("§cYou must have a tool in hand.");
            return null;
        }

        CommandTool tool = ToolManager.getTool(itemInHand);
        if(tool == null)
        {
            player.sendMessage("§cThis tool does not exist in the Tool Database. It may have been deleted.");
            return null;
        }

        return tool;

    }
    
    static public void tellRaw(Player player, String rawMessage)
    {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), 
                "tellraw " + player.getName() + " " + rawMessage);
    }
    
    protected CommandTool getDesignatedTool(Player player)
    {
        CommandTool tool;

        if(args.length >= 1)//Designated by name
        {
            String toolName = "";
            for (String arg : args) 
            {
                toolName += arg + " ";
            }
            toolName = toolName.trim();

            tool = ToolManager.getTool(player.getUniqueId(), toolName);
            if(tool == null)
            {
                player.sendMessage("§cThis tool does not exist.");
            }
        }
        else
        {
            tool = getToolInHand(player);
        }

        return tool;
    }

}
