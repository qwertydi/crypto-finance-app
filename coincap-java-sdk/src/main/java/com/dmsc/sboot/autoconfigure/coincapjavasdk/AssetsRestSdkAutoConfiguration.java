package com.dmsc.sboot.autoconfigure.coincapjavasdk;

import com.dmsc.coincapjavasdk.WebClientAssetsRestSdk;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.validation.annotation.Validated;

@Lazy
@Configuration
@AutoConfigureAfter(HttpAutoConfigure.class)
public class AssetsRestSdkAutoConfiguration {
    private static final String PROPERTIES_PREFIX = "coincap.sdk";

    @Bean
    @Validated
    @ConfigurationProperties(PROPERTIES_PREFIX)
    public RestClientProperties coincapRestClientProperties() {
        return new RestClientProperties();
    }

    @Bean
    @ConditionalOnBean(value = WebClientBuilderSupplier.class)
    public WebClientAssetsRestSdk webClientAssetsRestSdk(
        RestClientProperties coincapRestClientProperties,
        WebClientBuilderSupplier webClientBuilderSupplier) {
        return new WebClientAssetsRestSdk(webClientBuilderSupplier.createBuilder(coincapRestClientProperties));
    }
}
