package cuhk.dede;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

/**
 * split a InputStream as chunks
 * Give them to the given handler to upload
 */
public class FileChunker
{
    private static final String hash_func = "SHA-1";
    private static final int checksum_hexlen = 40;

    public static void upload(InputStream raw_input, String uploaded_name,
            RabinChunker.Params params, Handler upload_handler) throws IOException {
        MetadataStore meta_store = new MetadataStore(MetadataStore.Mode.LOCAL);
        if (meta_store.fileExist(uploaded_name)) {
            throw new IOException("File already exist in the store");
        }

        BufferedInputStream in_stream = new BufferedInputStream(raw_input);
        RabinChunker chunker = new RabinChunker(in_stream, params);
        /*
         * TODO: make it general to handle remote store
         */

        MessageDigest sha1sum = null;
        try {
            sha1sum = MessageDigest.getInstance(hash_func);
        } catch(java.security.NoSuchAlgorithmException e) {
            System.err.printf("This java runtime does not support %s\n", hash_func);
            System.exit(1);
        }

        ByteArrayOutputStream all_checksums = new ByteArrayOutputStream();
        RabinChunker.ChunkInfo chunki;

        while ((chunki = chunker.nextChunk()) != null) {
            sha1sum.update(chunki.chunk, 0, chunki.size);
            byte[] chunk_checksum = sha1sum.digest();
            String hex_checksum = printHexBinary(chunk_checksum);
            if (meta_store.isnew(hex_checksum)){
                //System.out.printf("New chunk %s\n", new String(chunki.chunk));
                /* make new metadata record */
                meta_store.newChecksum(hex_checksum);
                /* upload the chunk using its checksum as name */
                upload_handler.upload(hex_checksum, chunki.chunk, chunki.size);
            } else {
                //System.out.printf("Depuplicated chunk %s\n", new String(chunki.chunk));
                meta_store.refCountIncr(hex_checksum);
            }

            all_checksums.write(hex_checksum.getBytes(Charset.forName("US-ASCII")));
        }

        meta_store.newFileRecord(uploaded_name, all_checksums.toByteArray());
        meta_store.commit();
        meta_store.close();
    }

    public static void download(OutputStream dest, String uploaded_name,
            Handler download_handler) throws IOException {
        MetadataStore meta_store = new MetadataStore(MetadataStore.Mode.LOCAL);
        if (!meta_store.fileExist(uploaded_name)) {
            throw new IOException("File not exist in store");
        }

        byte[] checksum_all = meta_store.getFileRecord(uploaded_name);
        byte[] buf = new byte[2048];

        for (int i=0; i < checksum_all.length; i += checksum_hexlen) {
            String nth_checksum = new String(checksum_all, i, checksum_hexlen, "US-ASCII");
            InputStream chunk_stream = download_handler.download(nth_checksum);

            int count;
            while ((count = chunk_stream.read(buf)) >= 0) {
                dest.write(buf, 0, count);
            }
        }

        meta_store.close();
    }
}