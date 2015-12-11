package cuhk.dede;

import cuhk.dede.localBackend.CheckInit;
import cuhk.dede.localBackend.LocalHandler;
import cuhk.dede.remoteBackend.RemoteHandler;

import java.lang.ClassLoader;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.File;

import java.io.IOException;

/**
 * command line interface
 */
public class Dede
{
    private static int default_win_size = 28;

    public static void main(String[] args) {
        if (args.length < 1)
            usageExit("Error: Incorrect number of arguments", 1);

        String action = args[0];
        switch (action) {
            case "upload":
                upload(args);
                break;
            case "download":
                download(args);
                break;
            case "delete":
                delete(args);
                break;
            case "list":
                list(args);
                break;
            default:
                usageExit("Error: Unknown action " + action, 1);
        }
    }

    private static void upload(String[] args) {
        checkArgLen(args, 7);

        RabinChunker.Params rabin_params = null;
        try {
            int min_chunk = Integer.parseInt(args[1]);
            int avg_chunk = Integer.parseInt(args[2]);
            int max_chunk = Integer.parseInt(args[3]);
            int prime = Integer.parseInt(args[4]);
            rabin_params = new RabinChunker.Params(
                    default_win_size, max_chunk, min_chunk, avg_chunk, prime);
        } catch (java.lang.NumberFormatException e) {
            usageExit("Error: non-number given as chunk size or prime d", 1);
        }

        /* The assignment specification is rubbish. the local and remote file
         * name are the same */
        String file_name = args[5];
        MetadataStore.Mode mode = getMode(args[6]);

        if (mode == MetadataStore.Mode.LOCAL) {
            CheckInit.run();
        }
        Handler handler = getHandler(args[6]);

        /* end setting up, try to upload */
        try (BufferedInputStream in_stream =
                new BufferedInputStream(new FileInputStream(file_name))) {
            try {
                new FileChunker(mode, handler).upload(in_stream, file_name, rabin_params);
            } catch (FileChunker.StoreException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        } catch (java.io.FileNotFoundException e) {
            System.err.printf("Error: file %s not found\n", file_name);
            System.exit(1);
        } catch (IOException e) {
            System.err.printf("Error: closing file: " + e.getMessage());
        }
    }

    /*
     * For download/delete, try remote first.  If fail, try local.
     * If it fail agian, it really fail.
     */
    private static void download(String[] args) {
        checkArgLen(args, 2);

        String file_name = args[1];
        boolean success = false;
        try (BufferedOutputStream out_stream
                = new BufferedOutputStream(new FileOutputStream(file_name), 1<<13)) {
            try {
                new FileChunker(MetadataStore.Mode.REMOTE, new RemoteHandler()).download(out_stream, file_name);
                return;
            } catch (FileChunker.StoreException e) {
                System.err.printf("Failed trying remote storage: %s\n", e.getMessage());
            }
            try {
                new FileChunker(MetadataStore.Mode.LOCAL, new LocalHandler()).download(out_stream, file_name);
                return;
            } catch (FileChunker.StoreException e) {
                System.err.printf("Failed trying local storage: %s\n", e.getMessage());
            }
        } catch (java.io.FileNotFoundException e) {
            System.out.printf("Error: cannnot write file %s\n  " + e.getMessage() + "\n", file_name);
        } catch (IOException e) {
            System.err.printf("Error closing file: %s\n", e.getMessage());
        }

        /* reaching here means all fail */
        System.err.println("Error: all attempts failed");
        new File(file_name).delete();
    }

    private static void delete(String[] args) {
        checkArgLen(args, 2);

        String file_name = args[1];
        try {
            new FileChunker(MetadataStore.Mode.REMOTE, new RemoteHandler()).delete(file_name);
            return;
        } catch (FileChunker.StoreException e) {
            System.err.printf("Failed trying remote storage: %s\n", e.getMessage());
        }
        try {
            new FileChunker(MetadataStore.Mode.LOCAL, new LocalHandler()).delete(file_name);
            return;
        } catch (FileChunker.StoreException e) {
            System.err.printf("Failed trying local storage: %s\n", e.getMessage());
        }
    }

    private static void list(String[] args) {
        MetadataStore meta_store = new MetadataStore(getMode(args[1]));
        for (String file: meta_store.listFile()) {
            System.out.println(file);
        }
        meta_store.close();
    }

    private static MetadataStore.Mode getMode(String str) {
        return str.equals("remote") ? MetadataStore.Mode.REMOTE :
                                      MetadataStore.Mode.LOCAL;
    }

    private static Handler getHandler(String str) {
        return str.equals("remote") ? null :
                                      new LocalHandler();
    }

    private static void checkArgLen(String[] args, int len) {
        if (args.length != len)
            usageExit("Error: Incorrect number of arguments", 1);
    }

    private static void usageExit(String msg, int code) {
        System.err.println(msg);
        System.err.println("Usage:\n"
                + "java program_name upload <min_chunk> <avg_chunk> <max_chunk> <prime d> <local_file_name> <local|remote>\n"
                + "java program_name download <remote_file_name>\n"
                + "java program_name delete <remote_file_name>\n");
        System.exit(code);
    }
}
