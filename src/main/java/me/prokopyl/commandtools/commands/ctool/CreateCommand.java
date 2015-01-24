/*
 * Copyright (C) 2015 Prokopyl<prokopylmc@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package me.prokopyl.commandtools.commands.ctool;

import me.prokopyl.commandtools.CommandTool;
import me.prokopyl.commandtools.ToolManager;
import me.prokopyl.commandtools.commands.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@CommandInfo(name = "new")
public class CreateCommand extends Command
{
    public CreateCommand(Commands commandGroup) {
        super(commandGroup);
    }

    @Override
    protected void run() throws CommandException
    {
        Player player = playerSender();
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

}
