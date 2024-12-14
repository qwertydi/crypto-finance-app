package com.dmsc.coincapjavasdk;

import com.dmsc.coincapjavasdk.model.response.CryptoByIdData;
import com.dmsc.coincapjavasdk.model.response.CryptoData;
import com.dmsc.coincapjavasdk.model.request.CryptoDataRequest;
import com.dmsc.coincapjavasdk.model.request.CryptoHistoryRequest;
import com.dmsc.coincapjavasdk.model.response.CryptoPriceHistoryData;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.QueueDispatcher;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WebClientAssetsRestSdkTest {


    private final WebClient.Builder webClientBuilder;
    private WebClientAssetsRestSdk classUnderTest;
    private MockWebServer mockWebServer;

    public WebClientAssetsRestSdkTest() {
        this.webClientBuilder = WebClient.builder();
    }

    @BeforeEach
    void setup() {
        mockWebServer = new MockWebServer();
        ((QueueDispatcher) mockWebServer.getDispatcher()).setFailFast(true);
        this.classUnderTest = new WebClientAssetsRestSdk(webClientBuilder.baseUrl(mockWebServer.url("").toString()));
    }

    @Test
    void testGetAssets() {
        String json = getString("getAssets.json");

        enqueueMockResponse(json, HttpStatus.OK);

        CryptoData assets = classUnderTest.getAssets(new CryptoDataRequest());

        assertValidGetRequest("/assets");

        Assertions.assertNotNull(assets);
    }

    @Test
    void testGetAssetsById() {
        String json = getString("getAssetsById.json");

        enqueueMockResponse(json, HttpStatus.OK);

        CryptoByIdData assets = classUnderTest.getAssetsById("id");

        assertValidGetRequest("/assets/id");

        Assertions.assertNotNull(assets);
    }

    @Test
    void testGetHistoryByAsset() {
        String json = getString("getAssetsHistoryById.json");

        enqueueMockResponse(json, HttpStatus.OK);

        CryptoPriceHistoryData cryptoPriceHistoryData = classUnderTest.getHistoryByAsset("id", new CryptoHistoryRequest());

        assertValidGetRequest("/assets/id/history");

        Assertions.assertNotNull(cryptoPriceHistoryData);
    }

    @SneakyThrows
    @NotNull
    private static String getString(String filename)  {
        // Locate the file in the resources folder
        File file = ResourceUtils.getFile("classpath:" + filename);

        // Read the file content as a string
        return new String(Files.readAllBytes(file.toPath()));
    }

    private void enqueueMockResponse(String body, HttpStatus httpStatus) {
        mockWebServer.enqueue(new MockResponse()
            .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .setBody(body)
            .setResponseCode(httpStatus.value()));
    }

    private void assertValidGetRequest(String expectedPath) {
        assertEquals(1, mockWebServer.getRequestCount());
        final RecordedRequest recordedRequest = assertDoesNotThrow(() -> mockWebServer.takeRequest());
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals(expectedPath, recordedRequest.getPath());
    }
}
