/*
 * Copyright (C) 2014 ProkopyL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.prokopyl.commandtools;

import java.util.List;
import me.prokopyl.commandtools.migration.UUIDMigrator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

abstract public class CommandExecutor 
{
static private CommandSender sender;
static private String[] args;
static public void Execute(CommandSender sender, String[] args)
{
    CommandExecutor.sender = sender;
    CommandExecutor.args = args;
    
    if(args.length <= 0)
    {
        Help();
        return;
    }
    
    try
    {
        switch (args[0].toLowerCase()) 
        {
            case "new": Create(); break;
            case "edit": Edit(); break;
            case "list": List(); break;
            case "get": Get(); break;
            case "info": Info(); break;
            case "rename": Rename(); break;
            case "delete": DeleteConfirm(); break;
            case "delete-noconfirm": DeleteNoConfirm(); break;
            case "remove": Remove(); break;
            case "clear": Clear(); break;
            case "enable": SetEnabled(true); break;
            case "disable": SetEnabled(false); break;
            case "migrate": Migrate(); break;
            default: sender.sendMessage("§cUnknown action.");
            case "help": Help();
        }
    }
    catch(InvalidCommandSenderException e)
    {
        sender.sendMessage("§cYou must be a " + e.expectedSender() +" to use this command.");
    }
}

static private void Create() throws InvalidCommandSenderException
{
    Player player = (Player) sender;
    Inventory playerInventory = player.getInventory();
    
    ItemStack itemInHand = player.getItemInHand();
    if(itemInHand.getType() == Material.AIR)
    {
        player.sendMessage("§cYou must have an item in hand in order to get a tool.");
        return;
    }
    
    int firstEmptySlot = playerInventory.firstEmpty();
    if(firstEmptySlot < 0)
    {
        player.sendMessage("§cYour inventory is full ! You must have some space left in order to get a tool.");
        return;
    }

    CommandTool newTool = ToolManager.createNewTool(player, itemInHand.getType());

    playerInventory.setItem(firstEmptySlot, itemInHand);
    player.setItemInHand(newTool.createItem());
}

static private void Edit() throws InvalidCommandSenderException
{
    Player player = playerSender();
    ItemStack item = player.getItemInHand();
    CommandTool tool = ToolManager.getTool(item);
    if(tool == null)
    {
        player.sendMessage("§cYou must have a tool in your hand.");
        return;
    }
    
    if(ToolEditor.isToolEditor(item))
    {
        player.setItemInHand(tool.createItem());
    }
    else
    {
        player.setItemInHand(ToolEditor.createEditedCommandTool(tool));
    }
}

static private void List() throws InvalidCommandSenderException
{
    Player player = playerSender();
    List<CommandTool> toolList = ToolManager.getToolList(player.getUniqueId());
    if(toolList == null|| toolList.isEmpty())
    {
        player.sendMessage("§7No tool found.");
        return;
    }
    
    player.sendMessage("§7" + toolList.size() + " tools found.");
    
    String sToolList = toolList.get(0).getId();
    for(int i = 1; i < toolList.size(); i++)
    {
        sToolList += "§7, §r" + toolList.get(i).getId();
    }
    player.sendMessage(sToolList);
}

static private void Get() throws InvalidCommandSenderException
{
    Player player = playerSender();
    if(player.getInventory().firstEmpty() < 0)
    {
        player.sendMessage("§cYour inventory is full ! You must have some space left in order to get a tool.");
        return;
    }
    
    String toolName = "";
    
    if(args.length < 2)
    {
        player.sendMessage("§cYou must give a name to get your tool.");
        return;
    }
    
    for(int i = 1; i < args.length; i++)
    {
        toolName += args[i] + " ";
    }
    toolName = toolName.trim();
    
    CommandTool tool = ToolManager.getTool(player.getUniqueId(), toolName);
    
    if(tool == null)
    {
        player.sendMessage("§cThis tool does not exist.");
        return;
    }
    player.getInventory().addItem(tool.createItem());
}

static private void Info() throws InvalidCommandSenderException
{
    Player player = playerSender();
    CommandTool tool = getDesignatedTool(player);
    if(tool == null) return;
    
    player.sendMessage("§3§l ===== Information about " + tool.getName() + " =====");
    player.sendMessage("§7 Name : §r" + tool.getName());
    player.sendMessage("§7 Idetifier : §r" + tool.getId());
    player.sendMessage("§7 Material : §r" + tool.getType().toString());
    player.sendMessage("§7 Lines : §r" + tool.getCommands().size());
    
}

static private void Rename() throws InvalidCommandSenderException
{
    Player player = playerSender();
    CommandTool tool = getToolInHand(player);
    if(tool == null) return;
    
    String sNewName = "";
    
    if(args.length < 2)
    {
        player.sendMessage("§cYou must give a name to your tool.");
        return;
    }
    
    for(int i = 1; i < args.length; i++)
    {
        sNewName += args[i] + " ";
    }
    sNewName = sNewName.trim();
    
    tool.rename(ToolManager.getNextAvailableToolID(sNewName, player.getUniqueId()), sNewName);
    
    player.setItemInHand(tool.createItem());
}

static private void DeleteConfirm() throws InvalidCommandSenderException
{
    Player player = playerSender();
    CommandTool tool = getDesignatedTool(player);
    if(tool != null)
    {
        TellRaw(player, "{text:\"You are going to delete \",extra:[{text:\""+ tool.getId() +"\",color:gold},{text:\". Are you sure ? \",color:white}," +
            "{text:\"[Confirm]\", color:green, clickEvent:{action:run_command,value:\"/ctool delete-noconfirm "+ tool.getId() +"\"}, " + 
            "hoverEvent:{action:show_text,value:{text:\"This tool will be deleted \",extra:[{text:\"forever\",color:red,bold:true,italic:true,underlined:true}, {text:\" !\", underlined:true}],underlined:true}}}]}");
    }
}

static private void DeleteNoConfirm() throws InvalidCommandSenderException
{
    Player player = playerSender();
    CommandTool tool = getDesignatedTool(player);
    
    if(tool != null) 
    {
        Clear(player, tool);
        ToolManager.deleteTool(tool);
        player.sendMessage("§7Tool successfully deleted.");
    }
    
}

static private void Remove() throws InvalidCommandSenderException
{
    Player player = playerSender();
    if(!CommandTool.isCommandTool(player.getItemInHand()))
    {
        player.sendMessage("§cThis is not a valid command tool.");
        return;
    }
    
    player.setItemInHand(new ItemStack(Material.AIR));
    player.sendMessage("§7This tool has been removed from your inventory.");
    
}

static private void Clear() throws InvalidCommandSenderException
{
    Player player = playerSender();
    CommandTool tool = getDesignatedTool(player);
    if(tool == null) return;
    Clear(player, tool);
}

static private void Clear(Player player, CommandTool tool)
{
    //Remove all the tool copies from the player's inventory
    for(int i = 0; i < player.getInventory().getSize(); i++)
    {
        ItemStack item = player.getInventory().getItem(i);
        CommandTool itemTool = ToolManager.getTool(item);
        if(itemTool == null) continue;
        if(itemTool.equals(tool))
        {
            player.getInventory().setItem(i, new ItemStack(Material.AIR));
        }
    }
}

static private void SetEnabled(boolean enabled) throws InvalidCommandSenderException
{
    Player player = playerSender();
    ToolManager.setEnabled(player.getUniqueId(), enabled);
    player.sendMessage("§7Your Command Tools are now " + (enabled ? "§2enabled" : "§cdisabled") + "§7.");
}

static private void Migrate()
{
    if(!sender.isOp())
    {
        sender.sendMessage("§cYou must be an operator to start CommandTool migration.");
        return;
    }
    
    sender.sendMessage("Migration started. See console for details.");
    UUIDMigrator.Migrate();
}

static private void Help()
{
    sender.sendMessage("Usage :");
}


static private Player playerSender() throws InvalidCommandSenderException
{
    if(!(sender instanceof Player)) throw new InvalidCommandSenderException("player");
    return (Player)sender;
}

static private CommandTool getToolInHand(Player player)
{
    ItemStack itemInHand = player.getItemInHand();
    if(itemInHand.getType() == Material.AIR || !CommandTool.isCommandTool(itemInHand))
    {
        player.sendMessage("§cYou must have a tool in hand.");
        return null;
    }

    CommandTool tool = ToolManager.getTool(itemInHand);
    if(tool == null)
    {
        player.sendMessage("§cThis tool does not exist in the Tool Database. It may have been deleted.");
        return null;
    }

    return tool;

}

static private CommandTool getDesignatedTool(Player player)
{
    CommandTool tool = null;
    
    if(args.length >= 2)//Designated by name
    {
        String toolName = "";
        for(int i = 1; i < args.length; i++)
        {
            toolName += args[i] + " ";
        }
        toolName = toolName.trim();
        
        tool = ToolManager.getTool(player.getUniqueId(), toolName);
        if(tool == null)
        {
            player.sendMessage("§cThis tool does not exist.");
        }
    }
    else
    {
        tool = getToolInHand(player);
    }
    
    return tool;
}

static public void TellRaw(Player player, String rawMessage)
{
    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + " " + rawMessage);
}

}

class InvalidCommandSenderException extends Exception
{
    private final String expectedSender;
    public InvalidCommandSenderException(String expectedSender)
    {
        this.expectedSender = expectedSender;
    }
    
    public String expectedSender() { return expectedSender; }
}
