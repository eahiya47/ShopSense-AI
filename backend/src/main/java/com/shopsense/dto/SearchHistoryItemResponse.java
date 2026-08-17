package com.shopsense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistoryItemResponse {
    private Long id;
    private String query;
    private LocalDateTime searchedAt;
}
