package echen0719.serverscan;

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

        executable = new File(folder, "masscan");

        if (!executable.exists()) {
            // gets the bin at the resource folder and then open a stream to it (i think?)
            try (InputStream input = nativeUtil.class.getClassLoader().getResourceAsStream("native/masscan")) {
                if (input == null) throw new IOException("Could not find masscan binary in resources");
                Files.copy(input, executable.toPath(), StandardCopyOption.REPLACE_EXISTING); // copies bin to folder
            }
            executable.setExecutable(true); // so JAR can execute it
        }

        return executable;
    }
}