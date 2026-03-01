package echen0719.serverscan.screens;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class pastScansScreen extends Screen {
    private final Screen parent;

    private EditBox searchBox;

    private Button searchSubmitButton, backButton;

    private int guiStartX, guiStartY;
    private int padding = 16;
    private int widgetHeight = 20;

    private final int white = 0xFFFFFFFF;

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

    @Override
    protected void init() {
        super.init();
        this.clearWidgets(); // prevents duplicates

        guiStartX = pxW(0.05f);
		guiStartY = pxH(0.15f);

        int searchBoxWidth = pxW(0.35f);
        int searchSubmitButtonWidth = pxW(0.15f);
        int backButtonWidth = pxW(0.2f);

        searchBox = new EditBox(this.font, guiStartX, guiStartY, searchBoxWidth, widgetHeight, Component.literal(""));
        searchBox.setHint(Component.literal("Input file name..."));
        this.addRenderableWidget(searchBox);

        searchSubmitButton = Button.builder(Component.literal("Search"), button -> {
            // search logic
        }).bounds(searchBox.getX() + searchBox.getWidth() + padding, guiStartY, searchSubmitButtonWidth, widgetHeight).build();
        this.addRenderableWidget(searchSubmitButton);

        backButton = Button.builder(Component.literal("Back"), button -> {
            this.minecraft.setScreen(parent);
        }).bounds(this.width / 2 - 50, this.height - 40, backButtonWidth, widgetHeight).build();
        this.addRenderableWidget(backButton);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredString(this.font, Component.literal("Past Scan Results Here"), this.width / 2, guiStartY - pxH(0.1f), white);
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