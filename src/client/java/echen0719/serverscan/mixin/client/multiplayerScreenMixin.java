package echen0719.serverscan.mixin.client;
import echen0719.serverscan.scanScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;

// https://stackoverflow.com/questions/72516476
@Mixin(JoinMultiplayerScreen.class)
public class multiplayerScreenMixin extends Screen {
	protected multiplayerScreenMixin(Component title) {
        super(title);
    }

	@Inject(at = @At("TAIL"), method = "init")
	private void init(CallbackInfo info) {
		int buttonWidth = 100;
		int buttonHeight = 20;
		int x = 5;
		int y = 5;

		// https://github.com/orgs/FabricMC/discussions/1795
		Button openButton = Button.builder(Component.literal("Scan Servers"), (button) -> {
            this.minecraft.setScreen(new scanScreen(this));
        }).bounds(x, y, buttonWidth, buttonHeight).build();
		this.addRenderableWidget(openButton);
	}
}