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

public final class Assert {

    private Assert() {
    }

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
