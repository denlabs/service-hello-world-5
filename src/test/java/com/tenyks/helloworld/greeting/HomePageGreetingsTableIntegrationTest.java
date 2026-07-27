package com.tenyks.helloworld.greeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tenyks.helloworld.greeting.repository.GreetingRepository;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifies homepage submissions against the real greetings table, counting rows with plain SQL
 * before and after each request.
 *
 * <p>Covers AC-4 (blank submissions render a validation message, no greeting, and write no row)
 * and AC-6 (the valid name "Alice" flows through the existing {@code POST /greeting} service flow
 * and results in exactly one new row).
 */
@SpringBootTest
@AutoConfigureMockMvc
class HomePageGreetingsTableIntegrationTest {

    /** Physical table backing the greetings entity (Liquibase changeSet 001). */
    private static final String GREETINGS_TABLE = "greeting";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private GreetingRepository greetingRepository;

    @BeforeEach
    void clearGreetings() {
        greetingRepository.deleteAll();
    }

    @ParameterizedTest(name = "blank name [{0}] writes no row")
    @ValueSource(strings = {"", " ", "   ", "\t", "\n", " \t \n "})
    void blankOrWhitespaceOnlyNameRendersValidationMessageAndLeavesRowCountUnchanged(String name)
            throws Exception {
        seedExistingGreeting("Existing");
        long before = countGreetingRows();

        String body = mockMvc.perform(post("/").param("name", name))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).contains("id=\"validation-message\"");
        assertThat(body).doesNotContain("id=\"greeting\"");
        assertThat(body).doesNotContain("it is ");
        assertThat(countGreetingRows()).isEqualTo(before);
    }

    @Test
    void missingNameParameterRendersValidationMessageAndLeavesRowCountUnchanged() throws Exception {
        long before = countGreetingRows();

        String body = mockMvc.perform(post("/"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).contains("id=\"validation-message\"");
        assertThat(body).doesNotContain("id=\"greeting\"");
        assertThat(countGreetingRows()).isEqualTo(before);
    }

    @Test
    void submittingAliceAddsExactlyOneRowThroughTheExistingGreetingServiceFlow() throws Exception {
        seedExistingGreeting("Existing");
        long before = countGreetingRows();

        mockMvc.perform(post("/").param("name", "Alice")).andExpect(status().isOk());

        assertThat(countGreetingRows()).isEqualTo(before + 1);

        Map<String, Object> inserted = jdbcTemplate.queryForMap(
                "SELECT name, message, created_at FROM " + GREETINGS_TABLE + " WHERE name = 'Alice'");
        assertThat(inserted.get("NAME")).isEqualTo("Alice");
        assertThat(inserted.get("MESSAGE")).isEqualTo("Hello, Alice!");
        assertThat(inserted.get("CREATED_AT")).isNotNull();
    }

    @Test
    void homepageSubmissionAndPostGreetingProduceEquivalentRows() throws Exception {
        mockMvc.perform(post("/").param("name", "Alice")).andExpect(status().isOk());

        mockMvc.perform(post("/greeting")
                        .contentType("application/json")
                        .content("{\"name\":\"Bob\"}"))
                .andExpect(status().isCreated());

        assertThat(countGreetingRows()).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject(
                        "SELECT message FROM " + GREETINGS_TABLE + " WHERE name = 'Alice'",
                        String.class))
                .isEqualTo("Hello, Alice!");
        assertThat(jdbcTemplate.queryForObject(
                        "SELECT message FROM " + GREETINGS_TABLE + " WHERE name = 'Bob'",
                        String.class))
                .isEqualTo("Hello, Bob!");
    }

    private void seedExistingGreeting(String name) throws Exception {
        mockMvc.perform(post("/").param("name", name)).andExpect(status().isOk());
    }

    private long countGreetingRows() {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + GREETINGS_TABLE, Long.class);
        return count == null ? 0L : count;
    }
}
