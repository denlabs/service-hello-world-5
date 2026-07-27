package com.tenyks.helloworld.greeting.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tenyks.helloworld.greeting.dto.GreetingResponse;
import com.tenyks.helloworld.greeting.service.GreetingService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Web-layer tests for the anonymous homepage greeting form (FSH-190 / FSH-184).
 *
 * <p>Covers AC-1 (anonymous GET / returns 200 with the form and never redirects to a
 * login page), AC-2 (submitting "Alice" renders "Hello Alice, it is " immediately
 * followed by the service-provided date) and AC-3 (the date is human readable and no
 * raw ISO-8601 timestamp leaks into the HTML).
 */
@WebMvcTest(HomeController.class)
class HomePageFormWebTest {

    /** Matches ISO-8601 date-times such as 2026-07-27T00:00:00Z. */
    private static final Pattern ISO_TIMESTAMP =
            Pattern.compile("\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}");

    /** Matches ISO-8601 calendar dates such as 2026-07-27. */
    private static final Pattern ISO_DATE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("MMMM d, uuuu", Locale.ENGLISH);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GreetingService greetingService;

    // ---------------------------------------------------------------- AC-1

    @Test
    void anonymousGetRootReturnsOkWithTextInputNamedNameAndSubmitButton() throws Exception {
        MvcResult result = mockMvc.perform(get("/")).andExpect(status().isOk()).andReturn();

        String body = result.getResponse().getContentAsString();

        assertThat(body).containsPattern("<form[^>]*>");
        assertThat(body).containsPattern("<input[^>]*type=\"text\"[^>]*name=\"name\"[^>]*>");
        assertThat(body).containsPattern("<button[^>]*type=\"submit\"[^>]*>");
    }

    @Test
    void anonymousGetRootDoesNotRedirectToALoginPage() throws Exception {
        MvcResult result = mockMvc.perform(get("/")).andReturn();
        MockHttpServletResponse response = result.getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getStatus() / 100)
                .as("no 3xx redirect to a login page")
                .isNotEqualTo(3);
        assertThat(response.getRedirectedUrl()).isNull();
        assertThat(response.getHeader("Location")).isNull();
        assertThat(response.getContentAsString().toLowerCase(Locale.ROOT))
                .doesNotContain("login")
                .doesNotContain("sign in")
                .doesNotContain("password");
    }

    @Test
    void anonymousGetRootRequiresNoPreExistingSession() throws Exception {
        MvcResult result = mockMvc.perform(get("/")).andExpect(status().isOk()).andReturn();

        assertThat(result.getRequest().getSession(false))
                .as("homepage must not depend on an existing session")
                .isNull();
    }

    @Test
    void anonymousGetRootIsServedAsHtml() throws Exception {
        MvcResult result = mockMvc.perform(get("/")).andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentType()).startsWith("text/html");
    }

    // ---------------------------------------------------------------- AC-2

    @Test
    void submittingAliceRendersGreetingImmediatelyFollowedByServiceDate() throws Exception {
        Instant createdAt = instantOn(LocalDate.of(2026, 7, 27));
        given(greetingService.createGreeting(any()))
                .willReturn(new GreetingResponse(1L, "Alice", "Hello, Alice!", createdAt));

        String body = submit("Alice");

        assertThat(body).contains("Hello Alice, it is " + expectedDisplayDate(createdAt));
    }

    @Test
    void greetingDateTracksWhateverDateTheServiceReturns() throws Exception {
        Instant createdAt = instantOn(LocalDate.of(2031, 1, 3));
        given(greetingService.createGreeting(any()))
                .willReturn(new GreetingResponse(7L, "Alice", "Hello, Alice!", createdAt));

        String body = submit("Alice");

        assertThat(body).contains("Hello Alice, it is January 3, 2031");
        assertThat(body).doesNotContain("July 27, 2026");
    }

    @Test
    void submittedFormStillRendersTheFormForAFollowUpSubmission() throws Exception {
        given(greetingService.createGreeting(any()))
                .willReturn(new GreetingResponse(
                        1L, "Alice", "Hello, Alice!", instantOn(LocalDate.of(2026, 7, 27))));

        String body = submit("Alice");

        assertThat(body).containsPattern("<input[^>]*type=\"text\"[^>]*name=\"name\"[^>]*>");
        assertThat(body).containsPattern("<button[^>]*type=\"submit\"[^>]*>");
    }

    // ---------------------------------------------------------------- AC-3

    @Test
    void greetingDateIsHumanReadableAndHidesTheRawIsoTimestamp() throws Exception {
        Instant createdAt = instantOn(LocalDate.of(2026, 7, 27));
        given(greetingService.createGreeting(any()))
                .willReturn(new GreetingResponse(1L, "Alice", "Hello, Alice!", createdAt));

        String body = submit("Alice");

        assertThat(body).contains("Hello Alice, it is July 27, 2026");
        assertThat(body).doesNotContain(createdAt.toString());
        assertThat(body).doesNotContainPattern(ISO_TIMESTAMP);
        assertThat(body).doesNotContainPattern(ISO_DATE);
    }

    @Test
    void greetingHtmlDoesNotExposeTheInternalGreetingMessageOrIsoInstant() throws Exception {
        Instant createdAt = instantOn(LocalDate.of(2026, 7, 27));
        given(greetingService.createGreeting(any()))
                .willReturn(new GreetingResponse(1L, "Alice", "Hello, Alice!", createdAt));

        String body = submit("Alice");

        assertThat(body).doesNotContain("createdAt");
        assertThat(body).doesNotContainPattern(ISO_TIMESTAMP);
    }

    // ---------------------------------------------------------------- helpers

    private String submit(String name) throws Exception {
        return mockMvc.perform(post("/").param("name", name))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private static Instant instantOn(LocalDate date) {
        return date.atTime(13, 45).atZone(ZoneId.systemDefault()).toInstant();
    }

    private static String expectedDisplayDate(Instant instant) {
        return DISPLAY_DATE.format(instant.atZone(ZoneId.systemDefault()).toLocalDate());
    }
}
