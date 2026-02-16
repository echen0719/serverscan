package echen0719.serverscan;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

// https://wiki.fabricmc.net/tutorial:screen
public class scanScreen extends Screen {
    private final Screen parent;

    public scanScreen(Screen parent) {
        super(Component.literal("Server Scanner"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        Button backButton = Button.builder(Component.literal("Back"), button -> {
            this.minecraft.setScreen(parent);
        }).bounds((this.width - 100)/2, this.height - 40, 100, 20).build();
        this.addRenderableWidget(backButton);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        // bro, this single line below caused me so much confusion, it turns out colors are in 0xFFFFFFFF format, not 0xFFFFFF
        context.drawCenteredString(this.font, Component.literal("Ha! You cliked a button"), this.width/2, this.height/2, 0xFFFFFFFF);
    }
}