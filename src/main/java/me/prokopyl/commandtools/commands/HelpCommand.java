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

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import me.prokopyl.commandtools.PluginLogger;

@CommandInfo(name = "help", usageParameters = "<command name>")
public class HelpCommand extends Command
{
    
    public HelpCommand(Commands commandGroup) {
        super(commandGroup);
    }
    
    @Override
    protected void run() throws CommandException 
    {
        if(args.length < 1)
            groupHelp();
        else
            commandHelp();
    }
    
    private void groupHelp() throws CommandException 
    {
        sender.sendMessage(commandGroup.getDescription());
        
        String tDescription;
        for(String tCommandName : commandGroup.getCommandsNames())
        {
            tDescription = commandGroup.getDescription(tCommandName);
            tCommandName = commandGroup.getUsualName() + " " + tCommandName;
            if(tDescription == null)
                sender.sendMessage("§6/" + tCommandName + "§r");
            else
                sender.sendMessage("§6/" + tCommandName + " : §r" + tDescription);
        }
    }
    
    private void commandHelp() throws CommandException 
    {
        Command command = commandGroup.getMatchingCommand(args[0]);
        if(command == null)
        {
            error("The specified command does not exist.");
            return;
        }
        
        sender.sendMessage("Usage : " + command.getUsageString());
        
        try
        {
            String help = getHelpText(command);
            if(help.isEmpty())
            {
                warning("There is no help message for this command.");
            }
            else
            {
                sender.sendMessage(help);
            }
        }
        catch(IOException ex)
        {
            warning("Could not read help for this command.");
            PluginLogger.LogWarning("Could not read help for the command : " + command.getName(), ex);
        }
    }
    
    private String getHelpText(Command command) throws IOException
    {
        String fileName = "help/"+ commandGroup.getUsualName() + 
                    "/" + command.getName() + ".txt";
        
        StringBuilder result = new StringBuilder("");
        
        InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);
        if(stream == null) return "";
        
	Scanner scanner = new Scanner(stream);
        
        while (scanner.hasNextLine()) 
        {
            String line = scanner.nextLine();
            result.append(line).append("\n");
        }
 
        scanner.close();
 
	return result.toString().trim();
    }
    

    
  
}
