package com.shopsense.connector;

import com.shopsense.entity.Product;
import com.shopsense.entity.ProductVariant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectorManagerTest {

    private AmazonConnector amazonConnector;
    private FlipkartConnector flipkartConnector;
    private CromaConnector cromaConnector;
    private ConnectorManager connectorManager;
    private ProductVariant sampleVariant;

    @BeforeEach
    void setUp() {
        amazonConnector = new AmazonConnector();
        flipkartConnector = new FlipkartConnector();
        cromaConnector = new CromaConnector();

        connectorManager = new ConnectorManager(List.of(amazonConnector, flipkartConnector, cromaConnector));

        Product product = Product.builder()
                .id(100L)
                .brand("Apple")
                .series("iPhone")
                .model("16 Pro")
                .build();

        sampleVariant = ProductVariant.builder()
                .id(1002L)
                .product(product)
                .variantName("256GB / Natural Titanium")
                .isDefault(false)
                .build();
    }

    @AfterEach
    void tearDown() {
        amazonConnector.reset();
        flipkartConnector.reset();
        cromaConnector.reset();
    }

    @Test
    @DisplayName("ConnectorManager coordinates multiple connectors and returns all available connectors")
    void testConnectorManager_coordinatesMultipleConnectors() {
        List<MarketplaceConnector> connectors = connectorManager.getAvailableConnectors();
        assertEquals(3, connectors.size());
        assertTrue(connectors.stream().anyMatch(c -> "Amazon".equals(c.getPlatformName())));
        assertTrue(connectors.stream().anyMatch(c -> "Flipkart".equals(c.getPlatformName())));
        assertTrue(connectors.stream().anyMatch(c -> "Croma".equals(c.getPlatformName())));
    }

    @Test
    @DisplayName("Multiple successful connectors return multiple normalized results")
    void testConnectorManager_multipleSuccessfulConnectorsReturnMultipleResults() {
        List<ConnectorResult> results = connectorManager.fetchOffersForVariant(sampleVariant);

        assertEquals(3, results.size());
        for (ConnectorResult result : results) {
            assertEquals(ConnectorStatus.AVAILABLE, result.getStatus());
            assertNotNull(result.getOffer());
            assertNotNull(result.getOffer().getPlatformName());
            assertNotNull(result.getOffer().getCurrentPrice());
        }
    }

    @Test
    @DisplayName("One failed connector does not prevent other connectors from returning results (Failure Isolation)")
    void testConnectorManager_oneFailedConnectorDoesNotPreventOtherResults() {
        cromaConnector.setSimulatedStatus(ConnectorStatus.UNAVAILABLE);

        List<ConnectorResult> results = connectorManager.fetchOffersForVariant(sampleVariant);

        assertEquals(3, results.size());

        ConnectorResult amazonResult = results.stream()
                .filter(r -> "Amazon".equalsIgnoreCase(r.getPlatformName()))
                .findFirst().orElseThrow();
        assertEquals(ConnectorStatus.AVAILABLE, amazonResult.getStatus());
        assertNotNull(amazonResult.getOffer());

        ConnectorResult flipkartResult = results.stream()
                .filter(r -> "Flipkart".equalsIgnoreCase(r.getPlatformName()))
                .findFirst().orElseThrow();
        assertEquals(ConnectorStatus.AVAILABLE, flipkartResult.getStatus());
        assertNotNull(flipkartResult.getOffer());

        ConnectorResult cromaResult = results.stream()
                .filter(r -> "Croma".equalsIgnoreCase(r.getPlatformName()))
                .findFirst().orElseThrow();
        assertEquals(ConnectorStatus.UNAVAILABLE, cromaResult.getStatus());
        assertNull(cromaResult.getOffer());
        assertNotNull(cromaResult.getErrorMessage());
    }

    @Test
    @DisplayName("Connector exceptions are isolated by ConnectorManager without failing overall request")
    void testConnectorManager_isolatesConnectorExceptions() {
        cromaConnector.setSimulateException(true, "Unexpected Croma API timeout");

        List<ConnectorResult> results = connectorManager.fetchOffersForVariant(sampleVariant);

        assertEquals(3, results.size());

        long availableCount = results.stream().filter(r -> r.getStatus() == ConnectorStatus.AVAILABLE).count();
        assertEquals(2, availableCount);

        ConnectorResult cromaResult = results.stream()
                .filter(r -> "Croma".equalsIgnoreCase(r.getPlatformName()))
                .findFirst().orElseThrow();
        assertEquals(ConnectorStatus.UNAVAILABLE, cromaResult.getStatus());
        assertNull(cromaResult.getOffer());
        assertTrue(cromaResult.getErrorMessage().contains("Unexpected Croma API timeout"));
    }

    @Test
    @DisplayName("NO_OFFER status is handled separately from UNAVAILABLE")
    void testConnectorManager_handlesNoOfferSeparatelyFromUnavailable() {
        flipkartConnector.setSimulatedStatus(ConnectorStatus.NO_OFFER);
        cromaConnector.setSimulatedStatus(ConnectorStatus.UNAVAILABLE);

        List<ConnectorResult> results = connectorManager.fetchOffersForVariant(sampleVariant);

        assertEquals(3, results.size());

        ConnectorResult amazonResult = results.stream()
                .filter(r -> "Amazon".equalsIgnoreCase(r.getPlatformName()))
                .findFirst().orElseThrow();
        assertEquals(ConnectorStatus.AVAILABLE, amazonResult.getStatus());

        ConnectorResult flipkartResult = results.stream()
                .filter(r -> "Flipkart".equalsIgnoreCase(r.getPlatformName()))
                .findFirst().orElseThrow();
        assertEquals(ConnectorStatus.NO_OFFER, flipkartResult.getStatus());

        ConnectorResult cromaResult = results.stream()
                .filter(r -> "Croma".equalsIgnoreCase(r.getPlatformName()))
                .findFirst().orElseThrow();
        assertEquals(ConnectorStatus.UNAVAILABLE, cromaResult.getStatus());
    }

    @Test
    @DisplayName("ConnectorManager filters by active platforms list when provided")
    void testConnectorManager_filtersActivePlatforms() {
        List<ConnectorResult> results = connectorManager.fetchOffersForVariant(sampleVariant,
                List.of("Amazon", "Flipkart"));

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(r -> "Amazon".equalsIgnoreCase(r.getPlatformName())));
        assertTrue(results.stream().anyMatch(r -> "Flipkart".equalsIgnoreCase(r.getPlatformName())));
        assertFalse(results.stream().anyMatch(r -> "Croma".equalsIgnoreCase(r.getPlatformName())));
    }

    @Test
    @DisplayName("No marketplace-specific response structure leaks outside connector framework")
    void testConnectorManager_noMarketplaceSpecificStructureLeaked() {
        List<ConnectorResult> results = connectorManager.fetchOffersForVariant(sampleVariant);

        for (ConnectorResult result : results) {
            assertTrue(result instanceof ConnectorResult);
            if (result.getOffer() != null) {
                assertTrue(result.getOffer() instanceof NormalizedOffer);
            }
        }
    }

    @Test
    @DisplayName("fetchOfferFromPlatform returns result for specific platform")
    void testConnectorManager_fetchOfferFromPlatform() {
        ConnectorResult result = connectorManager.fetchOfferFromPlatform("Amazon", sampleVariant);
        assertNotNull(result);
        assertEquals("Amazon", result.getPlatformName());
        assertEquals(ConnectorStatus.AVAILABLE, result.getStatus());
        assertNotNull(result.getOffer());

        ConnectorResult nonExistent = connectorManager.fetchOfferFromPlatform("NonExistent", sampleVariant);
        assertNotNull(nonExistent);
        assertEquals(ConnectorStatus.UNAVAILABLE, nonExistent.getStatus());
        assertTrue(nonExistent.getErrorMessage().contains("No connector found"));
    }
}
