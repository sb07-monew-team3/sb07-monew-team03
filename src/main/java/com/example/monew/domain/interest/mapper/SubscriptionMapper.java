package com.example.monew.domain.interest.mapper;

import com.example.monew.domain.interest.dto.SubscriptionDto;
import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.Subscription;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubscriptionMapper {

    public SubscriptionDto toDto(Subscription subscription,
                                 List<String> keywords,
                                 Long subscriberCount) {

        Interest interest = subscription.getInterest();

        return new SubscriptionDto(
                subscription.getId(),
                interest.getId(),
                interest.getName(),
                keywords,
                subscriberCount,
                subscription.getCreatedAt()
        );
    }
}
