/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package me.prokopyl.commandtools.nbt;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;

abstract public class ReflectionUtils 
{
    static public String getBukkitPackageVersion()
    {
        return getBukkitPackageName().substring("org.bukkit.craftbukkit.".length());
    }
    
    static public String getBukkitPackageName()
    {
        return Bukkit.getServer().getClass().getPackage().getName();
    }
    
    static public String getMinecraftPackageName()
    {
        return "net.minecraft.server." + getBukkitPackageVersion();
    }
    
    static public Class getBukkitClassByName(String name) throws ClassNotFoundException
    {
        return Class.forName(getBukkitPackageName() + "." + name);
    }
    
    static public Class getMinecraftClassByName(String name) throws ClassNotFoundException
    {
        return Class.forName(getMinecraftPackageName() + "." + name);
    }
    
    static public Object getField(Object instance, String name) 
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        Field field = instance.getClass().getField(name);
        field.setAccessible(true);
        return field.get(instance);
    }
    
    static public void setField(Object instance, String name, Object value) 
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        Field field = instance.getClass().getField(name);
        field.setAccessible(true);
        field.set(instance, value);
    }
    
    static public Object call(Class hClass, String name, Object ... parameters) 
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        return call(hClass, null, name, parameters);
    }
    
    static public Object call(Object instance, String name, Object ... parameters) 
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        return call(instance.getClass(), instance, name, parameters);
    }
    
    static public Object call(Class hClass, Object instance, String name, Object ... parameters) 
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Method method = hClass.getMethod(name, getTypes(parameters));
        return method.invoke(instance, parameters);
    }
    
    static public Object instanciate(Class hClass, Object ... parameters) 
            throws NoSuchMethodException, InstantiationException, 
            IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Constructor constructor = hClass.getConstructor(getTypes(parameters));
        return constructor.newInstance(parameters);
    }
    
    static public Class[] getTypes(Object[] objects)
    {
        Class[] types = new Class[objects.length];
        for(int i = 0; i < objects.length; i++)
        {
            types[i] = objects[i].getClass();
        }
        return types;
    }
}
