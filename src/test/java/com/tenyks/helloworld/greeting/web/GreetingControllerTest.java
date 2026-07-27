package com.tenyks.helloworld.greeting.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenyks.helloworld.greeting.dto.CreateGreetingRequest;
import com.tenyks.helloworld.greeting.dto.GreetingResponse;
import com.tenyks.helloworld.greeting.service.GreetingService;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GreetingController.class)
@DisplayName("POST /greeting (happy path)")
class GreetingControllerTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-15T10:30:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GreetingService greetingService;

    @Test
    @DisplayName("returns 201 Created with the persisted greeting representation")
    void createGreetingReturnsCreatedWithGreetingBody() throws Exception {
        when(greetingService.createGreeting(any(CreateGreetingRequest.class)))
                .thenReturn(new GreetingResponse(1L, "Ada", "Hello, Ada!", CREATED_AT));

        mockMvc.perform(post("/greeting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateGreetingRequest("Ada"))))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ada"))
                .andExpect(jsonPath("$.message").value("Hello, Ada!"))
                .andExpect(jsonPath("$.createdAt").value("2026-01-15T10:30:00Z"));
    }

    @Test
    @DisplayName("passes the submitted name through to the greeting service")
    void createGreetingDelegatesRequestBodyToService() throws Exception {
        when(greetingService.createGreeting(any(CreateGreetingRequest.class)))
                .thenReturn(new GreetingResponse(7L, "Grace", "Hello, Grace!", CREATED_AT));

        mockMvc.perform(post("/greeting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Grace\"}"))
                .andExpect(status().isCreated());

        ArgumentCaptor<CreateGreetingRequest> captor = ArgumentCaptor.forClass(CreateGreetingRequest.class);
        verify(greetingService).createGreeting(captor.capture());
        assertThat(captor.getValue().name()).isEqualTo("Grace");
    }
}
