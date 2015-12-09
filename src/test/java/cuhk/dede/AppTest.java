package cuhk.dede;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.Arrays;

public class AppTest
{
    private RabinChunker.Params params = new RabinChunker.Params(3, 512, 8, 8, 7);
    private RabinChunker.Params paramsL = new RabinChunker.Params(8, 512, 8, 4, 7);

    public AppTest()
    {
    }

    @Test
    public void testChunkerInit() {
        ByteArrayInputStream mockFile = new ByteArrayInputStream("".getBytes());
        RabinChunker chunker = new RabinChunker(mockFile, params);
    }

    @Test
    public void testChunkerRun() throws IOException {
        byte[] data = "testtest".getBytes();
        ByteArrayInputStream mockFile = new ByteArrayInputStream(data);
        RabinChunker chunker = new RabinChunker(mockFile, params);
        RabinChunker.ChunkInfo chunki = chunker.nextChunk();
        assertArrayEquals("should give same data", data, Arrays.copyOf(chunki.chunk, chunki.size));
    }

    @Test
    public void testChunkerSplit() throws IOException {
        String datastr = "1234567890qwertyuiopasdfghjklzxcvbnm";
        byte[] data = (datastr+datastr+datastr+datastr).getBytes();
        ByteArrayInputStream mockFile = new ByteArrayInputStream(data);
        RabinChunker chunker = new RabinChunker(mockFile, params);
        assertNotNull("should give me at least 3 chunks: 1st", chunker.nextChunk());
        assertNotNull("should give me at least 3 chunks: 2st", chunker.nextChunk());
        assertNotNull("should give me at least 3 chunks: 3st", chunker.nextChunk());
    }

    @Test
    public void testChunkerAll() throws IOException {
        String datastr = "1234567890qwertyuiopasdfghjklzxcvbnm";
        byte[] data = (datastr+datastr+datastr).getBytes();
        ByteArrayInputStream mockFile = new ByteArrayInputStream(data);
        RabinChunker chunker = new RabinChunker(mockFile, paramsL);
        byte[] res = new byte[data.length];
        int pos = 0;
        RabinChunker.ChunkInfo chunki;
        while ((chunki = chunker.nextChunk()) != null) {
            //System.out.println("got chunk " + chunki.size + new String(Arrays.copyOf(chunki.chunk, chunki.size)));
            java.lang.System.arraycopy(chunki.chunk, 0, res, pos, chunki.size);
            pos += chunki.size;
        }
        assertArrayEquals("should give same data from all chunks", data, res);
    }
}
