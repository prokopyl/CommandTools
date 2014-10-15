package me.prokopyl.commandtools.migration;

import java.util.UUID;
import me.prokopyl.commandtools.ToolManager;
import me.prokopyl.commandtools.CommandTool;
import net.minecraft.server.v1_7_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemStack;
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
            player.sendMessage("§4This tool does not exist in the Tool Database. This may indicate a corrupted savefile or tool database .");
            player.setItemInHand(new ItemStack(Material.AIR));
            return true;
        }
        
        player.setItemInHand(tool.createItem());
        return true;
    }
    
    private static String getCommandToolID(ItemStack item)
    {
        return getNBTTagRO(item).getString(NBT_ID_TAG_NAME);
    }
    
    private static String getCommandToolOwner(ItemStack item)
    {
        return getNBTTagRO(item).getString(NBT_OWNER_TAG_NAME);
    }
    
    private static boolean getToolEditorMode(ItemStack item)
    {
        return getNBTTagRO(item).getBoolean(NBT_EDITION_TAG_NAME);
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
    
    static private boolean isOldCommandTool(ItemStack item)
    {
        if(item.getType() == Material.AIR) return false;
        if(NBTUtils.getToolEditorMode(item) == true) return false;
        return !(NBTUtils.getCommandToolID(item).equals(""));
    }
    
    
}
