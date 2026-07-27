package com.tenyks.helloworld.greeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tenyks.helloworld.greeting.domain.Greeting;
import com.tenyks.helloworld.greeting.repository.GreetingRepository;
import java.util.List;
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
    void submittingBlankNameWritesNoRow() throws Exception {
        mockMvc.perform(post("/").param("name", "  "))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.not(Matchers.containsString("it is"))));

        assertThat(greetingRepository.findAll()).isEmpty();
    }
}
