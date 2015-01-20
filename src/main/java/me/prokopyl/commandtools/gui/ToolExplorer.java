/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package me.prokopyl.commandtools.gui;

import java.util.ArrayList;
import java.util.HashMap;
import me.prokopyl.commandtools.CommandTool;
import me.prokopyl.commandtools.CommandTools;
import me.prokopyl.commandtools.ToolManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ToolExplorer implements Listener
{
    static private final int INVENTORY_ROW_SIZE = 9;
    static private final int MAX_INVENTORY_SIZE = INVENTORY_ROW_SIZE * 6;
    static private final String EXPLORER_INVENTORY_TITLE = "CommandTool explorer";
    static private final HashMap<HumanEntity, ViewData> users = new HashMap<HumanEntity, ViewData>();
    
    static public void openExplorer(HumanEntity player)
    {
        ArrayList<CommandTool> toolList = ToolManager.getToolList(player.getUniqueId());
        int size = getIdealInventorySize(toolList.size());
        Inventory view = Bukkit.createInventory(player, size, EXPLORER_INVENTORY_TITLE);
        
        if(toolList.size() <= MAX_INVENTORY_SIZE)
        {
            fillSinglePageView(view, toolList);
            users.put(player, new ViewData(false, toolList));
        }
        else
        {
            fillMultiplePageView(view, toolList);
            users.put(player, new ViewData(true, toolList));
        }
        
        player.openInventory(view);
    }
    
    static private void fillSinglePageView(Inventory view, ArrayList<CommandTool> toolList)
    {
        for(int i = 0; i < toolList.size(); i++)
        {
            view.setItem(i, createDisplayItem(toolList.get(i)));
        }
    }
    
    static private void fillMultiplePageView(Inventory view, ArrayList<CommandTool> toolList)
    {
        
    }
    
    static private ItemStack createDisplayItem(CommandTool tool)
    {
        ItemStack item = new ItemStack(tool.getType(), 1);
        ItemMeta metaData = item.getItemMeta();
        metaData.setDisplayName(tool.getId());
        item.setItemMeta(metaData);
        return item;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void clickExplorer(InventoryClickEvent event)
    {
        if(!users.containsKey(event.getWhoClicked())) return;
        if(event.getRawSlot() < event.getInventory().getSize())
        {
            switch(event.getAction())
            {
                case PICKUP_ALL: case PICKUP_HALF: case PICKUP_ONE: case PICKUP_SOME:
                case HOTBAR_MOVE_AND_READD: case HOTBAR_SWAP:
                case MOVE_TO_OTHER_INVENTORY:
                    actionPickTool(event); break;
                case PLACE_ALL: case PLACE_ONE: case PLACE_SOME:
                case SWAP_WITH_CURSOR:
                    actionPutTool(event); break;
                case DROP_ALL_CURSOR: case DROP_ONE_CURSOR: 
                    break;
                default:
                    event.setCancelled(true);
            }
        }
        else //The user clicked in its own inventory
        {
            if(event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY))
            {
                actionMoveTool(event);
            }
        }
    }
    
    private void actionPickTool(InventoryClickEvent event)
    {
        String toolId = event.getCurrentItem().getItemMeta().getDisplayName();
        CommandTool tool = ToolManager.getTool(event.getWhoClicked().getUniqueId(), toolId);
        event.setCurrentItem(tool.createItem());
        
        //We must delay the re-creating of the display item
        Bukkit.getScheduler().scheduleSyncDelayedTask(CommandTools.getPlugin(), 
                new CreateDisplayItemTask(event.getInventory(), tool, event.getRawSlot()));
    }
    
    private void actionPutTool(InventoryClickEvent event)
    {
        event.setCancelled(true); 
        if(!CommandTool.isCommandTool(event.getCursor())) return;
        
        event.setCursor(new ItemStack(Material.AIR));
    }
    
    private void actionMoveTool(InventoryClickEvent event)
    {
        event.setCancelled(true); 
        if(!CommandTool.isCommandTool(event.getCurrentItem())) return;
        event.setCurrentItem(new ItemStack(Material.AIR));
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void closeExplorer(InventoryCloseEvent event)
    {
        if(users.containsKey(event.getPlayer())) users.remove(event.getPlayer());
    }
    
    static private int getIdealInventorySize(int itemCount)
    {
        return Math.min(((int)(Math.ceil((double) itemCount / INVENTORY_ROW_SIZE))) * INVENTORY_ROW_SIZE, MAX_INVENTORY_SIZE);
    }
    
    static private class ViewData
    {
        private final boolean multiPage;
        private int currentPage;
        
        private final ArrayList<CommandTool> toolList;
        private final int pageCount;
        
        public ViewData(boolean multiPage, ArrayList<CommandTool> toolList)
        {
            this.multiPage = multiPage;
            this.currentPage = 1;
            this.toolList = toolList;
            this.pageCount = (int) Math.ceil(toolList.size() / (MAX_INVENTORY_SIZE - INVENTORY_ROW_SIZE));
        }
        
        public boolean isMultiPage()
        {
            return this.multiPage;
        }
        
        public ArrayList<CommandTool> getToolList()
        {
            return this.toolList;
        }
        
        public int getPage()
        {
            return this.currentPage;
        }
        
        public void nextPage()
        {
            this.currentPage++;
            if(this.currentPage > this.pageCount) this.currentPage = 1;
        }
        
        public void previousPage()
        {
            this.currentPage--;
            if(this.currentPage <= 0) this.currentPage = this.pageCount;
        }
        
    }
    
    private class CreateDisplayItemTask implements Runnable
    {
        private final Inventory inventory;
        private final CommandTool tool;
        private final int slot;
        
        public CreateDisplayItemTask(Inventory inventory, CommandTool tool, int slot)
        {
            this.inventory = inventory;
            this.tool = tool;
            this.slot = slot;
        }
        
        @Override
        public void run() 
        {
            inventory.setItem(slot, createDisplayItem(tool));
            for(HumanEntity player : inventory.getViewers())
            {
                ((Player)player).updateInventory();
            }
        }
        
    }
    
}
