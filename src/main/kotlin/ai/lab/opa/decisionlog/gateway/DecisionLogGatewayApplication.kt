package ai.lab.opa.decisionlog.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main application class for OPA Decision Log Gateway.
 */
@SpringBootApplication
class DecisionLogGatewayApplication

fun main(args: Array<String>) {
    runApplication<DecisionLogGatewayApplication>(*args)
}

