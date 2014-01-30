
package me.prokopyl.commandtools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;

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
    private VirtualPlayer virtualplayer = null;
    private final ArrayList<String> commands = new ArrayList<String>();
    
    public CommandTool(String sName, Material hItemType, String sOwnerName)
    {
        ownerName = sOwnerName;
        itemType = hItemType;
        setName(sName);
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
        if(virtualplayer == null) createVirtualPlayer(player);
        Location loc = player.getTargetBlock(null, 100).getLocation();
        
        //virtualplayer.teleport(loc);
        virtualplayer.moveTo(loc);
        //virtualplayer.executeCommand(command);
        for(String sCommand : commands)
        {
            virtualplayer.executeCommand(sCommand);
        }
    }
    
    public final void notify(String message)
    {
        Player player = Bukkit.getPlayerExact(ownerName);
        if(player == null) return;
        player.sendMessage("§6" + name + ">§r " + message);
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
        id = sName;
        
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
        this((String) map.get("id"), Material.getMaterial((String) map.get("material")), (String) sOwnerName);
        
        setCommands((List<String>) map.get("commands"));
        
    }
    
    @Override
    public Map<String, Object> serialize() 
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("commands", commands);
        map.put("id", id);
        map.put("material", itemType.toString());
        return map;
    }
    
    /*===== Internal object management =====*/
    
    
    private void createVirtualPlayer(Player player)
    {
        virtualplayer = VirtualPlayer.createVirtualPlayer(player.getName(), player.getLocation(), this);
    }
    
}
