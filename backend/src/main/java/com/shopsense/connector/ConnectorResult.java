package com.shopsense.connector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectorResult {

    private String platformName;
    private ConnectorStatus status;
    private NormalizedOffer offer;
    private String message;
    private String errorMessage;

    public static ConnectorResult available(NormalizedOffer offer) {
        return ConnectorResult.builder()
                .platformName(offer != null ? offer.getPlatformName() : null)
                .status(ConnectorStatus.AVAILABLE)
                .offer(offer)
                .build();
    }

    public static ConnectorResult noOffer(String platformName, String message) {
        return ConnectorResult.builder()
                .platformName(platformName)
                .status(ConnectorStatus.NO_OFFER)
                .message(message)
                .build();
    }

    public static ConnectorResult unavailable(String platformName, String errorMessage) {
        return ConnectorResult.builder()
                .platformName(platformName)
                .status(ConnectorStatus.UNAVAILABLE)
                .errorMessage(errorMessage)
                .build();
    }
}
