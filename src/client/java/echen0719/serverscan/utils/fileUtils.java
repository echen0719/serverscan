package echen0719.serverscan.utils;

import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class fileUtils {
    private static final String chunkExtension = ".tmp.chunk";
    private final File outputsFolder;

    public fileUtils(File gameDir) {
        this.outputsFolder = new File(gameDir, "serverscan/outputs");
        ensureFolderExists();
    }
    
    private void ensureFolderExists() {
        if (!outputsFolder.exists()) {
            outputsFolder.mkdirs();
        }
    }

    public File getOutputsFolder() {
        return outputsFolder;
    }

    public boolean outputFileExists(String outputName) {
        File outputFile = new File(outputsFolder, outputName);
        return outputFile.exists();
    }

    public File createOutputFile(String outputName) throws Exception {
        File outputFile = new File(outputsFolder, outputName);
        outputFile.createNewFile();
        return outputFile;
    }

    public File createChunkFile(String outputName, int chunkIndex) {
        return new File(outputsFolder, outputName + "." + chunkIndex + chunkExtension);
    }

    public void mergeChunks(String outputFileName) {
        File outputFile = new File(outputsFolder, outputFileName);
		File[] chunkFiles = outputsFolder.listFiles((dir, name) -> // i need to learn lambda
			name.startsWith(outputFileName) && name.endsWith(chunkExtension));
		
        if (chunkFiles == null || chunkFiles.length == 0) return;

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))) { // append
	    	for (File chunkFile : chunkFiles) {
				try (BufferedReader reader = new BufferedReader(new FileReader(chunkFile))) {
		   			String line;
		    		while ((line = reader.readLine()) != null) {
						writer.write(line);
						writer.newLine();
	    	    	}
				}
			chunkFile.delete();
	    	}
	    	writer.flush(); // data to disk
		}
		catch (Exception e) {
	   		e.printStackTrace();
		}
    }
}
