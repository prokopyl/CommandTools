package me.prokopyl.commandtools.interpreter;

import java.util.UUID;
import net.minecraft.server.v1_7_R3.MinecraftServer;
import net.minecraft.server.v1_7_R3.PlayerInteractManager;
import net.minecraft.server.v1_7_R3.WorldServer;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R3.CraftServer;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class VirtualPlayer extends CraftPlayer
{
    private final Environment environment;
    private Location location;
    private boolean isCommandRunning;

    public static VirtualPlayer createVirtualPlayer(UUID uuid, Environment environment)
    {
        VirtualPlayer virtualplayer;
        
        Location location = Bukkit.getPlayer(uuid).getLocation();
        CraftServer cserver = (CraftServer) Bukkit.getServer();
        MinecraftServer mcserver = cserver.getServer();
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        WorldServer worldserver = mcserver.getWorldServer(0);
        PlayerInteractManager pim = new PlayerInteractManager(worldserver);
        VirtualEntityPlayer entity = new VirtualEntityPlayer(mcserver, world, new GameProfile(uuid, Bukkit.getPlayer(uuid).getName()), pim);
        virtualplayer = new VirtualPlayer(cserver, entity, environment);
        virtualplayer.moveTo(location);

        return virtualplayer;
    }

    public VirtualPlayer(CraftServer cserver, VirtualEntityPlayer entity, Environment environment)
    {
            super(cserver, entity);
            entity.setVirtualPlayer(this);
            this.isCommandRunning = false;
            this.environment = environment;
    }

    @Override
    public void updateInventory()
    {
            // / Do nothing
    }

    @Override
    public void sendMessage(String s){
            if(isCommandRunning) environment.notify(s);
    }

    public void moveTo(Location hloc)
    {
            entity.world = ((CraftWorld) hloc.getWorld()).getHandle();
            entity.locX = hloc.getX();
            entity.locY = hloc.getY();
            entity.locZ = hloc.getZ();
            location = hloc.clone();
    }

    @Override
    public Location getLocation()
    {
        return location;
    }
    
    public void executeCommand(String sCommand)
    {
        isCommandRunning = true;
        PlayerCommandPreprocessEvent pcpe = new PlayerCommandPreprocessEvent(this, sCommand);
        Bukkit.getPluginManager().callEvent(pcpe);
        if(sCommand.charAt(0) == '/') sCommand = sCommand.substring(1);
        Bukkit.getServer().dispatchCommand(this, sCommand);
        isCommandRunning = false;

    }

}