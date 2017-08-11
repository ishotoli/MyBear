package io.mybear.common;

import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowingConsumer<T> extends Consumer<T> {

    @Override
    default void accept(final T elem) {
        try {
            acceptThrows(elem);
        } catch (final Exception e) {
            // Implement your own exception handling logic here..
            // For example:
            System.out.println("handling an exception...");
            e.printStackTrace();
            // Or ...
            throw new RuntimeException(e);
        }
    }

    void acceptThrows(T elem) throws Exception;

}