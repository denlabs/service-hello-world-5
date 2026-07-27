package com.tenyks.helloworld.greeting.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Stable JSON envelope for paginated responses, independent of Spring Data's
 * internal {@code Page} serialization format.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                List.copyOf(page.getContent()),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }
}
