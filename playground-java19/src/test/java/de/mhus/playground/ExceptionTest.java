package de.mhus.playground;

import org.junit.jupiter.api.Test;

public class ExceptionTest {

    @Test
    public void testThrowRuntimeException() {
        try {
            throw new RuntimeException("Test");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
