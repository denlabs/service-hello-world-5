package com.tenyks.helloworld.greeting.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tenyks.helloworld.greeting.dto.GreetingResponse;
import com.tenyks.helloworld.greeting.service.GreetingService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GreetingService greetingService;

    @Test
    void getRootRendersFormForAnonymousVisitor() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("<form")))
                .andExpect(content().string(Matchers.containsString("type=\"text\"")))
                .andExpect(content().string(Matchers.containsString("name=\"name\"")))
                .andExpect(content().string(Matchers.containsString("type=\"submit\"")));
    }

    @Test
    void submittingNameRendersHumanReadableGreeting() throws Exception {
        Instant createdAt = LocalDate.of(2026, 7, 27)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .plusSeconds(3600);
        given(greetingService.createGreeting(any()))
                .willReturn(new GreetingResponse(1L, "Alice", "Hello, Alice!", createdAt));

        mockMvc.perform(post("/").param("name", "Alice"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(Matchers.containsString("Hello Alice, it is July 27, 2026")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("T00:00"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Z"))));
    }

    @Test
    void blankNameRendersValidationMessageAndNoGreeting() throws Exception {
        mockMvc.perform(post("/").param("name", "   "))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(Matchers.containsString(HomeController.BLANK_NAME_MESSAGE)))
                .andExpect(content().string(Matchers.not(Matchers.containsString("it is"))));

        then(greetingService).should(never()).createGreeting(any());
    }

    @Test
    void serviceFailureRendersErrorMessageWithoutServerError() throws Exception {
        given(greetingService.createGreeting(any()))
                .willThrow(new IllegalStateException("boom"));

        mockMvc.perform(post("/").param("name", "Alice"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(Matchers.containsString(HomeController.SERVICE_ERROR_MESSAGE)))
                .andExpect(content().string(Matchers.not(Matchers.containsString("it is"))));
    }

    @Test
    void emptyNameRendersValidationMessageAndNeverCallsService() throws Exception {
        mockMvc.perform(post("/").param("name", ""))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(Matchers.containsString(HomeController.BLANK_NAME_MESSAGE)))
                .andExpect(content().string(Matchers.not(Matchers.containsString("it is"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Hello "))));

        then(greetingService).should(never()).createGreeting(any());
    }

    @Test
    void missingNameParameterRendersValidationMessageAndNeverCallsService() throws Exception {
        mockMvc.perform(post("/"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(Matchers.containsString(HomeController.BLANK_NAME_MESSAGE)))
                .andExpect(content().string(Matchers.not(Matchers.containsString("it is"))));

        then(greetingService).should(never()).createGreeting(any());
    }

    @Test
    void tabAndNewlineOnlyNameIsRejected() throws Exception {
        mockMvc.perform(post("/").param("name", "\t\n "))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(Matchers.containsString(HomeController.BLANK_NAME_MESSAGE)));

        then(greetingService).should(never()).createGreeting(any());
    }

    @Test
    void serviceErrorResponseRendersErrorMessageAndNoGreeting() throws Exception {
        given(greetingService.createGreeting(any())).willReturn(null);

        mockMvc.perform(post("/").param("name", "Alice"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(Matchers.containsString(HomeController.SERVICE_ERROR_MESSAGE)))
                .andExpect(content().string(Matchers.not(Matchers.containsString("it is"))));
    }

    @Test
    void incompleteServiceResponseRendersErrorMessageAndNoGreeting() throws Exception {
        given(greetingService.createGreeting(any()))
                .willReturn(new GreetingResponse(1L, "Alice", "Hello, Alice!", null));

        mockMvc.perform(post("/").param("name", "Alice"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(Matchers.containsString(HomeController.SERVICE_ERROR_MESSAGE)))
                .andExpect(content().string(Matchers.not(Matchers.containsString("it is"))));
    }

    @Test
    void serviceFailureResponseStatusIsNotServerError() throws Exception {
        given(greetingService.createGreeting(any()))
                .willThrow(new IllegalStateException("boom"));

        int status = mockMvc.perform(post("/").param("name", "Alice"))
                .andReturn()
                .getResponse()
                .getStatus();

        org.assertj.core.api.Assertions.assertThat(status).isLessThan(500);
    }
}
