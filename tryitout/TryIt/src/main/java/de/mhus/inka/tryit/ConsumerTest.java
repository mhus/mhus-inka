package de.mhus.inka.tryit;

import java.util.function.Consumer;

public class ConsumerTest {

    public static void main(String[] args) {
        new ConsumerTest().testIt();
    }

    private void testIt() {
        Consumer<String> c = this::consumer;
        System.out.println(c);
    }
    
    public void consumer(String in) {
        System.out.println(in);
    }
}
