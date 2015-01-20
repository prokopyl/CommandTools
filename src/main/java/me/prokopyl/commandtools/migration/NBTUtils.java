package me.prokopyl.commandtools.migration;

import java.lang.reflect.Method;
import java.util.UUID;
import me.prokopyl.commandtools.ToolManager;
import me.prokopyl.commandtools.CommandTool;
import me.prokopyl.commandtools.PluginLogger;
import me.prokopyl.commandtools.nbt.reflection.ReflectionUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

abstract public class NBTUtils 
{
    private static final String NBT_OWNER_TAG_NAME = "CommandToolOwner";
    private static final String NBT_ID_TAG_NAME = "CommandToolId";
    private static final String NBT_EDITION_TAG_NAME = "CommandToolEdition";
    
    public static boolean checkOldToolInHandMigration(Player player)
    {
        ItemStack item = player.getItemInHand();
        if(!isOldCommandTool(item)) return false;
        
        UUID ownerUUID = UsernameDictionary.getOldUser(getCommandToolOwner(item));
        String toolID = getCommandToolID(item);
        CommandTool tool = ToolManager.getTool(ownerUUID, toolID);
        
        if(tool == null)
        {
            player.sendMessage("§4This tool does not exist in the Tool Database. This tool may have been deleted.");
            player.setItemInHand(new ItemStack(Material.AIR));
            return true;
        }
        
        player.setItemInHand(tool.createItem());
        return true;
    }
    
    private static String getCommandToolID(ItemStack item)
    {
        return (String) getNBTTagRO(item ,"String", NBT_ID_TAG_NAME);
    }
    
    private static String getCommandToolOwner(ItemStack item)
    {
        return (String) getNBTTagRO(item, "String", NBT_OWNER_TAG_NAME);
    }
    
    private static boolean getToolEditorMode(ItemStack item)
    {
        return (Boolean) getNBTTagRO(item, "Boolean", NBT_EDITION_TAG_NAME);
    }
    
    private static Object getNBTTagRO(ItemStack item, String tagType, String tagName)
    {
        try
        {
            Class craftItemStack = ReflectionUtils.getBukkitClassByName("inventory.CraftItemStack");
            Object mcItem = craftItemStack.getMethod("asNMSCopy", ItemStack.class)
                    .invoke(null, item);
            Object nbtTagCompound = ReflectionUtils.call(mcItem, "getTag");
            if(nbtTagCompound == null)
            {
                nbtTagCompound = ReflectionUtils.instanciate(
                        ReflectionUtils.getMinecraftClassByName("NBTTagCompound"));
            }
            return ReflectionUtils.call(nbtTagCompound, "get"+tagType, tagName);
        }
        catch(Exception ex)
        {
            PluginLogger.LogError("Failed to retreive NBT tag from item", ex);
            return null;
        }
    }
    
    static private boolean isOldCommandTool(ItemStack item)
    {
        if(item.getType() == Material.AIR) return false;
        if(NBTUtils.getToolEditorMode(item) == true) return false;
        return !(NBTUtils.getCommandToolID(item).equals(""));
    }
    
    
}
