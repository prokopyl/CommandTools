/*
 * Copyright (C) 2014 ProkopyL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.prokopyl.commandtools.interpreter;

import me.prokopyl.commandtools.CommandTool;
import me.prokopyl.commandtools.ToolManager;
import org.bukkit.entity.Player;

public class Interpreter 
{
    static public void Execute(CommandTool tool, Player player)
    {
        Environment environment = ToolManager.getEnvironment(tool.getOwnerUUID());
        environment.init(tool, player);
        
        for(String sCommand : tool.getCommands())
        {
            if(!isIgnored(sCommand))
            {
                environment.executeCommand(sCommand);
            }
        }
        
        environment.exit();
    }
    
    static public boolean isIgnored(String sCommand)
    {
        sCommand = sCommand.trim();
        
        if(sCommand.isEmpty()) return true;
        char firstChar = sCommand.charAt(0);
        
        if(firstChar == '\'' || firstChar == '#') return true;
        
        return false;
    }
}
