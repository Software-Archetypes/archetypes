package com.bartslota.availability.application;

import org.springframework.scheduling.annotation.Scheduled;

import com.bartslota.availability.domain.AssetAvailabilityRepository;

class OverdueLockHandling {

    private final AvailabilityService availabilityService;
    private final AssetAvailabilityRepository repository;

    OverdueLockHandling(AvailabilityService availabilityService, AssetAvailabilityRepository repository) {
        this.availabilityService = availabilityService;
        this.repository = repository;
    }

    @Scheduled(fixedDelay = 10)
    void unlockOverdue() {
        repository
                .findOverdue()
                .forEach(availabilityService::unlockIfOverdue);
    }
}
