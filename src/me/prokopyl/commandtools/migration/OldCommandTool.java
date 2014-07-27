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


package me.prokopyl.commandtools.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.prokopyl.commandtools.CommandTool;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public final class OldCommandTool implements ConfigurationSerializable
{
    private String name;
    private String id;
    private Material itemType;
    private final ArrayList<String> commands = new ArrayList<String>();
    
    
    public OldCommandTool(String sId, String sName, Material hItemType)
    {
        itemType = hItemType;
        id = sId;
        name = sName;
        
    }
    /*===== Getters & Setters =====*/
    
    public String getId()
    {
        return id;
    }
    
    public String getName()
    {
        return name;
    }
    
    public Material getType()
    {
        return itemType;
    }
    
    public List<String> getCommands()
    {
        List<String> listCommands = new ArrayList<String>();
        listCommands.addAll(commands);
        return listCommands;
    }
    
    public void setCommands(List<String> newCommands)
    {
        commands.clear();
        commands.addAll(newCommands);
    }
    
    public boolean matchesCommands(CommandTool otherTool)
    {
        List<String> otherToolsCommands = otherTool.getCommands();
        if(otherToolsCommands.size() != commands.size()) return false;
        if(!otherToolsCommands.containsAll(commands)) return false;
        return true;
    }
    
    /*===== Serializable object =====*/
    public OldCommandTool(Map<String, Object> map)
    {
        //this(sName, hItem, hOwner);
        this((String) map.get("id"),(String) map.get("name"), Material.getMaterial((String) map.get("material")));
        
        setCommands((List<String>) map.get("commands"));
        
    }
    
    @Override
    public Map<String, Object> serialize() 
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("commands", getCommands());
        map.put("id", id);
        map.put("material", itemType.toString());
        map.put("name", name);
        return map;
    }
    
    
    
}
