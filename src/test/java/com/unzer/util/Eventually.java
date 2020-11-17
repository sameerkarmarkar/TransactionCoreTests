package com.unzer.util;
import org.awaitility.Awaitility;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.awaitility.core.Predicate;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class Eventually {

    public static <T> T get(Callable<T> callable) {
        return get(callable, 30);
    }

    public static <T> T get(Callable<T> callable, int timeoutSeconds) {
        return get(callable, 30, 1);
    }

    public static <T> T get(Callable<T> callable, int timeoutSeconds, int pollingInterval) {
        return Awaitility.await()
                .atMost(timeoutSeconds, TimeUnit.SECONDS)
                .pollInterval(pollingInterval, TimeUnit.SECONDS)
                .dontCatchUncaughtExceptions()
                .until(callable, isStringEmpty());
    }

    public static <T> List<T> waitForSize(Callable<List<T>> callable, int expectedSize) {
        return Awaitility.await()
                .atMost(60, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .dontCatchUncaughtExceptions()
                .until(callable, hasSize(expectedSize));
    }

    public static Predicate<Object> isStringEmpty() {
        return input -> !input.toString().isEmpty();
    }

    public static <T> T get(Callable<T> callable, Predicate<T> predicate) {
        return Awaitility.await()
                .atMost(30, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .dontCatchUncaughtExceptions()
                .until(callable, predicate);
    }

}
