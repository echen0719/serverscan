package echen0719.serverscan.screens;

import java.util.List;

import echen0719.serverscan.ServerscanClient;
import echen0719.serverscan.scanExecutor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

// https://wiki.fabricmc.net/tutorial:screen
public class scanScreen extends Screen {
    private final Screen parent;

    private EditBox ipBox;
    private EditBox portBox;
    private EditBox rateBox;
    private EditBox chunkBox;
    private EditBox outFileBox;

    private static String savedIPs = "";
    private static String savedPorts = "";
    private static String savedRate = "";
    private static String savedChunkSize = "";
    private static String savedOutFile = "";

    private int formStartX, formStartY;
    private int padding = 16;
    private int widthForInputs;
    private int inputHeight = 20;

    private int termX, termY, termWidth, termHeight;

    private Button submitButton, pauseButton, stopButton;

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

    private EditBox createInputBox(int x, int y, int width, String hint) {
        EditBox box = new EditBox(this.font, x, y, width, inputHeight, Component.literal(""));
        box.setHint(Component.literal(hint));
        this.addRenderableWidget(box);
        return box;
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
        termY = formStartY + inputHeight + 10;
        termWidth = (chunkBoxX + chunkBoxWidth) - formStartX;
        termHeight = this.height - termY - pxH(0.15f);

        int outFileBoxY = termY + termHeight + 10; // don't want the line to be too long

        ipBox = createInputBox(formStartX, formStartY, ipBoxWidth, "0.0.0.0-255.255.255.255");
        portBox = createInputBox(portBoxX, formStartY, portBoxWidth, "25565");
        rateBox = createInputBox(rateBoxX, formStartY, rateBoxWidth, "100000");
        chunkBox = createInputBox(chunkBoxX, formStartY, chunkBoxWidth, "65536");
        outFileBox = createInputBox(formStartX, outFileBoxY, outFileBoxWidth, "output.txt");
    }

    private void createBottomButtons() {
        int buttonY = termY + termHeight + 10;

        int submitWidth = (int)(widthForInputs * 0.25f);
        int pauseWidth = (int)(widthForInputs * 0.12f);
        int stopWidth = (int)(widthForInputs * 0.12f);
        int logsWidth = (int)(widthForInputs * 0.3f);
        int backWidth = (int)(widthForInputs * 0.2f);

        submitButton = Button.builder(Component.literal("Run Scan"), button -> {
		    String ips = ipBox.getValue().trim();
		    String ports = portBox.getValue().trim();
		    String rate = rateBox.getValue().trim();
            String chunkText = chunkBox.getValue().trim();
		    String outFile = outFileBox.getValue().trim();
	
		    if (ips.isEmpty() || ports.isEmpty() || rate.isEmpty() || chunkText.isEmpty() || outFile.isEmpty()) {
				scanExecutor.logs.add("Fill in the fields before scanning.");
	    	}

            int chunkSize = 262144;

            try {
                chunkSize = Integer.parseInt(chunkText);
                if (chunkSize <= 0) {
                    scanExecutor.logs.add("Batch size must be greater than 0.");
                    return;
                }
            } 
            catch (Exception e) {
                scanExecutor.logs.add("Invalid batch size, using batch_size=" + chunkSize);
                e.printStackTrace();
            }

            savedIPs = ips;
            savedPorts = ports;
            savedRate = rate;
            savedChunkSize = chunkText;
            savedOutFile = outFile;
                
            scanExecutor.startScan(ips, ports, rate, chunkSize, outFile);
			updateControlButtons();
        }).bounds(outFileBox.getX() + outFileBox.getWidth() + padding, buttonY, submitWidth, 20).build();
        this.addRenderableWidget(submitButton);

		pauseButton = Button.builder(Component.literal("Pause"), button -> {
		    if (scanExecutor.paused) { // user updates values during pause
                String rateVal = rateBox.getValue().trim();
                int chunkVal = 262144;
                try {
                    chunkVal = Integer.parseInt(chunkBox.getValue().trim());
                } 
                catch (Exception e) {
                    scanExecutor.logs.add("Invalid batch size, using batch_size=" + chunkVal);
                    e.printStackTrace();
                }
                if (chunkVal > 0) scanExecutor.resume(rateVal, chunkVal);
            }
            else {
                scanExecutor.pause();
            }
		    updateControlButtons();
        }).bounds(submitButton.getX(), buttonY, pauseWidth, 20).build();
        this.addRenderableWidget(pauseButton);

		stopButton = Button.builder(Component.literal("Stop"), button -> {
		    scanExecutor.stop();
		    updateControlButtons();
        }).bounds(pauseButton.getX() + pauseButton.getWidth(), buttonY, stopWidth, 20).build();
        this.addRenderableWidget(stopButton); // pBx + (sBw - 10) / 2 + 10 --> pBx + sBw/2 + 5

		Button logsButton = Button.builder(Component.literal("View Past Scans"), button -> {
            if (ServerscanClient.logsScreen == null) {
				ServerscanClient.logsScreen = new pastScansScreen(this);
			}

			this.minecraft.setScreen(ServerscanClient.logsScreen);
   	 	}).bounds(submitButton.getX() + submitButton.getWidth() + padding, buttonY, logsWidth, 20).build();
    	this.addRenderableWidget(logsButton);

        Button backButton = Button.builder(Component.literal("Back"), button -> {
            this.minecraft.setScreen(parent);
        }).bounds(logsButton.getX() + logsButton.getWidth() + padding, buttonY, backWidth, 20).build();
        this.addRenderableWidget(backButton);
    }

