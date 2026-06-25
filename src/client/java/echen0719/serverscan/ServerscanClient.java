package echen0719.serverscan;

import java.io.File;

import echen0719.serverscan.utils.nativeUtil;
import echen0719.serverscan.screens.pastScansScreen;
import echen0719.serverscan.screens.scanScreen;
import echen0719.serverscan.screens.viewServerScreen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class ServerscanClient implements ClientModInitializer {
	public static scanScreen mainScreen;
	public static pastScansScreen logsScreen;
	public static viewServerScreen serversScreen;

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		File gameDir = FabricLoader.getInstance().getGameDirectory();
        File folder = new File(gameDir, "serverscan");

		new File(folder, "outputs").mkdirs();
        new File(folder, "logs").mkdirs();
        new File(folder, "config").mkdirs();

		try {
			nativeUtil.writeConfig(folder, null);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}