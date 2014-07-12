/*
 * Copyright (C) 2014 adrien
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

package me.prokopyl.commandtools.interpreter;

import java.util.HashSet;
import java.util.UUID;
import me.prokopyl.commandtools.CommandTool;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Environment 
{
    
    private static final HashSet<Byte> TRANSPARENT_BLOCKS = new HashSet<Byte>();
    private final UUID playerUUID;
    private VirtualPlayer virtualPlayer;
    
    //Execution variables
    private CommandTool currentTool;
    private Player currentPlayer;
    private Location currentLocation;
    private int commandsExecuted;
    
    public Environment(UUID playerUUID)
    {
        this.playerUUID = playerUUID;
        
        if(TRANSPARENT_BLOCKS.isEmpty())
        {
            TRANSPARENT_BLOCKS.add((byte)0);
            TRANSPARENT_BLOCKS.add((byte)8);
            TRANSPARENT_BLOCKS.add((byte)9);
        }
    }
    
    public void init(CommandTool tool, Player player)
    {
        currentTool = tool;
        currentPlayer = player;
        currentLocation = player.getTargetBlock(TRANSPARENT_BLOCKS, 100).getLocation();
        commandsExecuted = 0;
        
        getVirtualPlayer().moveTo(currentLocation);
    }
    
    public void executeCommand(String command)
    {
        virtualPlayer.executeCommand(command);
        commandsExecuted++;
    }
    
    public void exit()
    {
        if(commandsExecuted == 0) notify("§7This tool has no command assigned. Use §f/ctool edit§7 to add some.");
        
        currentTool = null;
        currentPlayer = null;
        currentLocation = null;
        
    }
    
    public final void notify(String message)
    {
        if(currentPlayer == null) return;
        currentPlayer.sendMessage("§6" + currentTool.getName() + ">§r " + message);
    }
    
    private VirtualPlayer getVirtualPlayer() 
    { 
        if(virtualPlayer == null) virtualPlayer = VirtualPlayer.createVirtualPlayer(playerUUID, this);
        return virtualPlayer; 
    }
}
