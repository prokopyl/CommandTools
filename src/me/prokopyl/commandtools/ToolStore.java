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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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
    
    public CommandTool getTool(ItemStack itemTool)
    {
        if(itemTool.getType() == Material.AIR) return null;
        return getTool(NBTUtils.getCommandToolOwner(itemTool), NBTUtils.getCommandToolID(itemTool));
    }
    
    public ArrayList<CommandTool> getToolList(String playerName)
    {
        PlayerToolStore store = getPlayerToolStore(playerName);
        if(store == null) return null;
        return store.getToolList();
    }
    
    public void addTool(CommandTool tool)
    {
        getPlayerToolStore(tool.getOwnerName()).addTool(tool);
    }
    
    public CommandTool cloneTool(ItemStack tool, String destinationPlayerName)
    {
        return cloneTool( NBTUtils.getCommandToolID(tool), NBTUtils.getCommandToolOwner(tool), destinationPlayerName);
    }
    
    public CommandTool cloneTool(String toolID, String sourcePlayerName, String destinationPlayerName)
    {
        PlayerToolStore sourcePlayerStore = getPlayerToolStore(sourcePlayerName);
        PlayerToolStore destinationPlayerStore = getPlayerToolStore(destinationPlayerName);
        CommandTool clonedTool = sourcePlayerStore.getClonedTool(toolID, destinationPlayerName, destinationPlayerStore.getNextAvailableToolID(toolID));
        destinationPlayerStore.addTool(clonedTool);
        return clonedTool;
    }
    
    public void deleteTool(CommandTool tool)
    {
        getPlayerToolStore(tool.getOwnerName()).deleteTool(tool);
    }
    
    public void save()
    {
        for(PlayerToolStore tStore : playerTools)
        {
            tStore.saveToolsFile();
        }
    }
    
    public void setPlayersToolFresh(String playerName, boolean fresh)
    {
        PlayerToolStore tStore = getExistingPlayerToolStore(playerName);
        if(tStore == null) return;
        tStore.setToolsFresh(fresh);
    }
    
    public String getNextAvailableToolID(String toolID, String playerName)
    {
        return getPlayerToolStore(playerName).getNextAvailableToolID(toolID);
    }
    
    public void runVirtualPlayerCommands(List<String> sCommands, String sPlayerName, Location hLocation, CommandTool tool)
    {
        getPlayerToolStore(sPlayerName).runCommands(sCommands, hLocation, tool);
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
