package echen0719.serverscan.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class guiUtils {
    public static EditBox createInputBox(int x, int y, int width, int height, String hint) {
        EditBox box = new EditBox(Minecraft.getInstance().font, x, y, width, height, Component.literal(""));
        box.setHint(Component.literal(hint));
        return box;
    }

    public static Button createButton(Screen screen, String label, int x, int y, int width, int height, Button.OnPress action) {
        Button button = Button.builder(Component.literal(label), action).bounds(x, y, width, height).build();
        return button;
    }
}
