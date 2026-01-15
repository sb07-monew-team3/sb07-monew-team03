package com.example.monew.domain.interest.service;

import com.example.monew.domain.interest.dto.CursorPageResponseInterestDto;
import com.example.monew.domain.interest.dto.InterestDto;
import com.example.monew.domain.interest.dto.InterestRegisterRequest;
import com.example.monew.domain.interest.dto.InterestUpdateRequest;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.Keyword;
import com.example.monew.domain.interest.mapper.InterestMapper;
import com.example.monew.domain.interest.repository.InterestRepository;
import com.example.monew.domain.interest.repository.KeywordRepository;
import com.example.monew.domain.interest.repository.SubscriptionRepository;
import com.example.monew.global.exception.domain.interest.InterestDuplicateNameException;
import com.example.monew.global.exception.domain.interest.InterestNotExistException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InterestServiceImpl implements InterestService {

    private final InterestRepository interestRepository;
    private final KeywordRepository keywordRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InterestMapper interestMapper;

    @Override
    public InterestDto create(InterestRegisterRequest request) {

        validInterestName(request.name());
        checkDuplicateName(request.name());

        Interest interest = new Interest(request.name());
        Interest saved = interestRepository.save(interest);

        List<Keyword> keywords = request.keywords().stream()
                .map(key -> new Keyword(key, saved))
                .toList();

        keywordRepository.saveAll(keywords);

        return interestMapper.toDto(saved, request.keywords());
    }

    @Override
    public InterestDto update(UUID interestId, InterestUpdateRequest request) {
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new InterestNotExistException(interestId));

        keywordRepository.deleteByInterestId(interestId);

        List<Keyword> keywords = request.keywords().stream()
                .map(key -> new Keyword(key, interest))
                .toList();

        keywordRepository.saveAll(keywords);

        Long count = subscriptionRepository.countByInterestId(interestId);

        return interestMapper.toDto(interest, request.keywords(), count, null);
    }

    @Override
    public void delete(UUID interestId) {
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new InterestNotExistException(interestId));

        interestRepository.delete(interest);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseInterestDto search(String keyword, UUID userId, String orderBy,
                                                String direction, String cursor, Instant after, int limit) {
        List<Interest> interests = interestRepository.searchByInterestOrKeyword(
                keyword, orderBy, direction, cursor, after, limit);

        boolean hasNext = interests.size() > limit;

        List<Interest> interestList = hasNext ? interests.subList(0, limit) : interests;

        List<InterestDto> result = new ArrayList<>();

        for(Interest interest : interestList) {
            List<Keyword> keywordList = keywordRepository.findByInterest(interest);

            List<String> keywords = keywordList.stream()
                    .map(key -> key.getKeyword())
                    .toList();

            Long count = subscriptionRepository.countByInterestId(interest.getId());
            boolean subscribed = subscriptionRepository.isSubscribed(userId, interest.getId());

            InterestDto dto = interestMapper.toDto(interest, keywords, count, subscribed);

            result.add(dto);
        }

        String nextCursor = null;
        Instant nextAfter = null;

        if (hasNext && !result.isEmpty()) {
            InterestDto interestDto = result.get(result.size() - 1);
            Interest lastInterest = interestList.get(interestList.size() - 1);

            if ("name".equals(orderBy)) {
                nextCursor = interestDto.name();
            } else if ("subscriberCount".equals(orderBy)) {
                nextCursor = String.valueOf(interestDto.subscriberCount());
            }

            nextAfter = lastInterest.getCreatedAt();

        }
        return new CursorPageResponseInterestDto(
                result,
                nextCursor,
                nextAfter,
                result.size(),
                0L,
                hasNext
        );
    }

    private void validInterestName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("관심사 이름은 필수입니다.");
        }

        if(interestRepository.existsByName(name)) {
            throw new InterestDuplicateNameException(name);
        }
    }

    private void checkDuplicateName(String newName) {
        List<String> nameList = interestRepository.findAllNames();

        Optional<String> similarName = nameList.stream()
                .filter(name -> calculateSimilarity(name, newName) >= 0.8)
                .findFirst();

        if(similarName.isPresent()){
            throw new InterestDuplicateNameException(newName);
        }
    }

    private double calculateSimilarity(String name, String newName){
        LevenshteinDistance distance = new LevenshteinDistance();
        int applied = distance.apply(name, newName);
        int maxLength = Math.max(name.length(), newName.length());

        return 1.0 - (double) applied / maxLength;
    }

}
