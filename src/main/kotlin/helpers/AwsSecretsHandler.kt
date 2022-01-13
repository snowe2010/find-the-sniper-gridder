package helpers

import Serialization
import kotlinx.serialization.Serializable
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest
import javax.enterprise.context.ApplicationScoped
import javax.inject.Singleton
//import javax.ws.rs.Produces


@Serializable
data class Secrets(
    var imgurClientId: String = "secretWasNotSet",
    var imgurClientSecred: String = "secretWasNotSet",
    var redditClientId: String = "secretWasNotSet",
    var redditClientSecret: String = "secretWasNotSet",
    var username: String = "secretWasNotSet",
    var password: String = "secretWasNotSet",
)

@ApplicationScoped
class SecretsResolver(
    val secretsManagerClient: SecretsManagerClient,
) {
    @Singleton // data class and @ApplicationScoped do not play nice
//    @Produces
    fun resolveSecrets(
        @ConfigProperty(
            name = "secret.id",
            defaultValue = "secret-id-is-not-configured"
        ) secretId: String = "findthesniper-secrets"
    ): Secrets {
        return try {
            val response =
                secretsManagerClient.getSecretValue(GetSecretValueRequest.builder().secretId(secretId).build())
            val secrets = Serialization.json.decodeFromString(Secrets.serializer(), response.secretString())
            logger.info("Secrets have been resolved for secretId: $secretId")
            secrets
        } catch (e: Exception) {
            logger.error("Something went wrong resolving Secrets. Double check that your environment has the correct secretId set and that AWS has that secretId populated with the correct keys.")
            throw e
        }
    }

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
}
