package com.payroll.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/** Reusable input checks. All methods are null-safe. */
public final class InputValidator {
    private static final Pattern EMP_CODE = Pattern.compile("^[A-Z]{2,4}\\d{3,6}$");
    private static final Pattern NAME = Pattern.compile("^[A-Za-z][A-Za-z .'-]{1,99}$");
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern PHONE = Pattern.compile("^[6-9]\\d{9}$");

    private InputValidator() {
    }

    public static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public static boolean isValidEmpCode(String s) {
        return s != null && EMP_CODE.matcher(s).matches();
    }

    public static boolean isValidName(String s) {
        return s != null && NAME.matcher(s).matches();
    }

    public static boolean isValidEmail(String s) {
        return s != null && EMAIL.matcher(s).matches();
    }

    public static boolean isValidPhone(String s) {
        return s != null && PHONE.matcher(s).matches();
    }

    /** true if min <= value <= max */
    public static boolean inRange(BigDecimal value, long min, long max) {
        return value != null
                && value.compareTo(BigDecimal.valueOf(min)) >= 0
                && value.compareTo(BigDecimal.valueOf(max)) <= 0;
    }

    /** true if 0 < value <= max */
    public static boolean isPositive(BigDecimal value, long max) {
        return value != null && value.signum() > 0 && value.compareTo(BigDecimal.valueOf(max)) <= 0;
    }
}
