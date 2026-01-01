package org.medapp.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitConfig {
    @Value("${appointment.rabbit.exchange:appointment.exchange}")
    private String appointmentExchangeName;

    @Value("${appointment.rabbit.routing-key:appointment.*}")
    private String routingKeyPattern;

    @Value("${notification.rabbit.queue:appointment.notifications.queue}")
    private String notificationQueueName;

    @Bean
    public TopicExchange appointmentExchange() {
        return new TopicExchange(appointmentExchangeName, true, false);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(notificationQueueName).build();
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange appointmentExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(appointmentExchange)
                .with(routingKeyPattern);
    }
}
