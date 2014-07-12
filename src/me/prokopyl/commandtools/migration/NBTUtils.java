package me.prokopyl.commandtools.migration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.prokopyl.commandtools.CommandTool;
import net.minecraft.server.v1_7_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class NBTUtils {
    
    private static final String NBT_OWNER_TAG_NAME = "CommandToolOwner";
    private static final String NBT_ID_TAG_NAME = "CommandToolId";
    private static final String NBT_EDITION_TAG_NAME = "CommandToolEdition";
    
    public static ItemStack createCraftItemStack(Material id, int amount)
    {
        return CraftItemStack.asCraftCopy(new ItemStack(id, amount));
    }
    
    public static String getCommandToolID(ItemStack item)
    {
        return getNBTTagRO(item).getString(NBT_ID_TAG_NAME);
    }
    
    public static void setCommandToolID(ItemStack item, String owner)
    {
        getNBTTagRW(item).setString(NBT_ID_TAG_NAME, owner);
    }
    
    public static UUID getCommandToolOwner(ItemStack item)
    {
        return UUID.fromString(getNBTTagRO(item).getString(NBT_OWNER_TAG_NAME));
    }
    
    public static void setCommandToolOwner(ItemStack item, UUID ownerUUID)
    {
        getNBTTagRW(item).setString(NBT_OWNER_TAG_NAME, ownerUUID.toString());
    }
    
    public static boolean getToolEditorMode(ItemStack item)
    {
        return getNBTTagRO(item).getBoolean(NBT_EDITION_TAG_NAME);
    }
    
    public static void setToolEditorMode(ItemStack item)
    {
        getNBTTagRW(item).setBoolean(NBT_EDITION_TAG_NAME, true);
    }
    
    public static void setCommandToolData(ItemStack item, CommandTool tool)
    {
        ItemMeta metaData = item.getItemMeta();
        metaData.setDisplayName(tool.getName());
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("§6§l§nCommand Tool");
        lore.add("§7" + tool.getCommands().size() + " lines");
        metaData.setLore(lore);
        item.setItemMeta(metaData);
        NBTTagCompound tag = getNBTTagRW(item);
        tag.setString(NBT_ID_TAG_NAME, tool.getId());
        tag.setString(NBT_OWNER_TAG_NAME, tool.getOwnerUUID().toString());
    }
    
    private static NBTTagCompound getNBTTagRO(ItemStack item)
    {
        net.minecraft.server.v1_7_R3.ItemStack mcItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = mcItem.getTag();
        if(tag == null)
        {
            tag = new NBTTagCompound();
            mcItem.setTag(tag);
        }
        return tag;
    }
    
    private static NBTTagCompound getNBTTagRW(ItemStack item)
    {try{
        CraftItemStack craftItem = (CraftItemStack)item;//Getting the CraftBukkit Item Stack
        
        //Using reflection to get the handle
        Field handleField = CraftItemStack.class.getDeclaredField("handle");
        handleField.setAccessible(true);
        net.minecraft.server.v1_7_R3.ItemStack mcItem = (net.minecraft.server.v1_7_R3.ItemStack) handleField.get(craftItem);
        
        NBTTagCompound tag = mcItem.getTag();
        if(tag == null)
        {
            tag = new NBTTagCompound();
            mcItem.setTag(tag);
        }
        
        return tag;
    } catch(Exception ex) {
        Logger.getLogger(CommandTool.class.getName()).log(Level.SEVERE, null, ex);
    }
        return null;
    }
    
}
