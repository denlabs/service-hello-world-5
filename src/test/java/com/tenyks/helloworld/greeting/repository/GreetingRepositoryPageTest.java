package com.tenyks.helloworld.greeting.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenyks.helloworld.greeting.domain.Greeting;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@DataJpaTest
@DisplayName("GreetingRepository.findAll(Pageable)")
class GreetingRepositoryPageTest {

    private static final Instant BASE_TIME = Instant.parse("2026-01-15T10:30:00Z");

    @Autowired
    private GreetingRepository greetingRepository;

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
    @DisplayName("returns a single slice of persisted greetings with page metadata")
    void returnsRequestedSliceFromDatabase() {
        Pageable pageable = PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "createdAt"));

        Page<Greeting> page = greetingRepository.findAll(pageable);

        assertThat(page.getContent())
                .extracting(Greeting::getName)
                .containsExactly("User2", "User3");
        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getSize()).isEqualTo(2);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.isFirst()).isFalse();
        assertThat(page.isLast()).isFalse();
    }

    @Test
    @DisplayName("returns every persisted field of a stored greeting")
    void returnsPersistedGreetingFields() {
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Greeting> page = greetingRepository.findAll(pageable);

        assertThat(page.getContent()).singleElement().satisfies(greeting -> {
            assertThat(greeting.getId()).isNotNull().isPositive();
            assertThat(greeting.getName()).isEqualTo("User4");
            assertThat(greeting.getMessage()).isEqualTo("Hello, User4!");
            assertThat(greeting.getCreatedAt()).isEqualTo(BASE_TIME.plus(4, ChronoUnit.MINUTES));
        });
    }

    @Test
    @DisplayName("orders results by the requested sort across pages")
    void ordersResultsAcrossPages() {
        Sort newestFirst = Sort.by(Sort.Direction.DESC, "createdAt", "id");

        Page<Greeting> firstPage = greetingRepository.findAll(PageRequest.of(0, 3, newestFirst));
        Page<Greeting> secondPage = greetingRepository.findAll(PageRequest.of(1, 3, newestFirst));

        assertThat(firstPage.getContent())
                .extracting(Greeting::getName)
                .containsExactly("User4", "User3", "User2");
        assertThat(firstPage.isFirst()).isTrue();
        assertThat(secondPage.getContent())
                .extracting(Greeting::getName)
                .containsExactly("User1", "User0");
        assertThat(secondPage.isLast()).isTrue();
    }
}
