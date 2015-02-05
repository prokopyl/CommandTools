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

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.UUID;
import me.prokopyl.commandtools.CommandTool;
import me.prokopyl.commandtools.PluginLogger;
import me.prokopyl.commandtools.nbt.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Environment 
{
    
    private static final HashSet<Byte> TRANSPARENT_BLOCKS = new HashSet<Byte>();
    private final UUID playerUUID;
    
    //Execution variables
    private CommandTool currentTool;
    private Player currentPlayer;
    private Location currentLocation;
    private Location oldLocation;
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
        oldLocation = player.getLocation().clone();
        commandsExecuted = 0;
        setPlayerLocation(currentLocation);
    }
    
    public void executeCommand(String sCommand)
    {
        PlayerCommandPreprocessEvent pcpe = new PlayerCommandPreprocessEvent(currentPlayer, sCommand);
        Bukkit.getPluginManager().callEvent(pcpe);
        if(sCommand.charAt(0) == '/') sCommand = sCommand.substring(1);
        Bukkit.getServer().dispatchCommand(currentPlayer, sCommand);
        commandsExecuted++;
    }
    
    public void exit()
    {
        if(commandsExecuted == 0) notify("§7This tool has no command assigned. Use §f/ctool edit§7 to add some.");
        
        setPlayerLocation(oldLocation);
        currentTool = null;
        currentPlayer = null;
        currentLocation = null;
        
    }
    
    public final void notify(String message)
    {
        if(currentPlayer == null) return;
        currentPlayer.sendMessage("§6" + currentTool.getName() + ">§r " + message);
    }
    
    private void setPlayerLocation(Location newLocation)
    {
        try 
        {
            Field f = ReflectionUtils.getBukkitClassByName("entity.CraftEntity").getDeclaredField("entity");
            f.setAccessible(true);
            Object mcEntity = f.get(currentPlayer);
            Class mcEntityClass = ReflectionUtils.getMinecraftClassByName("Entity");
            ReflectionUtils.setField(mcEntityClass, mcEntity, "locX", newLocation.getX());
            ReflectionUtils.setField(mcEntityClass, mcEntity, "locY", newLocation.getY());
            ReflectionUtils.setField(mcEntityClass, mcEntity, "locZ", newLocation.getZ());
        } 
        catch (Exception ex) 
        {
            PluginLogger.LogError("Could not set Player location", ex);
        }
    }
    
    public void applyTeleportation(Location newLocation)
    {
        if(currentPlayer == null) return;
        Location diffLocation = newLocation.clone().subtract(currentPlayer.getLocation().clone());
        currentPlayer.teleport(currentPlayer.getLocation().add(diffLocation));
    }
}
