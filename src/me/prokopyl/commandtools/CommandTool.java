
package me.prokopyl.commandtools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.prokopyl.commandtools.attributes.ToolAttribute;
import me.prokopyl.commandtools.interpreter.Interpreter;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class CommandTool implements ConfigurationSerializable
{
    private String name;
    private String id;
    private final UUID ownerUUID;
    private Material itemType;
    private final ArrayList<String> commands = new ArrayList<String>();
    
    public CommandTool(String sId, String sName, Material hItemType, UUID ownerUUID)
    {
        this.ownerUUID = ownerUUID;
        itemType = hItemType;
        id = sId;
        name = sName;
        
    }
    
    public CommandTool(String sId, CommandTool otherTool, UUID ownerUUID)
    {
        this(sId, otherTool.getName(), otherTool.getType(), ownerUUID);
        setCommands(otherTool.getCommands());
    }
    
    public ItemStack createItem()
    {
        synchronized(this)
        {
            ItemStack item = new ItemStack(itemType, 1);
            setCommandToolData(item);
            return ToolAttribute.toItemStack(this, item);
        }
    }
    
    private void setCommandToolData(ItemStack item)
    {
        ItemMeta metaData = item.getItemMeta();
        metaData.setDisplayName(name);
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("§6§l§nCommand Tool");
        lore.add("§7" + commands.size() + " lines");
        metaData.setLore(lore);
        item.setItemMeta(metaData);
    }
    
    public void use(Player player)
    {
        synchronized(this)
        {
            Interpreter.Execute(this, player);
        }
    }
    
    public void notify(String message, Player player)
    {
        player.sendMessage("§6" + "Tool" + ">§r " + message);
    }
    
    /*===== Getters & Setters =====*/
    
    public UUID getOwnerUUID()
    {
        return ownerUUID;
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
        synchronized(this)
        {   
            id = sId;
            name = sName;
        }
    }
    
    public List<String> getCommands()
    {
        List<String> listCommands = new ArrayList<String>();
        synchronized(this){listCommands.addAll(commands);}
        return listCommands;
    }
    
    public void setCommands(List<String> newCommands)
    {
        synchronized(this)
        {
            commands.clear();
            commands.addAll(newCommands);
        }
    }
    
    /*===== CommandTools Utils =====*/
    
    static public boolean isCommandTool(ItemStack item)
    {
        if(item.getType() == Material.AIR) return false;
        ToolAttribute attribute = ToolAttribute.fromItemStack(item);
        if(attribute == null) return false;
        if(attribute.isToolEditor()) return false;
        return true;
    }
    
    /*===== Serializable object =====*/
    public CommandTool(Map<String, Object> map, UUID ownerUUID)
    {
        //this(sName, hItem, hOwner);
        this((String) map.get("id"),(String) map.get("name"), Material.getMaterial((String) map.get("material")), ownerUUID);
        
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
