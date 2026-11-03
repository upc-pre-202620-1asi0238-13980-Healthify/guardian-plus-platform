package com.healthify.guardian.platform.shared.interfaces.rest.resources;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Standard response resource for a page of results.
 *
 * @param content       the items of the requested page
 * @param page          zero-based index of the requested page
 * @param size          requested page size
 * @param totalElements number of items across every page
 * @param totalPages    number of pages
 * @param <T>           the item resource type
 */
public record PageResource<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    /**
     * Builds a page resource from a Spring Data page, converting each item.
     *
     * @param page      the page of domain objects
     * @param assembler converts each domain object into its resource
     * @param <E>       the domain object type
     * @param <T>       the item resource type
     * @return the page resource
     */
    public static <E, T> PageResource<T> from(Page<E> page, Function<E, T> assembler) {
        return new PageResource<>(
                page.getContent().stream().map(assembler).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
