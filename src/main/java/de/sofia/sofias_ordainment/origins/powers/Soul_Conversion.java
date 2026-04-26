package de.sofia.sofias_ordainment.origins.powers;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;

import java.util.Map;

public class Soul_Conversion extends Power {
    public Soul_Conversion(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }

    public void removeEnchant() {
        PlayerEntity player = ((PlayerEntity)this.entity);
        PlayerInventory inv = player.getInventory();

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);

            if (!stack.hasEnchantments()) continue;

            Map<Enchantment, Integer> enchants = EnchantmentHelper.get(stack);
            int total = 0;

            for (var entry : enchants.entrySet()) {
                Enchantment enchant = entry.getKey();
                int level = entry.getValue();

                total += enchant.getRarity().getWeight() * level;
            }

            total *= 2;
            player.addExperience(total);

            EnchantmentHelper.set(Map.of(), stack);
        }
    }

    public void mendItems(int experience) {
        PlayerEntity player = (PlayerEntity) this.entity;
        PlayerInventory inv = player.getInventory();
        int mendLeft = experience;

        for (int i = 0; i < inv.size(); i++) {
            if (mendLeft <= 0) break;

            ItemStack stack = inv.getStack(i);
            if (!stack.isDamageable() || !stack.isDamaged() || stack.isEmpty()) continue;

            int damageBefore = stack.getDamage();
            int repairAmount = Math.min(damageBefore, mendLeft);

            //Attempt repair (this is a check to avoid modded items to eat XP)
            stack.setDamage(damageBefore - repairAmount);
            int damageAfter = stack.getDamage();

            if (damageAfter < damageBefore) {
                int actualRepaired = damageBefore - damageAfter;
                mendLeft -= actualRepaired;
            }
        }
    }

    public int getTotalItemDamage() {
        PlayerEntity player = ((PlayerEntity)this.entity);
        PlayerInventory inv = player.getInventory();

        int total = 0;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);

            if (!stack.isDamageable()) continue;
            if (!stack.isDamaged()) continue;

            total += stack.getDamage();
        }

        return total;
    }
}
