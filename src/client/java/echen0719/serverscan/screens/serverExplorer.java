package echen0719.serverscan.screens;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

// apparently gson is bundled with Minecraft
// so my implementation is just a copy paste
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
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
    private static int checkboxChecked = 0xFF55FF55;
    private static int checkboxUnchecked = 0xFF555555;

    // layout constants
    private static int rowHeight = 20;
    private static int scrollBarWidth = 5;

    // scrolling vars
    private int scrollPos = 0;
    private int scrollMax = 0;
    private int visibleRows = 0;
    private boolean isScrollDragging = false;

    // parameter values
    private File targetFile;

    // stored values for user selections
    private ArrayList<Button> activeButtons = new ArrayList<Button>();
    private ArrayList<Integer> selectedRows = new ArrayList<Integer>();
    private ArrayList<ServerEntry> serverEntries = new ArrayList<ServerEntry>();

    private GuiGraphics context;
    private fileUtils filesManager = new fileUtils(FabricLoader.getInstance().getGameDirectory());

    // class makes logic easier 🤷
    public class ServerEntry {
        public String ip;
        public int port;
        public boolean isSelected;

        public ServerEntry(String ip, int port) {
            this.ip = ip;
            this.port = port;
            this.isSelected = false;
        }
    }

    public serverExplorer(Screen screen, int tableX, int tableY, int tableWidth, int tableHeight, File targetFile) {
        this.parent = screen;
        this.tableX = tableX; this.tableY = tableY;
        this.tableWidth = tableWidth; this.tableHeight = tableHeight;
        this.targetFile = targetFile;

        loadServerEntries();
    }

    // https://stackoverflow.com/questions/5015844
    // https://stackoverflow.com/questions/20057695
    private void loadServerEntries() {
        serverEntries.clear();
        selectedRows.clear();
        
        try (BufferedReader br = new BufferedReader(new FileReader(targetFile))) {
            String content = "";
            String line;

            while ((line = br.readLine()) != null) {
                content += line; // if json files are concatenated
            }

            content = content.replace("][", ",");
            JsonArray servers = JsonParser.parseString(content).getAsJsonArray();

            for (JsonElement element : servers) {
                JsonObject server = element.getAsJsonObject();

                String ip = server.get("ip").getAsString();
                JsonArray ports = server.getAsJsonArray("ports");

                // ports is an array since user can search multiple for each IP
                if (ports.size() > 0) {
                    int port = ports.get(0).getAsJsonObject().get("port").getAsInt();
                    serverEntries.add(new ServerEntry(ip, port));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("You probably have an invalid or improperly formatted file.");
        }
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
            ((viewServerScreen) parent).removeButton(button);
        }
        activeButtons.clear();

        int usableWidth = tableWidth - scrollBarWidth;
        int checkBoxColWidth = (int)(usableWidth * 0.1f);
        int ipColWidth = (int)(usableWidth * 0.3f);
        int portColWidth = (int)(usableWidth * 0.25f);
        int emptySpaceWidth = (int)(usableWidth * 0.1f);
        int adButtonWidth = (int)(usableWidth * 0.25f);

        ArrayList<ServerEntry> displayedEntries = new ArrayList<ServerEntry>();
        for (ServerEntry entry : serverEntries) {
            // if user searches for phrase, this checks and skips over files without it
            if (!searchTerm.isEmpty() && !entry.ip.toLowerCase().contains(searchTerm)) { 
                continue;
            }
            displayedEntries.add(entry);
        }

        int totalRows = displayedEntries.size();
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

            ServerEntry entry = displayedEntries.get(index);
            int currentX = tableX + 5;
        
            // checkbox
            int checkBoxColor = checkboxUnchecked;

            if (entry.isSelected) {
                checkBoxColor = checkboxChecked;
            }

            context.fill(currentX, rowY + 5, currentX + 10, rowY + 15, checkBoxColor);
            currentX += checkBoxColWidth;

            // ip address
            context.drawCenteredString(parent.getFont(), entry.ip, currentX, rowY + 5, white);
            currentX += ipColWidth;

            // port
            context.drawCenteredString(parent.getFont(), String.valueOf(entry.port), currentX, rowY + 5, white);
            currentX += portColWidth;

            // empty space
            currentX += emptySpaceWidth;

            // format & view
            Button addServerButton = guiUtils.createButton(parent, "Add to server list", currentX, rowY, adButtonWidth, rowHeight, button -> {
                // implement
            });

            activeButtons.add(addServerButton);

            ((viewServerScreen) parent).addButton(addServerButton);
        }

        renderScrollBar(mouseX, mouseY);
    }

    private int[] calcScrollBarAttr() {
        int totalRows = serverEntries.size();

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
        loadServerEntries();
        this.scrollPos = 0;
    }
}