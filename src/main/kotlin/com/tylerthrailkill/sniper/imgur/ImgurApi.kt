import com.tylerthrailkill.sniper.helpers.SecretsResolver
import com.tylerthrailkill.sniper.helpers.Serialization
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.enterprise.context.ApplicationScoped
import javax.imageio.ImageIO

private val logger = KotlinLogging.logger {}

typealias ImgurUrl = String

@Serializable
data class ImgurResponse(
    val status: Int,
    val success: Boolean,
    val data: ImgurData,
)

@Serializable
data class ImgurData(
    val link: String,
// val id: String,
// val deletehash: String,
// val account_id: String,
// val account_url: String,
// val ad_type: String,
// val ad_url: String,
// val title: String,
// val description: String,
// val name: String,
// val type: String,
// val width: Int,
// val height: Int,
// val size: Int,
// val views: Int,
// val section: String,
// val vote: String,
// val bandwidth: Int,
// val animated: Boolean,
// val favorite: Boolean,
// val in_gallery: Boolean,
// val in_most_viral: Boolean,
// val has_sound: Boolean,
// val is_ad: Boolean,
// val nsfw: String,
// val tags: List<String>,
// val datetime: Int,
// val mp4: String,
// val hls: String,
)

@ApplicationScoped
class ImgurApi(val secretsResolver: SecretsResolver) {
    suspend fun uploadPhoto(bufferedImage: BufferedImage): ImgurUrl {
        logger.info { "Uploading photo to imgur" }
        val secrets = secretsResolver.resolveSecrets()
        val response: HttpResponse = HttpClient(CIO) {
            install(JsonFeature) {
                serializer = Serialization.ktorSerializer
            }
        }.use { client ->
            val parts: List<PartData> = formData {
                val baos = ByteArrayOutputStream()
                ImageIO.write(bufferedImage, "jpg", baos)
                append("image", baos.toByteArray(), Headers.build {
                    append(HttpHeaders.ContentType, "image/jpg")
                    append(HttpHeaders.ContentDisposition, "filename=bear.jpg")
                })
            }
//            client.submitFormWithBinaryData<>()
            client.submitFormWithBinaryData("https://api.imgur.com/3/upload", parts) {
                header("Authorization", "Client-ID ${secrets.imgurClientId}")
            }
        }
        val json = response.readText()
        println("imgur url $json")
        val imgurResponse = Serialization.json.decodeFromString(ImgurResponse.serializer(), json)
        return imgurResponse.data.link
    }
}
