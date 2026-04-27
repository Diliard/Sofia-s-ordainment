package de.sofia.sofias_ordainment.origins.powers;

import io.github.apace100.apoli.power.ModifyDamageTakenPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierOperation;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.List;
import java.util.Locale;

public class SoapWeaknessPower extends ModifyDamageTakenPower {
    private static final Modifier SOAP_DAMAGE = ModifierUtil.createSimpleModifier(ModifierOperation.MULTIPLY_TOTAL_MULTIPLICATIVE, 1.3);

    public SoapWeaknessPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity, damage -> true, SoapWeaknessPower::attackerIsHoldingSoap);
    }

    @Override
    public List<Modifier> getModifiers() {
        return List.of(SOAP_DAMAGE);
    }

    private static boolean attackerIsHoldingSoap(Pair<Entity, Entity> pair) {
        if (!(pair.getLeft() instanceof LivingEntity attacker)) return false;

        for (Hand hand : Hand.values()) {
            if (isSoap(attacker.getStackInHand(hand))) {
                return true;
            }
        }

        return false;
    }

    private static boolean isSoap(ItemStack stack) {
        if (stack.isEmpty()) return false;

        Identifier id = Registries.ITEM.getId(stack.getItem());
        return id.toString().toLowerCase(Locale.ROOT).contains("soap");
    }
}
