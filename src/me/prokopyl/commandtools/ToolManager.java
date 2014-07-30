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
import java.util.UUID;
import me.prokopyl.commandtools.attributes.ToolAttribute;
import me.prokopyl.commandtools.interpreter.Environment;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

abstract public class ToolManager 
{
    static private final LinkedList<PlayerToolStore> playerTools = new LinkedList<PlayerToolStore>();
    
    static public CommandTool createNewTool(Player player, Material material)
    {
        return createNewTool("Tool", player.getUniqueId(), material);
    }
    
    static public CommandTool createNewTool(String toolName, UUID playerUUID, Material material)       
    {
        CommandTool newTool = new CommandTool(getNextAvailableToolID(toolName, playerUUID), toolName, material, playerUUID);
        addTool(newTool);
        return newTool;
    }
    
    static public CommandTool getTool(UUID playerUUID, String toolID)
    {
        PlayerToolStore store = getPlayerToolStore(playerUUID);
        return store.getTool(toolID);
    }
    
    static public CommandTool getTool(ItemStack item)
    {
        if(item.getType() == Material.AIR) return null;
        return getTool(ToolAttribute.fromItemStack(item));
    }
    
    static public CommandTool getTool(ToolAttribute attribute)
    {
        if(attribute == null) return null;
        return getTool(attribute.getOwnerUUID(), attribute.getToolID());
    }
    
    static public ArrayList<CommandTool> getToolList(UUID playerUUID)
    {
        PlayerToolStore store = getPlayerToolStore(playerUUID);
        return store.getToolList();
    }
    
    static public Environment getEnvironment(UUID playerUUID)
    {
        PlayerToolStore store = getPlayerToolStore(playerUUID);
        return store.getEnvironment();
    }
    
    static public boolean isEnabled(UUID playerUUID)
    {
        PlayerToolStore store = getPlayerToolStore(playerUUID);
        return store.isEnabled();
    }
    
    static public void setEnabled(UUID playerUUID, boolean enabled)
    {
        PlayerToolStore store = getPlayerToolStore(playerUUID);
        store.setEnabled(enabled);
    }
    
    static private void addTool(CommandTool tool)
    {
        getPlayerToolStore(tool.getOwnerUUID()).addTool(tool);
    }
    
    static public CommandTool cloneTool(ItemStack tool, UUID destinationPlayerUUID)
    {
        ToolAttribute attr = ToolAttribute.fromItemStack(tool);
        if(attr == null) return null;
        return cloneTool(attr.getToolID(), attr.getOwnerUUID(), destinationPlayerUUID);
    }
    
    static public CommandTool cloneTool(String toolID, UUID sourcePlayerUUID, UUID destinationPlayerUUID)
    {
        PlayerToolStore sourcePlayerStore = getPlayerToolStore(sourcePlayerUUID);
        PlayerToolStore destinationPlayerStore = getPlayerToolStore(destinationPlayerUUID);
        CommandTool clonedTool = sourcePlayerStore.getClonedTool(toolID, destinationPlayerUUID, destinationPlayerStore.getNextAvailableToolID(toolID));
        destinationPlayerStore.addTool(clonedTool);
        return clonedTool;
    }
    
    static public void deleteTool(CommandTool tool)
    {
        getPlayerToolStore(tool.getOwnerUUID()).deleteTool(tool);
    }
    
    static public void save()
    {
        synchronized(playerTools)
        {
            for(PlayerToolStore tStore : playerTools)
            {
                tStore.saveToolsFile();
            }
        }
    }
    
    static public String getNextAvailableToolID(String toolID, UUID playerUUID)
    {
        return getPlayerToolStore(playerUUID).getNextAvailableToolID(toolID);
    }
    
    static private PlayerToolStore getPlayerToolStore(UUID playerUUID)
    {
        PlayerToolStore store = getExistingPlayerToolStore(playerUUID);
        if(store == null) 
        {
            store = new PlayerToolStore(playerUUID);
            synchronized(playerTools){playerTools.add(store);}
        }
        return store;
    }
    
    static private PlayerToolStore getExistingPlayerToolStore(UUID playerUUID)
    {
        synchronized(playerTools)
        {
            for(PlayerToolStore tStore : playerTools)
            {
                if(tStore.getPlayerUUID().equals(playerUUID)) return tStore;
            }
        }
        return null;
    }
    
    
}
