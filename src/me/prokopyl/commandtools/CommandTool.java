
package me.prokopyl.commandtools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class CommandTool implements ConfigurationSerializable
{
    private String name;
    private String id;
    private final String ownerName;
    private Material itemType;
    private final ArrayList<String> commands = new ArrayList<String>();
    
    public CommandTool(String sId, String sName, Material hItemType, String sOwnerName)
    {
        ownerName = sOwnerName;
        itemType = hItemType;
        id = sId;
        name = sName;
        commands.add("/chunk");
    }
    
    public ItemStack createItem()
    {
        ItemStack item = NBTUtils.createCraftItemStack(itemType, 1);
        NBTUtils.setCommandToolData(item, this);
        return item;
    }
    
    public void use(Player player)
    {
        Location loc = player.getTargetBlock(null, 100).getLocation();
        
        //virtualplayer.teleport(loc);
        //virtualplayer.executeCommand(command);
        CommandTools.getPlugin().runVirtualPlayerCommands(commands, ownerName, loc);
    }
    
    /*===== Getters & Setters =====*/
    
    public String getOwnerName()
    {
        return ownerName;
    }
    
    public String getId()
    {
        return id;
    }
    
    public String getName()
    {
        return name;
    }
    
    public final void setName(String sName)
    {
        name = sName;
    }
    
    
    public List<String> getCommands()
    {
        List<String> listCommands = new ArrayList<String>();
        for(String sCommand : commands)
        {
            listCommands.add("/" + sCommand);
        }
        return listCommands;
    }
    
    public void setCommands(List<String> newCommands)
    {
        commands.clear();
        for(String sCommand : newCommands)
        {
            if(sCommand.charAt(0) == '/') sCommand = sCommand.substring(1);
            commands.add(sCommand);
            
        }
    }
    
    /*===== CommandTools Utils =====*/
    
    static public boolean isCommandTool(ItemStack item)
    {
        if(NBTUtils.getToolEditorMode(item) == true) return false;
        return !(NBTUtils.getCommandToolID(item).equals(""));
    }
    
    /*===== Serializable object =====*/
    public CommandTool(Map<String, Object> map, String sOwnerName)
    {
        //this(sName, hItem, hOwner);
        this((String) map.get("id"),(String) map.get("name"), Material.getMaterial((String) map.get("material")), (String) sOwnerName);
        
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
    
    /*===== Internal object management =====*/
    
    
    
}
