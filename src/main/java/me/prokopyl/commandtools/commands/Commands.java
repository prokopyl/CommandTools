/*
 * Copyright (C) 2015 Prokopyl<prokopylmc@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package me.prokopyl.commandtools.commands;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import me.prokopyl.commandtools.PluginLogger;

import me.prokopyl.commandtools.commands.ctool.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Prokopyl<prokopylmc@gmail.com>
 */
public enum Commands implements TabCompleter, CommandExecutor
{
    CTOOL(new String[]{"ctool", "commandtools"},
            ClearCommand.class,
            CreateCommand.class,
            DeleteConfirmCommand.class,
            DeleteNoConfirmCommand.class,
            DisableCommand.class,
            EditCommand.class,
            EnableCommand.class,
            ExploreCommand.class,
            GetCommand.class,
            InfoCommand.class,
            ListCommand.class,
            MigrateCommand.class,
            RemoveCommand.class,
            RenameCommand.class
        );
    
    static private final Commands[] commandGroups = Commands.class.getEnumConstants();
    private final String[] names;
    private final Class<? extends Command>[] commandsClasses;
    private final ArrayList<Command> commands = new ArrayList<>();
    private final HashMap<String, String> commandsDescriptions = new HashMap<>();
    private String description = "";
    private Commands(String[] names, Class<? extends Command> ... commandsClasses)
    {
        this.names = names;
        this.commandsClasses = commandsClasses;
        initDescriptions();
        initCommands();
    }
    
    private void initDescriptions()
    {
        String fileName = "help/" + getUsualName() + ".txt";
        InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);
        if(stream == null)
        {
            PluginLogger.LogWarning("Could not load description file for the " + getUsualName() + " command");
            return;
        }
        
	Scanner scanner = new Scanner(stream);
        StringBuilder builder = new StringBuilder();
        
        //Getting the group's description
        //And then each command's description
        int colonIndex, firstSpaceIndex;
        boolean isGroupDescription = true;
        while (scanner.hasNextLine()) 
        {
            String line = scanner.nextLine();
            colonIndex = line.indexOf(':');
            if(isGroupDescription)
            {
                firstSpaceIndex = line.indexOf(' ');
                if(colonIndex > 0 && firstSpaceIndex > colonIndex)
                    isGroupDescription = false;
            }
            
            if(isGroupDescription)
            {
                builder.append(line).append('\n');
            }
            else
            {
                commandsDescriptions.put(line.substring(0, colonIndex).trim(), 
                                         line.substring(colonIndex + 1).trim());
            }
        }
        
