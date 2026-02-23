package echen0719.serverscan;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.minecraft.client.Minecraft;
import net.fabricmc.loader.api.FabricLoader;

public class scanExecutor {
    // executor pool that creates threads as it needs
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    // https://www.baeldung.com/java-executor-service-tutorial
    private static void runScan(String ipRanges, String portRanges, String rate, String output, ScanCallback callback) {
        try {
            // calls nativeUtil with game directory
            File binary = nativeUtil.getBinary(FabricLoader.getInstance().getGameDirectory());
            File outputFile = new File(output); // output

            // ./masscan x.x.x.x-y.y.y.y -p zzzzz --rate dddddd -oL output
            ProcessBuilder peanutButter = new ProcessBuilder(binary.getAbsolutePath(), ipRanges, "-p",
                portRanges, "--rate", rate, "-oL", outputFile.getAbsolutePath()
            );
            
            peanutButter.redirectErrorStream(true);
            Process process = peanutButter.start();

	    // for every line of output by the command, it takes it and then sends it to the Minecraft instance
	    try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
            	    final String logLine = line;
            	    Minecraft.getInstance().execute(() -> callback.onLog(logLine));
                }
	    }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                /* Future: parse IPs to GUI, I guess */
                Minecraft.getInstance().execute(() -> callback.onComplete("Scan complete."));
                }
            else {
                Minecraft.getInstance().execute(() -> callback.onError("Scan failed. Code: " + exitCode));
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
            callback.onError(e.getMessage());
        }
    };

    public static void startScan(String ipRanges, String portRanges, String rate, String output, ScanCallback callback) {
        executor.submit(() -> runScan(ipRanges, portRanges, rate, output, callback)); // submit runScan task
    }

    // IDK what this does exactly
    public interface ScanCallback {
        void onComplete(String message);
        void onError(String message);
	void onLog(String lines); // to Minecraft, i think?
    }
}
