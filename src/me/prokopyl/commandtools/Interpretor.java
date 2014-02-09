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

package me.prokopyl.commandtools;

import java.util.List;

public class Interpretor 
{
    static public boolean Execute(List<String> aCommands, VirtualPlayer virtualPlayer)
    {
        boolean hasExecutedSomething = false;
        if(aCommands.isEmpty()) return false;
        for(String sCommand : aCommands)
        {
            if(!isIgnored(sCommand))
            {
                virtualPlayer.executeCommand(sCommand);
                hasExecutedSomething = true;
            }
        }
        return hasExecutedSomething;
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
