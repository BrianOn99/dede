package cuhk.dede.remoteBackend;

import java.io.InputStream;
import cuhk.dede.Handler;

import java.io.IOException;

public class RemoteHandler implements Handler
{
    public void upload(String name, byte[] content, int size) {
    }

    public InputStream download(String name) {
        return null;
    }

    public void delete(String name) {
    }
}
