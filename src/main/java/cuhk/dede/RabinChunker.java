package cuhk.dede;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * split a InputStream as chunks
 * It will read file in byte, so better give it BufferedInputSteam
 */
public class RabinChunker
{
    /*
     * A struct to groups parameters together
     */
    public static class Params
    {
        public int winSize;
        public int maxSize;
        public int minSize;
        public int avgSize;
        public int prime;

        public Params(int winSize, int maxSize, int minSize, int avgSize, int prime) {
            /*
             * window size must be at least minimum chunk size, otherwise the rabin
             * fingerprint does not only depends on the chunk.
             * In real world, it will not happen.  But this is an assignment.
             */
            if (winSize < minSize)
                winSize = minSize;
            if (maxSize < minSize) {
                System.err.println("max chunck size is smaller than min chunk size. " +
                                   "Increasing max chunk size");
                maxSize = minSize * 4;
            }
            this.winSize = winSize;
            this.maxSize = maxSize;
            this.minSize = minSize;
            this.avgSize = avgSize;
            this.prime = prime;
        }
    }

    public static class ChunkInfo
    {
        public int size;
        public byte[] chunk;

        public ChunkInfo(int size, byte[] chunk) {
            this.size = size;
            this.chunk = chunk;
        }
    }

    private Params params;
    private InputStream filein;
    private int avgSize_1;
    private byte[] chunk;
    private int currSize;
    // fpt means fingerprint, not file pointer, not File Transfer Protocol
    private int fpt;
    private int modmask;

    public RabinChunker(InputStream filein, Params params) {
        this.filein = filein;
        this.params = params;

        if ((params.avgSize & (params.avgSize - 1)) == 0) {
            this.avgSize_1 = params.avgSize - 1;
        } else {
            throw new RuntimeException("avgSize not power of 2");
        }

        this.chunk = new byte[params.maxSize];
        this.currSize = 0;
        this.fpt = 0;
        this.modmask = (1 << 29) - 1;
    }

    private boolean goodfpt() {
        return (fpt & avgSize_1) == 1;
    }

    /*
     * calculate num^pow % mod
     */
    public static int modExp(int num, int pow, int mod) {
        int answer = 1;
        for (; pow > 0; pow >>= 1) {
            if ((pow & 1) != 0)
                answer = (answer*num) % mod;
            num = (num*num) % mod;
        }
        return answer;
    }

    public ChunkInfo nextChunk() throws IOException {
        int modExpPrime = modExp(params.prime, params.winSize, modmask+1);
        for (int ch; (ch = filein.read()) != -1;) {
            chunk[currSize++] = (byte)ch;
            /*
             * If we are near the start of processing, or just have given out a chunk
             * skip subtracting the byte m index before
             */
            if (currSize > params.winSize)
                fpt -= chunk[currSize - (params.winSize+1)] * modExpPrime;

            fpt = fpt * params.prime + ch;
            fpt &= modmask;
            //System.out.printf("processed %s, now fingerprint %d, masked %d\n", ch, fpt, (fpt & avgSize_1));

            if ((currSize >= params.minSize) &&
                ((currSize >= params.maxSize) || goodfpt())
                ) {
                ChunkInfo retInfo = new ChunkInfo(currSize, chunk);
                chunk = new byte[params.maxSize];
                fpt = 0;
                currSize = 0;
                return retInfo;
            }
        }

        /* the file has end */
        if (currSize != 0) {
            ChunkInfo ret = new ChunkInfo(currSize, chunk);
            currSize = 0;
            return ret;
        } else {
            return null;
        }
    }
}
