/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.api.material;

import com.terraforged.mod.Log;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class WGTags {

    public static final ITag.INamedTag<Block> STONE = tag("forge:wg_stone");
    public static final ITag.INamedTag<Block> DIRT = tag("forge:wg_dirt");
    public static final ITag.INamedTag<Block> CLAY = tag("forge:wg_clay");
    public static final ITag.INamedTag<Block> SEDIMENT = tag("forge:wg_sediment");
    public static final ITag.INamedTag<Block> ERODIBLE = tag("forge:wg_erodible");
    public static final List<ITag.INamedTag<Block>> WG_TAGS = Collections.unmodifiableList(Arrays.asList(STONE, DIRT, CLAY, SEDIMENT, ERODIBLE));
    public static final Set<ResourceLocation> NAMED_WG_TAGS = WG_TAGS.stream().map(ITag.INamedTag::getName).collect(Collectors.toSet());

    public static void init() {
        Log.info("Initializing tags");
    }

    private static Tags.IOptionalNamedTag<Block> tag(String name) {
        return BlockTags.createOptional(new ResourceLocation(name));
    }

    public static Predicate<BlockState> stone() {
        return toStatePredicate(STONE);
    }

    private static Predicate<BlockState> toStatePredicate(ITag<Block> tag) {
        return state -> tag.contains(state.getBlock());
    }

    public static void printTags() {
        for (ITag.INamedTag<Block> tag : WG_TAGS) {
            Log.debug("World-Gen Tag: {}", tag.getName());
            for (Block block : tag.getValues()) {
                Log.debug(" - {}", block.getRegistryName());
            }
        }
    }
}
