package com.example.monew.domain.interest.mapper;

import com.example.monew.domain.interest.dto.InterestDto;
import com.example.monew.domain.interest.entity.Interest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InterestMapper {

    public InterestDto toDto(Interest interest, List<String> keywords) {
        return new InterestDto(
                interest.getId(),
                interest.getName(),
                keywords,
                0L,
                false
        );
    }
}
