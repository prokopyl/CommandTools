package me.prokopyl.commandtools;

import me.prokopyl.commandtools.nbt.GlowingEnchantmentWrapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.prokopyl.commandtools.attributes.ToolAttribute;
import org.bukkit.Material;
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

public static ItemStack createEditedCommandTool(CommandTool tool)
{
    ItemStack book = new ItemStack(Material.BOOK_AND_QUILL, 1);
    book.addUnsafeEnchantment(GlowingEnchantmentWrapper.getGlowingEnchantment(), 1);
    
    BookMeta metaData = (BookMeta) book.getItemMeta();
    metaData.setDisplayName("§6[Edition]§r" + tool.getName());
    
    metaData.setPages(commandsToPages(tool.getCommands()));
    
    book.setItemMeta(metaData);
    
    return ToolAttribute.toItemStack(tool, book, true);
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
    ItemStack book = event.getPlayer().getItemInHand();
    if(isToolEditor(book))
    {
        CommandTool tool = ToolManager.getTool(book);
        tool.setCommands(pagesToCommands(event.getNewBookMeta().getPages()));
        event.getPlayer().setItemInHand(tool.createItem());
        event.getPlayer().sendMessage("Commands successfuly updated.");
    }
}

public static boolean isToolEditor(ItemStack hItem)
{
    if(hItem.getType() != Material.BOOK_AND_QUILL) return false;
    ToolAttribute attribute = ToolAttribute.fromItemStack(hItem);
    if(attribute == null) return false;
    return attribute.isToolEditor();
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

}
