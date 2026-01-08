package ai.lab.opa.decisionlog.gateway.service

import ai.lab.opa.decisionlog.gateway.model.PublishResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.test.util.ReflectionTestUtils
import java.util.concurrent.CompletableFuture

class DecisionLogPublisherTest {

    private lateinit var kafkaTemplate: KafkaTemplate<String, String>
    private lateinit var publisher: DecisionLogPublisher

    private val testTopic = "test-topic"
    private val testPayload = """[{"decision": "allow", "user": "alice"}]"""

    @BeforeEach
    fun setUp() {
        kafkaTemplate = mockk()
        publisher = DecisionLogPublisher(kafkaTemplate)
        ReflectionTestUtils.setField(publisher, "topic", testTopic)
    }

    @Test
    fun `should return Success when Kafka publish succeeds`() {
        // Given
        val sendResult = mockk<SendResult<String, String>>()
        val future = CompletableFuture.completedFuture(sendResult)
        
        every { 
            kafkaTemplate.send(testTopic, "opa-decision-batch", testPayload) 
        } returns future

        // When
        val result = publisher.publish(testPayload)

        // Then
        assertTrue(result is PublishResult.Success)
        verify(exactly = 1) { 
            kafkaTemplate.send(testTopic, "opa-decision-batch", testPayload) 
        }
    }

    @Test
    fun `should return Failure when Kafka publish fails`() {
        // Given
        val exception = RuntimeException("Kafka connection error")
        val future = CompletableFuture<SendResult<String, String>>()
        future.completeExceptionally(exception)
        
        every { 
            kafkaTemplate.send(testTopic, "opa-decision-batch", testPayload) 
        } returns future

        // When
        val result = publisher.publish(testPayload)

        // Then
        assertTrue(result is PublishResult.Failure)
        assertEquals(exception, (result as PublishResult.Failure).error.cause)
        verify(exactly = 1) { 
            kafkaTemplate.send(testTopic, "opa-decision-batch", testPayload) 
        }
    }
}

