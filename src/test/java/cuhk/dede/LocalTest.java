package cuhk.dede;

import cuhk.dede.localBackend.CheckInit;
import cuhk.dede.localBackend.LocalHandler;
import java.lang.Runtime;
import java.io.ByteArrayInputStream;

import org.junit.*;
import static org.junit.Assert.assertFalse;

public class LocalTest
{
    @BeforeClass
    public static void setup() {
        try {
            Runtime.getRuntime().exec(new String[]{"rm", "-r", CheckInit.dir});
        } catch (java.io.IOException e) {
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
     * This test is temporary, checking is done by eye
     */
    @Test
    public void testUpload() {
        CheckInit.run();

        String datastr = "1234567890qwertyuiopasdfghjklzxcvbnm";
        byte[] data = (datastr+datastr+datastr).getBytes();
        ByteArrayInputStream mockFile = new ByteArrayInputStream(data);
        RabinChunker.Params paramsL = new RabinChunker.Params(8, 512, 8, 4, 7);

        try {
            new FileUploadChunker(mockFile, paramsL, new LocalHandler());
        } catch (java.io.IOException e) {
            System.err.println("Error uploading chunk");
        }
    }
}
