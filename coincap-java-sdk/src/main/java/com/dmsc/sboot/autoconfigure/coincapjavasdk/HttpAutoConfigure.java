package com.dmsc.sboot.autoconfigure.coincapjavasdk;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration(proxyBeanMethods = false)
public class HttpAutoConfigure {

    @Lazy
    @Bean
    @ConditionalOnMissingBean
    public WebClientBuilderSupplier mpesaWebClientBuilderSupplier() {
        // NOTE: this is not the only possible fix, but is the one we'll
        // be using most of the time, hence the suggestion...
        throw new BeanInstantiationException(WebClientBuilderSupplier.class,
            "Missing dependencies (try adding " +
                "\"org.springframework.boot:spring-boot-starter-webflux\"" +
                " to your POM)");
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(reactor.netty.http.client.HttpClient.class)
    public static class ReactorNetty {
        @Lazy
        @Bean
        public WebClientBuilderSupplier nettyWebClientBuilderSupplier(
            ObjectProvider<WebClient.Builder> webClientBuilder) {
            return properties -> {
                /* We only create a new custom WebClient.Builder if the given
                 * properties change anything from the defaults. Otherwise, we
                 * just return Spring's existing builder.
                 */
                final WebClient.Builder builder = webClientBuilder.getIfUnique();
                if (builder == null) {
                    throw new BeanInstantiationException(WebClient.Builder.class,
                        "Could not create default builder");
                }
                if (properties.getBaseUrl() != null) {
                    builder.baseUrl(properties.getBaseUrl().toString());
                }
                return builder;
            };
        }
    }
}
