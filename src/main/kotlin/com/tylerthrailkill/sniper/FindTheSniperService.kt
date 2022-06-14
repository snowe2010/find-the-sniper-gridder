package com.tylerthrailkill.sniper

import ImgurApi
import RedditApi
import com.tylerthrailkill.sniper.aws.DynamoDbService
import com.tylerthrailkill.sniper.processing.GridSize
import com.tylerthrailkill.sniper.processing.ImageProcessor
import io.quarkus.runtime.configuration.ProfileManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import org.eclipse.microprofile.config.inject.ConfigProperty
import javax.enterprise.context.ApplicationScoped


private val logger = KotlinLogging.logger {}

@ApplicationScoped
class FindTheSniperService(
    val redditApi: RedditApi,
    val imgurApi: ImgurApi,
    val imageProcessor: ImageProcessor,
    val dynamo: DynamoDbService,
    @ConfigProperty(name = "make-real-comment") val makeRealComment: Boolean,
) {

    fun commentOnNewPosts() {
        runBlocking {
            withTimeout(300000L) {
                val all = dynamo.getAll()
                logger.info { all }
                val listing = redditApi.getLatestPosts()
                val posts = listing.data.children
                posts
                    .filterNot { all.contains(it.data.name) }
                    .forEach {
                        val post = it.data
                        if (post.postHint == "image") {
                            logger.info { "Post URL: ${post.url}" }
                            val originalImage = redditApi.downloadImage(post.url)

                            val profile = ProfileManager.getActiveProfile()

                            val bigImage = imageProcessor.renderImage(originalImage, GridSize.Big)
                            val mediumImage = imageProcessor.renderImage(originalImage, GridSize.Medium)
                            val smallImage = imageProcessor.renderImage(originalImage, GridSize.Small)

                            if (profile == "dev") {
                                DebugImage.postImage(bigImage, mediumImage, smallImage)
                            }
                            if (makeRealComment) {
                                println("running prod")
                                val bigImageUrl = imgurApi.uploadPhoto(bigImage)
                                val mediumImageUrl = imgurApi.uploadPhoto(mediumImage)
                                val smallImageUrl = imgurApi.uploadPhoto(smallImage)

                                val success =
                                    redditApi.commentWithNewPhoto(
                                        post.name,
                                        bigImageUrl,
                                        mediumImageUrl,
                                        smallImageUrl
                                    )
                                if (success) {
                                    logger.info { "saving ${post.name} in dynamodb" }
                                    dynamo.save(it)
                                } else {
                                    logger.info { "NOT SAVING ${post.name} in dynamodb" }
                                }
                            }
                        } else {
                            logger.info { "Skipping ${post.name} because it wasn't of type image" }
                        }
                    }
            }
        }
    }
}
