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

package com.terraforged.mod.profiler;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.gen.GenerationStage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public enum Profiler {
    STRUCTURE_STARTS("structure starts"),
    STRUCTURE_REFS("structure references"),
    BIOMES("biomes"),
    TERRAIN("base terrain"),
    SURFACE("biome surfaces"),
    AIR_CARVERS("air carvers"),
    FLUID_CARVERS("fluid carvers"),
    DECORATION("features & structures"),
    MOB_SPAWNS("mob spawns"),
    ;

    private final AtomicLong time = new AtomicLong();
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong longest = new AtomicLong();
    private final AtomicLong shortest = new AtomicLong(Long.MAX_VALUE);
    private final ThreadLocal<Section> section = ThreadLocal.withInitial(ProfilerSection::new);

    private final String reportDescription;

    Profiler(String reportDescription) {
        this.reportDescription = reportDescription;
    }

    public String getReportDescription() {
        return reportDescription;
    }

    public Section punchIn() {
        return section.get().punchIn();
    }

    public void punchOut() {
        section.get().close();
    }

    public long timeMS() {
        return TimeUnit.NANOSECONDS.toMillis(time.get());
    }

    public long minMS() {
        long min = shortest.get();
        if (min == Long.MAX_VALUE) {
            return 0;
        }
        return TimeUnit.NANOSECONDS.toMillis(min);
    }

    public long maxMS() {
        return TimeUnit.NANOSECONDS.toMillis(longest.get());
    }

    public long hits() {
        return hits.get();
    }

    public double averageMS() {
        return timeMS() / Math.max(1.0, hits());
    }

    public ITextComponent toText() {
        return new StringTextComponent(name().toLowerCase())
                .append(new StringTextComponent(String.format(": %.3fms", averageMS()))
                        .withStyle(style -> style.withColor(TextFormatting.WHITE)))
                .withStyle(style -> style.withColor(TextFormatting.YELLOW)
                        .withHoverEvent(createHoverStats(minMS(), maxMS())));
    }

    public static Profiler get(GenerationStage.Carving stage) {
        return stage == GenerationStage.Carving.AIR ? AIR_CARVERS : FLUID_CARVERS;
    }

    public static HoverEvent createHoverStats(long min, long max) {
        String message = String.format("Min: %sms, Max: %sms", min, max);
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new StringTextComponent(message).withStyle(s -> s.withColor(TextFormatting.WHITE)));
    }

    public static void reset() {
        for (Profiler profiler : Profiler.values()) {
            profiler.time.set(0);
            profiler.hits.set(0);
            profiler.longest.set(0);
            profiler.shortest.set(Long.MAX_VALUE);
        }
    }

    public static void dump(File dir) {
        dump(dir.toPath());
    }

    public static void dump(Path dir) {
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            try (BufferedWriter writer = Files.newBufferedWriter(dir.resolve("dump-" + System.currentTimeMillis() + ".txt"))) {
                ProfilerPrinter.print(writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ProfilerSection implements Section {

        private long timestamp = 0L;

        @Override
        public Section punchIn() {
            timestamp = System.nanoTime();
            return this;
        }

        @Override
        public void close() {
            long duration = System.nanoTime() - timestamp;
            time.addAndGet(duration);
            hits.incrementAndGet();

            if (duration <= 0) {
                return;
            }

            long max = longest.get();
            if (duration > max) {
                longest.compareAndSet(max, duration);
            }

            long min = shortest.get();
            if (duration < min) {
                shortest.compareAndSet(min, duration);
            }
        }
    }
}
