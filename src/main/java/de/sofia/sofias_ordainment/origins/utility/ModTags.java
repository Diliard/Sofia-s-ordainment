package de.sofia.sofias_ordainment.origins.utility;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import static de.sofia.sofias_ordainment.Sofias_ordainment.MOD_ID;

public class ModTags {
    public static final TagKey<Block> SCULK_BLOCKS =
            TagKey.of(RegistryKeys.BLOCK, new Identifier(MOD_ID, "sculk_blocks"));

    public static final TagKey<Block> SOUL_BLOCKS =
            TagKey.of(RegistryKeys.BLOCK, new Identifier(MOD_ID, "soul_blocks"));
}
