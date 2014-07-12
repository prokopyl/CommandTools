/*
 * Copyright (C) 2014 adrien
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

package me.prokopyl.commandtools.attributes;

import java.util.UUID;
import me.prokopyl.commandtools.CommandTool;
import org.bukkit.inventory.ItemStack;

public class ToolAttribute 
{
    static private UUID pluginStorageID = UUID.fromString("8675a26f-5d66-49ca-9eef-094bbea764eb");
    private UUID ownerUUID;
    private String toolID;
    private boolean isToolEditor;
    
    static public ToolAttribute fromItemStack(ItemStack itemStack)
    {
        AttributeStorage storage = AttributeStorage.newTarget(itemStack, pluginStorageID);
        return fromString(storage.getData(""));
    }
    
    static public ToolAttribute fromString(String data)
    {
        
        if(data == null) return null;
        
        String[] fields = data.split(";");
        if(fields.length != 3) return null;
        
        boolean isToolEditor = ! fields[0].equals("0");
        
        UUID ownerUUID;
        try
        {
            ownerUUID = UUID.fromString(fields[1]);
            
        }
        catch(Exception e)
        {
            return null;
        }
        
        return new ToolAttribute(ownerUUID, fields[2], isToolEditor);
    }
    
    static public ItemStack toItemStack(CommandTool tool, ItemStack itemStack, boolean isEditor)
    {
        return new ToolAttribute(tool.getOwnerUUID(), tool.getId(), isEditor).toItemStack(itemStack);
    }
    
    static public ItemStack toItemStack(CommandTool tool, ItemStack itemStack)
    {
        return toItemStack(tool, itemStack, false);
    }
    
    public ToolAttribute(UUID ownerUUID, String toolID, boolean isToolEditor)
    {
        this.ownerUUID = ownerUUID;
        this.toolID = toolID;
        this.isToolEditor = isToolEditor;
    }
    
    public ToolAttribute()
    {
        this(null, null, false);
    }

    public UUID getOwnerUUID() 
    {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID ownerUUID) 
    {
        this.ownerUUID = ownerUUID;
    }

    public String getToolID() 
    {
        return toolID;
    }

    public void setToolID(String toolID) 
    {
        this.toolID = toolID;
    }

    public boolean isToolEditor() 
    {
        return isToolEditor;
    }

    public void setToolEditor(boolean isToolEditor) 
    {
        this.isToolEditor = isToolEditor;
    }
    
    @Override
    public String toString()
    {
        return (isToolEditor ? "1" : "0") + ";" + ownerUUID.toString() + ";" + toolID;
    }
    
    public ItemStack toItemStack(ItemStack itemStack)
    {
        AttributeStorage storage = AttributeStorage.newTarget(itemStack, pluginStorageID);
        storage.setData(this.toString());
        return storage.getTarget();
    }
    
}
