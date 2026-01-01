package org.medapp.admineventsservice.config;

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

    @Value("${appointment.rabbit.routing-key:appointment.*}")
    private String routingKeyPattern;

    @Value("${appointment.rabbit.notification-queue:appointment.admin.queue}")
    private String queueName;

    @Bean
    public TopicExchange appointmentExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue adminQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding adminBinding(Queue adminQueue, TopicExchange appointmentExchange) {
        return BindingBuilder.bind(adminQueue)
                .to(appointmentExchange)
                .with(routingKeyPattern);
    }
}