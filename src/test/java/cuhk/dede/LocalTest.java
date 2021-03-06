package cuhk.dede;

import cuhk.dede.localBackend.CheckInit;
import cuhk.dede.localBackend.LocalHandler;
import java.lang.Runtime;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.Process;
import java.io.IOException;

import org.junit.*;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;

import org.junit.runners.MethodSorters;

/* must not access database in parallel */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LocalTest
{
    private static MetadataStore.Mode localmode = MetadataStore.Mode.LOCAL;
    private static RabinChunker.Params paramsL = new RabinChunker.Params(3, 512, 3, 4, 7);
    private byte[] data = "123456789012345678901234567890".getBytes();

    @BeforeClass
    public static void setup() {
        try {
            Runtime.getRuntime().exec(
                    new String[]{"rm", "-r", CheckInit.dir, MetadataStore.db_name}
                    ).waitFor();
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
    public void testUploadDownload() throws IOException, FileChunker.StoreException {
        setup();
        CheckInit.run();

        ByteArrayInputStream mockFile = new ByteArrayInputStream(data);
        new FileChunker(localmode, new LocalHandler()).upload(mockFile, "on99file", paramsL);

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

        ByteArrayOutputStream mockoutput = new ByteArrayOutputStream();
        new FileChunker(localmode, new LocalHandler()).download(mockoutput, "on99file");
        //System.out.println(mockoutput.toString());
        assertArrayEquals(data, mockoutput.toByteArray());
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testUploadSameFile() throws IOException, FileChunker.StoreException {
        CheckInit.run();

        ByteArrayInputStream mockFile2 = new ByteArrayInputStream(data);
        new FileChunker(localmode, new LocalHandler()).upload(mockFile2, "on99file2", paramsL);
        mockFile2 = new ByteArrayInputStream(data);
        thrown.expect(FileChunker.StoreException.class);
        thrown.expectMessage("File already exist in the store");
        new FileChunker(localmode, new LocalHandler()).upload(mockFile2, "on99file2", paramsL);
    }

    /*
     * This test case is inspected by eye
     */
    @Test
    public void testDeleteFile() throws IOException, FileChunker.StoreException {
        CheckInit.run();

        ByteArrayInputStream mockFile3 = new ByteArrayInputStream(data);
        new FileChunker(localmode, new LocalHandler()).upload(mockFile3, "on99file3", paramsL);
        mockFile3 = new ByteArrayInputStream("this will not delete".getBytes());
        new FileChunker(localmode, new LocalHandler()).upload(mockFile3, "shouldNotDelete", paramsL);
        new FileChunker(localmode, new LocalHandler()).delete("on99file3");
    }
}
