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

package me.prokopyl.commandtools.commands;

public class CommandException extends Exception
{
    public enum Reason
    {
        COMMANDSENDER_EXPECTED_PLAYER
    }
    
    private final Reason reason;
    
    public CommandException(Reason reason)
    {
        this.reason = reason;
    }
    
    public Reason getReason() { return reason; }
    
    public String getReasonString()
    {
        switch(reason)
        {
            case COMMANDSENDER_EXPECTED_PLAYER:
                return "You must be a player to use this command.";
            default:
                return "An unknown error suddenly happened.";
        }
    }
}
