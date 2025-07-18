/*
 * Copyright 2025 Fliqa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fliqa.client.interledger.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Utility class providing assertion methods for input validation.
 *
 * <p>This class provides a collection of static assertion methods used throughout
 * the Interledger client for validating method parameters and preventing invalid
 * states. All assertion methods throw {@link IllegalArgumentException} by default
 * or can accept custom exception suppliers for specific error handling.
 *
 * <h2>Common Usage</h2>
 * <pre>{@code
 * public void processPayment(PaymentPointer wallet, BigDecimal amount) {
 *     Assert.notNull(wallet, "Wallet cannot be null");
 *     Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "Amount must be positive");
 * }
 * }</pre>
 *
 * @author Fliqa
 * @version 1.0
 * @since 1.0
 */
public final class Assert {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private Assert() {
    }

    /**
     * Asserts that a boolean condition is true.
     *
     * @param value   the boolean condition to check
     * @param message the error message if the condition is false
     * @throws IllegalArgumentException if the condition is false
     */
    public static void isTrue(boolean value, String message) {
        if (!value) {
            throw new IllegalArgumentException(message);
        }
    }

    public static <E extends Throwable> void isTrue(
            boolean value,
            Supplier<E> exceptionSupplier) throws E {
        if (!value) {
            throw exceptionSupplier.get();
        }
    }

    /**
     * Asserts that a boolean condition is false.
     *
     * @param value   the boolean condition to check
     * @param message the error message if the condition is true
     * @throws IllegalArgumentException if the condition is true
     */
    public static void isFalse(boolean value, String message) {
        if (value) {
            throw new IllegalArgumentException(message);
        }
    }

    public static <E extends Throwable> void isFalse(
            boolean value,
            Supplier<E> exceptionSupplier) throws E {
        if (value) {
            throw exceptionSupplier.get();
        }
    }

    /**
     * Asserts that an object is not null.
     *
     * @param value   the object to check
     * @param message the error message if the object is null
     * @throws IllegalArgumentException if the object is null
     */
    public static void notNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static <E extends Throwable> void notNull(
            Object value,
            Supplier<E> exceptionSupplier) throws E {
        if (value == null) {
            throw exceptionSupplier.get();
        }
    }

    /**
     * Asserts that a string is not null and not blank.
     *
     * @param value   the string to check
     * @param message the error message if the string is null or blank
     * @throws IllegalArgumentException if the string is null or blank
     */
    public static void notNullOrEmpty(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static <E extends Throwable> void notNullOrEmpty(
            String value,
            Supplier<E> exceptionSupplier) throws E {
        if (value == null || value.isBlank()) {
            throw exceptionSupplier.get();
        }
    }

    public static <K, V> void notNullOrEmpty(Map<K, V> value, String message) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static <E extends Throwable, T> void notNullOrEmpty(
            List<T> value,
            Supplier<E> exceptionSupplier) throws E {
        if (value == null || value.isEmpty()) {
            throw exceptionSupplier.get();
        }
    }

    public static <E extends Throwable, T> void notNullOrEmpty(
            Set<T> value,
            Supplier<E> exceptionSupplier) throws E {
        if (value == null || value.isEmpty()) {
            throw exceptionSupplier.get();
        }
    }
}
