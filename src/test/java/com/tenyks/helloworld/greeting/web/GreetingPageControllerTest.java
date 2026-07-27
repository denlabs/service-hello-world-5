package com.tenyks.helloworld.greeting.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tenyks.helloworld.greeting.dto.GreetingResponse;
import com.tenyks.helloworld.greeting.dto.PageResponse;
import com.tenyks.helloworld.greeting.service.GreetingService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GreetingController.class)
@DisplayName("GET /greetings")
class GreetingPageControllerTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-15T10:30:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GreetingService greetingService;

    @Test
    @DisplayName("returns 200 OK with a stable page envelope")
    void returnsPageEnvelope() throws Exception {
        when(greetingService.getGreetings(any(Pageable.class)))
                .thenReturn(new PageResponse<>(
                        List.of(new GreetingResponse(1L, "Ada", "Hello, Ada!", CREATED_AT)),
                        0,
                        20,
                        1L,
                        1,
                        true,
                        true));

        mockMvc.perform(get("/greetings"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Ada"))
                .andExpect(jsonPath("$.content[0].message").value("Hello, Ada!"))
                .andExpect(jsonPath("$.content[0].createdAt").value("2026-01-15T10:30:00Z"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @DisplayName("applies the default page size and sort when no parameters are supplied")
    void appliesDefaultPageable() throws Exception {
        when(greetingService.getGreetings(any(Pageable.class)))
                .thenReturn(emptyPage(0, 20));

        mockMvc.perform(get("/greetings")).andExpect(status().isOk());

        Pageable pageable = capturePageable();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "createdAt", "id"));
    }

    @Test
    @DisplayName("passes standard page, size and sort request parameters to the service")
    void passesRequestParametersToService() throws Exception {
        when(greetingService.getGreetings(any(Pageable.class)))
                .thenReturn(emptyPage(2, 5));

        mockMvc.perform(get("/greetings")
                        .param("page", "2")
                        .param("size", "5")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk());

        Pageable pageable = capturePageable();
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "name"));
    }

    private Pageable capturePageable() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(greetingService).getGreetings(captor.capture());
        return captor.getValue();
    }

    private static PageResponse<GreetingResponse> emptyPage(int page, int size) {
        return new PageResponse<>(List.of(), page, size, 0L, 0, true, true);
    }
}
