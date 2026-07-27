package com.tenyks.helloworld.greeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tenyks.helloworld.greeting.domain.Greeting;
import com.tenyks.helloworld.greeting.repository.GreetingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class GreetingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GreetingRepository greetingRepository;

    @BeforeEach
    void clearGreetings() {
        greetingRepository.deleteAll();
    }

    @Test
    void createGreetingReturnsPersistedGreetingView() throws Exception {
        mockMvc.perform(post("/greeting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ada\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Ada"))
                .andExpect(jsonPath("$.message").value("Hello, Ada!"))
                .andExpect(jsonPath("$.createdAt").exists());

        assertThat(greetingRepository.findAll())
                .singleElement()
                .satisfies(greeting -> {
                    assertThat(greeting.getName()).isEqualTo("Ada");
                    assertThat(greeting.getMessage()).isEqualTo("Hello, Ada!");
                    assertThat(greeting.getCreatedAt()).isNotNull();
                });
    }

    @Test
    void createGreetingRejectsBlankName() throws Exception {
        mockMvc.perform(post("/greeting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \"}"))
                .andExpect(status().isBadRequest());

        assertThat(greetingRepository.findAll()).isEmpty();
    }

    @Test
    void greetingTableIsMappedToLiquibaseSchema() {
        Greeting saved = greetingRepository.save(Greeting.builder()
                .name("Grace")
                .message("Hello, Grace!")
                .createdAt(java.time.Instant.now())
                .build());

        assertThat(saved.getId()).isNotNull();
    }
}
