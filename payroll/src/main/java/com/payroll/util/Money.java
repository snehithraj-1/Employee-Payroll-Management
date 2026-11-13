package com.payroll.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/** Money helpers. BigDecimal is used everywhere because double cannot represent rupees and paise exactly. */
public final class Money {
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final NumberFormat INR = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"));

    private Money() {
    }

    public static BigDecimal of(String value) {
        return round(new BigDecimal(value));
    }

    public static BigDecimal of(long value) {
        return round(BigDecimal.valueOf(value));
    }

    public static BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /** Returns pct% of amount, e.g. percent(50000, 20) = 10000.00 */
    public static BigDecimal percent(BigDecimal amount, double pct) {
        return amount.multiply(BigDecimal.valueOf(pct)).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    public static String format(BigDecimal value) {
        return value == null ? "-" : INR.format(value);
    }
}
