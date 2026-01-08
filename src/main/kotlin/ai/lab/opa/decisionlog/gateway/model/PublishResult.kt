package ai.lab.opa.decisionlog.gateway.model

/**
 * Represents the result of publishing a message to Kafka.
 */
sealed class PublishResult {
    /**
     * Successful publication.
     */
    data object Success : PublishResult()

    /**
     * Failed publication with error details.
     */
    data class Failure(val error: Throwable) : PublishResult()
}

