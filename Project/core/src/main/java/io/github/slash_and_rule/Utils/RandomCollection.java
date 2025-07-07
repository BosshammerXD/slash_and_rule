package io.github.slash_and_rule.Utils;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Random;

public class RandomCollection<E> {
    public static class weightedValue<T> {
        public final double weight;
        public final T value;

        public weightedValue(double weight, T value) {
            this.weight = weight;
            this.value = value;
        }

        public static <T> weightedValue<T>[] of(double[] weights, T[] values) {
            if (weights.length != values.length) {
                throw new IllegalArgumentException("Weights and values must have the same length");
            }
            @SuppressWarnings("unchecked")
            weightedValue<T>[] result = new weightedValue[weights.length];
            for (int i = 0; i < weights.length; i++) {
                result[i] = new weightedValue<>(weights[i], values[i]);
            }
            return result;
        }
    }

    private final NavigableMap<Double, E> map = new TreeMap<Double, E>();
    private final Random random;
    private double total = 0;

    public RandomCollection() {
        this(new Random());
    }

    public RandomCollection(Random random) {
        this.random = random;
    }

    public RandomCollection<E> add(double weight, E result) {
        if (weight <= 0)
            return this;
        total += weight;
        map.put(total, result);
        return this;
    }

    public RandomCollection<E> add(weightedValue<E> weightedValue) {
        return add(weightedValue.weight, weightedValue.value);
    }

    public E next() {
        double value = random.nextDouble() * total;
        return map.higherEntry(value).getValue();
    }
}
