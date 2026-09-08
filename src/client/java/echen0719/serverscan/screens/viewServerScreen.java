package echen0719.serverscan.screens;

import java.io.File;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import echen0719.serverscan.utils.guiUtils;

public class viewServerScreen extends Screen {
    private final Screen parent;

    // gui components
    private EditBox searchBox;
    private Button searchSubmitButton, selectionButton, addServersButton, backButton;

    // values calculated by init
    private int guiStartX, guiStartY;
    private int tableX, tableY, tableWidth, tableHeight;
    private int widthForWidgets;

    // layouts constants
    private int padding = 16;
    private int widgetHeight = 20;

    // colors
    private final int white = 0xFFFFFFFF;
    private final int gray = 0xFFAAAAAA;
    private final int black = 0xFF000000;

    // parameter values
    private File targetFile;

    // utils
    private serverExplorer explorer;

    public viewServerScreen(Screen parent, File targetFile) {
        super(Component.literal("View Servers"));
        this.parent = parent;
        this.targetFile = targetFile;
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

        tableX = guiStartX;
        tableY = guiStartY + widgetHeight + 10;
        tableWidth = widthForWidgets;
        tableHeight = this.height - tableY - pxH(0.15f);

        searchBox = guiUtils.createInputBox(this, guiStartX, guiStartY, searchBoxWidth, widgetHeight, "Input file name...");
        this.addRenderableWidget(searchBox);

        searchSubmitButton = guiUtils.createButton(this, "Search", searchBox.getX() + searchBox.getWidth() + padding, guiStartY, searchSubmitButtonWidth, widgetHeight,
        button -> {
            String searchTerm = searchBox.getValue().trim();
            explorer.setSearchTerm(searchTerm);
        });
        this.addRenderableWidget(searchSubmitButton);
    }

    private void createBottomButtons() {
        int buttonY = tableY + tableHeight + 10;

        int selectionButtonWidth = (int)(widthForWidgets * 0.3f);
        int addServersButtonWidth = (int)(widthForWidgets * 0.42f);
        int backButtonWidth = (int)(widthForWidgets * 0.2f);

        selectionButton = guiUtils.createButton(this, "Select/Deselect All", guiStartX, buttonY, selectionButtonWidth, widgetHeight,
        button -> {
            explorer.toggleSelectAll();
        });
        this.addRenderableWidget(selectionButton);

        addServersButton = guiUtils.createButton(this, "Add Selected to Servers List", guiStartX + selectionButtonWidth + padding, buttonY, addServersButtonWidth, widgetHeight,
        button -> {
            explorer.addAllSelectedServers();
            System.out.println("Added add selected to servers.");
        });
        this.addRenderableWidget(addServersButton);

        backButton = guiUtils.createButton(this, "Back", guiStartX + widthForWidgets - backButtonWidth, buttonY, backButtonWidth, widgetHeight,
        button -> {
            Minecraft.getInstance().setScreenAndShow(parent);
        });
        this.addRenderableWidget(backButton);
    }

    private void renderTable(GuiGraphicsExtractor context, double mouseX, double mouseY) {
        explorer.setContext(context);
        explorer.createBackground();
        explorer.renderServerTable(mouseX, mouseY);
    }

    public void addButton(Button button) {
        this.addRenderableWidget(button);
    }

    public void removeButton(Button button) {
        this.removeWidget(button);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (searchBox.isFocused() && (keyCode == GLFW.GLFW_KEY_ENTER)) {
            String searchTerm = searchBox.getValue().trim();
            explorer.setSearchTerm(searchTerm);
            return true;
        }
        return super.keyPressed(event);
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
        this.clearWidgets();

        guiStartX = pxW(0.05f);
		guiStartY = pxH(0.05f);
        widthForWidgets = this.width - (guiStartX * 2);

        createTopControlsAndCalcTable();
        createBottomButtons();

        explorer = new serverExplorer(this, tableX, tableY, tableWidth, tableHeight, targetFile);

        // docs are confusing
        ScreenMouseEvents.afterMouseScroll(this).register((ScreenMouseEvents.AfterMouseScroll) this::onMouseScroll); // method reference
        ScreenMouseEvents.afterMouseClick(this).register((ScreenMouseEvents.AfterMouseClick) this::onMouseClick);
        ScreenMouseEvents.afterMouseRelease(this).register((ScreenMouseEvents.AfterMouseRelease) this::onMouseRelease);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {    
        renderTable(context, mouseX, mouseY);
        explorer.handleMouseDrag(mouseY);

        super.extractRenderState(context, mouseX, mouseY, delta);
    }
}
