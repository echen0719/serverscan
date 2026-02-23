package echen0719.serverscan;

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
    private EditBox outFileBox;

    private int formStartX, formStartY;
    private int padding;
    private int widthForInputs;

    private int termX, termY, termWidth, termHeight;

    private int white = 0xFFFFFFFF;
    private int gray = 0xFFAAAAAA;
    private int black = 0xFF000000;

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

    private void createFormAndCalcTerm() {
        int inputHeight = 20;

        int ipBoxWidth = (int)(widthForInputs * 0.5f);
        int portBoxWidth = (int)(widthForInputs * 0.25f);
        int rateBoxWidth = (int)(widthForInputs * 0.25f);
        int outFileBoxWidth = (int)(widthForInputs * 0.25f);

        int portBoxX = formStartX + ipBoxWidth + padding;
        int rateBoxX = portBoxX + portBoxWidth + padding;

        termX = formStartX;
        termY = formStartY + inputHeight + padding / 2;
        termWidth = (rateBoxX + rateBoxWidth) - formStartX;
        termHeight = this.height - termY - pxH(0.15f);

        ipBox = new EditBox(this.font, formStartX, formStartY, ipBoxWidth, inputHeight, Component.literal(""));
        ipBox.setHint(Component.literal("0.0.0.0-255.255.255.255"));
        this.addRenderableWidget(ipBox);

        portBox = new EditBox(this.font, portBoxX, formStartY, portBoxWidth, inputHeight, Component.literal(""));
        portBox.setHint(Component.literal("25565"));
        this.addRenderableWidget(portBox);

        rateBox = new EditBox(this.font, rateBoxX, formStartY, rateBoxWidth, inputHeight, Component.literal(""));
        rateBox.setHint(Component.literal("100000"));
        this.addRenderableWidget(rateBox);

        int outFileBoxY = termY + termHeight + padding / 2; // don't want the line to be too long
        outFileBox = new EditBox(this.font, formStartX, outFileBoxY, outFileBoxWidth, inputHeight, Component.literal(""));
        outFileBox.setHint(Component.literal("output.txt"));
        this.addRenderableWidget(outFileBox);
    }

    public void createTerm(GuiGraphics context) { // context works only within render()
        // black background with gray borders
        context.fill(termX - 1, termY - 1, termX + termWidth + 1, termY + termHeight + 1, gray); // gray color
        context.fill(termX, termY, termX + termWidth, termY + termHeight, black); // black color

        int maxLines = (termHeight - 10) / font.lineHeight;
        int currentY = termY + 5; // new lines are 5 pixels below

        for (int i = 0; i < maxLines; i++) {
            context.drawString(this.font, Component.literal("Line " + (i + 1)), termX + 5, currentY, white);
            currentY += this.font.lineHeight;
        }
    }

    @Override
    protected void init() {
        super.init();

	formStartX = pxW(0.05f); // start 5% of width out
	formStartY = pxH(0.1f); // start 10% of height out
	padding = 20;

	// whole width minus padding on each side and minus padding between boxes
	widthForInputs = this.width - (formStartX * 2) - (padding * 2);
	int formEnd = formStartX + widthForInputs + (padding * 2);

        createFormAndCalcTerm();

	Button submitButton = Button.builder(Component.literal("Run Scan"), button -> {
            //
        }).bounds(outFileBox.getX() + outFileBox.getWidth() + 15, termY + termHeight + 10, (int)(widthForInputs * 0.25f), 20).build();
        this.addRenderableWidget(submitButton);

	Button logsButton = Button.builder(Component.literal("View Past Scans"), button -> {
            //
        }).bounds(submitButton.getX() + submitButton.getWidth() + 15, termY + termHeight + 10, (int)(widthForInputs * 0.3f), 20).build();
        this.addRenderableWidget(logsButton);

	// this is all so the back button can be aligned to the right of the form
	int backButtonWidth = (int)(widthForInputs * 0.2f);
        Button backButton = Button.builder(Component.literal("Back"), button -> {
            this.minecraft.setScreen(parent);
        }).bounds(formEnd - backButtonWidth, termY + termHeight + 10, backButtonWidth, 20).build();
        this.addRenderableWidget(backButton);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int labelOffsetY = -this.font.lineHeight - 3;

        // Labels for input boxes
        context.drawString(this.font, Component.literal("IP Ranges: "), ipBox.getX(), ipBox.getY() + labelOffsetY, white);
        context.drawString(this.font, Component.literal("Port Ranges: "), portBox.getX(), portBox.getY() + labelOffsetY, white);
        context.drawString(this.font, Component.literal("Speed (pps): "), rateBox.getX(), rateBox.getY() + labelOffsetY, white);

        createTerm(context);

        // bro, this single line below caused me so much confusion, it turns out colors are in 0xFFFFFFFF format, not 0xFFFFFF
        // context.drawCenteredString(this.font, Component.literal("Ha! You cliked a button"), this.width/2, this.height/2, white);
    }
}
