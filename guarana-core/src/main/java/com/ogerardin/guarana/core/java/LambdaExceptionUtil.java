/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.java;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Handle checked exceptions in streams.
 * From https://stackoverflow.com/questions/27644361/how-can-i-throw-checked-exceptions-from-inside-java-8-streams
 *
 * @author oge
 * @since 11/07/2017
 */
public final class LambdaExceptionUtil {

    @FunctionalInterface
    public interface ConsumerWithExceptions<T, E extends Exception> {
        void accept(T t) throws E;
    }

    @FunctionalInterface
    public interface BiConsumerWithExceptions<T, U, E extends Exception> {
        void accept(T t, U u) throws E;
    }

    @FunctionalInterface
    public interface FunctionWithExceptions<T, R, E extends Exception> {
        R apply(T t) throws E;
    }

    @FunctionalInterface
    public interface SupplierWithExceptions<T, E extends Exception> {
        T get() throws E, IOException, ClassNotFoundException;
    }

    @FunctionalInterface
    public interface RunnableWithExceptions<E extends Exception> {
        void run() throws E;
    }

    /**
     * .forEach(rethrowConsumer(name -> System.out.println(Class.forName(name)))); or .forEach(rethrowConsumer(ClassNameUtil::println));
     */
    public static <T, E extends Exception> Consumer<T> rethrowingConsumer(ConsumerWithExceptions<T, E> consumer) throws E {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Exception exception) {
                throwActualException(exception);
            }
        };
    }

    public static <T, U, E extends Exception> BiConsumer<T, U> rethrowingBiConsumer(BiConsumerWithExceptions<T, U, E> biConsumer) throws E {
        return (t, u) -> {
            try {
                biConsumer.accept(t, u);
            } catch (Exception exception) {
                throwActualException(exception);
            }
        };
    }

    /**
     * .map(rethrowFunction(name -> Class.forName(name))) or .map(rethrowFunction(Class::forName))
     */
    public static <T, R, E extends Exception> Function<T, R> rethrowingFunction(FunctionWithExceptions<T, R, E> function) throws E {
        return t -> {
            try {
                return function.apply(t);
            } catch (Exception exception) {
                throwActualException(exception);
                return null;
            }
        };
    }

    /**
     * rethrowSupplier(() -> new StringJoiner(new String(new byte[]{77, 97, 114, 107}, "UTF-8"))),
     */
    public static <T, E extends Exception> Supplier<T> rethrowingSupplier(SupplierWithExceptions<T, E> function)  throws E {
        return () -> {
            try {
                return function.get();
            } catch (Exception exception) {
                throwActualException(exception);
                return null;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static <E extends Exception> void throwActualException(Exception exception) throws E {
        throw (E) exception;
    }

}
