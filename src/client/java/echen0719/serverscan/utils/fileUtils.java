package echen0719.serverscan.utils;

import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class fileUtils {
    private static final String chunkExtension = ".tmp.chunk";
    private final File outputsFolder;

    public fileUtils(File gameDir) {
        this.outputsFolder = new File(gameDir, "serverscan/outputs");
        ensureFolderExists();
    }

    public File[] getChildFiles() {
        ArrayList<File> files = new ArrayList<File>();
        for (File file : outputsFolder.listFiles()) {
            if (file.isFile()) {
                files.add(file);
            }
        }; // prefer to work arrays instead
        return files.toArray(new File[files.size()]);
    }

    public String formattedFileSize(File file) {
        if (file == null || !file.exists()) return "N/A";
        long bytes = file.length();

        if (bytes < 1024) {
            return bytes + " B";
        }

        else if (bytes < 1048576) {
            return String.format("%.1f KB", bytes / 1024.0);
        }

        else if (bytes < 1073741824) {
            return String.format("%.1f MB", bytes / 1048576.0);
        }

        else {
            return String.format("%.1f GB", bytes / 1073741824.0);
        } 
        // 🤨 why would you need TB?
    }

    public String formattedDate(File file) {
        if (file == null || !file.exists()) return "N/A";

        SimpleDateFormat formatter = new SimpleDateFormat("M/d/yyyy");
        return formatter.format(new Date(file.lastModified()));
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
