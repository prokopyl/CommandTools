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
import java.util.List;
import org.bukkit.Location;

public class ToolStore 
{
    private final LinkedList<PlayerToolStore> playerTools;
    
    public ToolStore()
    {
        playerTools = new LinkedList<PlayerToolStore>();
    }
    
    public CommandTool getTool(String playerName, String toolID)
    {
        PlayerToolStore store = getPlayerToolStore(playerName);
        return store.getTool(toolID);
    }
    
    public void addTool(CommandTool tool)
    {
        getPlayerToolStore(tool.getOwnerName()).addTool(tool);
    }
    
    public void save()
    {
        for(PlayerToolStore tStore : playerTools)
        {
            tStore.saveToolsFile();
        }
    }
    
    public String getNextAvailableToolID(String toolID, String playerName)
    {
        return getPlayerToolStore(playerName).getNextAvailableToolID(toolID);
    }
    
    public void runVirtualPlayerCommands(List<String> sCommands, String sPlayerName, Location hLocation)
    {
        getPlayerToolStore(sPlayerName).runCommands(sCommands, hLocation);
    }
    
    private PlayerToolStore getPlayerToolStore(String playerName)
    {
        PlayerToolStore store = getExistingPlayerToolStore(playerName);
        if(store == null) store = new PlayerToolStore(playerName);
        playerTools.add(store);
        return store;
    }
    
    private PlayerToolStore getExistingPlayerToolStore(String playerName)
    {
        for(PlayerToolStore tStore : playerTools)
        {
            if(tStore.getPlayerName().equals(playerName)) return tStore;
        }
        return null;
    }
}
