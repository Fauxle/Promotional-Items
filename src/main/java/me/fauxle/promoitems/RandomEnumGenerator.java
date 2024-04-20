package me.fauxle.promoitems;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class RandomEnumGenerator<T extends Enum<T>> implements Supplier<T> {
    private final T[] values;

    public RandomEnumGenerator(Class<T> e) {
        values = e.getEnumConstants();
    }

    @Override
    public T get() {
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }
}