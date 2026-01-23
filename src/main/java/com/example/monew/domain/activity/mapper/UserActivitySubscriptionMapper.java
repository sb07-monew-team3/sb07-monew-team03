package com.example.monew.domain.activity.mapper;

import com.example.monew.domain.activity.dto.UserActivitySubscriptionDto;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.Keyword;
import com.example.monew.domain.interest.entity.Subscription;
import com.example.monew.domain.interest.repository.KeywordRepository;
import com.example.monew.domain.interest.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserActivitySubscriptionMapper {

    private final KeywordRepository keywordRepository;

    public UserActivitySubscriptionDto toUserActivitySubscriptionDto(Subscription subscription){

        Interest interest = subscription.getInterest();
        String[] keywords = keywordRepository.findAllByInterestId(interest.getId())
                .stream()
                .map(Keyword::getKeyword)
                .toArray(String[]::new);

        return new UserActivitySubscriptionDto(
                subscription.getId(),
                interest.getId(),
                interest.getName(),
                keywords,
                0,
                subscription.getCreatedAt()
        );
    }
}
