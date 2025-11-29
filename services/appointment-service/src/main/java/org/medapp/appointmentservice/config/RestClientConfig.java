package org.medapp.appointmentservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Value("${services.patient.base-url:http://patient-service:8081}")
    private String patientBaseUrl;

    @Value("${services.doctor.base-url:http://doctor-service:8082}")
    private String doctorBaseUrl;

    @Bean
    public RestClient patientRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl(patientBaseUrl)
                .build();
    }

    @Bean
    public RestClient doctorRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl(doctorBaseUrl)
                .build();
    }

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
