package echen0719.serverscan.screens;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;

import echen0719.serverscan.utils.fileUtils;
import echen0719.serverscan.utils.guiUtils;

public class confirmationScreen extends Screen {
    private EditBox renameInputBox;
    private Button cancelButton, confirmButton;

    private final int white = 0xFFFFFFFF;
    private final int gray = 0xFFAAAAAA;
    private final int black = 0xFF000000;

    private File targetFile;
    private String message;
    private String type;

    private final Screen parent;
    private GuiGraphics context;
    private fileUtils filesManager = new fileUtils(FabricLoader.getInstance().getGameDirectory());
    private File[] items = filesManager.getChildFolders();

    public confirmationScreen(Screen parent, File targetFile, String type) {
        super(Component.literal("View Past Scans"));
        this.parent = parent;

        if (type.equals("RENAME")) {
            this.message = "Enter new file name for \"" + targetFile.getName() + "\":";
        }
        else if (type.equals("DELETE")) {
            this.message = "Are you sure you want to delete \"" + targetFile.getName() + "\"?";
        }
        else {
            this.message = "This developer named echen0719 can't check his own code (shake my head)...";
        }
    }

    private void createRenameDialog() {
        int widthCenter = this.width / 2;
        int heightCenter = this.height / 2;

        newNameBox = guiUtils.createInputBox(parent, widthCenter, heightCenter - 50, 100, 20, "Enter a new file name...");
        this.addRenderableOnly(newNameBox);

        cancelButton = guiUtils.createButton(parent, "Cancel", widthCenter - 50, heightCenter, 100, 20, button -> {

        });
        this.addRenderableOnly(cancelButton);
    }

    private void createDeleteDialog() {

    }

    @Override
    public void init() {
        super.init();
        this.clearWidgets();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) { 
        super.render(context, mouseX, mouseY, delta);
    }
}