    public void renderTerm(GuiGraphics context) { // context works only within render()
        // black background with gray borders
        context.fill(termX - 1, termY - 1, termX + termWidth + 1, termY + termHeight + 1, gray); // gray color
        context.fill(termX, termY, termX + termWidth, termY + termHeight, black); // black color

        int maxLines = (termHeight - 10) / font.lineHeight;
	    List<String> logs = scanExecutor.logs; 

	    int startIndex = Math.max(0, logs.size() - maxLines); // show last X lines
        int currentY = termY + 5; // new lines are 5 pixels below

        for (int i = startIndex; i < logs.size(); i++) {
            context.drawString(this.font, Component.literal(logs.get(i)), termX + 5, currentY, white);
            currentY += this.font.lineHeight;
        }
    }

    public void updateControlButtons() {
	    submitButton.visible = !scanExecutor.running;
	    pauseButton.visible = scanExecutor.running;
	    stopButton.visible = scanExecutor.running;

	    if (scanExecutor.running) {
	        if (scanExecutor.paused) {
		        pauseButton.setMessage(Component.literal("Resume"));
                pauseButton.active = !scanExecutor.isChunkRunning(); // disable until chunk finishes

                // batch size and rate allowed for change except the following during a pause
                ipBox.active = false; portBox.active = false; outFileBox.active = false;
                rateBox.active = true; chunkBox.active = true;

                ipBox.setTextColor(gray); portBox.setTextColor(gray); outFileBox.setTextColor(gray);
                rateBox.setTextColor(white); chunkBox.setTextColor(white);
	        }
	        else {
		        pauseButton.setMessage(Component.literal("Pause"));
                pauseButton.active = true;

                ipBox.active = false; portBox.active = false; rateBox.active = false;
                chunkBox.active = false; outFileBox.active = false;

                ipBox.setTextColor(gray); portBox.setTextColor(gray); rateBox.setTextColor(gray);
                chunkBox.setTextColor(gray); outFileBox.setTextColor(gray);
	        }
	    }
        else { // if finished scanning, all boxes become editable
            ipBox.active = true; portBox.active = true; outFileBox.active = true;
            rateBox.active = true; chunkBox.active = true;

            ipBox.setTextColor(white); portBox.setTextColor(white); outFileBox.setTextColor(white);
            rateBox.setTextColor(white); chunkBox.setTextColor(white);
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

        // bro, this single line below caused me so much confusion, it turns out colors are in 0xFFFFFFFF format, not 0xFFFFFF
        // context.drawCenteredString(this.font, Component.literal("Ha! You cliked a button"), this.width/2, this.height/2, white);
    }  
}