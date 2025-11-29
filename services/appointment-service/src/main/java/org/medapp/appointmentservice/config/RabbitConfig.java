package org.medapp.appointmentservice.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Value("${appointment.rabbit.exchange:appointment.exchange}")
    private String exchangeName;

    @Value("${appointment.rabbit.routing-key:appointment.created}")
    private String routingKey;

    @Value("${appointment.rabbit.queue:appointment-notifications}")
    private String queueName;

    @Bean
    public TopicExchange appointmentExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue appointmentQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding appointmentBinding(Queue appointmentQueue, TopicExchange appointmentExchange) {
        return BindingBuilder.bind(appointmentQueue).to(appointmentExchange).with(routingKey);
    }
}
