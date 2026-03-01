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
}