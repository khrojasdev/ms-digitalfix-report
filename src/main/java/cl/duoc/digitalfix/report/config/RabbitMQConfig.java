package cl.duoc.digitalfix.report.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${amqp.exchange.negocio}")
    private String exchangeName;

    @Value("${amqp.queue.reportes}")
    private String queueName;

    @Value("${amqp.queue.reportes-dlq}")
    private String dlqName;

    @Value("${amqp.routing-key.reportes}")
    private String routingKey;

    // 1. Exchange Principal
    @Bean
    public TopicExchange negocioExchange() {
        return new TopicExchange(exchangeName);
    }

    // 2. Dead Letter Queue (DLQ) para apartar eventos fallidos
    @Bean
    public Queue reportesDlq() {
        return QueueBuilder.durable(dlqName).build();
    }

    // 3. Cola Principal (con enlace directo a la DLQ en caso de falla)
    @Bean
    public Queue reportesQueue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", dlqName)
                .build();
    }

    // 4. Binding: Enlaza la cola al exchange usando la routing key
    @Bean
    public Binding reportesBinding(Queue reportesQueue, TopicExchange negocioExchange) {
        return BindingBuilder.bind(reportesQueue).to(negocioExchange).with(routingKey);
    }
}