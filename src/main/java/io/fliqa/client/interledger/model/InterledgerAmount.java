package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class InterledgerAmount {

    public static final int DEFAULT_AMOUNT_SCALE = 2;

    /* The assetCode is a code that indicates the underlying asset. This SHOULD be an ISO4217 currency code. */
    @JsonProperty(value = "assetCode", required = true)
    public String assetCode;

    /* The scale of amounts denoted in the corresponding asset code. 0-255 */
    @JsonProperty(value = "assetScale", required = true)
    public int assetScale;

    /* The value is an unsigned 64-bit integer amount, represented as a string. */
    @JsonProperty(value = "value", required = true)
    public String amount;

    public BigDecimal asBigDecimal() {
        return new BigDecimal(amount).movePointLeft(assetScale);
    }

    /**
     * Builds an InterledgerAmount object by converting the provided BigDecimal amount into an Interledger
     * formatted amount (string representation) with a default scale of 2 and asset code.
     *
     * @param amount    the monetary value, must not be null
     * @param assetCode the asset code, must be a non-empty string with 3 characters (ISO4217 currency code)
     * @return InterledgerAmount instance representing the given parameters
     */
    public static InterledgerAmount build(BigDecimal amount, String assetCode) {
        return build(amount, assetCode, DEFAULT_AMOUNT_SCALE);
    }

    /**
     * Builds an InterledgerAmount object by converting the provided BigDecimal amount into an Interledger
     * formatted amount (string representation) with the specified scale and asset code.
     *
     * @param amount    the monetary value, must not be null
     * @param assetCode the asset code, must be a non-empty string with 3 characters (ISO4217 currency code)
     * @param scale     the scale/precision for the amount, must be between 0-255
     * @return InterledgerAmount instance representing the given parameters
     */
    public static InterledgerAmount build(BigDecimal amount, String assetCode, int scale) {

        if (amount == null) {
            throw new IllegalArgumentException("amount cannot be null or empty.");
        }
        if (assetCode == null || assetCode.isBlank()) {
            throw new IllegalArgumentException("assetCode cannot be null or empty.");
        }
        if (assetCode.length() != 3) {
            throw new IllegalArgumentException(String.format("assetCode must be 3 characters long / ISO4217 currency code, but was: '%s'.", assetCode));
        }

        InterledgerAmount out = new InterledgerAmount();
        out.assetCode = assetCode;
        out.amount = toInterledgerAmount(amount, scale);
        out.assetScale = scale;
        return out;
    }

    public static String toInterledgerAmount(BigDecimal amount, int amountScale) {
        BigDecimal out = amount.setScale(2, RoundingMode.HALF_UP).scaleByPowerOfTen(amountScale);

        DecimalFormat decimalFormat = new DecimalFormat("#");
        return decimalFormat.format(out);
    }

    public static String toInterledgerAmount(BigDecimal amount) {
        return toInterledgerAmount(amount, DEFAULT_AMOUNT_SCALE);
    }

    @Override
    public String toString() {
        return "InterledgerAmount{" +
                "assetCode='" + assetCode + '\'' +
                ", assetScale=" + assetScale +
                ", amount='" + amount + '\'' +
                '}';
    }
}
