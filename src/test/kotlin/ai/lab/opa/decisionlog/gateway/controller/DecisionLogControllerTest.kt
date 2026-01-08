package ai.lab.opa.decisionlog.gateway.controller

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(
    partitions = 1,
    topics = ["decision-logs"],
    brokerProperties = ["listeners=PLAINTEXT://localhost:9093", "port=9093"]
)
@TestPropertySource(
    properties = [
        "kafka.bootstrap-servers=\${spring.embedded.kafka.brokers}",
        "kafka.topic.decision-logs=decision-logs"
    ]
)
@DirtiesContext
class DecisionLogControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    @Test
    fun `should return 201 and publish to Kafka when receiving valid decision logs`() {
        // Given
        val payload = """[{"decision": "allow", "user": "alice"}, {"decision": "deny", "user": "bob"}]"""

        // Setup consumer with unique group ID to avoid offset issues
        val consumerProps = KafkaTestUtils.consumerProps(
            "test-group-${System.currentTimeMillis()}",
            "true",
            embeddedKafkaBroker
        ).apply {
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest") // Changed to latest to only get new messages
        }

        val consumerFactory = DefaultKafkaConsumerFactory<String, String>(consumerProps)
        val consumer = consumerFactory.createConsumer()
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "decision-logs")

        // Consume any existing messages
        consumer.poll(Duration.ofMillis(100))

        // When - send HTTP request
        webTestClient.post()
            .uri("/decision-logs")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .exchange()
            .expectStatus().isCreated

        // Then - verify message was published to Kafka
        val records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10))
        assert(records.count() >= 1) { "Expected at least one record, got ${records.count()}" }
        
        val lastRecord = records.records("decision-logs").last()
        assert(lastRecord.key() == "opa-decision-batch") { "Expected key 'opa-decision-batch', got '${lastRecord.key()}'" }
        assert(lastRecord.value() == payload) { "Expected payload '$payload', got '${lastRecord.value()}'" }
        
        consumer.close()
    }

    @Test
    fun `should accept empty JSON array`() {
        // Given
        val payload = "[]"

        // When & Then
        webTestClient.post()
            .uri("/decision-logs")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .exchange()
            .expectStatus().isCreated
    }
}

