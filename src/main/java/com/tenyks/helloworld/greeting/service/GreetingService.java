package com.tenyks.helloworld.greeting.service;

import com.tenyks.helloworld.greeting.dto.CreateGreetingRequest;
import com.tenyks.helloworld.greeting.dto.GreetingResponse;
import com.tenyks.helloworld.greeting.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface GreetingService {

    GreetingResponse createGreeting(CreateGreetingRequest request);

    PageResponse<GreetingResponse> getGreetings(Pageable pageable);
}
