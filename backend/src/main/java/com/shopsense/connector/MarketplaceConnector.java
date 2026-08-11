package com.shopsense.connector;

import com.shopsense.entity.ProductVariant;

import java.util.List;

public interface MarketplaceConnector {

    String getPlatformName();

    ConnectorResult fetchOffer(ProductVariant variant);

    List<NormalizedReview> fetchReviews(ProductVariant variant);
}
