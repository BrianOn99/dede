package cuhk.dede.localBackend;

import cuhk.dede.Handler;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.File;

import java.io.FileNotFoundException;

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

    public FileInputStream download(String name) {
        File fpath = new File(CheckInit.dir, name);
        /* buffering (I think) does not help because the program read in block
         * if one use small minSize, it will matter.  But it is not realistic
         */
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(fpath);
        } catch (FileNotFoundException e) {
            System.err.printf("Error: chunk %s is not found\n", name);
            System.exit(1);
        }
        return stream;
    }
}
