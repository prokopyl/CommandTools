package me.prokopyl.commandtools;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

/**
 *
 * @author adrien
 */
public class ToolEditor implements Listener {
    
private final CommandTools plugin;
public static GlowingEnchantmentWrapper GlowEnchantment;

public ToolEditor(CommandTools hPlugin)
{
    plugin = hPlugin;
    enableGlowEnchantment();
}

public static ItemStack createEditedCommandTool(CommandTool tool) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
{
    ItemStack book = NBTUtils.createCraftItemStack(Material.BOOK_AND_QUILL, 1);
    book.addUnsafeEnchantment(ToolEditor.GlowEnchantment, 1);
    
    BookMeta metaData = (BookMeta) book.getItemMeta();
    metaData.setDisplayName("§6[Edition]§r" + tool.getName());
    
    String[] aLore = {"Hi !"};
    
    metaData.setLore(Arrays.asList(aLore));
    metaData.setPages(commandsToPages(tool.getCommands()));
    
    book.setItemMeta(metaData);
    NBTUtils.setCommandToolOwner(book, tool.getOwnerName());
    NBTUtils.setCommandToolID(book, tool.getId());
    
    
    return book;
}

@EventHandler(priority=EventPriority.HIGH)
public void onItemDrop(PlayerDropItemEvent event)
{
    if(isToolEditor(event.getItemDrop().getItemStack()))
    {
        event.getPlayer().sendMessage("§cYou can't drop a Command Tool in edition mode !");
        event.setCancelled(true);
    }
}

@EventHandler(priority=EventPriority.HIGH)
public void onBookEdit(PlayerEditBookEvent event)
{
    if(isToolEditor(event.getPlayer().getItemInHand()))
    {
        ItemStack book = event.getPlayer().getItemInHand();
        CommandTool tool = plugin.getTool(NBTUtils.getCommandToolOwner(book), NBTUtils.getCommandToolID(book));
        tool.setCommands(pagesToCommands(event.getNewBookMeta().getPages()));
        event.getPlayer().setItemInHand(tool.createItem());
        event.getPlayer().sendMessage("Commands successfuly updated.");
    }
}

public static boolean isToolEditor(ItemStack hItem)
{
    if(hItem.getType() != Material.BOOK_AND_QUILL) return false;
    return NBTUtils.getCommandToolOwner(hItem).length() > 0;
}

private static List<String> commandsToPages(List<String> commands)
{
    ArrayList<String> pages = new ArrayList<String>();
    String currentPage = "";
    for(String sCommand : commands)
    {
        currentPage += sCommand + "\n";
    }
    
    pages.add(currentPage);
    
    return pages;
}

private static List<String> pagesToCommands(List<String> pages)
{
    ArrayList<String> commands = new ArrayList<String>();
    
    for(String sPage : pages)
    {
        commands.addAll(Arrays.asList(sPage.split("\n")));
    }
    
    return commands;
}

private void enableGlowEnchantment()
{
    try
    {
        Field f = Enchantment.class.getDeclaredField("acceptingNew");
        f.setAccessible(true);
        f.set(null, true);
        
        try 
        {
            GlowEnchantment = new GlowingEnchantmentWrapper(61037);
            EnchantmentWrapper.registerEnchantment(GlowEnchantment);
        } catch (IllegalArgumentException e){}
        
    }catch(Exception e){
        Logger.getLogger(CommandTool.class.getName()).log(Level.SEVERE, null, e);
    }
}

}