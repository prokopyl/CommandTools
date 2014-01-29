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

import java.util.LinkedList;

class PlayerToolStore 
{
    private final String playerName;
    private final LinkedList<CommandTool> toolList;
    
    public PlayerToolStore(String sPlayerName)
    {
        playerName = sPlayerName;
        toolList = new LinkedList<CommandTool>();
    }
    
    public String getPlayerName()
    {
        return playerName;
    }
    
    public CommandTool getTool(String toolID)
    {
        for(CommandTool tTool : toolList)
        {
            if(tTool.getId().equals(toolID)) return tTool;
        }
        return null;
    }
    
    public void addTool(CommandTool newTool)
    {
        toolList.add(newTool);
    }
}
