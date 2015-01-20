package me.prokopyl.commandtools.interpreter;

import java.lang.reflect.Constructor;
import java.util.UUID;
import me.prokopyl.commandtools.PluginLogger;
import me.prokopyl.commandtools.nbt.reflection.ProxyClass;
import me.prokopyl.commandtools.nbt.reflection.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class VirtualPlayer extends ProxyClass
{
    private Environment environment;
    private Location location;
    private boolean isCommandRunning = false;

    public static VirtualPlayer createVirtualPlayer(UUID uuid, Environment environment)
    {
        try
        {
            VirtualPlayer instance = (VirtualPlayer) createInstance(VirtualPlayer.class, Player.class);
            instance.init(uuid, environment);
            return instance;
        }
        catch(Exception ex)
        {
            PluginLogger.LogError("Could not create virtual player", ex);
            return null;
        }
    }
    
    public static void setLocation(Player player, Location hloc)
    {
        try{
        Object entity = ReflectionUtils.call(player, "getHandle");
        World world = hloc.getWorld();
        entity.getClass().getMethod("setLocation", 
                double.class, double.class, double.class, float.class, float.class)
                .invoke(entity, hloc.getX(), hloc.getY(), hloc.getZ(), hloc.getYaw(), hloc.getPitch());
        }catch(Exception ex)
        {
            PluginLogger.LogError("Could not set player virtual location", ex);
        }
    }

    public VirtualPlayer()
    {
        
    }
    
    protected void init(UUID uuid, Environment environment)
    {
        this.environment = environment;
        setFallbackInstance(createCraftPlayer(uuid));
    }

    public void updateInventory()
    {
            // / Do nothing
    }

    public void sendMessage(String s){
            if(isCommandRunning) environment.notify(s);
    }

    public void moveTo(Location hloc)
    {
        try{
        Object entity = getField("entity");
        World world = hloc.getWorld();
        ReflectionUtils.setField(entity, "world", ReflectionUtils.call(world, "getHandle"));
        ReflectionUtils.setField(entity, "locX", hloc.getX());
        ReflectionUtils.setField(entity, "locY", hloc.getY());
        ReflectionUtils.setField(entity, "locZ", hloc.getZ());
        location = hloc.clone();
        }catch(Exception ex){}
    }

    public Location getLocation()
    {
        return location.clone();
    }
    
    public boolean teleport(Location newLocation)
    {
        environment.applyTeleportation(newLocation);
        location = newLocation.clone();
        return true;
    }
    
    public boolean teleport(Location newLocation, TeleportCause cause)
    {
        teleport(newLocation);
        return ((Player)getFallbackInstance()).teleport(newLocation, cause);
    }
    
    public void executeCommand(String sCommand)
    {
        isCommandRunning = true;
        PlayerCommandPreprocessEvent pcpe = new PlayerCommandPreprocessEvent((Player)this, sCommand);
        Bukkit.getPluginManager().callEvent(pcpe);
        if(sCommand.charAt(0) == '/') sCommand = sCommand.substring(1);
        Bukkit.getServer().dispatchCommand((Player)this, sCommand);
        isCommandRunning = false;
    }
    
    private Player createCraftPlayer(UUID uuid)
    {
        
        try
        {
            Location hlocation = Bukkit.getPlayer(uuid).getLocation();
            Object craftServer = Bukkit.getServer();
            Object minecraftServer = ReflectionUtils.call(craftServer, "getServer");
            Object minecraftWorld = ReflectionUtils.call(hlocation.getWorld(), "getHandle");
            
            Object minecraftWorldServer = minecraftServer.getClass()
                    .getMethod("getWorldServer", int.class)
                    .invoke(minecraftServer, 0);
            
            Object playerInteractManager = ReflectionUtils.getMinecraftClassByName("PlayerInteractManager").
                    getConstructor(ReflectionUtils.getMinecraftClassByName("World"))
                    .newInstance(minecraftWorldServer);
            
            Object gameProfile = ReflectionUtils.instanciate(
                    Class.forName("com.mojang.authlib.GameProfile"),
                    uuid, Bukkit.getPlayer(uuid).getName());
            
            Object craftEntityPlayer = ReflectionUtils.getMinecraftClassByName("EntityPlayer")
                    .getConstructor(ReflectionUtils.getMinecraftClassByName("MinecraftServer"), 
                            ReflectionUtils.getMinecraftClassByName("WorldServer"),
                            gameProfile.getClass(), playerInteractManager.getClass())
                    .newInstance(minecraftServer, minecraftWorld, gameProfile, playerInteractManager);
            Object craftPlayer = ReflectionUtils.instanciate(
                    ReflectionUtils.getBukkitClassByName("entity.CraftPlayer"), 
                    craftServer, craftEntityPlayer);
            return (Player) craftPlayer;
        }
        catch(Exception ex)
        {
            PluginLogger.LogError("Failed to create craft player.", ex);
            return null;
        }
    }
    
}