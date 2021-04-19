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

package com.terraforged.mod.api.material.geology;

import com.terraforged.engine.world.geology.Geology;
import com.terraforged.engine.world.geology.Strata;
import net.minecraft.block.BlockState;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;

public interface GeologyManager {

    StrataGenerator getStrataGenerator();

    /**
     * Register a global strata group (applies to any biome that does not have specific geology defined.
     */
    void register(Strata<BlockState> strata);

    /**
     * Register a biome specific strata group.
     */
    default void register(RegistryKey<Biome> biome, Strata<BlockState> strata) {
        register(biome, strata, false);
    }

    /**
     * Register a biome specific strata group.
     */
    void register(RegistryKey<Biome> biome, Strata<BlockState> strata, boolean inheritGlobal);

    /**
     * Register/replace a biome-specific geology group
     */
    void register(RegistryKey<Biome> biome, Geology<BlockState> geology);
}
