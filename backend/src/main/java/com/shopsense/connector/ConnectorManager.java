package com.shopsense.connector;

import com.shopsense.entity.ProductVariant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConnectorManager {

    private final List<MarketplaceConnector> connectors;

    public ConnectorManager(List<MarketplaceConnector> connectors) {
        this.connectors = connectors != null ? new ArrayList<>(connectors) : new ArrayList<>();
    }

    public List<ConnectorResult> fetchOffersForVariant(ProductVariant variant) {
        return fetchOffersForVariant(variant, null);
    }

    public List<ConnectorResult> fetchOffersForVariant(ProductVariant variant, List<String> activePlatforms) {
        if (connectors.isEmpty()) {
            log.warn("No marketplace connectors registered in ConnectorManager");
            return Collections.emptyList();
        }

        Set<String> activeSet = (activePlatforms != null && !activePlatforms.isEmpty())
                ? activePlatforms.stream().map(String::toLowerCase).collect(Collectors.toSet())
                : null;

        List<ConnectorResult> results = new ArrayList<>();

        for (MarketplaceConnector connector : connectors) {
            String platformName = connector.getPlatformName();

            if (activeSet != null && !activeSet.contains(platformName.toLowerCase())) {
                log.debug("Skipping connector for disabled platform: {}", platformName);
                continue;
            }

            try {
                ConnectorResult result = connector.fetchOffer(variant);
                if (result == null) {
                    result = ConnectorResult.unavailable(platformName, "Connector returned null response");
                }
                results.add(result);
            } catch (Exception e) {
                log.warn("Connector execution failed for platform {}: {}", platformName, e.getMessage());
                results.add(ConnectorResult.unavailable(platformName,
                        "Marketplace data is temporarily unavailable: " + e.getMessage()));
            }
        }

        return results;
    }

    public ConnectorResult fetchOfferFromPlatform(String platformName, ProductVariant variant) {
        Optional<MarketplaceConnector> connectorOpt = getConnector(platformName);
        if (connectorOpt.isEmpty()) {
            return ConnectorResult.unavailable(platformName, "No connector found for platform: " + platformName);
        }

        MarketplaceConnector connector = connectorOpt.get();
        try {
            ConnectorResult result = connector.fetchOffer(variant);
            return result != null ? result
                    : ConnectorResult.unavailable(platformName, "Connector returned null response");
        } catch (Exception e) {
            log.warn("Connector execution failed for platform {}: {}", platformName, e.getMessage());
            return ConnectorResult.unavailable(platformName,
                    "Marketplace data is temporarily unavailable: " + e.getMessage());
        }
    }

    public List<NormalizedReview> fetchReviewsFromPlatform(String platformName, ProductVariant variant) {
        Optional<MarketplaceConnector> connectorOpt = getConnector(platformName);
        if (connectorOpt.isEmpty()) {
            log.warn("No connector found for platform: {}", platformName);
            return Collections.emptyList();
        }

        MarketplaceConnector connector = connectorOpt.get();
        try {
            List<NormalizedReview> reviews = connector.fetchReviews(variant);
            return reviews != null ? reviews : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Review fetching failed for platform {}: {}", platformName, e.getMessage());
            throw e; // rethrow to allow caller/service to catch and preserve existing DB reviews
        }
    }

    public Map<String, List<NormalizedReview>> fetchReviewsForVariant(ProductVariant variant,
            List<String> activePlatforms) {
        if (connectors.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<String> activeSet = (activePlatforms != null && !activePlatforms.isEmpty())
                ? activePlatforms.stream().map(String::toLowerCase).collect(Collectors.toSet())
                : null;

        Map<String, List<NormalizedReview>> reviewMap = new HashMap<>();

        for (MarketplaceConnector connector : connectors) {
            String platformName = connector.getPlatformName();
            if (activeSet != null && !activeSet.contains(platformName.toLowerCase())) {
                continue;
            }

            try {
                List<NormalizedReview> reviews = connector.fetchReviews(variant);
                reviewMap.put(platformName, reviews != null ? reviews : Collections.emptyList());
            } catch (Exception e) {
                log.warn("Review fetching failed for platform {}: {}", platformName, e.getMessage());
            }
        }

        return reviewMap;
    }

    public Optional<MarketplaceConnector> getConnector(String platformName) {
        if (platformName == null) {
            return Optional.empty();
        }
        return connectors.stream()
                .filter(c -> platformName.equalsIgnoreCase(c.getPlatformName()))
                .findFirst();
    }

    public List<MarketplaceConnector> getAvailableConnectors() {
        return Collections.unmodifiableList(connectors);
    }
}
