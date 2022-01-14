package com.tylerthrailkill.sniper.processing

import mu.KotlinLogging
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.enterprise.context.ApplicationScoped
import javax.imageio.ImageIO
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

@ApplicationScoped
class ImageProcessor {
    private val headerHeight = 20
    private val headerWidth = 20

    fun renderImage(img: BufferedImage): BufferedImage {
        val columnWidth = findClosestWholeNumberPixel(img.width, 108)
        val rowHeight = findClosestWholeNumberPixel(img.height, 108)
        drawGrid(img, columnWidth, rowHeight)
        drawHeaders(img, columnWidth, rowHeight)
        return drawHeaderNames(img, columnWidth, rowHeight)
    }

    fun loadImage(): BufferedImage {
        var img: BufferedImage? = null
        try {
            img = ImageIO.read(File("/Users/tyler/Downloads/findthesniper/bear.jpg"))
        } catch (e: IOException) {
            println("exception $e")
            exitProcess(1)
        }
        return img
    }

    fun drawHeaders(img: BufferedImage, columnWidth: Int, rowHeight: Int) {
        logger.info { "header width $headerWidth" }

    }

    fun drawGrid(img: BufferedImage, columnWidth: Int, rowHeight: Int) {
        val width = img.width
        val height = img.height
        val numberOfColumns = width / columnWidth
        val numberOfRows = height / rowHeight

        val g2d: Graphics2D = img.createGraphics()
        g2d.background = Color.WHITE
        g2d.color = Color.BLACK
        val bs = BasicStroke(2f)
        g2d.stroke = bs

        for (i in 0..numberOfColumns) {
            g2d.drawLine(columnWidth * i, 0, columnWidth * i, height - 1)
        }
        for (i in 0..numberOfRows) {
            g2d.drawLine(0, rowHeight * i, width - 1, rowHeight * i)
        }
    }

    fun drawHeaderNames(img: BufferedImage, columnWidth: Int, rowHeight: Int): BufferedImage {
        val numberOfColumns = img.width / columnWidth
        val numberOfRows = img.height / rowHeight

        val imgWithHeaders = BufferedImage(img.width + headerWidth, img.height + headerHeight, img.type)

        val g2d: Graphics2D = imgWithHeaders.createGraphics()
        g2d.color = Color.WHITE
        g2d.font = Font("Dialog", Font.PLAIN, 20)
        g2d.drawImage(img, headerWidth, headerHeight, null)

        0.until(numberOfColumns).forEach {
            g2d.drawString("$it", headerWidth + (it * columnWidth) + (columnWidth / 2), headerHeight)
        }
        (0..numberOfRows).forEach {
            g2d.drawString("$it", 0, headerHeight + (it * rowHeight) + (rowHeight / 2))
        }
        return imgWithHeaders
    }
    
    fun findClosestWholeNumberPixel(totalPixels: Int, desiredSize: Int): Int {
        var size = desiredSize
        var modifier = 1
        var negative = false
        while (totalPixels.mod(size) != 0) {
            size += modifier
            if (negative) {
                modifier -= 1
                negative = false
            } else {
                modifier += 1
                negative = true
            }
            modifier *= -1
        }
        return size.also { logger.info { "found ideal size of $it" } }
    }
}
