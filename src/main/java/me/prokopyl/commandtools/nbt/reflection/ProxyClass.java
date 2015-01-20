/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package me.prokopyl.commandtools.nbt.reflection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

abstract public class ProxyClass
{
    static public Object createInstance(Class hClass, Class hInterface) throws InstantiationException, IllegalAccessException
    {
        ProxyClassHandler handler = new ProxyClassHandler();
        
        ProxyClass instance = (ProxyClass) Proxy.newProxyInstance(hInterface.getClassLoader(), 
                new Class<?>[]{hInterface}, handler);
        
        handler.instance = instance;
        instance.handler = handler;
        return instance;
    }
    
    private ProxyClassHandler handler;
    
    protected Object getFallbackInstance()
    {
        return handler.fallbackInstance;
    }
    
    protected void setFallbackInstance(Object instance)
    {
        handler.fallbackInstance = instance;
    }
    
    protected Object getField(String name) 
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        return ReflectionUtils.getField(getFallbackInstance(), name);
    }
    
    protected void setField(String name, Object value)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        ReflectionUtils.setField(getFallbackInstance(), name, value);
    }
    
    static private class ProxyClassHandler implements InvocationHandler
    {
        public ProxyClass instance;
        public Object fallbackInstance;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable 
        {
            Method toCall = ReflectionUtils.getCompatibleMethod(proxy.getClass(), method);
            if(toCall == null && fallbackInstance != null)
            {
                toCall = ReflectionUtils.getCompatibleMethod(fallbackInstance.getClass(), method);
            }

            return toCall.invoke(proxy, args);
        }
    }

    
}
