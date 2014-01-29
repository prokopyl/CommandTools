/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package me.prokopyl.commandtools;

/**
 *
 * @author prokopyl
 */
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;
 
public class GlowingEnchantmentWrapper extends EnchantmentWrapper {
 
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