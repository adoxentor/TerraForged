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

package com.terraforged.mod.featuremanager.matcher.biome;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.terraforged.engine.world.biome.map.BiomeContext;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.featuremanager.modifier.Jsonifiable;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public class BiomeMatcher implements Predicate<Biome>, Comparable<BiomeMatcher>, Jsonifiable {

    public static final BiomeMatcher ANY = new BiomeMatcher(Collections.emptySet());

    private final Set<Biome> biomes;

    private BiomeMatcher(Set<Biome> biomes) {
        this.biomes = biomes;
    }

    @Override
    public String getType() {
        return "biomes";
    }

    @Override
    public JsonElement toJson(BiomeContext<?> context) {
        if (!biomes.isEmpty()) {
            JsonArray array = new JsonArray();
            biomes.stream().map(Biome::getRegistryName).map(Objects::toString).sorted().forEach(array::add);
            return array;
        }
        return JsonNull.INSTANCE;
    }

    @Override
    public int compareTo(BiomeMatcher o) {
        // reverse order so more biomes == tested first
        return Integer.compare(o.biomes.size(), biomes.size());
    }

    @Override
    public boolean test(Biome biome) {
        return biomes.isEmpty() || biomes.contains(biome);
    }

    public BiomeMatcher or(BiomeMatcher other) {
        Set<Biome> combined = new HashSet<>();
        combined.addAll(biomes);
        combined.addAll(other.biomes);
        return new BiomeMatcher(combined);
    }

    public static BiomeMatcher vanilla(TFBiomeContext context) {
        return of(context, "minecraft:*");
    }

    public static BiomeMatcher of(TFBiomeContext context, Biome.Category... types) {
        Set<Biome> biomes = new HashSet<>();
        for (Biome biome : context.biomes) {
            for (Biome.Category category : types) {
                if (biome.getBiomeCategory() == category) {
                    biomes.add(biome);
                    break;
                }
            }
        }
        if (biomes.isEmpty()) {
            return ANY;
        }
        return BiomeMatcher.of(biomes);
    }

    public static BiomeMatcher of(TFBiomeContext context, String... biomes) {
        BiomeMatcherParser.Collector collector = new BiomeMatcherParser.Collector();
        for (String name : biomes) {
            BiomeMatcherParser.collectBiomes(name, collector, context);
        }
        return collector.create();
    }

    @SafeVarargs
    public static BiomeMatcher of(TFBiomeContext context, RegistryKey<Biome>... biomes) {
        BiomeMatcherParser.Collector collector = new BiomeMatcherParser.Collector();
        for (RegistryKey<Biome> name : biomes) {
            BiomeMatcherParser.collectBiomes(name.location().toString(), collector, context);
        }
        return collector.create();
    }

    public static BiomeMatcher of(Biome biome) {
        return new BiomeMatcher(Collections.singleton(biome));
    }

    public static BiomeMatcher of(Set<Biome> biomes) {
        return new BiomeMatcher(biomes);
    }
}
