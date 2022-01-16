package com.tylerthrailkill.sniper.processing

import mu.KotlinLogging
import java.awt.*
import java.awt.font.TextAttribute
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.enterprise.context.ApplicationScoped
import javax.imageio.ImageIO
import kotlin.system.exitProcess


private val logger = KotlinLogging.logger {}

enum class GridSize(val desiredCellSize: Int) {
    Big(108), Medium(70), Small(40)
}

@ApplicationScoped
class ImageProcessor {
    fun renderImage(img: BufferedImage, gridSize: GridSize): BufferedImage {
        val columnWidth = findClosestWholeNumberPixel(img.width, gridSize.desiredCellSize)
        val rowHeight = findClosestWholeNumberPixel(img.height, gridSize.desiredCellSize)

        val image = deepCopyImage(img)
        drawGrid(image, columnWidth, rowHeight)
        return drawHeaderNames(image, columnWidth, rowHeight)
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


    fun drawGrid(img: BufferedImage, columnWidth: Int, rowHeight: Int) {
        val width = img.width
        val height = img.height
        val numberOfColumns = width / columnWidth
        val numberOfRows = height / rowHeight

        val g2d: Graphics2D = img.createGraphics()
        g2d.background = Color.WHITE
        g2d.color = Color.BLACK
        val bs = BasicStroke(1f)
        g2d.stroke = bs

        0.until(numberOfColumns).forEach {
            if (it.mod(2) == 0) {
                g2d.color = Color.DARK_GRAY
            } else {
                g2d.color = Color(0, 255, 255, 90)
            }
            g2d.drawLine(columnWidth * it - 2, 0, columnWidth * it - 2, height - 1)
            g2d.drawLine(columnWidth * it + 2, 0, columnWidth * it + 2, height - 1)


            if (it.mod(2) == 0) {
                g2d.color = Color.LIGHT_GRAY
            } else {
                g2d.color = Color(60, 160, 30, 140)
            }
            g2d.drawLine(columnWidth * it, 0, columnWidth * it, height - 1)
        }
        0.until(numberOfRows).forEach {
            if (it.mod(2) == 0) {
                g2d.color = Color.DARK_GRAY
            } else {
                g2d.color = Color(0, 255, 255, 90)
            }
            g2d.drawLine(0, rowHeight * it - 2, width - 1, rowHeight * it - 2)
            g2d.drawLine(0, rowHeight * it + 2, width - 1, rowHeight * it + 2)

            if (it.mod(2) == 0) {
                g2d.color = Color.LIGHT_GRAY
            } else {
                g2d.color = Color(60, 160, 30, 140)
            }
            g2d.drawLine(0, rowHeight * it, width - 1, rowHeight * it)
        }
    }

    fun drawHeaderNames(img: BufferedImage, columnWidth: Int, rowHeight: Int): BufferedImage {
        val numberOfColumns = img.width / columnWidth
        val numberOfRows = img.height / rowHeight
        val font = Font.decode("Dialog")
        val (fontWidth, fontHeight) = deriveHeaderSizesFromFont(img, font, columnWidth)

        val columnHeaderHeight = fontHeight.toInt() + 10
        val rowHeaderWidth = fontWidth.toInt() + 10

        val imgWithHeaders = BufferedImage(img.width + rowHeaderWidth, img.height + columnHeaderHeight, img.type)

        val g2d: Graphics2D = imgWithHeaders.createGraphics()
        g2d.color = Color.WHITE
        g2d.drawImage(img, rowHeaderWidth, columnHeaderHeight, null)

        0.until(numberOfColumns).forEach {
            val rect = Rectangle(rowHeaderWidth + (it * columnWidth), 0, columnWidth, columnHeaderHeight)
            drawCenteredString(g2d, "$it", rect, font.deriveFont(fontWidth))
        }
        0.until(numberOfRows).forEach {
            val rect = Rectangle(0, columnHeaderHeight + (it * rowHeight), rowHeaderWidth, rowHeight)
            drawCenteredString(g2d, "$it", rect, font.deriveFont(fontWidth))
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

    private fun deepCopyImage(bi: BufferedImage): BufferedImage {
        val cm = bi.colorModel
        val isAlphaPremultiplied = cm.isAlphaPremultiplied
        val raster = bi.copyData(bi.raster.createCompatibleWritableRaster())
        return BufferedImage(cm, raster, isAlphaPremultiplied, null)
    }

    /**
     * Draw a String centered in the middle of a Rectangle.
     *
     * @param g The Graphics instance.
     * @param text The String to draw.
     * @param rect The Rectangle to center the text in.
     */
    fun drawCenteredString(g: Graphics, text: String, rect: Rectangle, font: Font?) {
        // Get the FontMetrics
        val metrics = g.getFontMetrics(font)
        // Determine the X coordinate for the text
        val x = rect.x + (rect.width - metrics.stringWidth(text)) / 2
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        val y = rect.y + (rect.height - metrics.height) / 2 + metrics.ascent
        // Set the font
        g.font = font
        // Draw the String
        g.drawString(text, x, y)
    }

    fun deriveHeaderSizesFromFont(img: BufferedImage, font: Font, columnWidth: Int): Pair<Float, Float> {
        val fontGraphics = img.createGraphics()
        fontGraphics.setRenderingHint(
            RenderingHints.KEY_FRACTIONALMETRICS,
            RenderingHints.VALUE_FRACTIONALMETRICS_ON
        )
        fontGraphics.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        )

        val atts: MutableMap<TextAttribute, Any> = HashMap()
        atts[TextAttribute.KERNING] = TextAttribute.KERNING_ON

        val text = "10"
        val r2d: Rectangle2D = fontGraphics.getFontMetrics(font).getStringBounds(text, fontGraphics)

        val fontWidth = (font.size2D * (columnWidth / 3) / r2d.width).toFloat()
        val fontHeight = (font.size2D * (columnWidth / 3) / r2d.height).toFloat()

        return fontWidth to fontHeight
    }

}
