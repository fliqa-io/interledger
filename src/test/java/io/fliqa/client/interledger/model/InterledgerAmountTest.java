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
package io.fliqa.client.interledger.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InterledgerAmountTest {

    @Test
    void buildAmount() {
        InterledgerAmount amount = InterledgerAmount.build(BigDecimal.valueOf(12.3456), "EUR");

        assertEquals("1235", amount.amount);
        assertEquals(2, amount.assetScale);
        assertEquals("EUR", amount.assetCode);
        assertEquals(BigDecimal.valueOf(12.35), amount.asBigDecimal());
    }

    @Test
    void buildAmountWithLargeValue() {
        InterledgerAmount amount = InterledgerAmount.build(new BigDecimal("12345678901234567890.12345"), "USD");

        assertEquals("1234567890123456789012", amount.amount);
        assertEquals(2, amount.assetScale);
        assertEquals("USD", amount.assetCode);
        assertEquals(new BigDecimal("12345678901234567890.12"), amount.asBigDecimal());
    }

    @Test
    void buildAmountWithInvalidInput() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                InterledgerAmount.build(null, "EUR"));

        assertEquals("amount cannot be null.", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () ->
                InterledgerAmount.build(BigDecimal.valueOf(1.23), null)
        );
        assertEquals("assetCode cannot be null or empty.", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () ->
                InterledgerAmount.build(BigDecimal.valueOf(1.23), "EU")
        );
        assertEquals("assetCode must be 3 characters long / ISO4217 currency code, but was: 'EU'.", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () ->
                InterledgerAmount.build(BigDecimal.valueOf(1.23), "EURO")
        );
        assertEquals("assetCode must be 3 characters long / ISO4217 currency code, but was: 'EURO'.", exception.getMessage());
    }
}