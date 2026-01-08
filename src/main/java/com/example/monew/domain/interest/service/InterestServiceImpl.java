package com.example.monew.domain.interest.service;

import com.example.monew.domain.interest.dto.InterestDto;
import com.example.monew.domain.interest.dto.InterestRegisterRequest;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.Keyword;
import com.example.monew.domain.interest.mapper.InterestMapper;
import com.example.monew.domain.interest.repository.InterestRepository;
import com.example.monew.domain.interest.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InterestServiceImpl implements InterestService {

    private final InterestRepository interestRepository;
    private final KeywordRepository keywordRepository;
    private final InterestMapper interestMapper;

    public InterestDto create(InterestRegisterRequest request) {

        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("관심사 이름은 필수입니다.");
        }

        if(interestRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("유사한 이름의 관심사가 이미 존재합니다.");
        }

        checkDuplicateName(request.name());

        Interest interest = new Interest(request.name());
        Interest saved = interestRepository.save(interest);

        List<Keyword> keywords = request.keywords().stream()
                .map(key -> new Keyword(key, saved))
                .toList();

        keywordRepository.saveAll(keywords);

        return interestMapper.toDto(saved, request.keywords());
    }

    private void checkDuplicateName(String newName) {
        List<String> nameList = interestRepository.findAllNames();

        Optional<String> similarName = nameList.stream()
                .filter(name -> calculateSimilarity(name, newName) >= 0.8)
                .findFirst();

        if(similarName.isPresent()){
            throw new IllegalArgumentException("유사한 이름의 관심사가 이미 존재합니다.");
        }
    }

    private double calculateSimilarity(String name1, String name2){
        LevenshteinDistance distance = new LevenshteinDistance();
        int applied = distance.apply(name1, name2);
        int maxLength = Math.max(name1.length(), name2.length());

        return 1.0 - (double) applied / maxLength;
    }

}
