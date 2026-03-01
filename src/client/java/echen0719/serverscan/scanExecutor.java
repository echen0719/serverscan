package echen0719.serverscan;

import java.io.File;

import java.util.List;

import java.util.concurrent.CopyOnWriteArrayList; // arraylist but for concurrency
import java.util.concurrent.ConcurrentLinkedDeque; // queue but for concurrency
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import echen0719.serverscan.utils.IPUtils;
import echen0719.serverscan.utils.fileUtils;
import echen0719.serverscan.utils.nativeUtil;
import net.fabricmc.loader.api.FabricLoader;

public class scanExecutor {
	// executor pool that creates threads as it needs
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    
    // process and logs visible to all methods; removed callback
    private static volatile Process currentProcess = null;
    public static final CopyOnWriteArrayList<String> logs = new CopyOnWriteArrayList<String>();

    // volatile for allowing other threads to view changes?
    public static volatile boolean running = false;
    public static volatile boolean paused = false;

	public static volatile String rate = "";
    public static volatile int chunkSize = 262144; // = 2^18

    private static final ConcurrentLinkedDeque<long[]> ipRangeQueue = new ConcurrentLinkedDeque<long[]>();

	private static fileUtils filesManager;

    private static void addLog(String message) {
		logs.add(message);
		if (logs.size() > 500) { // reduces logs memory usage with 500 lines
	    	logs.removeFirst();
		}
    }

    // https://www.baeldung.com/java-executor-service-tutorial
    private static void runScan(String portRanges, String output) {
		addLog("Scan started...");

        try {
	    	File gameDir = FabricLoader.getInstance().getGameDirectory();
        	File binary = nativeUtil.getBinary(gameDir); // calls nativeUtil with game directory
	    	
			filesManager = new fileUtils(gameDir);

            if (filesManager.outputFileExists(output)) {
				running = false;
                addLog("File already exists. Used another file name.");
                return;
			}

			filesManager.createOutputFile(output);

	    	int chunkIndex = 0;
			long[] activeRange = null;
			long nextIP = 0;

	    	while (running) {
				if (paused) { // merge on pause
					if (activeRange != null && nextIP <= activeRange[1]) {
						ipRangeQueue.addLast(new long[] {nextIP, activeRange[1]});
					}

		    		filesManager.mergeChunks(output);
					activeRange = null;
					nextIP = 0;

					currentProcess = null;
                    
					while (paused && running) {
                        Thread.sleep(300);
                    }
                    if (!running) break;
                    continue;
                }

				if (activeRange != null && nextIP > activeRange[1]) {
					activeRange = null;
				}

				if (activeRange == null) {
					activeRange = ipRangeQueue.pollFirst();
					if (activeRange != null) nextIP = activeRange[0];
				}

				if (activeRange == null) break; // No more work
				
				long chunkEnd = Math.min(nextIP + chunkSize - 1, activeRange[1]);
				String ipChunk = IPUtils.longToIP(nextIP) + "-" + IPUtils.longToIP(chunkEnd);
				System.out.println("DEBUG:" + ipChunk);
				System.out.println("IPs to scan: " + (chunkEnd - nextIP + 1));
				nextIP = chunkEnd + 1;
				if (nextIP > activeRange[1]) activeRange = null;

				File chunkFile = filesManager.createChunkFile(output, chunkIndex);
				chunkIndex++;

				String time = "7";
				if (chunkSize <= 16384) time = "3";
				else if (chunkSize <= 131702) time = "5";
				else if (chunkSize <= 524288) time = "7";
				else if (chunkSize <= 2097152) time = "10";
				else time = "12";

				// ./masscan x.x.x.x-y.y.y.y -p zzzzz --rate dddddd --exclude 255.255.255.255 --wait 5 --append-output -oL output
				ProcessBuilder peanutButter = new ProcessBuilder(binary.getAbsolutePath(), ipChunk, "-p", portRanges,
					"--rate", scanExecutor.rate, "--exclude", "255.255.255.255", "--wait", time, "-oL", chunkFile.getCanonicalPath()
				); // 7 second wait time is good, i think?

				peanutButter.directory(gameDir); // paused.conf 
				peanutButter.redirectErrorStream(true);
				Process process = peanutButter.start();
				currentProcess = process;

				// for every line of output by the command, it takes it and then sends it to the Minecraft instance
				try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
					String line;
					while (running && (line = reader.readLine()) != null) {
						addLog(line);
					}
				}
				catch (Exception e) {
					if (!paused) {
						e.printStackTrace();
						addLog("Error reading output: " + e.getMessage());
					}
				}

				int exitCode = process.waitFor();
				if (exitCode != 0 && running) { // handle chunk error
					addLog("Chunk failed (Exit: " + exitCode + ")");
				}
	    	}

	    	if (running) {
				addLog("Scan complete.");
	    	}
        } 
        catch (Exception e) {
            e.printStackTrace();
            addLog("Error with scanning: " + e.getMessage());
        }
		finally { // reset all status variables
	    	running = false;
	    	paused = false;
	    	currentProcess = null;
			ipRangeQueue.clear();

	    	try { // merge when stopped
				if (filesManager != null) {
                    filesManager.mergeChunks(output);
                }
	    	}
	    	catch (Exception e) {
				addLog("Could not clean up .tmp.chunk files...");
	    	}
		}
    }

	public static boolean isChunkRunning() {
    	return currentProcess != null;
	}

    public static void startScan(String ipRanges, String portRanges, String rate, int chunkSize, String output) {
		if (running) return; // prevent race-conditions

		running = true;
		paused = false;

		scanExecutor.rate = rate;
		scanExecutor.chunkSize = chunkSize; // static issues
		ipRangeQueue.clear();

		try {
	    	List<long[]> rawRanges = IPUtils.parseIPRanges(ipRanges);
			ipRangeQueue.addAll(rawRanges);
		}
		catch (Exception e) {
	    	addLog("Invalid IP ranges");
	    	e.printStackTrace();
	    	return;
		}

		logs.clear();

        executor.submit(() -> runScan(portRanges, output)); // submit runScan task
    }

    public static void pause() { // user presses pause button
		if (!running) return;
		paused = true;
		addLog("Scan pausing...Wait until chunk finishes");
    }

    public static void resume(String newRate, int newChunkSize) {
		if (!running) return;
		paused = false;

		rate = newRate;
		chunkSize = newChunkSize;

		addLog("Scan resuming with rate=" + rate + " and batch_size=" + chunkSize);
    }

    public static void stop() {
		if (!running) return;
		running = false;

		if (currentProcess != null) {
	    	currentProcess.destroy(); // graceful SIGTERM

	    	try {
				if (!currentProcess.waitFor(5, TimeUnit.SECONDS)) {
		    		currentProcess.destroyForcibly(); // if destroy() doesn't terminate, it waits 5s for kill
		    		addLog("Chunk closed abruptly (File may be incomplete)");
				}
	    	}
	    	catch (Exception e) {
				currentProcess.destroyForcibly(); // destroyForcibly even if error
				addLog("Error closing previous chunk: " + e.getMessage());
	    	}
		}

		ipRangeQueue.clear();
		addLog("Scan stopped");
    }
}