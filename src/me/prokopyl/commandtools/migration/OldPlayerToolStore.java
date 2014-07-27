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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.prokopyl.commandtools.CommandTool;
import me.prokopyl.commandtools.ToolManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class OldPlayerToolStore implements ConfigurationSerializable
{
    private final File file;
    private final String userName;
    private final LinkedList<OldCommandTool> toolList = new LinkedList<OldCommandTool>();
    
    public OldPlayerToolStore(File file, String userName)
    {
        this.file = file;
        this.userName = userName;
        loadToolsFile();
    }
    
    public File getFile()
    {
        return file;
    }
    
    public String getUserName()
    {
        return userName;
    }
    
    public OldCommandTool getTool(String toolID)
    {
        for(OldCommandTool tTool : toolList)
        {
            if(tTool.getId().equals(toolID)) return tTool;
        }
        return null;
    }
    
    public ArrayList<OldCommandTool> getToolList()
    {
        ArrayList<OldCommandTool> tools = new ArrayList<OldCommandTool>();
        tools.addAll(toolList);
        return tools;
    }
    
    public String getNextAvailableToolID(String toolID)
    {
        if(!toolExists(toolID)) return toolID;
        int id = 0;
        
        do
        {
            id++;
        }while(toolExists(toolID + "~" + id));
        
        return toolID + "~" + id;
        
    }
    
    public boolean toolExists(String toolID)
    {
        for(OldCommandTool tTool : toolList)
        {
            if(tTool.getId().equals(toolID)) return true;
        }
        
        return false;
    }
    
    /* ****** Serializing ***** */
    
    @Override
    public Map<String, Object> serialize() 
    {
        Map<String, Object> map = new HashMap<String, Object>();
        ArrayList<Map> list = new ArrayList<Map>();
        for(OldCommandTool tTool : toolList)
        {
            list.add(tTool.serialize());
        }
        map.put("toolList", list);
        return map;
    }
    
    private void loadFromConfig(ConfigurationSection section)
    {
        if(section == null) return;
        List<Map<String, Object>> list = (List<Map<String, Object>>) section.getList("toolList");
        if(list == null) return;
        for(Map<String, Object> tMap : list)
        {
            toolList.add(new OldCommandTool(tMap));
        }
    }
    
    /* ****** Configuration Files management ***** */
    
    private FileConfiguration toolConfig = null;
    
    private FileConfiguration getToolConfig()
    {
        if(toolConfig == null) loadToolsFile();
        
        return toolConfig;
    }
    
    private void loadToolsFile()
    {
        toolConfig = YamlConfiguration.loadConfiguration(file);
        loadFromConfig(getToolConfig().getConfigurationSection("PlayerToolStore"));
    }
    
    /* ****** Merging ***** */
    
    public void mergeWithExistingToolStore(UUID playerUUID)
    {
        CommandTool existingTool;
        for(OldCommandTool tool : toolList)
        {
            existingTool = ToolManager.getTool(playerUUID, tool.getName());
            
            if(existingTool == null || !tool.matchesCommands(existingTool))
            {
                existingTool = ToolManager.createNewTool(tool.getName(), playerUUID, tool.getType());
                existingTool.setCommands(tool.getCommands());
            }
        }
    }
    
}
