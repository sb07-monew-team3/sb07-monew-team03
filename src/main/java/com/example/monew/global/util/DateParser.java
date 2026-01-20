package com.example.monew.global.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateParser {
    public static LocalDateTime parse(String pubDate) {
        return ZonedDateTime
                .parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME)
                .toLocalDateTime();
    }

    public static LocalDateTime parse(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
