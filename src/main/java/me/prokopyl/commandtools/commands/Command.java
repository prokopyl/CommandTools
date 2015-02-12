package me.prokopyl.commandtools.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
    protected final String commandDescription;
    
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
        commandDescription = commandGroup.getDescription(commandName);
    }
    
    public boolean canExecute(CommandSender sender)
    {
        return sender.hasPermission("commandtools." + commandGroup.getUsualName());
    }
    
    protected List<String> complete() throws CommandException
    {
        return null;
    }
    
    public void execute(CommandSender sender, String[] args)
    {
        this.sender = sender; this.args = args;
        try
        {
            if(!canExecute(sender))
                throw new CommandException(this, Reason.SENDER_NOT_AUTHORIZED);
            run();
        }
        catch(CommandException ex)
        {
            warning(ex.getReasonString());
        }
        this.sender = null; this.args = null;
    }
    
    public List<String> tabComplete(CommandSender sender, String[] args)
    {
        List<String> result = null;
        this.sender = sender; this.args = args;
        try
        {
            if(canExecute(sender))
                result = complete();
        }
        catch(CommandException ex){}
        
        this.sender = null; this.args = null;
        if(result == null) result = new ArrayList<String>();
        return result;
    }
    
    
    public String getUsageString()
    {
        return "/" + commandGroup.getUsualName() + " " + commandName + " " + usageParameters;
    }
    
    public String getName()
    {
        return commandName;
    }
    
    public Commands getCommandGroup()
    {
        return commandGroup;
    }
    
    public boolean matches(String name)
    {
        return commandName.equals(name.toLowerCase());
    }
    
    
    ///////////// Common methods for commands /////////////
    
    protected void throwInvalidArgument(String reason) throws CommandException
    {
        throw new CommandException(this, Reason.INVALID_PARAMETERS, reason);
    }
        
    protected Player playerSender() throws CommandException
    {
        if(!(sender instanceof Player)) 
            throw new CommandException(this, Reason.COMMANDSENDER_EXPECTED_PLAYER);
        return (Player)sender;
    }
    
    protected CommandTool getToolInHand(Player player) throws CommandException
    {
        ItemStack itemInHand = player.getItemInHand();
        if(itemInHand.getType() == Material.AIR || !CommandTool.isCommandTool(itemInHand))
            error("You must have a tool in hand.");

        CommandTool tool = ToolManager.getTool(itemInHand);
        if(tool == null)
            error("This tool does not exist in the Tool Database. It may have been deleted.");

        return tool;
    }
    
    protected CommandTool getToolFromArgs(Player player) throws CommandException
    {
        CommandTool tool;
        String toolName = "";
        for (String arg : args) 
        {
            toolName += arg + " ";
        }
        toolName = toolName.trim();

        tool = ToolManager.getTool(player.getUniqueId(), toolName);
        if(tool == null)
        {
            error("This tool does not exist.");
        }
        return tool;
    }
    
    protected CommandTool getDesignatedTool(Player player) throws CommandException
    {
        CommandTool tool;

        if(args.length >= 1)//Designated by name
        {
            tool = getToolFromArgs(player);
        }
        else
        {
            tool = getToolInHand(player);
        }

        return tool;
    }
    
    ///////////// Methods for command execution /////////////
    
    protected void info(String message)
    {
        sender.sendMessage("§7" + message);
    }
    
    protected void warning(String message)
    {
        sender.sendMessage("§c" + message);
    }
    
    protected void error(String message) throws CommandException
    {
        throw new CommandException(this, Reason.COMMAND_ERROR, message);
    }
    
    protected void tellRaw(String rawMessage) throws CommandException
    {
        Player player = playerSender();
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), 
                "tellraw " + player.getName() + " " + rawMessage);
    }
    
    ///////////// Methods for autocompletion /////////////
    
    protected List<String> getMatchingSubset(String prefix, String... list)
    {
        return getMatchingSubset(Arrays.asList(list), prefix);
    }
    
    protected List<String> getMatchingSubset(Iterable<? extends String> list, String prefix)
    {
        List<String> matches = new ArrayList<String>();
        
        for(String item : list)
        {
            if(item.startsWith(prefix)) matches.add(item);
        }
        
        return matches;
    }
    
    protected List<String> getMatchingPlayerNames(String prefix)
    {
        return getMatchingPlayerNames(Bukkit.getOnlinePlayers(), prefix);
    }
    
    protected List<String> getMatchingPlayerNames(Iterable<? extends Player> players, String prefix)
    {
        List<String> matches = new ArrayList<String>();
        
        for(Player player : players)
        {
            if(player.getName().startsWith(prefix)) matches.add(player.getName());
        }
        
        return matches;
    }
    
    protected List<String> getMatchingToolNames(Player player, String prefix)
    {
        return getMatchingToolNames(ToolManager.getToolList(player.getUniqueId()), prefix);
    }
    
    protected List<String> getMatchingToolNames(Iterable<? extends CommandTool> tools, String prefix)
    {
        List<String> matches = new ArrayList<String>();
        
        for(CommandTool tool : tools)
        {
            if(tool.getId().startsWith(prefix)) matches.add(tool.getId());
        }
        
        return matches;
    }

}
