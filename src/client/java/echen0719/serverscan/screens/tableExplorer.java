package echen0719.serverscan.screens;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.fabricmc.loader.api.FabricLoader;

import echen0719.serverscan.utils.fileUtils;
import echen0719.serverscan.utils.guiUtils;

public class tableExplorer {
    private int tableX, tableY, tableWidth, tableHeight;
    private int borderColor, innerColor;

    private static int white = 0xFFFFFFFF;
    private static int black = 0xFF000000;
    private static int gray = 0xFFAAAAAA;
    private static int darkGray = 0xFF404040;
    private static int lightGray = 0xFF808080;
    private static int scrollBarColor = 0xFF4A4A4A;
    private static int scrollBarHoverColor = 0xFF8A8A8A;

    private static int rowHeight = 20;
    private static int scrollBarWidth = 5;

    private int scrollPos = 0;
    private int scrollMax = 0;
    private int visibleRows = 0;
    private boolean isScrollDragging = false;

    // making logic for clearing button (for preventing overlap) easier
    private List<Button> activeButtons = new ArrayList<Button>();

    private Screen screen;
    private GuiGraphics context;
    private fileUtils filesManager = new fileUtils(FabricLoader.getInstance().getGameDirectory());
    private File[] items = filesManager.getChildFolders();

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
        for (Button btn : activeButtons) {
            ((pastScansScreen) screen).removeButton(btn);
        }
        activeButtons.clear();

        int usableWidth = tableWidth - scrollBarWidth;
        int nameColWidth = (int)(usableWidth * 0.225f);
        int sizeColWidth = (int)(usableWidth * 0.1f);
        int dateColWidth = (int)(usableWidth * 0.175f);
        int formatButtonWidth = (int)(usableWidth * 0.2f);
        int renameButtonWidth = (int)(usableWidth * 0.15f);
        int deleteButtonWidth = (int)(usableWidth * 0.15f);

        int totalRows = items.length;
        visibleRows = tableHeight / rowHeight;
        scrollMax = Math.max(0, totalRows - visibleRows);

        for (int i = 0; i < visibleRows; i++) {
            int rowY = tableY + (i * rowHeight);
            int index = scrollPos + i;

            if (index < 0 && index >= totalRows) continue; 
                
            if (i % 2 == 0) {
                context.fill(tableX + 1, rowY, tableX + tableWidth - scrollBarWidth - 1, rowY + rowHeight, darkGray);
            }
            else {
                context.fill(tableX + 1, rowY, tableX + tableWidth - scrollBarWidth - 1, rowY + rowHeight, lightGray);
            }

            File item = items[index];
            String fileName = "";
            int currentX = tableX;

            if (item.isDirectory()) { // icons, i guess
                fileName = "📂  " + items[index].getName();
                if (fileName.length() > 25) fileName = fileName.substring(0, 22) + "...";
            }
            else {
                fileName = "📄  " + items[index].getName();
                if (fileName.length() > 16) fileName = fileName.substring(0, 13) + "...";
            }
            
            context.drawString(screen.getFont(), fileName, currentX + 5, rowY + 5, white);
                   
            if(item.isFile()) {
                currentX += nameColWidth;
                context.fill(currentX, rowY, currentX + 1, rowY + rowHeight, gray);

                // file size
                context.drawCenteredString(screen.getFont(), filesManager.formattedFileSize(item), currentX + sizeColWidth / 2, rowY + 5, white);

                currentX += sizeColWidth;
                context.fill(currentX, rowY, currentX + 1, rowY + rowHeight, gray);

                // file date
                context.drawCenteredString(screen.getFont(), filesManager.formattedDate(item), currentX + dateColWidth / 2, rowY + 5, white);

                currentX += dateColWidth;

                // format & view
                Button formatAndViewButton = guiUtils.createButton(screen, "View Servers", currentX, rowY, formatButtonWidth, rowHeight, button -> {

                });
                currentX += formatButtonWidth;

                // rename
                Button renameButton = guiUtils.createButton(screen, "Rename", currentX, rowY, renameButtonWidth, rowHeight, button -> {

                });
                currentX += renameButtonWidth;

                // delete
                Button deleteButton = guiUtils.createButton(screen, "Delete", currentX, rowY, deleteButtonWidth, rowHeight, button -> {

                });

                activeButtons.add(formatAndViewButton);
                activeButtons.add(renameButton);
                activeButtons.add(deleteButton);

                // this is so weird but it works
                ((pastScansScreen) screen).addButton(formatAndViewButton);
                ((pastScansScreen) screen).addButton(renameButton);
                ((pastScansScreen) screen).addButton(deleteButton);
            }
        }

