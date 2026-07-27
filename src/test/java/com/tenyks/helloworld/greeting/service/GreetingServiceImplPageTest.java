package com.tenyks.helloworld.greeting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenyks.helloworld.greeting.domain.Greeting;
import com.tenyks.helloworld.greeting.dto.GreetingResponse;
import com.tenyks.helloworld.greeting.dto.PageResponse;
import com.tenyks.helloworld.greeting.repository.GreetingRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
@DisplayName("GreetingServiceImpl.getGreetings")
class GreetingServiceImplPageTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-15T10:30:00Z");

    @Mock
    private GreetingRepository greetingRepository;

    @InjectMocks
    private GreetingServiceImpl greetingService;

    @Test
    @DisplayName("delegates the requested pageable to the repository")
    void delegatesPageableToRepository() {
        Pageable pageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(greetingRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        greetingService.getGreetings(pageable);

        verify(greetingRepository).findAll(pageable);
    }

    @Test
    @DisplayName("maps persisted greetings into the page response envelope")
    void mapsGreetingsIntoPageResponse() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<Greeting> page = new PageImpl<>(
                List.of(greeting(1L, "Ada"), greeting(2L, "Grace")), pageable, 5);
        when(greetingRepository.findAll(pageable)).thenReturn(page);

        PageResponse<GreetingResponse> response = greetingService.getGreetings(pageable);

        assertThat(response.content()).containsExactly(
                new GreetingResponse(1L, "Ada", "Hello, Ada!", CREATED_AT),
                new GreetingResponse(2L, "Grace", "Hello, Grace!", CREATED_AT));
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(5);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isFalse();
    }

    @Test
    @DisplayName("returns an empty page when no greetings exist")
    void returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(greetingRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        PageResponse<GreetingResponse> response = greetingService.getGreetings(pageable);

        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isZero();
        assertThat(response.totalPages()).isZero();
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isTrue();
    }

    private static Greeting greeting(long id, String name) {
        return Greeting.builder()
                .id(id)
                .name(name)
                .message("Hello, %s!".formatted(name))
                .createdAt(CREATED_AT)
                .build();
    }
}
