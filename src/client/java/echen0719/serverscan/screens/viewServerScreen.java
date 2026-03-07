package echen0719.serverscan.screens;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import echen0719.serverscan.utils.guiUtils;

public class viewServerScreen extends Screen {
    private final Screen parent;

    public viewServerScreen(Screen parent) {
        super(Component.literal("View Servers"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {    
        super.render(context, mouseX, mouseY, delta);
    }
}
