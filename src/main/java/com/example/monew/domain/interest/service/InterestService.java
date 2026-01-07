package com.example.monew.domain.interest.service;

import com.example.monew.domain.interest.dto.InterestDto;
import com.example.monew.domain.interest.dto.InterestRegisterRequest;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.Keyword;
import com.example.monew.domain.interest.repository.InterestRepository;
import com.example.monew.domain.interest.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterestService {

    private final InterestRepository interestRepository;
    private final KeywordRepository keywordRepository;

    public InterestDto create(InterestRegisterRequest request) {

        Interest interest = new Interest(request.name());
        Interest saved = interestRepository.save(interest);

        List<Keyword> keywords = request.keywords().stream()
                .map(key -> new Keyword(key, interest))
                .toList();

        keywordRepository.saveAll(keywords);

        return new InterestDto(
                saved.getId(),
                saved.getName(),
                keywords.stream().map(Keyword::getKeyword).toList(),
                0L,
                false
        );
    }
}
