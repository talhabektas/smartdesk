package com.example.smartdeskbackend.dto.response.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic page response wrapper for paginated API responses
 * Provides a consistent structure for paginated data across the application
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean isFirst;
    private boolean isLast;

    // Manual getters for better compatibility
    public boolean getHasNext() {
        return hasNext;
    }

    public boolean getHasPrevious() {
        return hasPrevious;
    }

    public boolean getIsFirst() {
        return isFirst;
    }

    public boolean getIsLast() {
        return isLast;
    }

    /**
     * Creates a PageResponse from Spring Data Page object
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> PageResponse<T> of(org.springframework.data.domain.Page<T> page) {
        PageResponse response = new PageResponse();
        response.setContent(page.getContent());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setCurrentPage(page.getNumber());
        response.setPageSize(page.getSize());
        response.setHasNext(page.hasNext());
        response.setHasPrevious(page.hasPrevious());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        return response;
    }

    /**
     * Creates a PageResponse with transformed content from Spring Data Page
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T, R> PageResponse<R> of(org.springframework.data.domain.Page<T> page, List<R> transformedContent) {
        PageResponse response = new PageResponse();
        response.setContent(transformedContent);
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setCurrentPage(page.getNumber());
        response.setPageSize(page.getSize());
        response.setHasNext(page.hasNext());
        response.setHasPrevious(page.hasPrevious());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        return response;
    }

    /**
     * Creates an empty PageResponse
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> PageResponse<T> empty() {
        PageResponse response = new PageResponse();
        response.setContent(List.of());
        response.setTotalElements(0L);
        response.setTotalPages(0);
        response.setCurrentPage(0);
        response.setPageSize(0);
        response.setHasNext(false);
        response.setHasPrevious(false);
        response.setFirst(true);
        response.setLast(true);
        return response;
    }
}
