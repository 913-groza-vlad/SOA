package org.medapp.appointmentservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitConfig {
    @Value("${appointment.rabbit.exchange:appointment.exchange}")
    private String exchangeName;

    @Bean
    public TopicExchange appointmentExchange() {
        return new TopicExchange(exchangeName, true, false);
    }
}
