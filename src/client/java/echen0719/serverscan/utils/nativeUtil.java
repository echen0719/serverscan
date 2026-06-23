package echen0719.serverscan.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class nativeUtil {
    private static File executable;

    public static File getBinary(File gameDir) throws IOException {
        // check if masscan executable is already unpacked
        if (executable != null && executable.exists()) {
            return executable;
        }

        // creates folder at ./minecraft/serverscan with bin
        File folder = new File(gameDir, "serverscan");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String binName;
        String osName = System.getProperty("os.name");

        if (osName.toLowerCase().contains("win")) {
            binName = "masscan.exe";
        }
        else if (osName.toLowerCase().contains("linux")) {
            binName = "masscan";

            File foundExecutable = findMasscan();
            if (foundExecutable != null) {
                System.out.println("Using installed masscan: " + foundExecutable.getAbsolutePath());
                executable = foundExecutable;
                return executable;
            }
        }
        else { // only support for Windows and Linux, nothing else now
            throw new UnsupportedOperationException("Unsupported OS: " + osName);
        }

        executable = new File(folder, binName);

        if (!executable.exists()) {
            // gets the bin at the resource folder and then open a stream to it (i think?)
            try (InputStream input = nativeUtil.class.getClassLoader().getResourceAsStream("native/" + binName)) {
                if (input == null) throw new IOException("Could not find masscan binary in resources");
                Files.copy(input, executable.toPath(), StandardCopyOption.REPLACE_EXISTING); // copies bin to folder
            }
            executable.setExecutable(true); // so JAR can execute it
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

        String home = System.getProperty("user.home");
        if (home != null) {
            File homeDir = new File(home);
            File found = searchHomeForMasscan(homeDir, "masscan", 3); // depth 3 for performance
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    private static File searchHomeForMasscan(File dir, String name, int depth) {
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }

        for (File file : files) {
            if (file.isFile() && file.getName().equals(name) && file.canExecute()) {
                return file;
            }
        }

        for (File file : files) {
            if (file.isDirectory()) { // some random ahh recursion
                File found = searchHomeForMasscan(file, name, depth - 1);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }
}