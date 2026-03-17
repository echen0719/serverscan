package echen0719.serverscan.screens;

import java.util.List;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import echen0719.serverscan.ServerscanClient;
import echen0719.serverscan.scanExecutor;
import echen0719.serverscan.utils.guiUtils;

// https://wiki.fabricmc.net/tutorial:screen
public class scanScreen extends Screen {
    private final Screen parent;

    // gui components
    private EditBox ipBox, portBox, rateBox, chunkBox, outFileBox;
    private Button submitButton, pauseButton, stopButton, logsButton, backButton;

    // static values
    private static String savedIPs = "";
    private static String savedPorts = "";
    private static String savedRate = "";
    private static String savedChunkSize = "";
    private static String savedOutFile = "";

    // values calculated by init
    private int formStartX, formStartY;
    private int termX, termY, termWidth, termHeight;
    private int widthForInputs;

    // layout constants
    private int padding = 16;
    private int widgetHeight = 20;

    // colors
    private final int white = 0xFFFFFFFF;
    private final int gray = 0xFFAAAAAA;
    private final int black = 0xFF000000;

    public scanScreen(Screen parent) {
        super(Component.literal("Server Scanner"));
        this.parent = parent;
    }

    private int pxW(float percent) {
        return (int)(this.width * percent);
    }

    private int pxH(float percent) {
        return (int)(this.height * percent);
    }

    private int parseChunkSize(String text, String logMessage) {
        int chunkSize = 262144; // default
        try {
            chunkSize = Integer.parseInt(text.trim());
            if (chunkSize <= 0) {
                scanExecutor.logs.add("Batch size must be greater than 0.");
                return -1;
            }
        } 
        catch (Exception e) {
            scanExecutor.logs.add(logMessage);
            e.printStackTrace();
        }
        return chunkSize;
    }

    private void setInputState(EditBox box, boolean active) {
        box.active = active;
        if (active) {
            box.setTextColor(white);
        }
        else {
            box.setTextColor(gray);
        }
    }

    private void createFormAndCalcTerm() {
        int ipBoxWidth = (int)(widthForInputs * 0.4f);
        int portBoxWidth = (int)(widthForInputs * 0.2f);
        int rateBoxWidth = (int)(widthForInputs * 0.2f);
	    int chunkBoxWidth = (int)(widthForInputs * 0.2f);
        int outFileBoxWidth = (int)(widthForInputs * 0.25f);

        int portBoxX = formStartX + ipBoxWidth + padding;
        int rateBoxX = portBoxX + portBoxWidth + padding;
	    int chunkBoxX = rateBoxX + rateBoxWidth + padding;

        termX = formStartX;
        termY = formStartY + widgetHeight + 10;
        termWidth = (chunkBoxX + chunkBoxWidth) - formStartX;
        termHeight = this.height - termY - pxH(0.15f);

        int outFileBoxY = termY + termHeight + 10; // don't want the line to be too long

        ipBox = guiUtils.createInputBox(this, formStartX, formStartY, ipBoxWidth, widgetHeight, "0.0.0.0-255.255.255.255");
        portBox = guiUtils.createInputBox(this, portBoxX, formStartY, portBoxWidth, widgetHeight, "25565");
        rateBox = guiUtils.createInputBox(this, rateBoxX, formStartY, rateBoxWidth, widgetHeight, "100000");
        chunkBox = guiUtils.createInputBox(this, chunkBoxX, formStartY, chunkBoxWidth, widgetHeight, "65536");
        outFileBox = guiUtils.createInputBox(this, formStartX, outFileBoxY, outFileBoxWidth, widgetHeight, "output.json");

        this.addRenderableWidget(ipBox);
        this.addRenderableWidget(portBox);
        this.addRenderableWidget(rateBox);
        this.addRenderableWidget(chunkBox);
        this.addRenderableWidget(outFileBox);
    }

