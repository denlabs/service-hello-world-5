package com.tenyks.helloworld.greeting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenyks.helloworld.greeting.domain.Greeting;
import com.tenyks.helloworld.greeting.dto.CreateGreetingRequest;
import com.tenyks.helloworld.greeting.dto.GreetingResponse;
import com.tenyks.helloworld.greeting.repository.GreetingRepository;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GreetingServiceImpl.createGreeting (happy path)")
class GreetingServiceImplTest {

    @Mock
    private GreetingRepository greetingRepository;

    @InjectMocks
    private GreetingServiceImpl greetingService;

    @Test
    @DisplayName("generates the greeting message from the submitted name")
    void createGreetingGeneratesMessageFromName() {
        stubSaveWithGeneratedId(42L);

        GreetingResponse response = greetingService.createGreeting(new CreateGreetingRequest("Ada"));

        assertThat(response.name()).isEqualTo("Ada");
        assertThat(response.message()).isEqualTo("Hello, Ada!");
    }

    @Test
    @DisplayName("persists a greeting with the generated message and creation timestamp")
    void createGreetingPersistsGreeting() {
        stubSaveWithGeneratedId(1L);
        Instant before = Instant.now();

        greetingService.createGreeting(new CreateGreetingRequest("Grace"));

        ArgumentCaptor<Greeting> captor = ArgumentCaptor.forClass(Greeting.class);
        verify(greetingRepository).save(captor.capture());

        Greeting persisted = captor.getValue();
        assertThat(persisted.getId()).isNull();
        assertThat(persisted.getName()).isEqualTo("Grace");
        assertThat(persisted.getMessage()).isEqualTo("Hello, Grace!");
        assertThat(persisted.getCreatedAt()).isBetween(before, Instant.now());
    }

    @Test
    @DisplayName("returns the identifier and timestamp assigned during persistence")
    void createGreetingReturnsPersistedState() {
        Instant createdAt = Instant.parse("2026-01-15T10:30:00Z");
        when(greetingRepository.save(any(Greeting.class)))
                .thenReturn(Greeting.builder()
                        .id(99L)
                        .name("Alan")
                        .message("Hello, Alan!")
                        .createdAt(createdAt)
                        .build());

        GreetingResponse response = greetingService.createGreeting(new CreateGreetingRequest("Alan"));

        assertThat(response).isEqualTo(new GreetingResponse(99L, "Alan", "Hello, Alan!", createdAt));
    }

    @Test
    @DisplayName("trims surrounding whitespace from the submitted name")
    void createGreetingTrimsName() {
        stubSaveWithGeneratedId(5L);

        GreetingResponse response = greetingService.createGreeting(new CreateGreetingRequest("  Ada  "));

        assertThat(response.name()).isEqualTo("Ada");
        assertThat(response.message()).isEqualTo("Hello, Ada!");
    }

    private void stubSaveWithGeneratedId(long id) {
        when(greetingRepository.save(any(Greeting.class)))
                .thenAnswer(invocation -> {
                    Greeting greeting = invocation.getArgument(0);
                    return Greeting.builder()
                            .id(id)
                            .name(greeting.getName())
                            .message(greeting.getMessage())
                            .createdAt(greeting.getCreatedAt())
                            .build();
                });
    }
}
