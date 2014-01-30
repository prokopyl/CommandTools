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

public class ToolStore 
{
    private final LinkedList<PlayerToolStore> playerTools;
    private final CommandTools plugin;
    
    public ToolStore(CommandTools hPlugin)
    {
        playerTools = new LinkedList<PlayerToolStore>();
        plugin = hPlugin;
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
    
    private PlayerToolStore getPlayerToolStore(String playerName)
    {
        PlayerToolStore store = getExistingPlayerToolStore(playerName);
        if(store == null) store = new PlayerToolStore(playerName, plugin);
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
