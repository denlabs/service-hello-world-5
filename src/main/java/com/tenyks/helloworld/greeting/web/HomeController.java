package com.tenyks.helloworld.greeting.web;

import com.tenyks.helloworld.greeting.dto.CreateGreetingRequest;
import com.tenyks.helloworld.greeting.dto.GreetingResponse;
import com.tenyks.helloworld.greeting.service.GreetingService;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
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

    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM d, uuuu", Locale.ENGLISH);

    private final GreetingService greetingService;

    @GetMapping(path = "/", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String home() {
        return renderPage("", null, null, null);
    }

    @PostMapping(
            path = "/",
            produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String submitGreeting(@RequestParam(name = "name", required = false) String name) {
        String submitted = name == null ? "" : name;

        if (submitted.isBlank()) {
            return renderPage(submitted, BLANK_NAME_MESSAGE, null, null);
        }

        try {
            GreetingResponse response =
                    greetingService.createGreeting(new CreateGreetingRequest(submitted.trim()));
            String greeting = "Hello " + response.name() + ", it is " + formatDate(response);
            return renderPage(submitted, null, null, greeting);
        } catch (RuntimeException ex) {
            log.warn("Greeting creation failed for homepage submission", ex);
            return renderPage(submitted, null, SERVICE_ERROR_MESSAGE, null);
        }
    }

    private String formatDate(GreetingResponse response) {
        if (response == null || response.createdAt() == null) {
            throw new IllegalStateException("Greeting service returned no creation date");
        }
        return DISPLAY_DATE_FORMATTER.format(
                response.createdAt().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    private String renderPage(
            String nameValue, String validationMessage, String errorMessage, String greeting) {

        StringBuilder html = new StringBuilder(512);
        html.append("<!DOCTYPE html>\n")
                .append("<html lang=\"en\">\n<head>\n")
                .append("<meta charset=\"UTF-8\">\n")
                .append("<title>Greeting</title>\n</head>\n<body>\n")
                .append("<h1>Greeting</h1>\n")
                .append("<form method=\"post\" action=\"/\">\n")
                .append("<label for=\"name\">Name</label>\n")
                .append("<input type=\"text\" id=\"name\" name=\"name\" value=\"")
                .append(escape(nameValue))
                .append("\">\n")
                .append("<button type=\"submit\">Say hello</button>\n")
                .append("</form>\n");

        if (validationMessage != null) {
            html.append("<p id=\"validation-message\">")
                    .append(escape(validationMessage))
                    .append("</p>\n");
        }
        if (errorMessage != null) {
            html.append("<p id=\"error-message\">")
                    .append(escape(errorMessage))
                    .append("</p>\n");
        }
        if (greeting != null) {
            html.append("<p id=\"greeting\">").append(escape(greeting)).append("</p>\n");
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
