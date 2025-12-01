package org.medapp.notificationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient patientRestClient(
            RestClient.Builder builder,
            @Value("${services.patient.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean
    public RestClient doctorRestClient(
            RestClient.Builder builder,
            @Value("${services.doctor.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}