package com.example.monew.domain.interest.service;

import com.example.monew.domain.interest.dto.InterestDto;
import com.example.monew.domain.interest.dto.InterestRegisterRequest;
import com.example.monew.domain.interest.dto.InterestUpdateRequest;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.Keyword;
import com.example.monew.domain.interest.entity.Subscription;
import com.example.monew.domain.interest.mapper.InterestMapper;
import com.example.monew.domain.interest.repository.InterestRepository;
import com.example.monew.domain.interest.repository.KeywordRepository;
import com.example.monew.domain.interest.repository.SubscriptionRepository;
import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.repository.UserRepository;
import com.example.monew.global.exception.domain.user.UserNotExistException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    private final UserRepository userRepository;
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "관심사가 없습니다."));

        keywordRepository.deleteByInterestId(interestId);

        List<Keyword> keywords = request.keywords().stream()
                .map(key -> new Keyword(key, interest))
                .toList();

        keywordRepository.saveAll(keywords);

        return interestMapper.toDto(interest, request.keywords());
    }

    @Override
    public void delete(UUID interestId) {
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "관심사가 없습니다."));

        interestRepository.delete(interest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterestDto> search(String keyword) {
        List<Interest> interests = interestRepository.searchByInterestOrKeyword(keyword);

        List<InterestDto> result = new ArrayList<>();

        for(Interest interest : interests) {
            List<Keyword> keywordList = keywordRepository.findByInterest(interest);

            List<String> keywords = keywordList.stream()
                    .map(key -> key.getKeyword())
                    .toList();

            InterestDto dto = interestMapper.toDto(interest, keywords);

            result.add(dto);
        }
        return result;
    }

    @Override
    public void subscribe(UUID userId, UUID interestId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotExistException(userId));

        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "관심사가 없습니다."));

        Subscription subscription = new Subscription(interest, user);

        subscriptionRepository.save(subscription);
    }

    @Override
    public void unsubscribe(UUID userId, UUID interestId) {
        Subscription subscription = subscriptionRepository.findSubscription(userId, interestId)
                .orElseThrow(() -> new IllegalArgumentException("구독이 없습니다."));

        subscriptionRepository.delete(subscription);
    }

    private void validInterestName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("관심사 이름은 필수입니다.");
        }

        if(interestRepository.existsByName(name)) {
            throw new IllegalArgumentException("유사한 이름의 관심사가 이미 존재합니다.");
        }
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

    private double calculateSimilarity(String name, String newName){
        LevenshteinDistance distance = new LevenshteinDistance();
        int applied = distance.apply(name, newName);
        int maxLength = Math.max(name.length(), newName.length());

        return 1.0 - (double) applied / maxLength;
    }

}
