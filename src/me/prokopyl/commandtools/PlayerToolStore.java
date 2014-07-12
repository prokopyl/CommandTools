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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.prokopyl.commandtools.interpreter.Environment;
import me.prokopyl.commandtools.interpreter.Interpreter;
import me.prokopyl.commandtools.interpreter.VirtualPlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class PlayerToolStore implements ConfigurationSerializable
{
    private final UUID playerUUID;
    private final LinkedList<CommandTool> toolList = new LinkedList<CommandTool>();
    private final Environment environment;
    
    public PlayerToolStore(UUID playerUUID)
    {
        this.playerUUID = playerUUID;
        environment = new Environment(playerUUID);
        loadToolsFile();
    }
    
    public UUID getPlayerUUID()
    {
        return playerUUID;
    }
    
    public CommandTool getTool(String toolID)
    {
        for(CommandTool tTool : toolList)
        {
            if(tTool.getId().equals(toolID)) return tTool;
        }
        return null;
    }
    
    public ArrayList<CommandTool> getToolList()
    {
        ArrayList<CommandTool> tools = new ArrayList<CommandTool>();
        tools.addAll(toolList);
        return tools;
    }
    
    public Environment getEnvironment()
    {
        return environment;
    }
    
    public void addTool(CommandTool newTool)
    {
        toolList.add(newTool);
    }
    
    public CommandTool getClonedTool(String toolName, UUID destinationPlayerUUID, String newToolName)
    {
        CommandTool hTool = ToolManager.getTool(playerUUID, toolName);
        CommandTool newTool = new CommandTool(newToolName, hTool, destinationPlayerUUID);
        return newTool;
    }
    
    public void deleteTool(CommandTool tool)
    {
        toolList.remove(tool);
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
        for(CommandTool tTool : toolList)
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
        for(CommandTool tTool : toolList)
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
            toolList.add(new CommandTool(tMap, playerUUID));
        }
    }
    
    
    
    /* ****** Configuration Files management ***** */
    
    static private final String TOOLS_DIR_NAME = "Tools";
    static private File toolsDir = null;
    
    private FileConfiguration toolConfig = null;
    private File toolsFile = null;
    
    private File getToolsDir()
    {
        if(toolsDir == null)
        {
            toolsDir = new File(CommandTools.getPlugin().getDataFolder(), TOOLS_DIR_NAME);
            toolsDir.mkdir();
        }
        
        return toolsDir;
    }
    
    private FileConfiguration getToolConfig()
    {
        if(toolConfig == null) loadToolsFile();
        
        return toolConfig;
    }
    
    private void loadToolsFile()
    {
        if(toolsFile == null)
        {
            toolsFile = new File(getToolsDir(), playerUUID.toString() + ".yml");
            if(!toolsFile.exists()) saveToolsFile();
        }
        toolConfig = YamlConfiguration.loadConfiguration(toolsFile);
        loadFromConfig(getToolConfig().getConfigurationSection("PlayerToolStore"));
    }
    
    public void saveToolsFile()
    {
        if(toolsFile == null || toolConfig == null) return;
        getToolConfig().set("PlayerToolStore", this.serialize());
        try 
        {
            getToolConfig().save(toolsFile);
        } catch (IOException ex) {
            Logger.getLogger(PlayerToolStore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
}
