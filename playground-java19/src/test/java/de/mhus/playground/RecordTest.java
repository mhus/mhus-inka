package de.mhus.playground;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

// https://dzone.com/articles/what-are-java-records

public class RecordTest {

    @Test
    public void testRecord() {
        TestRecord rec1 = new TestRecord("a", "b");
        System.out.println("Value1: " + rec1.value1);
        System.out.println("Value1: " + rec1.value1());
        System.out.println("Rec1: " + rec1);

        TestRecord rec2 = new TestRecord(rec1);
        System.out.println("Rec2: " + rec2);

        assertTrue( rec1.equals(rec2) );

    }

    @Test
    public void testWithInterface() {
        doInterface(new TestRecord("x", "y"));
    }

    public void doInterface(HasValue2 dings) {
        System.out.println("Interface: " + dings.value2());
    }

    interface HasValue2 {
        String value2();
    }

    record TestRecord(String value1, String value2) implements HasValue2 {
        public TestRecord(TestRecord clone) {
            this(clone.value1, clone.value2);
        }

        public void doSomething() {
            System.out.println("Do something");
        }

    }

}
