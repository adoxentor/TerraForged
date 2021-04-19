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

package com.terraforged.mod.client.gui.screen.page;

import com.terraforged.mod.client.gui.GuiKeys;
import com.terraforged.mod.client.gui.element.TFTextBox;
import com.terraforged.mod.client.gui.page.BasePage;
import com.terraforged.mod.client.gui.screen.Instance;
import com.terraforged.mod.client.gui.screen.overlay.OverlayScreen;
import com.terraforged.mod.util.DataUtils;
import com.terraforged.mod.util.DimUtils;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.world.ForgeWorldType;
import net.minecraftforge.registries.ForgeRegistries;

public class WorldPage extends BasePage {

    private final UpdatablePage preview;
    private final Instance instance;

    private CompoundNBT worldSettings = null;
    private CompoundNBT dimSettings = null;

    public WorldPage(Instance instance, UpdatablePage preview) {
        this.instance = instance;
        this.preview = preview;
    }

    @Override
    public String getTitle() {
        return GuiKeys.WORLD_SETTINGS.get();
    }

    @Override
    public void save() {

    }

    @Override
    public void init(OverlayScreen parent) {
        // re-sync settings from the settings object to the data structure
        worldSettings = getWorldSettings();
        dimSettings = getDimSettings();

        Column left = getColumn(0);
        addElements(left.left, left.top, left, worldSettings, true, left.scrollPane::addButton, this::update);

        addElements(left.left, left.top, left, dimSettings, true, left.scrollPane::addButton, this::update);
    }

    @Override
    public void onAddWidget(Widget widget) {
        if (widget instanceof TFTextBox) {
            TFTextBox input = (TFTextBox) widget;
            input.setColorValidator(string -> ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(string)));
        }
    }

    protected void update() {
        super.update();
        preview.apply(settings -> {
            DataUtils.fromNBT(worldSettings, settings.world);
            DataUtils.fromNBT(dimSettings, settings.dimensions);
        });
    }

    private CompoundNBT getWorldSettings() {
        return instance.settingsData.getCompound("world");
    }

    private CompoundNBT getDimSettings() {
        CompoundNBT dimSettings = instance.settingsData.getCompound("dimensions");
        CompoundNBT generators = dimSettings.getCompound("dimensions");
        for (String name : generators.getAllKeys()) {
            if (name.startsWith("#")) {
                INBT value = generators.get(name.substring(1));
                if (value instanceof StringNBT) {
                    CompoundNBT metadata = generators.getCompound(name);
                    metadata.put("options", getWorldTypes());
                }
            }
        }
        return dimSettings;
    }

    private static ListNBT getWorldTypes() {
        ListNBT options = new ListNBT();
        for (ForgeWorldType type : ForgeRegistries.WORLD_TYPES) {
            String name = DimUtils.getDisplayString(type);
            INBT value = StringNBT.valueOf(name);
            options.add(value);
        }
        return options;
    }
}
