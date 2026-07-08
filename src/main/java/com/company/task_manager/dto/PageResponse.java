package com.company.task_manager.dto;

import org.springframework.data.domain.Page;

import java.util.List;

// Vlastný obal namiesto vracania Page<T> priamo z controllera - stabilný JSON tvar,
// nezávislý od toho, ako Spring Data práve serializuje PageImpl.
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    // Wraps a Spring Data Page into a stable, serialization-independent JSON shape.
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
