package com.group58.slideshowplayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import javafx.application.Platform;

/**
 * Shared helpers for tests that need the JavaFX toolkit.
 */
public final class FxTestHelper {
    // A valid 1x1 PNG image.
    private static final byte[] PNG = Base64.getDecoder().decode(
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");

    private FxTestHelper() {
    }

    /**
     * Starts the JavaFX toolkit once per test JVM.
     */
    public static void startToolkit() {
        try {
            Platform.startup(() -> { });
        }
        catch (IllegalStateException e) {
            // already started by another test class
        }
        Platform.setImplicitExit(false);
    }

    /**
     * Runs code on the JavaFX thread and waits for it to finish.
     * Any exception it throws is rethrown on the calling thread.
     */
    public static void runOnFx(Callable<?> action) {
        callOnFx(action);
    }

    /**
     * Runs code on the JavaFX thread, waits, and returns its result.
     */
    public static <T> T callOnFx(Callable<T> action) {
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                result.set(action.call());
            }
            catch (Throwable t) {
                error.set(t);
            }
            finally {
                done.countDown();
            }
        });
        try {
            if (!done.await(10, TimeUnit.SECONDS)) {
                throw new AssertionError("JavaFX task timed out");
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
        Throwable t = error.get();
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        }
        if (t instanceof Error) {
            throw (Error) t;
        }
        if (t != null) {
            throw new RuntimeException(t);
        }
        return result.get();
    }

    /**
     * Polls a condition until it is true or the timeout passes.
     *
     * @return true if the condition became true in time
     */
    public static boolean waitFor(BooleanSupplier condition, long timeoutMillis) {
        long end = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < end) {
            if (condition.getAsBoolean()) {
                return true;
            }
            try {
                Thread.sleep(20);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return condition.getAsBoolean();
    }

    /**
     * Writes a small valid PNG file.
     */
    public static Path writePng(Path file) throws IOException {
        return Files.write(file, PNG);
    }
}
