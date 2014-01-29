
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

public class CommandTool implements ConfigurationSerializable
{
    private String name;
    private String id;
    private final Player owner;
    private Material itemType;
    private VirtualPlayer virtualplayer = null;
    private final ArrayList<String> commands = new ArrayList<String>();
    
    public CommandTool(String sName, ItemStack hItem, Player hOwner)
    {
        owner = hOwner;
        itemType = hItem.getType();
        setName(sName);
        notify("Tool successfuly created. Use /ctool edit to assign commands to this tool.");
        commands.add("/chunk");
    }
    
    public ItemStack createItem()
    {
        ItemStack item = NBTUtils.createCraftItemStack(itemType, 1);
        NBTUtils.setCommandToolData(item, this);
        return item;
    }
    
    public void use()
    {
        if(virtualplayer == null) createVirtualPlayer();
        Location loc = owner.getTargetBlock(null, 100).getLocation();
        
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
        owner.sendMessage("§6" + name + ">§r " + message);
    }
    
    /*===== Getters & Setters =====*/
    
    public String getOwnerName()
    {
        return owner.getName();
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
        /*ItemMeta metadata = linkedItem.getItemMeta();
        metadata.setDisplayName(name);
        linkedItem.setItemMeta(metadata);*/
        
    }
    
    public List<String> getCommands()
    {
        List<String> listCommands = new ArrayList<String>();
        for(int i = 0; i < listCommands.size(); i++)
        {
            listCommands.add("/" + commands.get(i));
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
        return !(NBTUtils.getCommandToolID(item).equals(""));
    }
    
    /*===== Serializable object =====*/
    public CommandTool(String sName, ItemStack hItem, Player hOwner, Map<String, Object> map)
    {
        this(sName, hItem, hOwner);
        Material mat = Material.STONE;
        
        
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
    
    
    private void createVirtualPlayer()
    {
        virtualplayer = VirtualPlayer.createVirtualPlayer(owner.getName(), owner.getLocation(), this);
    }
    
}
