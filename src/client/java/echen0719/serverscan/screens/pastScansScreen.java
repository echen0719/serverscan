package echen0719.serverscan.screens;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents; // fabric scroll
import net.fabricmc.loader.api.FabricLoader;

import echen0719.serverscan.utils.fileUtils;
import echen0719.serverscan.utils.guiUtils;

public class pastScansScreen extends Screen {
    private final Screen parent;

    // gui components
    private EditBox searchBox;
    private Button searchSubmitButton, openDirButton, refreshButton, backButton;

    // values calculated by init
    private int guiStartX, guiStartY;
    private int tableX, tableY, tableWidth, tableHeight;
    private int widthForWidgets;

    // layout constants
    private int padding = 16;
    private int widgetHeight = 20;

    // colors
    private final int white = 0xFFFFFFFF;
    private final int gray = 0xFFAAAAAA;
    private final int black = 0xFF000000;

    // utils
    private fileUtils filesManager = new fileUtils(FabricLoader.getInstance().getGameDirectory());
    private tableExplorer explorer; // persistant

    public pastScansScreen(Screen parent) {
        super(Component.literal("View Past Scans"));
        this.parent = parent;
    }

    private int pxW(float percent) {
        return (int)(this.width * percent);
    }

    private int pxH(float percent) {
        return (int)(this.height * percent);
    }

    private void createTopControlsAndCalcTable() {
        int searchBoxWidth = (int)(widthForWidgets * 0.35f);
        int searchSubmitButtonWidth = (int)(widthForWidgets * 0.15f);
        int refreshButtonWidth = (int)(widthForWidgets * 0.15f);

        tableX = guiStartX;
        tableY = guiStartY + widgetHeight + 10;
        tableWidth = widthForWidgets;
        tableHeight = this.height - tableY - pxH(0.15f);

        searchBox = guiUtils.createInputBox(this, guiStartX, guiStartY, searchBoxWidth, widgetHeight, "Input file name...");
        this.addRenderableWidget(searchBox);

        searchSubmitButton = guiUtils.createButton(this, "Search", searchBox.getX() + searchBox.getWidth() + padding, guiStartY, searchSubmitButtonWidth, widgetHeight,
        button -> {
            // search logic
        });
        this.addRenderableWidget(searchSubmitButton);

        refreshButton = guiUtils.createButton(this, "Refresh", pxW(0.95f) - refreshButtonWidth, guiStartY, refreshButtonWidth, widgetHeight,
        button -> {
            explorer.refresh();
        });
        this.addRenderableWidget(refreshButton);
    }

    private void createBottomButtons() {
        int buttonY = tableY + tableHeight + 10;

        int openDirButtonWidth = (int)(widthForWidgets * 0.25f);
        int backButtonWidth = (int)(widthForWidgets * 0.2f);

        openDirButton = guiUtils.createButton(this, "Open Directory", guiStartX, buttonY, openDirButtonWidth, widgetHeight,
        button -> {
            Util.getPlatform().openFile(filesManager.getOutputsFolder()); // got from resource pack screen
        });
        this.addRenderableWidget(openDirButton);

        backButton = guiUtils.createButton(this, "Back", guiStartX + widthForWidgets - backButtonWidth, buttonY, backButtonWidth, widgetHeight,
        button -> {
            this.minecraft.setScreen(parent);
        });
        this.addRenderableWidget(backButton);
    }

    private void renderTable(GuiGraphics context) {
        explorer.setContext(context);
        explorer.createBackground();
        explorer.renderFileTable();
    }

    public void addButton(Button button) {
        this.addRenderableWidget(button);
    }

    public void removeButton(Button button) {
        this.removeWidget(button);
    }

    private boolean onMouseScroll(Screen screen, double mouseX, double mouseY, double deltaX, double deltaY, boolean consumed) {
        explorer.handleScroll(mouseX, mouseY, deltaY);
        return true;
    }

    // Minecraft's MouseButtonEvent
    private boolean onMouseClick(Screen screen, MouseButtonEvent event, boolean consumed) {
        if (event.button() == 0) {
            return explorer.handleMouseClick(event.x(), event.y()) || consumed; // if else one-liner
        }
        return consumed;
    }

    private boolean onMouseRelease(Screen screen, MouseButtonEvent event, boolean consumed) {
        if (event.button() == 0) {
            explorer.handleMouseRelease();
        }
        return consumed;
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets(); // prevents duplicates

        guiStartX = pxW(0.05f);
		guiStartY = pxH(0.05f);
        widthForWidgets = this.width - (guiStartX * 2);

        createTopControlsAndCalcTable();
        createBottomButtons();

        explorer = new tableExplorer(this, tableX, tableY, tableWidth, tableHeight);

        // docs are confusing
        ScreenMouseEvents.afterMouseScroll(this).register((ScreenMouseEvents.AfterMouseScroll) this::onMouseScroll); // method reference
        ScreenMouseEvents.afterMouseClick(this).register((ScreenMouseEvents.AfterMouseClick) this::onMouseClick);
        ScreenMouseEvents.afterMouseRelease(this).register((ScreenMouseEvents.AfterMouseRelease) this::onMouseRelease);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {    
        renderTable(context);
        explorer.handleMouseDrag(mouseY);

        super.render(context, mouseX, mouseY, delta);
    }
}