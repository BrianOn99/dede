package cuhk.dede;

import cuhk.dede.localBackend.CheckInit;
import cuhk.dede.localBackend.LocalHandler;

import java.lang.ClassLoader;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;

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
        String mode = args[6];

        try (BufferedInputStream in_stream =
                new BufferedInputStream(new FileInputStream(file_name))) {
            switch (mode) {
                case "local":
                    CheckInit.run();
                    try {
                        FileChunker.upload(in_stream, file_name, rabin_params, new LocalHandler());
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                        System.exit(1);
                    }
                    break;
                case "remote":
                    System.out.println("not implemented yet");
                    break;
                default:
                    usageExit("Error: storage not supported - " + mode, 1);
            }
        } catch (java.io.FileNotFoundException e) {
            System.err.printf("Error: file %s not found\n", file_name);
            System.exit(1);
        } catch (IOException e) {
            System.err.printf("Error: closing file: " + e.getMessage());
        }
    }

    /* For download/delete, try remote first.  If fail, try local.
     * If it fail agian, it really fail.
     */
    private static void download(String[] args) {
        checkArgLen(args, 2);

        String file_name = args[1];
        try (BufferedOutputStream out_stream
                = new BufferedOutputStream(new FileOutputStream(file_name), 1<<13)) {
            FileChunker.download(out_stream, file_name, new LocalHandler());
        } catch (java.io.FileNotFoundException e) {
            System.out.printf("Error: cannnot write file %s\n  " + e.getMessage() + "\n", file_name);
        } catch (IOException e) {
            System.err.printf("Error: %s\n", e.getMessage());
        }
    }

    private static void delete(String[] args) {
        checkArgLen(args, 2);

        String file_name = args[1];
        try {
            FileChunker.delete(file_name, new LocalHandler());
        } catch (IOException e) {
            System.err.printf("Error: %s\n", e.getMessage());
        }
    }

    private static void list(String[] args) {
        MetadataStore.Mode mode = args[1].equals("remote") ? MetadataStore.Mode.REMOTE :
                                                             MetadataStore.Mode.LOCAL;
        MetadataStore meta_store = new MetadataStore(mode);
        for (String file: meta_store.listFile()) {
            System.out.println(file);
        }
        meta_store.close();
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
