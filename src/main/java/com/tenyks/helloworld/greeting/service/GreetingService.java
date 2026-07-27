package com.tenyks.helloworld.greeting.service;

import com.tenyks.helloworld.greeting.dto.CreateGreetingRequest;
import com.tenyks.helloworld.greeting.dto.GreetingResponse;

public interface GreetingService {

    GreetingResponse createGreeting(CreateGreetingRequest request);
}
