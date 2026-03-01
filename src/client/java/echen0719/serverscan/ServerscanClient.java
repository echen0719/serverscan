package echen0719.serverscan;

import echen0719.serverscan.screens.pastScansScreen;
import echen0719.serverscan.screens.scanScreen;
import net.fabricmc.api.ClientModInitializer;

public class ServerscanClient implements ClientModInitializer {
	public static scanScreen mainScreen;
	public static pastScansScreen logsScreen;

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
	}
}