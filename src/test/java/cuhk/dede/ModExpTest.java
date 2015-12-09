package cuhk.dede;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ModExpTest
{
    @Test
    public void testNum() {
        assertEquals("4^2 mod 11", 5, RabinChunker.modExp(4, 2, 11));
        assertEquals("44^128 mod 14", 4, RabinChunker.modExp(44, 128, 14));
        assertEquals("5^2 mod 2^29", 25, RabinChunker.modExp(5, 2, 1<<29));
        assertEquals("7^8 mod 2^29", 5764801, RabinChunker.modExp(7, 8, 1<<29));
    }
}
