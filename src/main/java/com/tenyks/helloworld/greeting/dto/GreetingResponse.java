package com.tenyks.helloworld.greeting.dto;

import com.tenyks.helloworld.greeting.domain.Greeting;
import java.time.Instant;

public record GreetingResponse(Long id, String name, String message, Instant createdAt) {

    public static GreetingResponse from(Greeting greeting) {
        return new GreetingResponse(
                greeting.getId(),
                greeting.getName(),
                greeting.getMessage(),
                greeting.getCreatedAt());
    }
}
