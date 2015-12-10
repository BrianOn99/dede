package cuhk.dede;

import java.io.InputStream;

public interface Handler {
    public void upload(String name, byte[] content, int size);
    public InputStream download(String name);
}
