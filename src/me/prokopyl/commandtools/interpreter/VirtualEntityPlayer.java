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

package me.prokopyl.commandtools.interpreter;

import net.minecraft.server.v1_7_R3.EntityPlayer;
import net.minecraft.server.v1_7_R3.IChatBaseComponent;
import net.minecraft.server.v1_7_R3.MinecraftServer;
import net.minecraft.server.v1_7_R3.PlayerInteractManager;
import net.minecraft.server.v1_7_R3.WorldServer;
import net.minecraft.util.com.mojang.authlib.GameProfile;

public class VirtualEntityPlayer extends EntityPlayer
{
    private VirtualPlayer virtualPlayer;
    public VirtualEntityPlayer(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) 
    {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
        
    }
    
    @Override
    public void sendMessage(IChatBaseComponent ichatbasecomponent)
    {
        virtualPlayer.sendMessage(ichatbasecomponent.c());
    }
    
    public void setVirtualPlayer(VirtualPlayer virtualPlayer)
    {
        this.virtualPlayer = virtualPlayer;
    }

}
