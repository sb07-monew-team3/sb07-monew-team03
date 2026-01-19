package com.example.monew.domain.article.dto;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Sort.Direction;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ArticleRequestDto(
        String keyword,
        UUID interestId,

        @NotEmpty(message="출처는 NAVER, HANKYUNG, CHOSUN, YEONHAP 중 적어도 하나를 입력해주세요.")
        @Size(min=1, max=4, message="출처는 최대 4개까지 선택 가능합니다.")
        List<Source> sourceIn,

        LocalDateTime publishDateFrom,
        LocalDateTime publishDateTo,

        @NotBlank(message = "정렬은 publishDate, commentCount, viewCount 중 하나를 입력해주세요.")
        @Pattern(regexp = "publishDate|commentCount|viewCount",
                message = "정렬 필드는 publishDate, commentCount, viewCount만 허용됩니다.")
        String orderBy,

        @NotBlank(message = "정렬 방향은 DESC, ASC중 하나를 입력해주세요.")
        @Pattern(regexp = "ASC|DESC",
                message = "정렬 방향은 ASC 또는 DESC만 허용됩니다.")
        String direction,

        String cursor,
        Instant after,

        @Positive(message="limit은 1 이상이어야 합니다.")
        Integer limit
) {
    public Order getOrder() {
        return Order.forValue(orderBy);
    }

    public Direction getDirection() {
        return Direction.fromString(direction);
    }
}
