import com.tylerthrailkill.sniper.helpers.SecretsResolver
import com.tylerthrailkill.sniper.helpers.Serialization
import com.tylerthrailkill.sniper.reddit.Listing
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.InputStream
import javax.enterprise.context.ApplicationScoped
import javax.imageio.ImageIO

private val logger = KotlinLogging.logger {}

const val subreddit: String = "snowe2010"
const val userAgent = "web:com.tylerthrailkill.findthesniper-helper:0.0.1 (by /u/snowe2010)"

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
        logger.info { "Authorizing reddit client" }
        val secrets = secretsResolver.resolveSecrets()
        return runBlocking {
            val response: HttpResponse = HttpClient(CIO) {
                install(JsonFeature) {
                    serializer = Serialization.ktorSerializer
                }

                install(Auth) {
                    basic {
                        credentials {
                            BasicAuthCredentials(
                                username = secrets.redditClientId,
                                password = secrets.redditClientSecret
                            )
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
            val (accessToken, _, _, _) = Json.decodeFromString(RedditAuthorizationResponse.serializer(), json)
            accessToken
        }
    }

    suspend fun getLatestPosts(): Listing {
        logger.info { "Getting latest posts" }
        val response: HttpResponse = HttpClient(CIO) {
            install(JsonFeature) {
                serializer = Serialization.ktorSerializer
            }
        }.use { client ->
            client.get("https://oauth.reddit.com/user/$subreddit/new.json?count=20") {
                this.header("User-Agent", userAgent)
                this.header("Authorization", "Bearer $token")
            }
        }
        logger.info { "getLatestPosts headers: ${response.headers}" }
        val json = response.readText()
        logger.info { "Latest Posts: ${json}" }
        return Serialization.json.decodeFromString(Listing.serializer(), json)
    }

    suspend fun downloadImage(imageUrl: String): BufferedImage {
        logger.info { "Downloading image $imageUrl" }
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
    suspend fun commentWithNewPhoto(postId: String, vararg imageUrl: String): Boolean {
        logger.info { "Commenting on $postId with image urls $imageUrl" }
        val response: HttpResponse = HttpClient(CIO) {
            install(JsonFeature) {
                serializer = Serialization.ktorSerializer
            }
        }.use { client ->
            client.post("https://oauth.reddit.com/api/comment") {
                header("User-Agent", userAgent)
                header("Authorization", "Bearer $token")

                parameter("api_type", "json")
                parameter("return_rtjson", true)
                parameter(
                    "text", """
                        Hi, I've placed some grids over your image to help commenters indicate where your sniper is:

                        - [Big Grid](${imageUrl[0]})
                        - [Medium Grid](${imageUrl[1]})
                        - [Small Grid](${imageUrl[2]})

                        ---

                        Some suggestions:

                        - Use the largest grid reasonable to describe your location
                        - You can provide hints by using the Big Grid with a wide range, something like (1,1) to (4,5).
                        - If it's in the center of the image, you probably don't need to bother with coordinates, unless someone asks!

                        The grid uses standard coordinates (x, y) where `x` is the horizontal axis and `y` is the vertical axis. 


                        ---

                        ^^Please ^^contact ^^my ^^creator ^^/u/snowe2010 ^^if ^^you ^^find ^^issues ^^or ^^/u/findthesniper-helper ^^does ^^not ^^comment ^^on ^^your ^^post. ^^Also ^^if ^^you ^^have ^^suggestions ^^for ^^improvement. 
                    """.trimIndent()
                )
                parameter("thing_id", postId)
            }
        }
        logger.info { "commentWithNewPhoto headers: ${response.headers}" }
        val json = response.readText()
        logger.info { "commentWithNewPhoto response: $json" }
        return !json.contains("errors")
    }

}
