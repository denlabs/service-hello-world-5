package com.tenyks.helloworld.greeting;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tenyks.helloworld.greeting.domain.Greeting;
import com.tenyks.helloworld.greeting.repository.GreetingRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("GET /greetings (integration)")
class GreetingPageIntegrationTest {

    private static final Instant BASE_TIME = Instant.parse("2026-01-15T10:30:00Z");

    @Autowired
    private MockMvc mockMvc;

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
    @DisplayName("returns the newest greetings first by default")
    void returnsNewestFirstByDefault() throws Exception {
        mockMvc.perform(get("/greetings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.content[0].name").value("User4"))
                .andExpect(jsonPath("$.content[4].name").value("User0"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @DisplayName("returns id, name, message and createdAt for each greeting")
    void returnsAllGreetingFields() throws Exception {
        Long newestId = greetingRepository
                .findAll(PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt", "id")))
                .getContent()
                .getFirst()
                .getId();

        mockMvc.perform(get("/greetings").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(newestId))
                .andExpect(jsonPath("$.content[0].name").value("User4"))
                .andExpect(jsonPath("$.content[0].message").value("Hello, User4!"))
                .andExpect(jsonPath("$.content[0].createdAt")
                        .value(BASE_TIME.plus(4, ChronoUnit.MINUTES).toString()));
    }

    @Test
    @DisplayName("honours page, size and sort request parameters")
    void honoursPageSizeAndSortParameters() throws Exception {
        mockMvc.perform(get("/greetings")
                        .param("page", "1")
                        .param("size", "2")
                        .param("sort", "createdAt,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("User2"))
                .andExpect(jsonPath("$.content[1].name").value("User3"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    @DisplayName("caps an oversized page size request at the configured maximum")
    void capsOversizedPageSize() throws Exception {
        mockMvc.perform(get("/greetings").param("size", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100))
                .andExpect(jsonPath("$.content.length()").value(5));
    }

    @Test
    @DisplayName("returns an empty page beyond the last page of results")
    void returnsEmptyPageBeyondResults() throws Exception {
        mockMvc.perform(get("/greetings").param("page", "9").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.last").value(true));
    }
}
