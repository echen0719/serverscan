package echen0719.serverscan.screens;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class pastScansScreen extends Screen {
    private final Screen parent;

    private final int white = 0xFFFFFFFF;

    public pastScansScreen(Screen parent) {
        super(Component.literal("View Past Scans"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets(); // prevents duplicates

        Button backButton = Button.builder(Component.literal("Back"), button -> {
            this.minecraft.setScreen(parent);
        }).bounds(this.width / 2 - 50, this.height - 40, 100, 20).build();
        this.addRenderableWidget(backButton);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredString(this.font, Component.literal("Past Scan Results Here"), this.width / 2, 20, white);
    }
}

// ok...so this is what it should look like but i am lazy right now

/*

                                      Past Scans (Table)
[Search Bar] (edit box)                                                       [Refresh] (button)

[  output.txt   |  1.0 MB  |  2/28/2026  |  Raw Preview  |  Formatted Preview  |  Delete File  ]
[  output1.txt  |  6.7 MB  |  2/28/2026  |  Raw Preview  |  Formatted Preview  |  Delete File  ]
[  output2.txt  |  6.9 MB  |  2/28/2026  |  Raw Preview  |  Formatted Preview  |  Delete File  ]

[Open Directory] (button)                                                        [Back] (button)

Search bar = search for file names, maybe I give size and date a sort?

Raw Preview = separate screen of whatever masscan outputs (text view)

Formatted Preview = separate screen of IPs and Ports formatted (to json?), displayed
in a multiselect view with the ability to add found servers to multiplayer list
Features: Select all / deselect all, searching, add selected to multiplayer list

Open Directory = opens the directory of .minecraft/serverscan/outputs in system file explorer

Delete file = just deletes the output files (and asks to delete formatted one as well)
Features: With confimration, if a formatted file appears with same name, also ask to delete that

Refresh button = recreate the screen if new data is moved or new scan is finished

If file in use, warn user, and let them continue if they choose to do so

*/