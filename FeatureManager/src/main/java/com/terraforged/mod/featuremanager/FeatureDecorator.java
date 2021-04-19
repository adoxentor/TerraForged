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

package com.terraforged.mod.featuremanager;

import com.terraforged.mod.featuremanager.biome.BiomeFeature;
import com.terraforged.mod.featuremanager.biome.BiomeFeatures;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;

import java.util.List;

public interface FeatureDecorator {

    int FEATURE_STAGES = GenerationStage.Decoration.values().length;

    FeatureManager getFeatureManager();

    default void decorate(ChunkGenerator generator, StructureManager manager, ISeedReader region, IChunk chunk, Biome biome, BlockPos pos) {
        SharedSeedRandom random = new SharedSeedRandom();
        long decorationSeed = random.setDecorationSeed(region.getSeed(), pos.getX(), pos.getZ());

        BiomeFeatures biomeFeatures = getFeatureManager().getFeatures(biome);
        List<List<BiomeFeature>> stagedFeatures = biomeFeatures.getFeatures();
        List<List<Structure<?>>> stagedStructures = biomeFeatures.getStructures();

        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

        int startX = chunkPos.getMinBlockX();
        int startZ = chunkPos.getMinBlockZ();
        MutableBoundingBox chunkBounds = new MutableBoundingBox(startX, startZ, startX + 15, startZ + 15);

        for (int stageIndex = 0; stageIndex < FEATURE_STAGES; stageIndex++) {
            int featureSeed = 0;

            if (stageIndex < stagedStructures.size()) {
                List<Structure<?>> structures = stagedStructures.get(stageIndex);
                for (int structureIndex = 0; structureIndex < structures.size(); structureIndex++) {
                    Structure<?> structure = structures.get(structureIndex);
                    random.setFeatureSeed(decorationSeed, featureSeed++, stageIndex);
                    try {
                        manager.startsForFeature(SectionPos.of(pos), structure).forEach(start -> start.placeInChunk(
                                region,
                                manager,
                                generator,
                                random,
                                chunkBounds,
                                chunkPos
                        ));
                    } catch (Throwable t) {
                        handle("structure", structure.getFeatureName(), t);
                    }
                }
            }

            if (stageIndex < stagedFeatures.size()) {
                List<BiomeFeature> features = stagedFeatures.get(stageIndex);
                for (int featureIndex = 0; featureIndex < features.size(); featureIndex++) {
                    BiomeFeature feature = features.get(featureIndex);
                    random.setFeatureSeed(decorationSeed, featureSeed++, stageIndex);
                    if (feature.getPredicate().test(chunk, biome)) {
                        try {
                            feature.getFeature().place(region, generator, random, pos);
                        } catch (Throwable t) {
                            handle("feature", feature.getIdentity().getComponents(), t);
                        }
                    }
                }
            }
        }
    }

    static void handle(String type, String identity, Throwable t) {
        FeatureManager.LOG.fatal("Fatal error placing {} '{}'", type, identity);
        t.printStackTrace();
    }
}
