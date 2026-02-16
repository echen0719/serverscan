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
        }).bounds(this.width/2 - 50, this.height - 40, 100, 20).build();
        this.addRenderableWidget(backButton);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, Component.literal("Server Scanner"), this.width/2, this.height/2, 0xFFFFFF);
    }
}