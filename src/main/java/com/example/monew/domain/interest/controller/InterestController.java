package com.example.monew.domain.interest.controller;

import com.example.monew.domain.interest.docs.InterestControllerDocs;
import com.example.monew.domain.interest.dto.*;
import com.example.monew.domain.interest.service.InterestService;
import com.example.monew.domain.interest.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController implements InterestControllerDocs {

    private final InterestService interestService;
    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<InterestDto> registerInterest(@Valid @RequestBody InterestRegisterRequest request) {
        InterestDto interestResponse = interestService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(interestResponse);
    }

    @PatchMapping("/{interestId}")
    public ResponseEntity<InterestDto> update(
            @PathVariable UUID interestId,
            @Valid @RequestBody InterestUpdateRequest request) {
        InterestDto interestResponse = interestService.update(interestId, request);
        return ResponseEntity.ok(interestResponse);
    }

    @DeleteMapping("/{interestId}")
    public ResponseEntity<Void> hardDelete(@PathVariable UUID interestId) {
        interestService.delete(interestId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<CursorPageResponseInterestDto> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = true) String orderBy,
            @RequestParam(required = true) String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Instant after,
            @RequestParam(required = true) int limit,
            @RequestHeader("Monew-Request-User-ID") UUID userId) {
        CursorPageResponseInterestDto search = interestService.search(
                keyword, userId, orderBy, direction, cursor, after, limit);
        return ResponseEntity.ok(search);
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
        return ResponseEntity.ok().build();
    }
}
