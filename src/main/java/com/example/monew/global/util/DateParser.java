package com.example.monew.global.util;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateParser {
    public static LocalDateTime parse(String pubDate) {
        return ZonedDateTime
                .parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME)
                .toLocalDateTime();
    }
}
