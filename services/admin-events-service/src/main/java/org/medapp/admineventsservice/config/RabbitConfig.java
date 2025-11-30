package org.medapp.admineventsservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${appointment.rabbit.notification-queue}")
    private String queueName;

    @Bean
    public Queue adminNotificationQueue() {
        return new Queue(queueName, true);
    }
}