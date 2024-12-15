package com.dmsc.cryptofinanceservice.configuration;

import com.dmsc.cryptofinanceservice.properties.JobServiceProperties;
import com.dmsc.cryptofinanceservice.properties.WalletRequestProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    @Bean
    @ConfigurationProperties(prefix = WalletRequestProperties.PREFIX)
    public WalletRequestProperties walletRequestProperties() {
        return new WalletRequestProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = JobServiceProperties.PREFIX)
    public JobServiceProperties jobServiceProperties() {
        return new JobServiceProperties();
    }
}
