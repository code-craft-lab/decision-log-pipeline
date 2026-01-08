package ai.lab.opa.decisionlog.gateway.config

import org.apache.kafka.clients.producer.ProducerConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

/**
 * Kafka producer configuration.
 */
@Configuration
class KafkaProducerConfig {

    @Value("\${kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${kafka.producer.acks}")
    private lateinit var acks: String

    @Value("\${kafka.producer.retries}")
    private var retries: Int = 3

    @Value("\${kafka.producer.key-serializer}")
    private lateinit var keySerializer: String

    @Value("\${kafka.producer.value-serializer}")
    private lateinit var valueSerializer: String

    @Bean
    fun producerFactory(): ProducerFactory<String, String> {
        val configProps = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to keySerializer,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to valueSerializer,
            ProducerConfig.ACKS_CONFIG to acks,
            ProducerConfig.RETRIES_CONFIG to retries
        )
        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, String> {
        return KafkaTemplate(producerFactory())
    }
}

