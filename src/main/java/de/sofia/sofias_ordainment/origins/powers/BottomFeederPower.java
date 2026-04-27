package de.sofia.sofias_ordainment.origins.powers;

import io.github.apace100.apoli.power.PowerType;
import net.merchantpug.apugli.power.EdibleItemPower;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Set;

public class BottomFeederPower extends EdibleItemPower {
    private static final int FOOD_GAIN = 4;
    private static final float SATURATION_MODIFIER = 0.5F;

    private static final FoodComponent FOOD_COMPONENT = new FoodComponent.Builder()
            .hunger(FOOD_GAIN)
            .saturationModifier(SATURATION_MODIFIER)
            .build();

    private static final Set<Item> EDIBLE_ITEMS = Set.of(
            Items.ROTTEN_FLESH,
            Items.SPIDER_EYE,
            Items.POISONOUS_POTATO,
            Items.RED_MUSHROOM,
            Items.BROWN_MUSHROOM
    );

    private static final Set<Block> EDIBLE_BLOCKS = Set.of(
            Blocks.DIRT,
            Blocks.COARSE_DIRT,
            Blocks.ROOTED_DIRT,
            Blocks.GRASS_BLOCK,
            Blocks.PODZOL,
            Blocks.MYCELIUM,
            Blocks.MUD,
            Blocks.MUDDY_MANGROVE_ROOTS,
            Blocks.SCULK,
            Blocks.SCULK_VEIN,
            Blocks.SCULK_CATALYST,
            Blocks.SCULK_SENSOR,
            Blocks.SCULK_SHRIEKER,
            Blocks.RED_MUSHROOM_BLOCK,
            Blocks.BROWN_MUSHROOM_BLOCK,
            Blocks.MUSHROOM_STEM
    );

    public BottomFeederPower(PowerType<?> type, LivingEntity entity) {
        super(
                type,
                entity,
                pair -> isEdibleStack(pair.getRight()),
                FOOD_COMPONENT,
                EatAnimation.EAT,
                ItemStack.EMPTY,
                null,
                null,
                null
        );
    }

    private static boolean isEdibleStack(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (EDIBLE_ITEMS.contains(stack.getItem())) return true;
        return stack.getItem() instanceof BlockItem blockItem && EDIBLE_BLOCKS.contains(blockItem.getBlock());
    }
}
