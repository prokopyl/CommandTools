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

import java.util.List;
import me.prokopyl.commandtools.CommandTool;
import me.prokopyl.commandtools.ToolManager;
import me.prokopyl.commandtools.commands.*;
import org.bukkit.entity.Player;

@CommandInfo(name =  "list")
public class ListCommand extends Command
{

    public ListCommand(Commands commandGroup) {
        super(commandGroup);
    }

    @Override
    protected void run() throws CommandException 
    {
        Player player = playerSender();
        List<CommandTool> toolList = ToolManager.getToolList(player.getUniqueId());
        if(toolList == null|| toolList.isEmpty())
        {
            info("No tool found.");
            return;
        }

        info("§7" + toolList.size() + " tools found.");

        String sToolList = toolList.get(0).getId();
        for(int i = 1; i < toolList.size(); i++)
        {
            sToolList += "§7, §r" + toolList.get(i).getId();
        }
        player.sendMessage(sToolList);
    }

}
