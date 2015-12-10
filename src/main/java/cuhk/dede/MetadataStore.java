package cuhk.dede;

import java.io.IOException;
import java.io.File;
import java.util.AbstractMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

public class MetadataStore
{
    public static final String db_name = "mydedup.meta";
    private DB db;
    private AbstractMap<String, byte[]> checksum_store;
    private AbstractMap<String, Integer> refcount_store;

    public enum Mode { LOCAL, REMOTE }

    public MetadataStore(Mode mode) {
        db = DBMaker
                 .fileDB(new File(db_name))
                 .fileMmapEnableIfSupported() // enable mmap on supported platforms
                 .fileMmapCleanerHackEnable() // closes file on DB.close()
                 .make();

        checksum_store = db.treeMapCreate("checksum-" + mode.toString())
                 .keySerializer(Serializer.STRING)
                 .valueSerializer(Serializer.BYTE_ARRAY)
                 .makeOrGet();
        refcount_store = db.treeMapCreate("refcount-" + mode.toString())
                 .keySerializer(Serializer.STRING_ASCII)
                 .valueSerializer(Serializer.INTEGER)
                 .makeOrGet();
    }

    /*
     * Report if a chunk represented by its checksum is new (does not exist)
     */
    public boolean isnew(String checksum) {
        return refcount_store.containsKey(checksum);
    }

    public void newChecksum(String checksum) {
        refcount_store.put(checksum, 1);
    }

    public void newFileRecord(String name, byte[] checksums) {
        checksum_store.put(name, checksums);
    }
}
