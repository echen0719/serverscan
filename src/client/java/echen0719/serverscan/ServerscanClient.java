package echen0719.serverscan;

import echen0719.serverscan.screens.pastScansScreen;
import echen0719.serverscan.screens.scanScreen;
import echen0719.serverscan.screens.viewServerScreen;
import net.fabricmc.api.ClientModInitializer;

public class ServerscanClient implements ClientModInitializer {
	public static scanScreen mainScreen;
	public static pastScansScreen logsScreen;
	public static viewServerScreen serversScreen;

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
	}
}