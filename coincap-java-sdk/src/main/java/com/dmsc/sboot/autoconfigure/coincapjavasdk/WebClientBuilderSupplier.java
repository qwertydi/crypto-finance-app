package com.dmsc.sboot.autoconfigure.coincapjavasdk;

import org.springframework.web.reactive.function.client.WebClient;

@FunctionalInterface
public interface WebClientBuilderSupplier {
    WebClient.Builder createBuilder(RestClientProperties properties);

    default WebClient.Builder createBuilderWithDefaultProperties() {
        return this.createBuilder(new RestClientProperties());
    }
}
