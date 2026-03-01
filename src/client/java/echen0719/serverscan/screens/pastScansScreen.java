package echen0719.serverscan.screens;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import echen0719.serverscan.utils.guiUtils;

public class pastScansScreen extends Screen {
    private final Screen parent;

    private EditBox searchBox;
    private Button searchSubmitButton, openDirButton, refreshButton, backButton;

    private int guiStartX, guiStartY;
    private int tableX, tableY, tableWidth, tableHeight;
    private int widthForWidgets;

    private int padding = 16;
    private int widgetHeight = 20;

    private final int white = 0xFFFFFFFF;
    private final int gray = 0xFFAAAAAA;
    private final int black = 0xFF000000;

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
            // refresh logic
        });
        this.addRenderableWidget(refreshButton);
    }

    private void renderTable(GuiGraphics context) {
        context.fill(tableX - 1, tableY - 1, tableX + tableWidth + 1, tableY + tableHeight + 1, gray);
        context.fill(tableX, tableY, tableX + tableWidth, tableY + tableHeight, black);

        // will finish table logic
    }

    private void createBottomButtons() {
        int buttonY = tableY + tableHeight + 10;

        int openDirButtonWidth = (int)(widthForWidgets * 0.25f);
        int backButtonWidth = (int)(widthForWidgets * 0.2f);

        openDirButton = guiUtils.createButton(this, "Open Directory", guiStartX, buttonY, openDirButtonWidth, widgetHeight,
        button -> {
            // open dir logic
        });
        this.addRenderableWidget(openDirButton);

        backButton = guiUtils.createButton(this, "Back", guiStartX + widthForWidgets - backButtonWidth, buttonY, backButtonWidth, widgetHeight,
        button -> {
            this.minecraft.setScreen(parent);
        });
        this.addRenderableWidget(backButton);
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
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    
        renderTable(context);
    }
}

// ok...so this is what it should look like but i am lazy right now

/*

                                      Past Scans (Table)
[Search Bar] (edit box)                                                       [Refresh] (button)

[  output.txt   |  1.0 MB  |  2/28/2026  |  Raw Preview  |  Formatted View  |  Delete File  ]
[  output1.txt  |  6.7 MB  |  2/28/2026  |  Raw Preview  |  Formatted View  |  Delete File  ]
[  output2.txt  |  6.9 MB  |  2/28/2026  |  Raw Preview  |  Formatted View  |  Delete File  ]

[Open Directory] (button)                                                        [Back] (button)

Search bar = search for file names, maybe I give size and date a sort?

Raw Preview = separate screen of whatever masscan outputs (text view)

Formatted View = separate screen of IPs and Ports formatted (to json?), displayed
in a multiselect view with the ability to add found servers to multiplayer list
Features: Select all / deselect all, searching, add selected to multiplayer list

Open Directory = opens the directory of .minecraft/serverscan/outputs in system file explorer

Delete file = just deletes the output files (and asks to delete formatted one as well)
Features: With confimration, if a formatted file appears with same name, also ask to delete that

Refresh button = recreate the screen if new data is moved or new scan is finished

If file in use, warn user, and let them continue if they choose to do so

*/