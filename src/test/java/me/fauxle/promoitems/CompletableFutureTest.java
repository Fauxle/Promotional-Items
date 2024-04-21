package me.fauxle.promoitems;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompletableFutureTest {

    @Test
    void test() throws Exception {
        CompletableFuture.runAsync(() -> System.out.println("running async using the common thread pool!"));
        assertTrue(expensiveRandomInteger().get() > 0);
    }

    private CompletableFuture<Integer> expensiveRandomInteger() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return ThreadLocalRandom.current().nextInt(0, 100);
        });
    }

}
