package cuhk.dede.localBackend;

import cuhk.dede.Handler;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;

public class LocalHandler implements Handler
{
    public void upload(String name, byte[] content, int size) {
        File fpath = new File(CheckInit.dir, name);
        try (FileOutputStream outStream = new FileOutputStream(fpath)) {
            outStream.write(content, 0, size);
        } catch (IOException e) {
            System.err.println("Error writing chunk to local: " + e.getMessage());
            System.exit(1);
        }
    }
}
