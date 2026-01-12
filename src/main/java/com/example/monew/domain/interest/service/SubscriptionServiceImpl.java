package com.example.monew.domain.interest.service;

import com.example.monew.domain.interest.dto.SubscriptionDto;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.Keyword;
import com.example.monew.domain.interest.entity.Subscription;
import com.example.monew.domain.interest.mapper.SubscriptionMapper;
import com.example.monew.domain.interest.repository.InterestRepository;
import com.example.monew.domain.interest.repository.KeywordRepository;
import com.example.monew.domain.interest.repository.SubscriptionRepository;
import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.repository.UserRepository;
import com.example.monew.global.exception.domain.user.UserNotExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService{

    private final SubscriptionRepository subscriptionRepository;
    private final InterestRepository interestRepository;
    private final UserRepository userRepository;
    private final KeywordRepository keywordRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Override
    public SubscriptionDto subscribe(UUID interestId, UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotExistException(userId));

        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "관심사가 없습니다."));

        Subscription subscription = new Subscription(interest, user);
        Subscription saved = subscriptionRepository.save(subscription);

        List<Keyword> keywordList = keywordRepository.findByInterest(interest);
        List<String> keywords = keywordList.stream()
                .map(keyword -> keyword.getKeyword())
                .toList();

        Long count = subscriptionRepository.countByInterestId(interestId);

        return subscriptionMapper.toDto(saved, keywords, count);
    }

    @Override
    public void unsubscribe(UUID interestId, UUID userId) {
        Subscription subscription = subscriptionRepository.findSubscription(userId, interestId)
                .orElseThrow(() -> new IllegalArgumentException("구독이 없습니다."));

        subscriptionRepository.delete(subscription);
    }
}
