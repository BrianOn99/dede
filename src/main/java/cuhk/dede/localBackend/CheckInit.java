package cuhk.dede.localBackend;

import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;

public class CheckInit
{
    public static String dir = "./data";

    public static boolean run() {
        Path dirpath = Paths.get(dir);
        if (Files.notExists(dirpath)) {
            try {
                Files.createDirectory(dirpath);
            } catch (IOException e) {
                System.err.println("Error initializing local backEnd: " + e.getMessage());
            }
            return true;
        } else {
            return false;
        }
    }
}
