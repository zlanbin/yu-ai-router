package com.yupi.airouter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MainApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
            void ThreadSyn() {
        ThreadSyn threadSynOne = new ThreadSyn();
        Thread thread1 = new Thread(threadSynOne, "ThreadSynOne");
        Thread thread2 = new Thread(threadSynOne, "ThreadSynTwo");
        thread1.start();
        thread2.start();
    }
}
