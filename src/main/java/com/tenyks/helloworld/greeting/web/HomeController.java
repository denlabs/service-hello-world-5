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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
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
    public String home() {
        return renderPage(HomeViewModel.blankForm());
    }

    @PostMapping(path = "/", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String submitGreeting(@RequestParam(name = "name", required = false) String name) {
        String submitted = name == null ? "" : name;

        if (submitted.isBlank()) {
            return renderPage(HomeViewModel.validationFailure(submitted, BLANK_NAME_MESSAGE));
        }

        CreateGreetingRequest request = new CreateGreetingRequest(submitted.trim());

        Set<ConstraintViolation<CreateGreetingRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(" "));
            return renderPage(HomeViewModel.validationFailure(submitted, message));
        }

        try {
            GreetingResponse response = greetingService.createGreeting(request);
            return renderPage(HomeViewModel.fromGreeting(submitted, response));
        } catch (RuntimeException ex) {
            log.warn("Greeting creation failed for homepage submission", ex);
            return renderPage(HomeViewModel.serviceFailure(submitted, SERVICE_ERROR_MESSAGE));
        }
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
