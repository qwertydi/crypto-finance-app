package com.dmsc.sboot.autoconfigure.coincapjavasdk;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.net.URI;

/**
 * Properties class that will hold values related with the API
 * More properties class could be added to adjust default values, like:
 * - Add auth information for the API
 * - Add OverridableHttpConnectionProperties support to override connection properties
 */
@Data
public class RestClientProperties {
    private @NotNull URI baseUrl;
}
