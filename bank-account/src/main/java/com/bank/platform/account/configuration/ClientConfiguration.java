package com.bank.platform.account.configuration;


import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfiguration {

    private static final int CONNECT_READ_TIMEOUT_MILLIS = 65000;
    private static final int MAX_CONNECTIONS = 100;
    private static final int MAX_CONCURRENT_CONNECTIONS_PER_ROUTE = 25;

    @Bean
    public RestClient restClient() {
        final PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(MAX_CONNECTIONS)
                .setMaxConnPerRoute(MAX_CONCURRENT_CONNECTIONS_PER_ROUTE)
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setConnectTimeout(Timeout.ofMilliseconds(CONNECT_READ_TIMEOUT_MILLIS))
                        .setSocketTimeout(Timeout.ofMilliseconds(CONNECT_READ_TIMEOUT_MILLIS))
                        .build())
                .build();

        final CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(RequestConfig.custom().setConnectionRequestTimeout(Timeout.ofMilliseconds(CONNECT_READ_TIMEOUT_MILLIS)).build())
                .build();

        final HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return RestClient.builder()
                .requestFactory(new BufferingClientHttpRequestFactory(requestFactory))
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                })
                .build();
    }
}