    private void createBottomButtons() {
        int buttonY = termY + termHeight + 10;

        int submitWidth = (int)(widthForInputs * 0.25f);
        int pauseWidth = (int)(widthForInputs * 0.12f);
        int stopWidth = (int)(widthForInputs * 0.12f);
        int logsWidth = (int)(widthForInputs * 0.3f);
        int backWidth = (int)(widthForInputs * 0.2f);

        submitButton = guiUtils.createButton(this, "Run Scan", outFileBox.getX() + outFileBox.getWidth() + padding, buttonY, submitWidth, widgetHeight,
        button -> {
		    String ips = ipBox.getValue().trim();
		    String ports = portBox.getValue().trim();
		    String rate = rateBox.getValue().trim();
            String chunkText = chunkBox.getValue().trim();
		    String outFile = outFileBox.getValue().trim();
	
		    if (ips.isEmpty() || ports.isEmpty() || rate.isEmpty() || chunkText.isEmpty() || outFile.isEmpty()) {
				scanExecutor.logs.add("Fill in the fields before scanning.");
                return;
	    	}

            int chunkSize = parseChunkSize(chunkText, "Invalid batch size, using default");
            if (chunkSize == -1) return;

            savedIPs = ips;
            savedPorts = ports;
            savedRate = rate;
            savedChunkSize = chunkText;
            savedOutFile = outFile;
                
            scanExecutor.startScan(ips, ports, rate, chunkSize, outFile);
			updateControlButtons();
        });
        this.addRenderableWidget(submitButton);

        pauseButton = guiUtils.createButton(this, "Pause", submitButton.getX(), buttonY, pauseWidth, widgetHeight,
        button -> {
		    if (scanExecutor.paused) { // user updates values during pause
                String rateVal = rateBox.getValue().trim();
                int chunkSize = parseChunkSize(chunkBox.getValue().trim(), "Invalid batch size, using default");
                if (chunkSize == -1) return;

                scanExecutor.resume(rateVal, chunkSize);
            }
            else {
                scanExecutor.pause();
            }
		    updateControlButtons();
        });
        this.addRenderableWidget(pauseButton);

        stopButton = guiUtils.createButton(this, "Stop", pauseButton.getX() + pauseButton.getWidth(), buttonY, stopWidth, widgetHeight,
        button -> {
		    scanExecutor.stop();
		    updateControlButtons();
        });
        this.addRenderableWidget(stopButton);

        logsButton = guiUtils.createButton(this, "View Past Scans", submitButton.getX() + submitButton.getWidth() + padding, buttonY, logsWidth, widgetHeight,
        button -> {
            if (ServerscanClient.logsScreen == null) {
				ServerscanClient.logsScreen = new pastScansScreen(this);
			}

			this.minecraft.setScreen(ServerscanClient.logsScreen);
        });
        this.addRenderableWidget(logsButton);

        backButton = guiUtils.createButton(this, "Back", logsButton.getX() + logsButton.getWidth() + padding, buttonY, backWidth, widgetHeight,
        button -> {
            this.minecraft.setScreen(parent);
        });
        this.addRenderableWidget(backButton);
    }

    public void updateControlButtons() {
	    submitButton.visible = !scanExecutor.running;
	    pauseButton.visible = scanExecutor.running;
	    stopButton.visible = scanExecutor.running;

	    if (!scanExecutor.running) { // if finished scanning, all boxes become editable
            setInputState(ipBox, true);
            setInputState(portBox, true);
            setInputState(rateBox, true);
            setInputState(chunkBox, true);
            setInputState(outFileBox, true);
            return;
        }

        setInputState(ipBox, false);
        setInputState(portBox, false);
        setInputState(outFileBox, false);

	    if (scanExecutor.paused) {
	        pauseButton.setMessage(Component.literal("Resume"));
            pauseButton.active = !scanExecutor.isChunkRunning(); // disable until chunk finishes

            // batch size and rate allowed for change except the following during a pause
            setInputState(rateBox, true);
            setInputState(chunkBox, true);
	        }
	    else {
	        pauseButton.setMessage(Component.literal("Pause"));
            pauseButton.active = true;

            setInputState(rateBox, false);
            setInputState(chunkBox, false);
        }
    }

    public void renderTerm(GuiGraphics context) { // context works only within render()
        int termInset = 5;

        // black background with gray borders
        context.fill(termX - 1, termY - 1, termX + termWidth + 1, termY + termHeight + 1, gray); // gray color
        context.fill(termX, termY, termX + termWidth, termY + termHeight, black); // black color

        int maxLines = (termHeight - 10) / font.lineHeight;
	    List<String> logs = scanExecutor.logs; 

	    int startIndex = Math.max(0, logs.size() - maxLines); // show last X lines
        int currentY = termY + termInset; // new lines are 5 pixels below

        for (int i = startIndex; i < logs.size(); i++) {
            context.drawString(this.font, Component.literal(logs.get(i)), termX + termInset, currentY, white);
            currentY += this.font.lineHeight;
        }
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets(); // prevents duplicates

		formStartX = pxW(0.05f); // start 5% of width out
		formStartY = pxH(0.1f); // start 10% of height out
	
		// whole width minus padding on each side and minus padding between boxes
		widthForInputs = this.width - (formStartX * 2) - (padding * 3);

        createFormAndCalcTerm();

        ipBox.setValue(savedIPs);
        portBox.setValue(savedPorts);
        rateBox.setValue(savedRate);
        chunkBox.setValue(savedChunkSize);
        outFileBox.setValue(savedOutFile);

		createBottomButtons();
	    updateControlButtons();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

	    updateControlButtons();

        int labelOffsetY = -this.font.lineHeight - 3;

        // Labels for input boxes
        context.drawString(this.font, Component.literal("IP Ranges: "), ipBox.getX(), ipBox.getY() + labelOffsetY, white);
        context.drawString(this.font, Component.literal("Port Ranges: "), portBox.getX(), portBox.getY() + labelOffsetY, white);
        context.drawString(this.font, Component.literal("Speed (pps): "), rateBox.getX(), rateBox.getY() + labelOffsetY, white);
	    context.drawString(this.font, Component.literal("Batch size: "), chunkBox.getX(), chunkBox.getY() + labelOffsetY, white);

        renderTerm(context);
    }
}