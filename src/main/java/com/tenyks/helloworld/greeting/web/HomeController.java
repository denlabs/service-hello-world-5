package com.tenyks.helloworld.greeting.web;

import com.tenyks.helloworld.greeting.dto.CreateGreetingRequest;
import com.tenyks.helloworld.greeting.dto.GreetingResponse;
import com.tenyks.helloworld.greeting.service.GreetingService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Public, unauthenticated homepage. Renders a greeting form and, on submission,
 * the greeting produced by the existing greeting service flow.
 *
 * <p>The submitted name is wrapped in the same {@link CreateGreetingRequest} used by
 * {@code POST /greeting}, validated against the same bean-validation constraints and
 * handed to the same {@link GreetingService}, so a successful submission inserts exactly
 * one row through the existing persistence layer.
 *
 * <p>The HTML is rendered directly rather than through a template engine so the
 * application does not require an additional view-technology dependency.
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class HomeController {

    static final String BLANK_NAME_MESSAGE = "Please enter a name.";
    static final String SERVICE_ERROR_MESSAGE =
            "Sorry, the greeting could not be created. Please try again.";

    private final GreetingService greetingService;
    private final Validator validator;

    @GetMapping(path = "/", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity<String> home() {
        return page(HttpStatus.OK, HomeViewModel.blankForm());
    }

    @PostMapping(path = "/", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity<String> submitGreeting(
            @RequestParam(name = "name", required = false) String name) {
        String submitted = name == null ? "" : name;

        String validationMessage = validate(submitted);
        if (validationMessage != null) {
            return page(
                    HttpStatus.OK, HomeViewModel.validationFailure(submitted, validationMessage));
        }

        try {
            GreetingResponse response =
                    greetingService.createGreeting(new CreateGreetingRequest(submitted.trim()));
            if (isErrorResponse(response)) {
                log.warn("Greeting service returned an unusable response: {}", response);
                return page(
                        HttpStatus.OK,
                        HomeViewModel.serviceFailure(submitted, SERVICE_ERROR_MESSAGE));
            }
            return page(HttpStatus.OK, HomeViewModel.fromGreeting(submitted, response));
        } catch (Exception ex) {
            log.warn("Greeting creation failed for homepage submission", ex);
            return page(
                    HttpStatus.OK, HomeViewModel.serviceFailure(submitted, SERVICE_ERROR_MESSAGE));
        }
    }

    /**
     * Last-resort safety net: any unexpected failure while handling a homepage request is
     * rendered as the ordinary page with an error message instead of a 500 error page.
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<String> handleUnexpectedFailure(Exception ex) {
        log.warn("Unexpected homepage failure", ex);
        return page(HttpStatus.OK, HomeViewModel.serviceFailure("", SERVICE_ERROR_MESSAGE));
    }

    /**
     * Rejects blank names before the greeting service is consulted, so no row can be written
     * for an empty or whitespace-only submission.
     */
    private String validate(String submitted) {
        if (submitted.isBlank()) {
            return BLANK_NAME_MESSAGE;
        }
        Set<ConstraintViolation<CreateGreetingRequest>> violations =
                validator.validate(new CreateGreetingRequest(submitted.trim()));
        if (violations.isEmpty()) {
            return null;
        }
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .collect(Collectors.joining(" "));
    }

    private boolean isErrorResponse(GreetingResponse response) {
        return response == null
                || response.name() == null
                || response.name().isBlank()
                || response.createdAt() == null;
    }

    private ResponseEntity<String> page(HttpStatus status, HomeViewModel model) {
        return ResponseEntity.status(status)
                .contentType(MediaType.TEXT_HTML)
                .body(renderPage(model));
    }

    private String renderPage(HomeViewModel model) {
        StringBuilder html = new StringBuilder(512);
        html.append("<!DOCTYPE html>\n")
                .append("<html lang=\"en\">\n<head>\n")
                .append("<meta charset=\"UTF-8\">\n")
                .append("<title>Greeting</title>\n</head>\n<body>\n")
                .append("<h1>Greeting</h1>\n")
                .append("<form method=\"post\" action=\"/\">\n")
                .append("<label for=\"name\">Name</label>\n")
                .append("<input type=\"text\" id=\"name\" name=\"name\" value=\"")
                .append(escape(model.nameValue()))
                .append("\">\n")
                .append("<button type=\"submit\">Say hello</button>\n")
                .append("</form>\n");

        if (model.validationMessage() != null) {
            html.append("<p id=\"validation-message\">")
                    .append(escape(model.validationMessage()))
                    .append("</p>\n");
        }
        if (model.errorMessage() != null) {
            html.append("<p id=\"error-message\">")
                    .append(escape(model.errorMessage()))
                    .append("</p>\n");
        }
        if (model.hasGreeting()) {
            html.append("<p id=\"greeting\">").append(escape(model.greetingText())).append("</p>\n");
        }

        return html.append("</body>\n</html>\n").toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
