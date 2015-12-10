package cuhk.dede;

import cuhk.dede.localBackend.CheckInit;
import cuhk.dede.localBackend.LocalHandler;
import java.lang.Runtime;
import java.io.ByteArrayInputStream;
import java.lang.Process;

import org.junit.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocalTest
{
    @BeforeClass
    public static void setup() {
        try {
            Runtime.getRuntime().exec(new String[]{"rm", "-r", CheckInit.dir}).waitFor();
        } catch (Exception e) {
            System.err.println("Error removing old data dir");
            System.exit(1);
        }
    }

    @Test
    public void testInit() {
        CheckInit.run();
        assertFalse("Should not initialize 2 times", CheckInit.run());
    }

    /*
     * test upload, check if the size of all chuncks is smaller than original
     */
    @Test
    public void testUpload() {
        CheckInit.run();

        String datastr = "1234567890";
        byte[] data = (datastr+datastr+datastr).getBytes();
        ByteArrayInputStream mockFile = new ByteArrayInputStream(data);
        RabinChunker.Params paramsL = new RabinChunker.Params(3, 512, 3, 4, 7);

        try {
            new FileUploadChunker(mockFile, "on99file", paramsL, new LocalHandler());
        } catch (java.io.IOException e) {
            System.err.println("Error uploading chunk");
        }

        byte[] du_output = new byte[64];
        int count = 0;
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"du", "-s", CheckInit.dir});
            count = p.getInputStream().read(du_output);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error running du command");
        }
        String s = new String(du_output, 0, count);
        int all_chunk_size = Integer.parseInt(s.split("\t")[0]);
        assertTrue("Total data chunk size should become smaller",
                   all_chunk_size < data.length);
    }
}
