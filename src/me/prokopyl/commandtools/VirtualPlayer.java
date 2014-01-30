package me.prokopyl.commandtools;

import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.MinecraftServer;
import net.minecraft.server.v1_7_R1.PlayerInteractManager;
import net.minecraft.server.v1_7_R1.WorldServer;
import net.minecraft.util.com.mojang.authlib.GameProfile;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R1.CraftServer;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class VirtualPlayer extends CraftPlayer
{
private PlayerToolStore toolStore;
private Location location;
    
public static VirtualPlayer createVirtualPlayer(String name, Location location, PlayerToolStore hToolStore)
{
    VirtualPlayer virtualplayer;
    
    CraftServer cserver = (CraftServer) Bukkit.getServer();
    MinecraftServer mcserver = cserver.getServer();
    WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
    WorldServer worldserver = mcserver.getWorldServer(0);
    PlayerInteractManager pim = new PlayerInteractManager(worldserver);
    virtualplayer = new VirtualPlayer(cserver, mcserver, world, name, pim, hToolStore);
    virtualplayer.setLocation(location);
    
    return virtualplayer;
}

public VirtualPlayer(CraftServer cserver, MinecraftServer mcserver,
                WorldServer world, String s, PlayerInteractManager iiw, PlayerToolStore hToolStore)
{
        super(cserver, new EntityPlayer(mcserver, world, new GameProfile("0", s), iiw));
        toolStore = hToolStore;
        this.setOp(true);
}

public VirtualPlayer(CraftServer cserver, EntityPlayer ep)
{
        super(cserver, ep);
}

@Override
public void updateInventory()
{
        // / Do nothing
}

@Override
public void sendMessage(String s){
        toolStore.notify(s);
}

public void moveTo(Location hloc){
        entity.move(hloc.getX(), hloc.getY(), hloc.getZ());
        location = hloc;
}

@Override
public Location getLocation()
{
    return location;
}

public void setLocation(Location hLoc)
{
    location = hLoc;
}

public void executeCommand(String command)
{
    PlayerCommandPreprocessEvent pcpe = new PlayerCommandPreprocessEvent(this, "/" + command);
    Bukkit.getPluginManager().callEvent(pcpe);
    Bukkit.getServer().dispatchCommand(this, command);

}

}

/*private void createVirtualPlayer()
    {
        CraftServer cserver = (CraftServer) Bukkit.getServer();
        List<World> worlds = cserver.getWorlds();
        if (worlds == null || worlds.isEmpty())
        {
            return;
        }
        CraftWorld w = (CraftWorld) worlds.get(0);
        Location location = owner.getLocation();
        MinecraftServer mcserver = cserver.getServer();
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        WorldServer worldserver = mcserver.getWorldServer(0);
        PlayerInteractManager pim = new PlayerInteractManager(worldserver);
        virtualplayer = new VirtualPlayer(cserver, mcserver, world, owner.getName(), pim, this);
        virtualplayer.setLocation(location);
    }*/