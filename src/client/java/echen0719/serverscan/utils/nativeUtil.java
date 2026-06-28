package echen0719.serverscan.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

import echen0719.serverscan.scanExecutor;

public class nativeUtil {
    private static File executable;
    private static final String configFilePath = "config/masscan.conf";

    public static File getBinary(File gameDir) throws IOException {
        // check if masscan executable is already unpacked
        if (executable != null && executable.exists() && executable.canExecute()) {
            return executable;
        }

        // creates folder at ./minecraft/serverscan with bin
        File folder = new File(gameDir, "serverscan");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File configFile = new File(folder, configFilePath);
        String osName = System.getProperty("os.name");

        if (osName.toLowerCase().contains("win")) {
            if (!configFile.exists() || !hasValidPath(configFile)) {
                writeConfig(folder, null);
            }
            else {
                scanExecutor.addLog("Using existing masscan configuration from: " + configFile.getAbsolutePath());
            }

            executable = readConfig(folder);
        }
        else if (osName.toLowerCase().contains("linux")) {
            File foundExecutable = findMasscan();

            if (foundExecutable != null) {
                if (!configFile.exists() || !hasValidPath(configFile)) {
                    writeConfig(folder, foundExecutable.getAbsolutePath());
                }

                System.out.println("Using installed masscan: " + foundExecutable.getAbsolutePath());
                scanExecutor.addLog("Using installed masscan: " + foundExecutable.getAbsolutePath());
                
                executable = foundExecutable;
                return executable;
            }
            
            if (!configFile.exists() || !hasValidPath(configFile)) {
                writeConfig(folder, null);
            }
            else {
                scanExecutor.addLog("Using existing masscan configuration from: " + configFile.getAbsolutePath());
            }

            executable = readConfig(folder);
        }
        else { // only support for Windows and Linux, nothing else now
            throw new UnsupportedOperationException("Unsupported OS: " + osName);
        }

        return executable;
    }

    private static File findMasscan() {
        String[] knownPaths = {"/usr/bin/masscan", "/usr/local/bin/masscan"};
        for (String path : knownPaths) {
            File file = new File(path);
            if (file.isFile() && file.canExecute()) {
                return file;
            }
        }

        return null;
    }

    public static void writeConfig(File serverscanFolder, String masscanPath) throws IOException {
        File configFile = new File(serverscanFolder, configFilePath);
        File configDir = configFile.getParentFile();

        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        Properties props = new Properties();
        if (masscanPath != null) {
            props.setProperty("masscan.path", masscanPath);
        }
        else {
            props.setProperty("masscan.path", "");
        }

        try (var out = Files.newOutputStream(configFile.toPath())) {
            props.store(out, "ServerScan Masscan File Config");
        }
    }

    public static File readConfig(File serverscanFolder) throws IOException {
        File configFile = new File(serverscanFolder, configFilePath);

        if (!configFile.exists()) {
            scanExecutor.addLog("Missing config file: " + configFile.getAbsolutePath());
            throw new IOException(
                "Missing config file: " + configFile.getAbsolutePath() +
                "\nCreate it and set masscan.path=<path to masscan binary/executable>"
            );
        }

        Properties props = new Properties();

        try (var in = Files.newInputStream(configFile.toPath())) {
            props.load(in);
        }

        String path = props.getProperty("masscan.path");

        if (path == null || path.isBlank()) {
            scanExecutor.addLog("masscan.path is not configured in " + configFile.getAbsolutePath());
            throw new IOException("masscan.path is not configured in " + configFile.getAbsolutePath());
        }

        File executable = new File(path);

        if (!executable.isFile() || !executable.canExecute()) {
            scanExecutor.addLog("Configured masscan executable is invalid: " + path);
            throw new IOException("Configured masscan executable is invalid: " + path);
        }

        return executable;
    }

    private static boolean hasValidPath(File configFile) {
        if (!configFile.exists()) {
            return false;
        }
        
        try {
            Properties props = new Properties();
            try (var in = Files.newInputStream(configFile.toPath())) {
                props.load(in);
            }

            String path = props.getProperty("masscan.path");
            if (path == null || path.isBlank()) {
                return false;
            }

            File executable = new File(path);
            return executable.isFile() && executable.canExecute();
        } 
        catch (IOException e) {
            return false;
        }
    }
}