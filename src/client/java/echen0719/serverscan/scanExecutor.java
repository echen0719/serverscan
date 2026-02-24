package echen0719.serverscan;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedDeque; // queue but for concurrency
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.minecraft.client.Minecraft;
import net.fabricmc.loader.api.FabricLoader;

public class scanExecutor {
    // executor pool that creates threads as it needs
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    
    // makes sure the process and callback are accessbile in the pause, resume, and stop methods
    private static Process currentProcess = null;
    private static scanCallback currentCallback = null;

    // volatile for allowing other threads to view changes?
    private static volatile boolean running = false;
    private static volatile boolean paused = false;
    private static final int chunkSize = 32768; // 2^15
    private static final ConcurrentLinkedDeque<String> ipQueue = new ConcurrentLinkedDeque<String>();

    // https://stackoverflow.com/questions/12057853
    private static long ipToLong(String ip) {
	String[] parts = ip.split("\\.");
	long result = 0;
	for (String part : parts) {
	    result = (result << 8) | Integer.parseInt(part);
	}
	return result;
    }

    private static String longToIP(long ip) {
        int o1 = (int)(ip >> 24) & 0xFF;
        int o2 = (int)(ip >> 16) & 0xFF;
        int o3 = (int)(ip >> 8) & 0xFF;
        int o4 = (int)ip & 0xFF;

        return o1 + "." + o2 + "." + o3 + "." + o4;
    }

    private static void parseIPRanges(String ipRange) {
	List<String> chunks = new ArrayList<String>();	

	// ex: ipRange = 1.2.3.4-5.6.7.8, 9.10.11.12
	String[] parts = ipRange.split(","); 

	for (String part : parts) {
	    part = part.trim();
	    if (part.isEmpty()) continue;

	    long start, end;

	    if (part.contains("-")) {
		String[] portions = part.split("-");
		if (portions.length != 2) continue;
		
		start = ipToLong(portions[0].trim());
		end = ipToLong(portions[1].trim());
	    }
	    else { // for single IP inputs
		start = ipToLong(part);
		end = start;
	    }
		
	    if (start > end) continue;

	    // ex: chunks of 0-2047, 2048-4095, 4096-4999 for 5000 length
	    for (long i = start; i <= end; i += chunkSize) {
		long chunkEnd = Math.min(i + chunkSize - 1, end);
		chunks.add(longToIP(i) + "-" + longToIP(chunkEnd));
	    }
	}

	Collections.shuffle(chunks); // shuffle to not overload servers
	ipQueue.addAll(chunks);
    }

    // https://www.baeldung.com/java-executor-service-tutorial
    private static void runScan(String portRanges, String rate, String output, scanCallback callback) {
	currentCallback = callback;
	running = true;
	paused = false;

        try {
	    File gameDir = FabricLoader.getInstance().getGameDirectory();

            // calls nativeUtil with game directory
            File binary = nativeUtil.getBinary(gameDir);
	    File folder = new File(gameDir, "serverscan");
	    if (!folder.exists()) {
		folder.mkdirs();
	    }
            File outputFile = new File(folder, output); // output
	    if (outputFile.exists()) {
		running = false;

		if (currentCallback != null) Minecraft.getInstance().execute(() -> callback.onError("File already exists. Used another file name."));
		return;
	    }

	    while (running) {
		while (paused && running) {
                    Thread.sleep(1000);
                }
		if (!running) break;

		String ipChunk = ipQueue.pollFirst();
		if (ipChunk == null) break;

		// ./masscan x.x.x.x-y.y.y.y -p zzzzz --rate dddddd --exclude 255.255.255.255 --wait 5 --append-output -oL output
		ProcessBuilder peanutButter = new ProcessBuilder(binary.getAbsolutePath(), ipChunk, "-p", portRanges, "--rate",
		    rate, "--exclude", "255.255.255.255", "--wait", "1", "--append-output", "-oL", outputFile.getAbsolutePath()
		); // 1 second wait time is good, i think?

		peanutButter.directory(gameDir); // paused.conf 
		peanutButter.redirectErrorStream(true);
		Process process = peanutButter.start();
		currentProcess = process;

		// for every line of output by the command, it takes it and then sends it to the Minecraft instance
		try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
		    String line;
		    while ((line = reader.readLine()) != null) {
			final String logLine = line;
			if (currentCallback != null) Minecraft.getInstance().execute(() -> callback.onLog(logLine));
		    }
		}
		catch (Exception e){
		    if (!paused) {
			e.printStackTrace();
           		if (currentCallback != null) callback.onError(e.getMessage());
		    }
		}

		int exitCode = process.waitFor();

		if (paused) { // handle pause
		    ipQueue.addFirst(ipChunk);
                    continue; 
		}

		if (exitCode != 0 && running) { // handle chunk error
		    if (currentCallback != null) Minecraft.getInstance().execute(() -> callback.onLog("Chunk failed."));
		}
	    }

	    if (running) {
		if (currentCallback != null) Minecraft.getInstance().execute(() -> callback.onComplete("Scan complete."));
	    }
        } 
        catch (Exception e) {
            e.printStackTrace();
            if (currentCallback != null) callback.onError(e.getMessage());
        }
    };

    public static void startScan(String ipRanges, String portRanges, String rate, String output, scanCallback callback) {
	stop(); // kill thread before starting new one

	ipQueue.clear();
	try {
	    parseIPRanges(ipRanges);
	}
	catch (Exception e) {
	    if (callback != null) callback.onError(e.getMessage() + " | Invalid IP ranges");
	    e.printStackTrace();
	}

        executor.submit(() -> runScan(portRanges, rate, output, callback)); // submit runScan task
    }

    public static void pause() {
	try {
	    paused = true;
	    if (currentProcess != null) currentProcess.destroy(); // kills chunking as pause
	    if (currentCallback != null) Minecraft.getInstance().execute(() -> currentCallback.onLog("Scan paused. Waiting to resume..."));
	}
	catch (Exception e) {
            e.printStackTrace();
            if (currentCallback != null) currentCallback.onError(e.getMessage());
        }
    }

    public static void resume() {
	try {
	    paused = false;
	    if (currentCallback != null) Minecraft.getInstance().execute(() -> currentCallback.onLog("Scan resuming..."));
        } 
        catch (Exception e) {
            e.printStackTrace();
            if (currentCallback != null) currentCallback.onError(e.getMessage());
        }
    }

    public static void stop() {
	try {
	    running = false;
	    paused = false;
	    if (currentProcess != null) currentProcess.destroyForcibly();
	    
	    ipQueue.clear();

	    if (currentCallback != null) Minecraft.getInstance().execute(() -> currentCallback.onComplete("Scan stopped."));
	}
	catch (Exception e) {
            e.printStackTrace();
        }
    }

    // IDK what this does exactly
    public interface scanCallback {
        void onComplete(String message);
        void onError(String message);
	void onLog(String lines); // to Minecraft, i think?
    }
}
