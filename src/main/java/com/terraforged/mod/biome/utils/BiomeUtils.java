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

package com.terraforged.mod.biome.utils;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.terraforged.mod.featuremanager.util.codec.Codecs;
import com.terraforged.mod.util.reflect.Accessor;
import com.terraforged.mod.util.reflect.FieldAccessor;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class BiomeUtils {

    private static final Codec<Biome.Climate> CLIMATE_CODEC = Biome.Climate.CODEC.codec();
    private static final Map<RegistryKey<Biome>, BiomeBuilder> BUILDERS = new HashMap<>();
    private static final FieldAccessor<Biome, Biome.Climate> CLIMATE_ACCESSOR = Accessor.field(Biome.class, Biome.Climate.class);

    public static BiomeBuilder getBuilder(RegistryKey<Biome> biome) {
        return BUILDERS.computeIfAbsent(biome, BiomeUtils::copy).init();
    }

    public static BiomeBuilder copy(RegistryKey<Biome> key) {
        Biome biome = ForgeRegistries.BIOMES.getValue(key.location());
        if (biome == null) {
            throw new NullPointerException(key.location().toString());
        }

        BiomeBuilder builder = new BiomeBuilder(key, biome);

        builder.scale(biome.getScale());
        builder.depth(biome.getDepth());
        builder.biomeCategory(biome.getBiomeCategory());

        // ambience
        builder.specialEffects(biome.getSpecialEffects());

        // climate
        Biome.Climate climate = getClimate(biome);
        builder.downfall(climate.downfall);
        builder.temperature(climate.temperature);
        builder.precipitation(climate.precipitation);
        builder.temperatureAdjustment(climate.temperatureModifier);

        // mobs
        builder.mobSpawnSettings(biome.getMobSettings());

        return builder;
    }

    private static Biome.Climate getClimate(Biome biome) {
        try {
            return CLIMATE_ACCESSOR.get(biome);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        try {
            JsonElement json = Codecs.encodeAndGet(Biome.DIRECT_CODEC, biome, JsonOps.INSTANCE);
            return Codecs.decodeAndGet(CLIMATE_CODEC, new Dynamic<>(JsonOps.INSTANCE, json));
        } catch (Throwable t) {
            t.printStackTrace();
            return new Biome.Climate(Biome.RainType.RAIN, 0.5F, Biome.TemperatureModifier.NONE, 0.5F);
        }
    }
}
