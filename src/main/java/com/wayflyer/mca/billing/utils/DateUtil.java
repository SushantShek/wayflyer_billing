package com.wayflyer.mca.billing.utils;

import java.time.LocalDate;

public class DateUtil {
    public static LocalDate today() {
        return LocalDate.now();
    }

    public static LocalDate backDate(long days) {
        return LocalDate.now().minusDays(days);
    }

    public static LocalDate futureDate(long days) {
        return LocalDate.now().plusDays(days);
    }
}
