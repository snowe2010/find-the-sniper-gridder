import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Named

data class Test(val str: String)

@Named("LambdaHandler")
class LambdaHandler @Inject constructor(
    val redditApi: RedditApi,
    val imgurApi: ImgurApi,
) : RequestHandler<Unit, Unit> {

    override fun handleRequest(request: Unit, context: Context?): Unit {
        runBlocking {
            val listing = redditApi.getLatestPosts()
            val posts = listing.data.children
            posts.forEach {
                val post = it.data
                if (post.postHint == "image") {
                    println(post.url)
                    val image = redditApi.downloadImage(post.url)
                    val griddedImage = renderImage(image)
                    val imageUrl = imgurApi.uploadPhoto(griddedImage)
                    redditApi.commentWithNewPhoto(post.name, imageUrl)
                }
            }
        }
//        return Test("")
    }
}
