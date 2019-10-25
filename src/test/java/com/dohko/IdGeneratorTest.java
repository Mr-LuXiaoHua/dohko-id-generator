package com.dohko;

import com.dohko.id.IdGenerator;
import org.junit.Test;


public class IdGeneratorTest {

    @Test
    public void testGenerateId() {
        int i = 0;
        long start = System.currentTimeMillis();
        while (i<1000) {
            System.out.println(IdGenerator.generateId());
            i++;
        }
        System.out.println(String.format("耗时: %d ms", (System.currentTimeMillis() - start)));
    }
}
