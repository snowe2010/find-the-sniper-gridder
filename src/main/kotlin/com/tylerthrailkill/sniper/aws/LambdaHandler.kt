import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.tylerthrailkill.sniper.processing.ImageProcessor
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Named

class A

@Named("LambdaHandler")
class LambdaHandler @Inject constructor(
    val redditApi: RedditApi,
    val imgurApi: ImgurApi,
    val imageProcessor: ImageProcessor,
) : RequestHandler<Unit, A> {

    override fun handleRequest(request: Unit, context: Context?): A {
        runBlocking {
            val listing = redditApi.getLatestPosts()
            val posts = listing.data.children
            posts.forEach {
                val post = it.data
                if (post.postHint == "image") {
                    println(post.url)
                    val image = redditApi.downloadImage(post.url)
                    val griddedImage = imageProcessor.renderImage(image)
                    val imageUrl = imgurApi.uploadPhoto(griddedImage)
                    redditApi.commentWithNewPhoto(post.name, imageUrl)
                }
            }
        }
        return A() // have to return object to avoid weird serialization error...
    }
}
