package com.tenyks.helloworld.greeting.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenyks.helloworld.greeting.domain.Greeting;
import com.tenyks.helloworld.greeting.dto.GreetingResponse;
import com.tenyks.helloworld.greeting.dto.PageResponse;
import com.tenyks.helloworld.greeting.repository.GreetingRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@DataJpaTest
@Import(GreetingServiceImpl.class)
@DisplayName("GreetingServiceImpl.getGreetings (database-backed)")
class GreetingServiceImplPageDatabaseTest {

    private static final Instant BASE_TIME = Instant.parse("2026-01-15T10:30:00Z");

    @Autowired
    private GreetingRepository greetingRepository;

    @Autowired
    private GreetingService greetingService;

    @BeforeEach
    void seedGreetings() {
        greetingRepository.deleteAll();
        for (int i = 0; i < 5; i++) {
            greetingRepository.save(Greeting.builder()
                    .name("User" + i)
                    .message("Hello, User%d!".formatted(i))
                    .createdAt(BASE_TIME.plus(i, ChronoUnit.MINUTES))
                    .build());
        }
    }

    @Test
    @DisplayName("returns the requested page of greetings read from the database")
    void returnsRequestedPageFromDatabase() {
        Pageable pageable = PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "createdAt"));

        PageResponse<GreetingResponse> response = greetingService.getGreetings(pageable);

        assertThat(response.content())
                .extracting(GreetingResponse::name)
                .containsExactly("User2", "User3");
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(5);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.first()).isFalse();
        assertThat(response.last()).isFalse();
    }

    @Test
    @DisplayName("populates id, name, message and createdAt from the persisted greeting")
    void populatesAllGreetingFields() {
        Greeting persisted = greetingRepository
                .findAll(PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent()
                .getFirst();

        PageResponse<GreetingResponse> response = greetingService.getGreetings(
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt")));

        assertThat(response.content()).singleElement().satisfies(greeting -> {
            assertThat(greeting.id()).isEqualTo(persisted.getId());
            assertThat(greeting.name()).isEqualTo("User4");
            assertThat(greeting.message()).isEqualTo("Hello, User4!");
            assertThat(greeting.createdAt()).isEqualTo(BASE_TIME.plus(4, ChronoUnit.MINUTES));
        });
    }

    @Test
    @DisplayName("returns newest greetings first for the API default sort")
    void returnsNewestFirstForDefaultSort() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt", "id"));

        PageResponse<GreetingResponse> response = greetingService.getGreetings(pageable);

        assertThat(response.content())
                .extracting(GreetingResponse::name)
                .containsExactly("User4", "User3", "User2", "User1", "User0");
        assertThat(response.totalElements()).isEqualTo(5);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isTrue();
    }

    @Test
    @DisplayName("covers every persisted greeting exactly once when paging through results")
    void pagesThroughAllGreetingsWithoutOverlap() {
        Sort oldestFirst = Sort.by(Sort.Direction.ASC, "createdAt", "id");

        PageResponse<GreetingResponse> first = greetingService.getGreetings(PageRequest.of(0, 2, oldestFirst));
        PageResponse<GreetingResponse> second = greetingService.getGreetings(PageRequest.of(1, 2, oldestFirst));
        PageResponse<GreetingResponse> third = greetingService.getGreetings(PageRequest.of(2, 2, oldestFirst));

        assertThat(first.content()).extracting(GreetingResponse::name).containsExactly("User0", "User1");
        assertThat(second.content()).extracting(GreetingResponse::name).containsExactly("User2", "User3");
        assertThat(third.content()).extracting(GreetingResponse::name).containsExactly("User4");
        assertThat(third.last()).isTrue();
    }
}
