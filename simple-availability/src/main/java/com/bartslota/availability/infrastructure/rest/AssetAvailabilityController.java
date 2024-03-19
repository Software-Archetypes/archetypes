package com.bartslota.availability.infrastructure.rest;

import java.security.Principal;
import java.time.Duration;
import java.util.function.Function;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bartslota.availability.application.AvailabilityService;
import com.bartslota.availability.commands.Activate;
import com.bartslota.availability.commands.Command;
import com.bartslota.availability.commands.Lock;
import com.bartslota.availability.commands.LockIndefinitely;
import com.bartslota.availability.commands.Register;
import com.bartslota.availability.commands.Unlock;
import com.bartslota.availability.commands.Withdraw;
import com.bartslota.availability.common.Result;
import com.bartslota.availability.domain.AssetId;
import com.bartslota.availability.domain.OwnerId;
import com.bartslota.availability.events.DomainEvent;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/api/assets/commands", consumes = APPLICATION_JSON_VALUE)
class AssetAvailabilityController {

    private final AvailabilityService availabilityService;

    AssetAvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping
    ResponseEntity<?> handle(@RequestBody Command command, Principal principal) {
        return switch (command) {
            case Register c -> handle(c);
            case Withdraw c -> handle(c);
            case Activate c -> handle(c);
            case Lock c -> handle(c, principal);
            case LockIndefinitely c -> handle(c, principal);
            case Unlock c -> handle(c, principal);
        };
    }

    private ResponseEntity<?> handle(Register command) {
        return execute(cmd -> availabilityService.registerAssetWith(AssetId.of(cmd.assetId())), command);
    }

    private ResponseEntity<?> handle(Withdraw command) {
        return execute(cmd -> availabilityService.withdraw(AssetId.of(cmd.assetId())), command);
    }

    private ResponseEntity<?> handle(Activate command) {
        return availabilityService
                .activate(AssetId.of(command.assetId()))
                .fold(rejected -> ResponseEntity.unprocessableEntity().build(),
                        accepted -> ResponseEntity.status(ACCEPTED).build());
    }

    private ResponseEntity<?> handle(Lock command, Principal user) {
        return availabilityService
                .lock(AssetId.of(command.assetId()), OwnerId.of(user.getName()), Duration.ofMinutes(command.durationInMinutes()))
                .fold(rejected -> ResponseEntity.unprocessableEntity().build(),
                        accepted -> ResponseEntity.status(ACCEPTED).build());
    }

    private ResponseEntity<?> handle(LockIndefinitely command, Principal user) {
        return availabilityService
                .lockIndefinitely(AssetId.of(command.assetId()), OwnerId.of(user.getName()))
                .fold(rejected -> ResponseEntity.unprocessableEntity().build(),
                        accepted -> ResponseEntity.status(ACCEPTED).build());
    }

    private ResponseEntity<?> handle(Unlock command, Principal user) {
        return availabilityService
                .lockIndefinitely(AssetId.of(command.assetId()), OwnerId.of(user.getName()))
                .fold(rejected -> ResponseEntity.unprocessableEntity().build(),
                        accepted -> ResponseEntity.status(ACCEPTED).build());
    }

    private <T extends Command> ResponseEntity<?> execute(Function<T, Result<? extends DomainEvent, ? extends DomainEvent>> function, T command) {
        return function
                .apply(command)
                .fold(rejected -> ResponseEntity.unprocessableEntity().build(),
                        accepted -> ResponseEntity.status(ACCEPTED).build());
    }
}
