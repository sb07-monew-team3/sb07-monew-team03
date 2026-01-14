package com.example.monew.domain.article.dto;
import org.springframework.data.domain.Sort.Direction;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ArticleRequestDto(
        String keyword,
        UUID interestId,
        List<Source> sourceIn,
        LocalDateTime publishDateFrom,
        LocalDateTime publishDateTo,
        String orderBy,
        String direction,
        String cursor,
        Instant after,
        Integer limit
) {
    public Order getOrder() {
        return Order.forValue(orderBy);
    }

    public Direction getDirection() {
        return Direction.fromString(direction);
    }

}
