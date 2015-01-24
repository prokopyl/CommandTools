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

import java.lang.reflect.Constructor;
import me.prokopyl.commandtools.PluginLogger;

import me.prokopyl.commandtools.commands.ctool.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Prokopyl<prokopylmc@gmail.com>
 */
public enum Commands 
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
    private final Command[] commands;
    private Commands(String[] names, Class<? extends Command> ... commandsClasses)
    {
        this.names = names;
        this.commandsClasses = commandsClasses;
        this.commands = new Command[commandsClasses.length];
        initCommands();
    }
    
    private void initCommands()
    {
        Constructor<? extends Command> constructor;
        for(int i = 0; i < commands.length; i++)
        {
            try 
            {
                constructor = commandsClasses[i].getConstructor(Commands.class);
                commands[i] = constructor.newInstance(this);
            } 
            catch (Exception ex) 
            {
                PluginLogger.LogWarning("Exception while initializing command", ex);
            }
        }
    }
    
    public boolean executeMatchingCommand(CommandSender sender, String[] args)
    {
        if(args.length <= 0)
        {
            sender.sendMessage(getUsage()); return false;
        }
        
        String commandName = args[0];
        String[] commandArgs = new String[args.length - 1];
        
        for(int i = 0; i < commandArgs.length; i++)
        {
            commandArgs[i] = args[i + 1];
        }
        
        return executeMatchingCommand(sender, commandName, commandArgs);
    }
    
    public boolean executeMatchingCommand(CommandSender sender, String commandName, String[] args)
    {
        for(Command command : commands)
        {
            if(command.matches(commandName))
            {
                command.execute(sender, args);
                return true;
            }
        }
        sender.sendMessage(getUsage());
        return false;
    }
    
    static public boolean execute(CommandSender sender, String commandName, String[] args)
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
        
        if(commandGroup == null) return false;
        commandGroup.executeMatchingCommand(sender, args);
        return true;
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
        String[] commandsNames = new String[commands.length];
        
        for(int i = 0; i < commands.length; i++)
        {
            commandsNames[i] = commands[i].getName();
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
    public Command[] getCommands() { return commands.clone();}
}
