package com.tenyks.helloworld.greeting.web;

import com.tenyks.helloworld.greeting.dto.GreetingResponse;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * View model backing the homepage. The greeting payload returned by the greeting
 * service is bound here as a name plus a human-readable date, so the rendering
 * layer never sees the raw ISO-8601 timestamp.
 */
public record HomeViewModel(
        String nameValue,
        String greetingName,
        String greetingDate,
        String validationMessage,
        String errorMessage) {

    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM d, uuuu", Locale.ENGLISH);

    public static HomeViewModel blankForm() {
        return new HomeViewModel("", null, null, null, null);
    }

    public static HomeViewModel fromGreeting(String nameValue, GreetingResponse response) {
        if (response == null || response.name() == null || response.createdAt() == null) {
            throw new IllegalStateException("Greeting service returned an incomplete greeting");
        }
        String date = DISPLAY_DATE_FORMATTER.format(
                response.createdAt().atZone(ZoneId.systemDefault()).toLocalDate());
        return new HomeViewModel(nameValue, response.name(), date, null, null);
    }

    public static HomeViewModel validationFailure(String nameValue, String message) {
        return new HomeViewModel(nameValue, null, null, message, null);
    }

    public static HomeViewModel serviceFailure(String nameValue, String message) {
        return new HomeViewModel(nameValue, null, null, null, message);
    }

    public boolean hasGreeting() {
        return greetingName != null && greetingDate != null;
    }

    public String greetingText() {
        return hasGreeting() ? "Hello " + greetingName + ", it is " + greetingDate : null;
    }
}
