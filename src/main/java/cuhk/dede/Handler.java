package cuhk.dede;

public interface Handler {
    public void upload(String name, byte[] content, int size);
}
