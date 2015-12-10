package cuhk.dede;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.security.MessageDigest;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

/**
 * split a InputStream as chunks
 * Give them to the given handler to upload
 */
public class FileUploadChunker
{
    private final String hash_func = "SHA-1";

    public FileUploadChunker(InputStream raw_input, RabinChunker.Params params, Handler upload_handler) throws IOException {
        BufferedInputStream in_stream = new BufferedInputStream(raw_input);
        RabinChunker chunker = new RabinChunker(in_stream, params);
        MessageDigest sha1sum = null;
        try {
            sha1sum = MessageDigest.getInstance(hash_func);
        } catch(java.security.NoSuchAlgorithmException e) {
            System.err.printf("This java runtime does not support %s\n", hash_func);
            System.exit(1);
        }

        RabinChunker.ChunkInfo chunki;
        while ((chunki = chunker.nextChunk()) != null) {
            sha1sum.update(chunki.chunk, 0, chunki.size);
            byte[] chunk_checksum = sha1sum.digest();

            /* upload the chunk using its checksum as name */
            upload_handler.upload(printHexBinary(chunk_checksum), chunki.chunk, chunki.size);
        }
    }
}
