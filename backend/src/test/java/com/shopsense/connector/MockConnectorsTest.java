package com.shopsense.connector;

import com.shopsense.entity.Product;
import com.shopsense.entity.ProductVariant;
import com.shopsense.exception.ConnectorException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MockConnectorsTest {

    private AmazonConnector amazonConnector;
    private FlipkartConnector flipkartConnector;
    private CromaConnector cromaConnector;
    private ProductVariant sampleVariant;

    @BeforeEach
    void setUp() {
        amazonConnector = new AmazonConnector();
        flipkartConnector = new FlipkartConnector();
        cromaConnector = new CromaConnector();

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
    @DisplayName("AmazonConnector returns normalized data for valid product variant")
    void testAmazonConnector_returnsNormalizedData() {
        assertEquals("Amazon", amazonConnector.getPlatformName());
        ConnectorResult result = amazonConnector.fetchOffer(sampleVariant);

        assertNotNull(result);
        assertEquals(ConnectorStatus.AVAILABLE, result.getStatus());
        assertNotNull(result.getOffer());

        NormalizedOffer offer = result.getOffer();
        assertEquals("Amazon", offer.getPlatformName());
        assertEquals(1002L, offer.getProductVariantId());
        assertEquals("256GB / Natural Titanium", offer.getProductVariantName());
        assertNotNull(offer.getCurrentPrice());
        assertNotNull(offer.getOriginalPrice());
        assertTrue(offer.getOriginalPrice().compareTo(offer.getCurrentPrice()) >= 0);
        assertEquals("INR", offer.getCurrency());
        assertNotNull(offer.getSellerName());
        assertNotNull(offer.getSellerRating());
        assertEquals("IN_STOCK", offer.getAvailabilityStatus());
        assertNotNull(offer.getDeliveryInfo());
        assertNotNull(offer.getProductUrl());
        assertTrue(offer.getProductUrl().contains("amazon.in"));
        assertNotNull(offer.getRetrievedAt());
    }

    @Test
    @DisplayName("FlipkartConnector returns normalized data for valid product variant")
    void testFlipkartConnector_returnsNormalizedData() {
        assertEquals("Flipkart", flipkartConnector.getPlatformName());
        ConnectorResult result = flipkartConnector.fetchOffer(sampleVariant);

        assertNotNull(result);
        assertEquals(ConnectorStatus.AVAILABLE, result.getStatus());
        assertNotNull(result.getOffer());

        NormalizedOffer offer = result.getOffer();
        assertEquals("Flipkart", offer.getPlatformName());
        assertEquals(1002L, offer.getProductVariantId());
        assertEquals("256GB / Natural Titanium", offer.getProductVariantName());
        assertNotNull(offer.getCurrentPrice());
        assertNotNull(offer.getOriginalPrice());
        assertEquals("INR", offer.getCurrency());
        assertNotNull(offer.getSellerName());
        assertNotNull(offer.getSellerRating());
        assertEquals("IN_STOCK", offer.getAvailabilityStatus());
        assertNotNull(offer.getDeliveryInfo());
        assertNotNull(offer.getProductUrl());
        assertTrue(offer.getProductUrl().contains("flipkart.com"));
        assertNotNull(offer.getRetrievedAt());
    }

    @Test
    @DisplayName("CromaConnector returns normalized data for valid product variant")
    void testCromaConnector_returnsNormalizedData() {
        assertEquals("Croma", cromaConnector.getPlatformName());
        ConnectorResult result = cromaConnector.fetchOffer(sampleVariant);

        assertNotNull(result);
        assertEquals(ConnectorStatus.AVAILABLE, result.getStatus());
        assertNotNull(result.getOffer());

        NormalizedOffer offer = result.getOffer();
        assertEquals("Croma", offer.getPlatformName());
        assertEquals(1002L, offer.getProductVariantId());
        assertEquals("256GB / Natural Titanium", offer.getProductVariantName());
        assertNotNull(offer.getCurrentPrice());
        assertNotNull(offer.getOriginalPrice());
        assertEquals("INR", offer.getCurrency());
        assertNotNull(offer.getSellerName());
        assertNotNull(offer.getSellerRating());
        assertEquals("IN_STOCK", offer.getAvailabilityStatus());
        assertNotNull(offer.getDeliveryInfo());
        assertNotNull(offer.getProductUrl());
        assertTrue(offer.getProductUrl().contains("croma.com"));
        assertNotNull(offer.getRetrievedAt());
    }

    @Test
    @DisplayName("Mock connector returns NO_OFFER status when configured")
    void testMockConnector_noOffer() {
        amazonConnector.setSimulatedStatus(ConnectorStatus.NO_OFFER);
        ConnectorResult result = amazonConnector.fetchOffer(sampleVariant);

        assertNotNull(result);
        assertEquals(ConnectorStatus.NO_OFFER, result.getStatus());
        assertNull(result.getOffer());
        assertNotNull(result.getMessage());
    }

    @Test
    @DisplayName("Mock connector returns UNAVAILABLE status when configured")
    void testMockConnector_unavailable() {
        flipkartConnector.setSimulatedStatus(ConnectorStatus.UNAVAILABLE);
        ConnectorResult result = flipkartConnector.fetchOffer(sampleVariant);

        assertNotNull(result);
        assertEquals(ConnectorStatus.UNAVAILABLE, result.getStatus());
        assertNull(result.getOffer());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    @DisplayName("Mock connector throws ConnectorException when configured to simulate exception")
    void testMockConnector_exceptionThrown() {
        cromaConnector.setSimulateException(true, "Network connection timeout");
        assertThrows(ConnectorException.class, () -> cromaConnector.fetchOffer(sampleVariant));
    }
}
