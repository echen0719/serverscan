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
    private final Screen parent;

    // gui components
    private EditBox renameInputBox;
    private Button cancelButton, confirmButton;

    // layouts constants
    private int padding = 16;
    private int widgetHeight = 20;

    // colors
    private final int white = 0xFFFFFFFF;
    private final int gray = 0xFFAAAAAA;
    private final int black = 0xFF000000;

    // parameter values
    private File targetFile;
    private String message;
    private String type;

    public confirmationScreen(Screen parent, File targetFile, String type) {
        super(Component.literal("View Past Scans"));
        this.parent = parent;
        this.targetFile = targetFile;
        this.type = type;

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
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int buttonWidth = 100;
        int totalWidth = buttonWidth * 2 + padding;

        renameInputBox = guiUtils.createInputBox(this, centerX - totalWidth / 2, centerY - padding, totalWidth, widgetHeight, "Enter a new file name...");
        this.addRenderableWidget(renameInputBox);

        cancelButton = guiUtils.createButton(this, "Cancel", centerX - totalWidth / 2, centerY + padding, buttonWidth, widgetHeight, button -> {
            this.minecraft.setScreen(parent);
        });
        this.addRenderableWidget(cancelButton);

        confirmButton = guiUtils.createButton(this, "Confirm", centerX + padding / 2, centerY + padding, buttonWidth, widgetHeight, button -> {
            String newFileName = renameInputBox.getValue().trim();

            if (newFileName.isEmpty()) {
                return;
            }

            if (targetFile.exists()) {
                boolean success = targetFile.renameTo(new File(targetFile.getParentFile(), newFileName));

                if (!success) {
                    // handle errors to screen
                }
            }
            
            this.minecraft.setScreen(parent);
        });
        this.addRenderableWidget(confirmButton);
    }

    private void createDeleteDialog() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int buttonWidth = 100;
        int totalWidth = buttonWidth * 2 + padding;

        cancelButton = guiUtils.createButton(this, "Cancel", centerX - totalWidth / 2, centerY, buttonWidth, widgetHeight, button -> {
            this.minecraft.setScreen(parent);
        });
        this.addRenderableWidget(cancelButton);

        confirmButton = guiUtils.createButton(this, "Confirm", centerX + padding / 2, centerY, buttonWidth, widgetHeight, button -> {
            if (targetFile.exists()) {
                boolean success = targetFile.delete();

                if (!success) {
                    // handle errors to screen
                }
            }

            this.minecraft.setScreen(parent);
        });
        this.addRenderableWidget(confirmButton);
    }

    @Override
    public void init() {
        super.init();
        this.clearWidgets();
        if (type.equals("RENAME")) {
            createRenameDialog();
        }
        else if (type.equals("DELETE")) {
            createDeleteDialog();
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) { 
        super.render(context, mouseX, mouseY, delta);

        if (type.equals("RENAME")) {
            context.drawString(this.font, Component.literal(message), renameInputBox.getX(), renameInputBox.getY() - padding, white);
        }
        else if (type.equals("DELETE")) {
            context.drawString(this.font, Component.literal(message), cancelButton.getX(), cancelButton.getY() - padding, white);
        }
    }
}