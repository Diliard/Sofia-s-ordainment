package de.sofia.sofias_ordainment;

import de.sofia.sofias_ordainment.entities.CustomPotionEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class CustomPotionItem extends Item {

    public CustomPotionItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);


        if (!world.isClient) {
            CustomPotionEntity entity = new CustomPotionEntity(world, user);
            entity.setItem(stack);
            entity.setVelocity(user, user.getPitch(), user.getYaw(), 0, 1.2f, 1.0f);
            world.spawnEntity(entity);
        }

        if (!user.getAbilities().creativeMode) {
            stack.decrement(1);
        }


        user.getWorld().playSoundAtBlockCenter(user.getBlockPos(), SoundEvents.ENTITY_SPLASH_POTION_THROW, SoundCategory.PLAYERS,100f, 0f, true);
        return TypedActionResult.success(stack, world.isClient);
    }
}

