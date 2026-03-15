package echen0719.serverscan.screens;

import java.io.File;
import java.util.ArrayList;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.fabricmc.loader.api.FabricLoader;
import echen0719.serverscan.utils.fileUtils;
import echen0719.serverscan.utils.guiUtils;

public class serverExplorer {
    private Screen parent;

    // values
    private String searchTerm = "";
    private ArrayList<ServerEntry> serverEntries = new ArrayList<ServerEntry>();
    private ArrayList<Boolean> selectedRows = new ArrayList<Boolean>();

    // values calculated by init
    private int tableX, tableY, tableWidth, tableHeight;

    // colors
    private static int white = 0xFFFFFFFF;
    private static int black = 0xFF000000;
    private static int gray = 0xFFAAAAAA;
    private static int darkGray = 0xFF404040;
    private static int lightGray = 0xFF808080;
    private static int scrollBarColor = 0xFF4A4A4A;
    private static int scrollBarHoverColor = 0xFF8A8A8A;

    // layout constants
    private static int rowHeight = 20;
    private static int scrollBarWidth = 5;

    // scrolling vars
    private int scrollPos = 0;
    private int scrollMax = 0;
    private int visibleRows = 0;
    private boolean isScrollDragging = false;

    private ArrayList<Button> activeButtons = new ArrayList<Button>();

    private GuiGraphics context;
    private fileUtils filesManager = new fileUtils(FabricLoader.getInstance().getGameDirectory());
    private File[] items = filesManager.getChildFiles();

    public serverExplorer(Screen screen, int tableX, int tableY, int tableWidth, int tableHeight) {
        this.parent = screen;
        this.tableX = tableX; this.tableY = tableY;
        this.tableWidth = tableWidth; this.tableHeight = tableHeight;
    }

    private void loadServerEntries() {
        serverEntries.clear();
        selectedRows.clear();
        // parse json
    }

    public void setContext(GuiGraphics context) {
        this.context = context;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm.toLowerCase();
        this.scrollPos = 0;
    }
    
    public void createBackground() {
        context.fill(tableX - 1, tableY - 1, tableX + tableWidth + 1, tableY + tableHeight + 1, gray);
        context.fill(tableX, tableY, tableX + tableWidth, tableY + tableHeight, black);
    }

    // https://github.com/GotoLink/SkillAPI/blob/master/skillapi/client/GuiKnownSkills.java
    // used a bunch of ideas but made them simpler and for my purposes

    public void renderFileTable(double mouseX, double mouseY) {
        for (Button button : activeButtons) {
            ((pastScansScreen) parent).removeButton(button);
        }
        activeButtons.clear();

        int usableWidth = tableWidth - scrollBarWidth;
        int checkBoxColWidth = (int)(usableWidth * 0.1f);
        int ipColWidth = (int)(usableWidth * 0.3f);
        int portColWidth = (int)(usableWidth * 0.25f);
        int emptySpaceWidth = (int)(usableWidth * 0.1f);
        int adButtonWidth = (int)(usableWidth * 0.25f);

        ArrayList<File> displayedItems = new ArrayList<ServerEntry>();
        for (File item : items) {
            // if user searches for phrase, this checks and skips over files without it
            if (!searchTerm.isEmpty() && !item.getName().toLowerCase().contains(searchTerm)) { 
                continue;
            }
            displayedItems.add(item);
        }

        int totalRows = displayedItems.size();
        visibleRows = tableHeight / rowHeight;
        scrollMax = Math.max(0, totalRows - visibleRows);

        for (int i = 0; i < visibleRows; i++) {
            int rowY = tableY + (i * rowHeight);
            int index = scrollPos + i;

            if (index < 0 || index >= totalRows) continue; 
                
            if (i % 2 == 0) {
                context.fill(tableX + 1, rowY, tableX + tableWidth - scrollBarWidth - 1, rowY + rowHeight, darkGray);
            }
            else {
                context.fill(tableX + 1, rowY, tableX + tableWidth - scrollBarWidth - 1, rowY + rowHeight, lightGray);
            }

            File item = displayedItems.get(index);

            String fileName = "";
            int currentX = tableX;
                   
            fileName = "📄  " + displayedItems.get(index).getName();
            if (fileName.length() > 16) fileName = fileName.substring(0, 13) + "...";
        
            context.drawString(parent.getFont(), fileName, currentX + 5, rowY + 5, white);

            currentX += nameColWidth;
            context.fill(currentX, rowY, currentX + 1, rowY + rowHeight, gray);

            // file size
            context.drawCenteredString(parent.getFont(), filesManager.formattedFileSize(item), currentX + sizeColWidth / 2, rowY + 5, white);

            currentX += sizeColWidth;
            context.fill(currentX, rowY, currentX + 1, rowY + rowHeight, gray);

            // file date
            context.drawCenteredString(parent.getFont(), filesManager.formattedDate(item), currentX + dateColWidth / 2, rowY + 5, white);

            currentX += dateColWidth;

            // format & view
            Button formatAndViewButton = guiUtils.createButton(parent, "View Servers", currentX, rowY, formatButtonWidth, rowHeight, button -> {
                Minecraft.getInstance().setScreen(new viewServerScreen(parent, item));
            });
            currentX += formatButtonWidth;

            // rename
            Button renameButton = guiUtils.createButton(parent, "Rename", currentX, rowY, renameButtonWidth, rowHeight, button -> {
                Minecraft.getInstance().setScreen(new confirmationScreen(parent, item, "RENAME"));
            });
            currentX += renameButtonWidth;

            // delete
            Button deleteButton = guiUtils.createButton(parent, "Delete", currentX, rowY, deleteButtonWidth, rowHeight, button -> {
                Minecraft.getInstance().setScreen(new confirmationScreen(parent, item, "DELETE"));
            });

            activeButtons.add(formatAndViewButton);
            activeButtons.add(renameButton);
            activeButtons.add(deleteButton);

            // this is so weird but it works
            ((pastScansScreen) parent).addButton(formatAndViewButton);
            ((pastScansScreen) parent).addButton(renameButton);
            ((pastScansScreen) parent).addButton(deleteButton);
        }

        renderScrollBar(mouseX, mouseY);
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

    private void renderScrollBar(double mouseX, double mouseY) {
        int[] scrollBarInfo = calcScrollBarAttr();
        if (scrollBarInfo == null) return;

        int scrollBarX = scrollBarInfo[0]; int scrollBarY = scrollBarInfo[1]; int scrollBarHeight = scrollBarInfo[2];

        int color = scrollBarColor;
        if (isMouseOverScrollbar(mouseX, mouseY)) {
            color = scrollBarHoverColor;
        }

        context.fill(scrollBarX, scrollBarY, scrollBarX + scrollBarWidth, scrollBarY + scrollBarHeight, color);
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
        this.items = filesManager.getChildFiles();
        this.scrollPos = 0;
    }

    // finding that a class is probably going to work better than a map or dictionary
    public static class ServerEntry {
        public String ip;
        public int port;

        public ServerEntry(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }
    }
}