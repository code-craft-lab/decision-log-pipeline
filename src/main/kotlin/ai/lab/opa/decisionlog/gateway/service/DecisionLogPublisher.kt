package ai.lab.opa.decisionlog.gateway.service

import ai.lab.opa.decisionlog.gateway.model.PublishResult
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

/**
 * Service responsible for publishing decision logs to Kafka.
 */
@Service
class DecisionLogPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {

    @Value("\${kafka.topic.decision-logs}")
    private lateinit var topic: String

    companion object {
        private const val MESSAGE_KEY = "opa-decision-batch"
    }

    /**
     * Publishes a decision log payload to Kafka.
     *
     * @param payload JSON array as string
     * @return PublishResult indicating success or failure
     */
    fun publish(payload: String): PublishResult {
        return try {
            logger.debug { "Publishing decision log to topic: $topic" }
            
            val sendResult = kafkaTemplate.send(topic, MESSAGE_KEY, payload)
            sendResult.get() // Blocking call to ensure synchronous behavior
            
            logger.info { "Successfully published decision log to topic: $topic" }
            PublishResult.Success
        } catch (e: Exception) {
            logger.error(e) { "Failed to publish decision log to Kafka: ${e.message}" }
            PublishResult.Failure(e)
        }
    }
}

