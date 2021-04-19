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

package com.terraforged.mod.util;

import com.terraforged.engine.util.NameUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class TranslationKey {

    private static final Map<String, TranslationKey> keys = new HashMap<>();

    private final String translationKey;
    private final String defaultValue;

    public TranslationKey(String key, String display) {
        this.translationKey = key;
        this.defaultValue = display;
        keys.put(translationKey, this);
    }

    public String getKey() {
        return translationKey;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String get() {
        if (I18n.exists(translationKey)) {
            return I18n.get(translationKey);
        }
        return defaultValue;
    }

    public ITextComponent getText() {
        return new StringTextComponent(get());
    }

    public String get(Object... args) {
        if (I18n.exists(translationKey)) {
            return I18n.get(translationKey, args);
        }
        return defaultValue;
    }

    public static void each(Consumer<TranslationKey> consumer) {
        keys.values().stream().sorted(Comparator.comparing(TranslationKey::getKey)).forEach(consumer);
    }

    public static TranslationKey gui(String text) {
        String key = NameUtil.toDisplayNameKey(text);
        String display = NameUtil.toDisplayName(text.substring(text.lastIndexOf('.') + 1));
        return new TranslationKey(key, display);
    }

    public static TranslationKey gui(String key, String display) {
        return new TranslationKey(NameUtil.toDisplayNameKey(key), display);
    }
}
