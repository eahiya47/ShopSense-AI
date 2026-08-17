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
public class WishlistItemResponse {
    private Long id;
    private Long productVariantId;
    private String productName;
    private String variantName;
    private LocalDateTime addedAt;
}
