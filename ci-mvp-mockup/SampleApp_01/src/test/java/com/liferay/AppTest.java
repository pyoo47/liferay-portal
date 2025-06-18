package com.liferay;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AppTest {

    @Test
    public void testApp() {
        assertEquals(1, 1); // dummy test

        // Capture output
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(out));
        
        App.main(null);  // call the main method
        
        String output = out.toString().trim();
        assertTrue(output.contains("Hello, Sample Java App 01!"));
        System.setOut(System.out); // reset output stream
        
    }
}
