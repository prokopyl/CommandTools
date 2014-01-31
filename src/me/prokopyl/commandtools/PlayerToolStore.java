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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

public class PlayerToolStore implements ConfigurationSerializable
{
    private final String playerName;
    private final LinkedList<CommandTool> toolList;
    private VirtualPlayer virtualPlayer;
    
    public PlayerToolStore(String sPlayerName)
    {
        playerName = sPlayerName;
        toolList = new LinkedList<CommandTool>();
        loadToolsFile();
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
    
    public void runCommands(List<String> aCommands, Location hLocation)
    {
        if(virtualPlayer == null) virtualPlayer = VirtualPlayer.createVirtualPlayer(playerName, hLocation, this);
        virtualPlayer.moveTo(hLocation);
        
        for(String sCommand : aCommands)
        {
            virtualPlayer.executeCommand(sCommand);
        }
    }
    
    public final void notify(String message)
    {
        Player player = Bukkit.getPlayerExact(playerName);
        if(player == null) return;
        player.sendMessage("§6" + "Tool" + ">§r " + message);
    }
    
    public void setToolsFresh(boolean fresh)
    {
        for(CommandTool tTool : toolList)
        {
            tTool.setFreshTool(fresh);
        }
    }
    
    /* ****** Serializing ***** */
    
    @Override
    public Map<String, Object> serialize() 
    {
        Map<String, Object> map = new HashMap<String, Object>();
        ArrayList<Map> list = new ArrayList<Map>();
        map.put("playerName", playerName);
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
            toolList.add(new CommandTool(tMap, playerName));
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
            toolsFile = new File(getToolsDir(), playerName + ".yml");
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
