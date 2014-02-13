
package me.prokopyl.commandtools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
    private boolean freshTool;
    private boolean freshEditTool;
    
    private static HashSet<Byte> TRANSPARENT_BLOCKS = new HashSet<Byte>();
    
    public CommandTool(String sId, String sName, Material hItemType, String sOwnerName)
    {
        ownerName = sOwnerName;
        itemType = hItemType;
        id = sId;
        name = sName;
        freshTool = true;
        freshEditTool = true;
        if(TRANSPARENT_BLOCKS.isEmpty())
        {
            TRANSPARENT_BLOCKS.add((byte)0);
            TRANSPARENT_BLOCKS.add((byte)8);
            TRANSPARENT_BLOCKS.add((byte)9);
        }
    }
    
    public CommandTool(String sId, CommandTool otherTool, String sOwnerName)
    {
        this(sId, otherTool.getName(), otherTool.getType(), sOwnerName);
        setCommands(otherTool.getCommands());
    }
    
    public ItemStack createItem()
    {
        ItemStack item = NBTUtils.createCraftItemStack(itemType, 1);
        NBTUtils.setCommandToolData(item, this);
        return item;
    }
    
    public void use(Player player)
    {
        Location loc = player.getTargetBlock(TRANSPARENT_BLOCKS, 100).getLocation();
        
        CommandTools.getPlugin().runVirtualPlayerCommands(commands, ownerName, loc, this);
    }
    
    public void notify(String message, Player player)
    {
        player.sendMessage("§6" + "Tool" + ">§r " + message);
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
    
    public Material getType()
    {
        return itemType;
    }
    
    public void rename(String sId, String sName)
    {
        id = sId;
        name = sName;
    }
    
    public boolean getFreshTool()
    {
        return freshTool;
    }
    
    public void setFreshTool(boolean nFresh)
    {
        freshTool = nFresh;
    }
    
    public boolean getFreshEditTool()
    {
        return freshEditTool;
    }
    
    public void setFreshEditTool(boolean nFresh)
    {
        freshEditTool = nFresh;
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
    
    /*===== CommandTools Utils =====*/
    
    static public boolean isCommandTool(ItemStack item)
    {
        if(item.getType() == Material.AIR) return false;
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
