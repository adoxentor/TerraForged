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

package com.terraforged.mod.biome.spawn;

import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.engine.world.continent.SpawnType;
import com.terraforged.mod.Log;
import com.terraforged.mod.biome.provider.TFBiomeProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.ServerWorldInfo;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpawnHandler {

    @SubscribeEvent
    public static void createSpawn(WorldEvent.CreateSpawnPosition event) {
        if (event.getWorld() instanceof ServerWorld) {
            ServerWorld world =(ServerWorld) event.getWorld();
            ChunkGenerator generator = world.getChunkSource().getGenerator();
            if (generator.getBiomeSource() instanceof TFBiomeProvider) {
                Log.info("Searching for world spawn position");
                TFBiomeProvider provider = (TFBiomeProvider) generator.getBiomeSource();
                BlockPos center = getSearchCenter(provider);
                SpawnSearch search = new SpawnSearch(center, provider);

                // SpawnSearch uses a rough look-up that does not account for the effects of erosion which
                // can add or remove from the heightmap depending on location. Use the chunk generator's
                // surface lookup function to obtain the actual height
                BlockPos spawn = getSurface(generator, search.get());

                Log.info("Setting world spawn: {}", spawn);
                event.setCanceled(true);
                ServerWorldInfo info = (ServerWorldInfo) event.getSettings();
                info.setXSpawn(spawn.getX());
                info.setYSpawn(spawn.getY());
                info.setZSpawn(spawn.getZ());

                if (info.worldGenSettings().generateBonusChest()) {
                    Log.info("Generating bonus chest");
                    createBonusChest(world, spawn);
                }
            }
        }
    }

    private static BlockPos getSearchCenter(TFBiomeProvider provider) {
        SpawnType spawnType = provider.getContext().terraSettings.world.properties.spawnType;
        if (spawnType == SpawnType.WORLD_ORIGIN) {
            return BlockPos.ZERO;
        } else {
            long center = provider.getContext().heightmap.get().getContinent().getNearestCenter(0, 0);
            int x = PosUtil.unpackLeft(center);
            int z = PosUtil.unpackRight(center);
            return new BlockPos(x, 0, z);
        }
    }

    private static BlockPos getSurface(ChunkGenerator generator, BlockPos pos) {
        int surface = generator.getBaseHeight(pos.getX(), pos.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
        return new BlockPos(pos.getX(), surface, pos.getZ());
    }

    private static void createBonusChest(ServerWorld world, BlockPos pos) {
        ConfiguredFeature<?, ?> chest = Feature.BONUS_CHEST.configured(IFeatureConfig.NONE);
        chest.place(world, world.getChunkSource().getGenerator(), world.random, pos);
    }
}