        scanner.close();
        description = builder.toString().trim();
 
    }
    
    private void initCommands()
    {
        for (Class<? extends Command> commandClass : commandsClasses) 
        {
            addCommand(commandClass);
        }
        
        addCommand(HelpCommand.class);
    }
    
    private void addCommand(Class<? extends Command> commandClass)
    {
        Constructor<? extends Command> constructor;
        try 
        {
            constructor = commandClass.getConstructor(Commands.class);
            commands.add(constructor.newInstance(this));
        } 
        catch (Exception ex) 
        {
            PluginLogger.LogWarning("Exception while initializing command", ex);
        }
    }
    
    public boolean executeMatchingCommand(CommandSender sender, String[] args)
    {
        if(args.length <= 0)
        {
            sender.sendMessage(getUsage()); return false;
        }
        
        String commandName = args[0];
        String[] commandArgs = getCommandArgsFromGroupArgs(args);
        
        return executeMatchingCommand(sender, commandName, commandArgs);
    }
    
    public boolean executeMatchingCommand(CommandSender sender, String commandName, String[] args)
    {
        Command command = getMatchingCommand(commandName);
        if(command != null)
        {
            command.execute(sender, args);
        }
        else
        {
            sender.sendMessage(getUsage());
        }
        return command != null;
    }
    
    static public void init(JavaPlugin plugin)
    {
        org.bukkit.command.PluginCommand bukkitCommand;
        for(Commands commandGroup : commandGroups)
        {
            bukkitCommand = plugin.getCommand(commandGroup.getUsualName());
            bukkitCommand.setAliases(commandGroup.getAliases());
            bukkitCommand.setExecutor(commandGroup);
            bukkitCommand.setTabCompleter(commandGroup);
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) 
    {
        return tabComplete(sender, args);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args)
    {
        return executeMatchingCommand(sender, args);
    }
    
    public List<String> tabComplete(CommandSender sender, String[] args)
    {
        if(args.length <= 1) return tabComplete(sender, args.length == 1 ? args[0] : null);
        String commandName = args[0];
        String[] commandArgs = getCommandArgsFromGroupArgs(args);
        return tabCompleteMatching(sender, commandName, commandArgs);
    }
    
    public List<String> tabComplete(CommandSender sender, String commandName)
    {
        ArrayList<String> matchingCommands = new ArrayList<String>();
        for(Command command : commands)
        {
            if(!command.canExecute(sender)) continue;
            if(commandName == null || command.getName().startsWith(commandName.toLowerCase()))
            {
                matchingCommands.add(command.getName());
            }
        }
        return matchingCommands;
    }
    
    public List<String> tabCompleteMatching(CommandSender sender, String commandName, String[] args)
    {
        Command command = getMatchingCommand(commandName);
        if(command != null)
        {
            return command.tabComplete(sender, args);
        }
        else
        {
            return new ArrayList<String>();
        }
    }
    
    public String[] getCommandArgsFromGroupArgs(String[] args)
    {
        String[] commandArgs = new String[args.length - 1];
        
        for(int i = 0; i < commandArgs.length; i++)
        {
            commandArgs[i] = args[i + 1];
        }
        
        return commandArgs;
    }
    
    public Command getMatchingCommand(String commandName)
    {
        for(Command command : commands)
        {
            if(command.matches(commandName))
            {
                return command;
            }
        }
        return null;
    }
    
    
    static public boolean execute(CommandSender sender, String commandName, String[] args)
    {
        Commands commandGroup = getMatchingCommandGroup(commandName);
        if(commandGroup == null) return false;
        commandGroup.executeMatchingCommand(sender, args);
        return true;
    }
    
    static public List<String> tabComplete(CommandSender sender, String commandName, String[] args)
    {
        Commands commandGroup = getMatchingCommandGroup(commandName);
        if(commandGroup == null) return new ArrayList<String>();
        return commandGroup.tabComplete(sender, args);
    }
    
    static private Commands getMatchingCommandGroup(String commandName)
    {
        Commands commandGroup = null;
        for(Commands tCommandGroup : commandGroups)
        {
            if(tCommandGroup.matches(commandName))
            {
                commandGroup = tCommandGroup;
                break;
            }   
        }
        return commandGroup;
    }
    
    public boolean matches(String name)
    {
        name = name.toLowerCase();
        for(String commandName : names)
        {
            if(commandName.equals(name)) return true;
        }
        return false;
    }
    
    public String[] getCommandsNames()
    {
        String[] commandsNames = new String[commands.size()];
        
        for(int i = 0; i < commands.size(); i++)
        {
            commandsNames[i] = commands.get(i).getName();
        }
        
        return commandsNames;
    }
    
    protected String getUsage()
    {
        return "§cUsage : /" + getUsualName() + 
                " <" + StringUtils.join(getCommandsNames(), "|") + ">";
    }
    
    public String getUsualName() { return names[0]; }
    public String[] getNames() { return names.clone(); }
    public List<String> getAliases() { return Arrays.asList(names).subList(1, names.length);}
    public Command[] getCommands() { return commands.toArray(new Command[commands.size()]);}
    public String getDescription() { return description; }
    public String getDescription(String commandName) { return commandsDescriptions.get(commandName); }

}
