package com.tenyks.helloworld.greeting.service;

import com.tenyks.helloworld.greeting.domain.Greeting;
import com.tenyks.helloworld.greeting.dto.CreateGreetingRequest;
import com.tenyks.helloworld.greeting.dto.GreetingResponse;
import com.tenyks.helloworld.greeting.dto.PageResponse;
import com.tenyks.helloworld.greeting.repository.GreetingRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GreetingServiceImpl implements GreetingService {

    private static final String MESSAGE_TEMPLATE = "Hello, %s!";

    private final GreetingRepository greetingRepository;

    @Override
    @Transactional
    public GreetingResponse createGreeting(CreateGreetingRequest request) {
        String name = request.name().trim();

        Greeting greeting = Greeting.builder()
                .name(name)
                .message(MESSAGE_TEMPLATE.formatted(name))
                .createdAt(Instant.now())
                .build();

        return GreetingResponse.from(greetingRepository.save(greeting));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GreetingResponse> getGreetings(Pageable pageable) {
        return PageResponse.from(greetingRepository.findAll(pageable).map(GreetingResponse::from));
    }
}
