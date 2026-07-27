package com.tenyks.helloworld.greeting.web;

import com.tenyks.helloworld.greeting.dto.CreateGreetingRequest;
import com.tenyks.helloworld.greeting.dto.GreetingResponse;
import com.tenyks.helloworld.greeting.dto.PageResponse;
import com.tenyks.helloworld.greeting.service.GreetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class GreetingController {

    private final GreetingService greetingService;

    @PostMapping(
            path = "/greeting",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public GreetingResponse createGreeting(@Valid @RequestBody CreateGreetingRequest request) {
        return greetingService.createGreeting(request);
    }

    @GetMapping(path = "/greetings", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<GreetingResponse> getGreetings(
            @PageableDefault(size = 20, sort = {"createdAt", "id"}, direction = Sort.Direction.DESC)
                    Pageable pageable) {
        return greetingService.getGreetings(pageable);
    }
}