        renderScrollBar();
    }

    private int[] calcScrollBarAttr() {
        int totalRows = items.length;

        if (totalRows <= visibleRows) return null;

        int scrollBarX = tableX + tableWidth - scrollBarWidth;
        // 20 or ratio between visible rows to total calcualted

        int scrollBarHeight = Math.max(20, (int)(tableHeight * ((float) visibleRows / totalRows)));
        int scrollableHeight = tableHeight - scrollBarHeight;
        
        int scrollBarY = tableY;
        if (scrollMax > 0) { // calculate position of scroll bar by scrollPos
            scrollBarY = tableY + (int)(scrollableHeight * ((float) scrollPos / scrollMax));
        }

        return new int[] {scrollBarX, scrollBarY, scrollBarHeight};
    }

    private void renderScrollBar() {
        int[] scrollBarInfo = calcScrollBarAttr();
        if (scrollBarInfo == null) return;

        int scrollBarX = scrollBarInfo[0]; int scrollBarY = scrollBarInfo[1]; int scrollBarHeight = scrollBarInfo[2];

        context.fill(scrollBarX, scrollBarY, scrollBarX + scrollBarWidth, scrollBarY + scrollBarHeight, scrollBarColor);
    }

    private boolean isMouseOverScrollbar(double mouseX, double mouseY) {
        int[] scrollBarInfo = calcScrollBarAttr();
        if (scrollBarInfo == null) return false;

        int scrollBarX = scrollBarInfo[0]; int scrollBarY = scrollBarInfo[1]; int scrollBarHeight = scrollBarInfo[2];

        return mouseX >= scrollBarX && mouseX <= scrollBarX + scrollBarWidth && mouseY >= scrollBarY && mouseY <= scrollBarY + scrollBarHeight;
    }

    public boolean handleMouseClick(double mouseX, double mouseY) {
        if (isMouseOverScrollbar(mouseX, mouseY)) {
            isScrollDragging = true;
            return true;
        }
        return false;
    }

    public void handleMouseRelease() {
        isScrollDragging = false;
    }

    public void handleMouseDrag(double mouseY) {
        if (!isScrollDragging) return; // update scroll while dragging

        int[] scrollBarInfo = calcScrollBarAttr();
        if (scrollBarInfo == null) return;

        int scrollBarHeight = scrollBarInfo[2];
        int scrollableHeight = tableHeight - scrollBarHeight;
        if (scrollableHeight <= 0) return;
    
        // convert mouseY to scrollPos
        int relativeY = (int)(mouseY - tableY - scrollBarHeight / 2);
        relativeY = Math.max(0, Math.min(scrollableHeight, relativeY));
    
        scrollPos = (int)(relativeY * scrollMax / (float)scrollableHeight);
        scrollPos = Math.max(0, Math.min(scrollMax, scrollPos)); // capping scrollPos
    }

    public void handleScroll(double mouseX, double mouseY, double delta) {
        if (mouseX >= tableX && mouseX <= tableX + tableWidth && // if mouse is inside table
            mouseY >= tableY && mouseY <= tableY + tableHeight) {
            scrollPos += (int)(-delta * rowHeight); // negative delta = scroll up
            scrollPos = Math.max(0, Math.min(scrollMax, scrollPos));
        }
    }

    public void refresh() {
        this.items = filesManager.getChildFolders();
    }
}
