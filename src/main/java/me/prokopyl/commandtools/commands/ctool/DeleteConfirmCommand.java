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
import me.prokopyl.commandtools.commands.*;
import org.bukkit.entity.Player;

@CommandInfo(name =  "delete", usageParameters = "[tool name]")
public class DeleteConfirmCommand extends Command
{

    public DeleteConfirmCommand(Commands commandGroup) {
        super(commandGroup);
    }

    @Override
    protected void run() throws CommandException 
    {
        Player player = playerSender();
        CommandTool tool = getDesignatedTool(player);
        tellRaw("{text:\"You are going to delete \",extra:[{text:\""+ tool.getId() +"\",color:gold},{text:\". Are you sure ? \",color:white}," +
            "{text:\"[Confirm]\", color:green, clickEvent:{action:run_command,value:\"/ctool delete-noconfirm "+ tool.getId() +"\"}, " + 
            "hoverEvent:{action:show_text,value:{text:\"This tool will be deleted \",extra:[{text:\"forever\",color:red,bold:true,italic:true,underlined:true}, {text:\" !\", underlined:true}],underlined:true}}}]}");
        
    }
    
    @Override
    protected List<String> complete() throws CommandException
    {
        if(args.length == 1) 
            return getMatchingToolNames(playerSender(), args[0]);
        return null;
    }

}
