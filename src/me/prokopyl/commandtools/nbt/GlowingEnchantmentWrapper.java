/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package me.prokopyl.commandtools.nbt;

/**
 *
 * @author prokopyl
 */
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;
 
public class GlowingEnchantmentWrapper extends EnchantmentWrapper {
    
    private static GlowingEnchantmentWrapper GlowEnchantment;
    
    static public GlowingEnchantmentWrapper getGlowingEnchantment()
    {
        if(GlowEnchantment == null) Enable();
        return GlowEnchantment;
    }
    
    static private void Enable()
    {
        try
        {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);

            GlowEnchantment = new GlowingEnchantmentWrapper(69);
            EnchantmentWrapper.registerEnchantment(GlowEnchantment);

        }catch(Exception e){
            Logger.getLogger(GlowingEnchantmentWrapper.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    public GlowingEnchantmentWrapper(int id) {
        super(id);
    }
   
    @Override
    public boolean canEnchantItem(ItemStack item) {
        return true;
    }
 
    @Override
    public boolean conflictsWith(Enchantment other) {
        return false;
    }
 
    @Override
    public EnchantmentTarget getItemTarget() {
        return null;
    }
 
    @Override
    public int getMaxLevel() {
        return 2;
    }
 
    @Override
    public String getName() {
        return "JoliGlow";
    }
 
    @Override
    public int getStartLevel() {
        return 1;
    }
}