package org.medapp.notificationservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${notification.rabbit.queue}")
    private String queueName;

    @Bean
    public Queue appointmentNotificationQueue() {
        return new Queue(queueName, true);
    }
}
