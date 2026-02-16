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
		int x = this.width - 100;
		int y = this.height - 30;

		// https://github.com/orgs/FabricMC/discussions/1795
		Button openButton = Button.builder(Component.literal("Scan Servers"), (button) -> {
            this.minecraft.setScreen(new scanScreen(this));
        }).bounds(x, y, 100, 20).build();
		this.addRenderableWidget(openButton);
	}
}