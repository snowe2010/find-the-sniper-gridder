package com.tylerthrailkill.sniper

import ImgurApi
import RedditApi
import com.tylerthrailkill.sniper.aws.DynamoDbService
import com.tylerthrailkill.sniper.processing.GridSize
import com.tylerthrailkill.sniper.processing.ImageProcessor
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import java.awt.image.BufferedImage
import javax.enterprise.context.ApplicationScoped
import javax.swing.ImageIcon
import javax.swing.JOptionPane

private val logger = KotlinLogging.logger {}

@ApplicationScoped
class FindTheSniperService(
    val redditApi: RedditApi,
    val imgurApi: ImgurApi,
    val imageProcessor: ImageProcessor,
    val dynamo: DynamoDbService,
) {
    private fun debugImages(vararg images: BufferedImage) {
        images.forEach {
            val icon = ImageIcon(it)
            JOptionPane.showMessageDialog(null, icon)
        }
    }

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
                            val image = redditApi.downloadImage(post.url)

                            val bigImage = imageProcessor.renderImage(image, GridSize.Big)
                            val mediumImage = imageProcessor.renderImage(image, GridSize.Medium)
                            val smallImage = imageProcessor.renderImage(image, GridSize.Small)
//                        debugImages(bigImage, mediumImage, smallImage)

                            val bigImageUrl = imgurApi.uploadPhoto(bigImage)
                            val mediumImageUrl = imgurApi.uploadPhoto(mediumImage)
                            val smallImageUrl = imgurApi.uploadPhoto(smallImage)
//
                            val success =
                                redditApi.commentWithNewPhoto(post.name, bigImageUrl, mediumImageUrl, smallImageUrl)
                            if (success) {
                                logger.info { "saving ${post.name} in dynamodb" }
                                dynamo.save(it)
                            } else {
                                logger.info { "NOT SAVING ${post.name} in dynamodb" }
                            }
                        } else {
                            logger.info { "Skipping ${post.name} because it wasn't of type image" }
                        }
                    }
            }
        }
    }
}
