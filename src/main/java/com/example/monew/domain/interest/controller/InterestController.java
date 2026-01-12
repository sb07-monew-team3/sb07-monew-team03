package com.example.monew.domain.interest.controller;

import com.example.monew.domain.interest.dto.InterestDto;
import com.example.monew.domain.interest.dto.InterestRegisterRequest;
import com.example.monew.domain.interest.dto.InterestUpdateRequest;
import com.example.monew.domain.interest.dto.SubscriptionDto;
import com.example.monew.domain.interest.service.InterestService;
import com.example.monew.domain.interest.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;
    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<InterestDto> registerInterest(@RequestBody InterestRegisterRequest request) {
        InterestDto interestResponse = interestService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(interestResponse);
    }

    @PatchMapping("/{interestId}")
    public ResponseEntity<InterestDto> update(
            @PathVariable UUID interestId,
            @RequestBody InterestUpdateRequest request) {
        InterestDto interestResponse = interestService.update(interestId, request);
        return ResponseEntity.ok(interestResponse);
    }

    @DeleteMapping("/{interestId}")
    public ResponseEntity<Void> hardDelete(@PathVariable UUID interestId) {
        interestService.delete(interestId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<InterestDto>> search(
            @RequestParam(required = false) String keyword,
            @RequestHeader("Monew-Request-User-ID") UUID userId) {
        List<InterestDto> interests = interestService.search(keyword, userId);
        return ResponseEntity.ok(interests);
    }

    @PostMapping("/{interestId}/subscriptions")
    public ResponseEntity<SubscriptionDto> subscribe(
            @PathVariable UUID interestId,
            @RequestHeader("Monew-Request-User-ID") UUID userId) {
        SubscriptionDto subscribe = subscriptionService.subscribe(interestId, userId);
        return ResponseEntity.ok(subscribe);
    }

    @DeleteMapping("/{interestId}/subscriptions")
    public ResponseEntity<Void> unsubscribe(
            @PathVariable UUID interestId,
            @RequestHeader("Monew-Request-User-ID") UUID userId) {
        subscriptionService.unsubscribe(interestId, userId);
        return ResponseEntity.noContent().build();
    }
}
