package com.muguett.moonforge.gui;

import com.muguett.moonforge.config.MoonforgeConfig;
import com.muguett.moonforge.config.MoonforgeConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.Locale;

public class MoonforgeConfigScreen extends Screen {
    private final Screen parent;
    private final MoonforgeConfig workingCopy;

    public MoonforgeConfigScreen(Screen parent) {
        super(Text.literal("Moonforge Tuning"));
        this.parent = parent;
        MoonforgeConfig config = MoonforgeConfigManager.getConfig();
        this.workingCopy = copy(config);
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 155;
        int y = this.height / 4;

        this.addDrawableChild(new ConfigSlider(left, y, 310, 20, "Voidburst Speed", 2.0D, 10.0D, workingCopy.voidburstSpeed, value -> workingCopy.voidburstSpeed = value));
        y += 24;
        this.addDrawableChild(new ConfigSlider(left, y, 310, 20, "Voidburst Drop", 0.0D, 0.08D, workingCopy.voidburstGravity, value -> workingCopy.voidburstGravity = value));
        y += 24;
        this.addDrawableChild(new ConfigSlider(left, y, 310, 20, "Guidebow Speed", 0.8D, 4.0D, workingCopy.guidebowSpeed, value -> workingCopy.guidebowSpeed = value));
        y += 24;
        this.addDrawableChild(new ConfigSlider(left, y, 310, 20, "Guidebow Drop", 0.0D, 0.08D, workingCopy.guidebowGravity, value -> workingCopy.guidebowGravity = value));
        y += 24;
        this.addDrawableChild(new ConfigSlider(left, y, 310, 20, "Guidebow Steering", 0.05D, 0.5D, workingCopy.guidebowSteerStrength, value -> workingCopy.guidebowSteerStrength = value));

        y += 34;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save"), button -> {
            apply();
            this.close();
        }).dimensions(this.width / 2 - 102, y, 100, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), button -> this.close()).dimensions(this.width / 2 + 2, y, 100, 20).build());
    }

    private void apply() {
        MoonforgeConfig config = MoonforgeConfigManager.getConfig();
        config.voidburstSpeed = workingCopy.voidburstSpeed;
        config.voidburstGravity = workingCopy.voidburstGravity;
        config.guidebowSpeed = workingCopy.guidebowSpeed;
        config.guidebowGravity = workingCopy.guidebowGravity;
        config.guidebowSteerStrength = workingCopy.guidebowSteerStrength;
        MoonforgeConfigManager.save();
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 18, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Tune weapon speed, drop, and steering live for practice."), this.width / 2, 34, 0xA0EACD);
    }

    private static MoonforgeConfig copy(MoonforgeConfig source) {
        MoonforgeConfig copy = new MoonforgeConfig();
        copy.trajectoryEnabled = source.trajectoryEnabled;
        copy.targetIndicatorEnabled = source.targetIndicatorEnabled;
        copy.motionPredictionEnabled = source.motionPredictionEnabled;
        copy.predictionMultiplier = source.predictionMultiplier;
        copy.maxTargetRange = source.maxTargetRange;
        copy.singleplayerOnlySafeguard = source.singleplayerOnlySafeguard;
        copy.bowEnabled = source.bowEnabled;
        copy.crossbowEnabled = source.crossbowEnabled;
        copy.tridentEnabled = source.tridentEnabled;
        copy.hostileTargetsOnly = source.hostileTargetsOnly;
        copy.voidburstSpeed = source.voidburstSpeed;
        copy.voidburstGravity = source.voidburstGravity;
        copy.guidebowSpeed = source.guidebowSpeed;
        copy.guidebowGravity = source.guidebowGravity;
        copy.guidebowSteerStrength = source.guidebowSteerStrength;
        return copy;
    }

    private interface ValueConsumer {
        void accept(double value);
    }

    private static class ConfigSlider extends SliderWidget {
        private final String label;
        private final double min;
        private final double max;
        private final ValueConsumer consumer;

        private ConfigSlider(int x, int y, int width, int height, String label, double min, double max, double value, ValueConsumer consumer) {
            super(x, y, width, height, Text.empty(), (value - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.consumer = consumer;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            double current = getCurrentValue();
            this.setMessage(Text.literal(label + ": " + String.format(Locale.ROOT, "%.3f", current)));
        }

        @Override
        protected void applyValue() {
            consumer.accept(getCurrentValue());
        }

        private double getCurrentValue() {
            return min + this.value * (max - min);
        }
    }
}
