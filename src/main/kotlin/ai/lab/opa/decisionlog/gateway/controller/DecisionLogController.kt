package ai.lab.opa.decisionlog.gateway.controller

import ai.lab.opa.decisionlog.gateway.model.PublishResult
import ai.lab.opa.decisionlog.gateway.service.DecisionLogPublisher
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

/**
 * REST controller for receiving OPA decision logs.
 */
@RestController
@RequestMapping("/decision-logs")
class DecisionLogController(
    private val publisher: DecisionLogPublisher
) {

    /**
     * Receives a batch of decision logs from OPA and publishes to Kafka.
     *
     * @param payload Raw JSON array as string
     * @return Mono with HTTP 201 on success, 500 on failure
     */
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun receiveDecisionLogs(@RequestBody payload: String): Mono<ResponseEntity<Void>> {
        logger.debug { "Received decision log batch, size: ${payload.length} bytes" }
        
        return Mono.fromCallable {
            when (val result = publisher.publish(payload)) {
                is PublishResult.Success -> {
                    logger.info { "Decision log batch published successfully" }
                    ResponseEntity.status(HttpStatus.CREATED).build<Void>()
                }
                is PublishResult.Failure -> {
                    logger.error { "Failed to publish decision log batch: ${result.error.message}" }
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build<Void>()
                }
            }
        }
    }
}

