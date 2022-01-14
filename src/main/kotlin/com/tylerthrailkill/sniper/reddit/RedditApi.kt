import helpers.SecretsResolver
import helpers.Serialization
import io.ktor.client.*
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.engine.cio.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.InputStream
import javax.enterprise.context.ApplicationScoped
import javax.imageio.ImageIO


const val subreddit: String = "snowe2010"

@Serializable
data class RedditAuthorizationResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("scope") val scope: String
)

@ApplicationScoped
class RedditApi(val secretsResolver: SecretsResolver) {
    private val token: String by lazy { authorize() }
    
    private fun authorize(): String {
        val secrets = secretsResolver.resolveSecrets()
        return runBlocking {
            val response: HttpResponse = HttpClient(CIO) {
                install(JsonFeature) {
                    serializer = Serialization.ktorSerializer
                }

                install(Auth) {
                    basic {
                        credentials {
                            BasicAuthCredentials(username = secrets.redditClientId, password = secrets.redditClientSecret)
                        }
                    }
                }
            }.use { client ->
                client.post("https://www.reddit.com/api/v1/access_token/") {
                    this.parameter("grant_type", "password")
                    this.parameter("username", secrets.redditUsername)
                    this.parameter("password", secrets.redditPassword)
                }
            }
            val json = response.readText()
            println("password grant access token is")
            println(json)
            val (accessToken, _, _, _) = Json.decodeFromString(RedditAuthorizationResponse.serializer(), json)
            accessToken
        }
    }

    suspend fun getLatestPosts(): Listing {
        val response: HttpResponse = HttpClient(CIO) {
            install(JsonFeature) {
                serializer = Serialization.ktorSerializer
            }
        }.use { client ->
            client.get("https://oauth.reddit.com/user/$subreddit/new.json?count=20") {
                this.header("User-Agent", "findthesniper-helper-0.0.1")
                this.header("Authorization", "Bearer $token")
            }
        }
        val json = response.readText()
        return Serialization.json.decodeFromString(Listing.serializer(), json)
    }

    suspend fun downloadImage(imageUrl: String): BufferedImage {
        val bytes = HttpClient(CIO).get<ByteArray>(imageUrl)
        val inputStream: InputStream = ByteArrayInputStream(bytes)
        val bi = ImageIO.read(inputStream)
        return bi
    }

    /**
     * api_type: the string json
    return_rtjson: boolean value
    richtext_json: JSON data
    text: raw markdown text
    thing_id: fullname of parent thing
    uh / X-Modhash header: a modhash
     */
    suspend fun commentWithNewPhoto(postId: String, imageUrl: String) {
        val response: HttpResponse = HttpClient(CIO) {
            install(JsonFeature) {
                serializer = Serialization.ktorSerializer
            }
        }.use { client ->
            client.post("https://oauth.reddit.com/api/comment") {
                header("User-Agent", "findthesniper-helper-0.0.1")
                header("Authorization", "Bearer $token")
                
                parameter("api_type", "json")
                parameter("return_rtjson", true)
                parameter(
                    "text", """
                    should be an image here
                    $imageUrl
                """.trimIndent()
                )
                parameter("thing_id", postId)
            }
        }
        val json = response.readText()
        println(json)
    }

}
