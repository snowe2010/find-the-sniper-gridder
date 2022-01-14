package com.tylerthrailkill.sniper

import ImgurApi
import RedditApi
import com.tylerthrailkill.sniper.processing.GridSize
import com.tylerthrailkill.sniper.processing.ImageProcessor
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import javax.enterprise.context.ApplicationScoped
import javax.swing.ImageIcon
import javax.swing.JOptionPane

private val logger = KotlinLogging.logger {}

@ApplicationScoped
class FindTheSniperService(
    val redditApi: RedditApi,
    val imgurApi: ImgurApi,
    val imageProcessor: ImageProcessor,
) {
    fun commentOnNewPosts() {
        runBlocking {
            val listing = redditApi.getLatestPosts()
            val posts = listing.data.children
            posts.forEach {
                val post = it.data
                if (post.postHint == "image") {
                    logger.info { "Post URL: ${post.url}" }
                    val image = redditApi.downloadImage(post.url)
                    val griddedImage = imageProcessor.renderImage(image, GridSize.Big)
                    val ii = ImageIcon(griddedImage)
                    JOptionPane.showMessageDialog(null, ii)
                    
                    val mediumGriddedImage = imageProcessor.renderImage(image, GridSize.Medium)
                    val mi = ImageIcon(mediumGriddedImage)
                    JOptionPane.showMessageDialog(null, mi)
                    
                    val small = imageProcessor.renderImage(image, GridSize.Small)
                    val si = ImageIcon(small)
                    JOptionPane.showMessageDialog(null, si)
//                    val imageUrl = imgurApi.uploadPhoto(griddedImage)
//                    redditApi.commentWithNewPhoto(post.name, imageUrl)
                }
            }
        }
    }
}
