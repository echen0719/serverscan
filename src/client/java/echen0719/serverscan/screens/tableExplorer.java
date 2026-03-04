package echen0719.serverscan.screens;

import java.io.File;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.fabricmc.loader.api.FabricLoader;

import echen0719.serverscan.utils.fileUtils;

public class tableExplorer {
    private fileUtils filesManager = new fileUtils(FabricLoader.getInstance().getGameDirectory());

    private Screen screen;
    private GuiGraphics context;
    private int tableX, tableY, tableWidth, tableHeight;
    private static int borderColor, innerColor;

    private static int white = 0xFFFFFFFF;
    private static int black = 0xFF000000;
    private static int darkGray = 0xFF404040;
    private static int lightGray = 0xFF808080;
    private static int scrollBarColor = 0xFF4A4A4A;
    private static int scrollBarHoverColor = 0xFF8A8A8A;

    private static int rowHeight = 20;
    private static int scrollBarWidth = 5;
    private static int scrollBarHeight = 10;

    private int scrollPos = 0;
    private int scrollMax = 0;
    private int visibleRows = 0;

    private int mouseX = -1; private int mouseY = -1;

    public tableExplorer(Screen screen, int tableX, int tableY, int tableWidth, int tableHeight, int borderColor, int innerColor) {
        this.screen = screen;
        this.tableX = tableX; this.tableY = tableY;
        this.tableWidth = tableWidth; this.tableHeight = tableHeight;
        this.borderColor = borderColor; this.innerColor = innerColor;
    }

    public void setContext(GuiGraphics context) {
        this.context = context;
    }
    
    public void createBackground() {
        context.fill(tableX - 1, tableY - 1, tableX + tableWidth + 1, tableY + tableHeight + 1, borderColor);
        context.fill(tableX, tableY, tableX + tableWidth, tableY + tableHeight, innerColor);
    }

    // https://github.com/GotoLink/SkillAPI/blob/master/skillapi/client/GuiKnownSkills.java
    // used a bunch of ideas but made them simpler and for my purposes

    public void renderFileTable() {
        File[] items = filesManager.getChildFolders();

        int totalRows = items.length;
        visibleRows = tableHeight / rowHeight;
        scrollMax = Math.max(0, totalRows - visibleRows);

        for (int i = 0; i < visibleRows; i++) {
            int rowY = tableY + (i * rowHeight);
            int index = scrollPos + i;

            if (index >= 0 && index < totalRows) {
                if (i % 2 == 0) {
                    context.fill(tableX + 1, rowY, tableX + tableWidth - scrollBarWidth - 1, rowY + rowHeight, darkGray);
                }
                else {
                    context.fill(tableX + 1, rowY, tableX + tableWidth - scrollBarWidth - 1, rowY + rowHeight, lightGray);
                }

                File item = items[index];
                String fileName = "";

                if (item.isDirectory()) { // icons, i guess
                    fileName = "📁  " + items[index].getName();
                }
                else if (item.isFile()) {
                    fileName = "📄  " + items[index].getName();
                }
                
                if (fileName.length() > 25) fileName = fileName.substring(0, 22) + "...";
                context.drawString(screen.getFont(), fileName, tableX + 5, rowY + 5, white);
            }
        }

        renderScrollBar(items);
    }

    public void renderScrollBar(File[] items) {
        int totalRows = items.length;

        if (totalRows <= visibleRows) return;

        int scrollBarX = tableX + tableWidth - scrollBarWidth;
        // 20 or ratio between visible rows to total calcualted

        int scrollBarHeight = Math.max(20, (int)(tableHeight * ((float) visibleRows / totalRows)));
        int scrollableHeight = tableHeight - scrollBarHeight;
        
        int scrollBarY = tableY;
        if (scrollMax > 0) { // calculate position of scroll bar by scrollPos
            scrollBarY = tableY + (int)(scrollableHeight * ((float) scrollPos / scrollMax));
        }

        context.fill(scrollBarX, scrollBarY, scrollBarX + scrollBarWidth, scrollBarY + scrollBarHeight, scrollBarColor);
    }

    private void handleScrollPos() {
        scrollPos = Math.max(0, Math.min(scrollMax, scrollPos));
    }

    public void scroll(int amount) {
        scrollPos += amount;
        handleScrollPos();
    }

    public void handleScroll(double mouseX, double mouseY, double delta) {
        if (mouseX >= tableX && mouseX <= tableX + tableWidth && // if mouse is inside table
            mouseY >= tableY && mouseY <= tableY + tableHeight) {
            scroll((int)(-delta * rowHeight)); // negative delta = scroll up
        }
    }
}
