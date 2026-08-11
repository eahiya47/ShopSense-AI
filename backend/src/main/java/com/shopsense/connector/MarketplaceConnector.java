package com.shopsense.connector;

import com.shopsense.entity.ProductVariant;

public interface MarketplaceConnector {

    String getPlatformName();

    ConnectorResult fetchOffer(ProductVariant variant);
}
