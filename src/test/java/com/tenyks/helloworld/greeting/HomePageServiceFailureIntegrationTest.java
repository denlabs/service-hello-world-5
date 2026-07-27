package com.tenyks.helloworld.greeting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.tenyks.helloworld.greeting.dto.CreateGreetingRequest;
import com.tenyks.helloworld.greeting.dto.GreetingResponse;
import com.tenyks.helloworld.greeting.service.GreetingService;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Covers AC-5: when the greeting service raises an exception or returns an error response, a valid
 * submission still renders the page with an error message, shows no greeting text, and never
 * responds with 500. The greetings table is also asserted to stay empty.
 */
@SpringBootTest
@AutoConfigureMockMvc
class HomePageServiceFailureIntegrationTest {

    private static final String GREETINGS_TABLE = "greeting";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private GreetingService greetingService;

    @BeforeEach
    void clearGreetings() {
        jdbcTemplate.update("DELETE FROM " + GREETINGS_TABLE);
    }

    @Test
    void serviceThrowingRuntimeExceptionRendersErrorMessageWithoutServerError() throws Exception {
        given(greetingService.createGreeting(any(CreateGreetingRequest.class)))
                .willThrow(new IllegalStateException("greeting backend unavailable"));

        assertFailureRendered();
    }

    @Test
    void serviceThrowingDataAccessExceptionRendersErrorMessageWithoutServerError()
            throws Exception {
        given(greetingService.createGreeting(any(CreateGreetingRequest.class)))
                .willThrow(new DataIntegrityViolationException("constraint violation"));

        assertFailureRendered();
    }

    @Test
    void serviceReturningNullResponseRendersErrorMessageWithoutServerError() throws Exception {
        given(greetingService.createGreeting(any(CreateGreetingRequest.class))).willReturn(null);

        assertFailureRendered();
    }

    @Test
    void serviceReturningIncompleteErrorResponseRendersErrorMessageWithoutServerError()
            throws Exception {
        given(greetingService.createGreeting(any(CreateGreetingRequest.class)))
                .willReturn(new GreetingResponse(null, null, null, null));

        assertFailureRendered();
    }

    @Test
    void serviceReturningResponseWithoutDateRendersErrorMessageWithoutServerError()
            throws Exception {
        given(greetingService.createGreeting(any(CreateGreetingRequest.class)))
                .willReturn(new GreetingResponse(1L, "Alice", "Hello, Alice!", null));

        assertFailureRendered();
    }

    @Test
    void successfulServiceResponseStillRendersGreetingAndNoErrorMessage() throws Exception {
        given(greetingService.createGreeting(any(CreateGreetingRequest.class)))
                .willReturn(new GreetingResponse(
                        1L, "Alice", "Hello, Alice!", Instant.parse("2026-07-27T10:15:30Z")));

        MvcResult result = mockMvc.perform(post("/").param("name", "Alice")).andReturn();
        String body = result.getResponse().getContentAsString();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(body).contains("Hello Alice, it is ");
        assertThat(body).doesNotContain("id=\"error-message\"");
    }

    private void assertFailureRendered() throws Exception {
        MvcResult result = mockMvc.perform(post("/").param("name", "Alice")).andReturn();
        String body = result.getResponse().getContentAsString();
        int status = result.getResponse().getStatus();

        assertThat(status).isNotEqualTo(500);
        assertThat(status).isLessThan(500);
        assertThat(body).contains("id=\"error-message\"");
        assertThat(body).doesNotContain("id=\"greeting\"");
        assertThat(body).doesNotContain("it is ");
        assertThat(body).contains("name=\"name\"");
        assertThat(jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM " + GREETINGS_TABLE, Long.class))
                .isZero();
    }
}
