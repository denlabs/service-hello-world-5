package com.tenyks.helloworld.greeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tenyks.helloworld.greeting.domain.Greeting;
import com.tenyks.helloworld.greeting.repository.GreetingRepository;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class HomePageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GreetingRepository greetingRepository;

    @BeforeEach
    void clearGreetings() {
        greetingRepository.deleteAll();
    }

    @Test
    void anonymousVisitorGetsHomepageFormWithoutLoginRedirect() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("<form")))
                .andExpect(content().string(Matchers.containsString("name=\"name\"")))
                .andExpect(content().string(Matchers.containsString("type=\"submit\"")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("login"))));
    }

    @Test
    void submittingValidNameCreatesExactlyOneGreetingRow() throws Exception {
        mockMvc.perform(post("/").param("name", "Alice"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Hello Alice, it is ")));

        List<Greeting> greetings = greetingRepository.findAll();
        assertThat(greetings).hasSize(1);
        assertThat(greetings.getFirst().getName()).isEqualTo("Alice");
    }

    @Test
    void greetingShowsHumanReadableDateAndNeverTheRawIsoTimestamp() throws Exception {
        String body = mockMvc.perform(post("/").param("name", "Alice"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Greeting persisted = greetingRepository.findAll().getFirst();
        String expectedDate = DateTimeFormatter.ofPattern("MMMM d, uuuu", Locale.ENGLISH)
                .format(persisted.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate());

        assertThat(body).contains("Hello Alice, it is " + expectedDate);
        assertThat(body).doesNotContain(persisted.getCreatedAt().toString());
        assertThat(body).doesNotContainPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}");
    }

    @Test
    void homepageSubmissionUsesSameServiceFlowAsPostGreeting() throws Exception {
        mockMvc.perform(post("/").param("name", "Alice"))
                .andExpect(status().isOk());

        List<Greeting> greetings = greetingRepository.findAll();
        assertThat(greetings).hasSize(1);
        Greeting persisted = greetings.getFirst();
        assertThat(persisted.getName()).isEqualTo("Alice");
        assertThat(persisted.getMessage()).isEqualTo("Hello, Alice!");
        assertThat(persisted.getCreatedAt()).isNotNull();
        assertThat(persisted.getId()).isNotNull();
    }

    @Test
    void eachSuccessfulSubmissionInsertsExactlyOneRow() throws Exception {
        mockMvc.perform(post("/").param("name", "Alice")).andExpect(status().isOk());
        assertThat(greetingRepository.count()).isEqualTo(1);

        mockMvc.perform(post("/").param("name", "Bob")).andExpect(status().isOk());
        assertThat(greetingRepository.count()).isEqualTo(2);
    }

    @Test
    void submittingOverlongNameWritesNoRowAndDoesNotFail() throws Exception {
        mockMvc.perform(post("/").param("name", "A".repeat(101)))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.not(Matchers.containsString("it is"))));

        assertThat(greetingRepository.findAll()).isEmpty();
    }

    @Test
    void submittingBlankNameWritesNoRow() throws Exception {
        mockMvc.perform(post("/").param("name", "  "))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.not(Matchers.containsString("it is"))));

        assertThat(greetingRepository.findAll()).isEmpty();
    }
}
