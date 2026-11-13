package com.payroll.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Simplified Indian statutory deductions, for learning purposes only.
 * Income tax follows the new-regime slabs (FY 2025-26) with standard deduction,
 * the Section 87A rebate up to Rs 12 lakh and 4% cess. Marginal relief and surcharge are ignored.
 */
public final class TaxRules {
    private static final BigDecimal STANDARD_DEDUCTION = Money.of(75_000);
    private static final BigDecimal REBATE_LIMIT = Money.of(12_00_000);
    private static final BigDecimal PF_WAGE_CEILING = Money.of(15_000);
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);

    /** {upper limit of slab, rate %} */
    private static final long[][] SLABS = {
            {4_00_000, 0}, {8_00_000, 5}, {12_00_000, 10}, {16_00_000, 15},
            {20_00_000, 20}, {24_00_000, 25}, {Long.MAX_VALUE, 30}
    };

    private TaxRules() {
    }

    public static BigDecimal monthlyIncomeTax(BigDecimal monthlyGross) {
        BigDecimal taxable = monthlyGross.multiply(TWELVE).subtract(STANDARD_DEDUCTION).max(BigDecimal.ZERO);
        if (taxable.compareTo(REBATE_LIMIT) <= 0) {
            return Money.ZERO;
        }
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal lower = BigDecimal.ZERO;
        for (long[] slab : SLABS) {
            if (taxable.compareTo(lower) <= 0) {
                break;
            }
            BigDecimal upper = BigDecimal.valueOf(slab[0]);
            BigDecimal amountInSlab = taxable.min(upper).subtract(lower);
            tax = tax.add(Money.percent(amountInSlab, slab[1]));
            lower = upper;
        }
        tax = tax.add(Money.percent(tax, 4)); // health & education cess
        return tax.divide(TWELVE, 2, RoundingMode.HALF_UP);
    }

    /** Employee PF: 12% of basic, capped at the Rs 15,000 wage ceiling. */
    public static BigDecimal providentFund(BigDecimal basic) {
        return Money.percent(basic.min(PF_WAGE_CEILING), 12);
    }

    /** Andhra Pradesh professional tax slabs (monthly). */
    public static BigDecimal professionalTax(BigDecimal monthlyGross) {
        if (monthlyGross.compareTo(Money.of(15_000)) <= 0) return Money.ZERO;
        if (monthlyGross.compareTo(Money.of(20_000)) <= 0) return Money.of(150);
        return Money.of(200);
    }
}
