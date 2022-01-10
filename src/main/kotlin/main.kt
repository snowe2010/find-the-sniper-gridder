import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JOptionPane
import kotlin.system.exitProcess


const val numberOfColumns = 10
const val numberOfRows = 10
const val headerHeight = 20
const val headerWidth = 20

fun main() {
    renderImage()
}

fun renderImage() {
    val img = loadImage()
    drawGrid(img)
    val newImg = drawColumnNames(img)

    val ii = ImageIcon(newImg)
    JOptionPane.showMessageDialog(null, ii)
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

fun drawGrid(img: BufferedImage) {
    val width = img.width
    val height = img.height

    val columnWidth = width / numberOfColumns
    val rowHeight = height / numberOfRows

    val columnWidthNotExact = width.rem(numberOfColumns)
    val rowHeightNotExact = height.rem(numberOfRows)

    println("img loaded")
    println("[height=$height] [width=$width]")
    println("[columnWidth=$columnWidth] [rowHeight=$rowHeight]")

    val g2d: Graphics2D = img.createGraphics()
    g2d.background = Color.WHITE
    g2d.color = Color.BLACK
    val bs = BasicStroke(2f)
    g2d.stroke = bs

    for (i in 0..numberOfColumns) {
        if (columnWidthNotExact != 0 && i == numberOfColumns) {
            continue
        }
        g2d.drawLine(columnWidth * i, 0, columnWidth * i, height - 1)
    }
    for (i in 0..numberOfRows) {
        if (rowHeightNotExact != 0 && i == numberOfRows) {
            continue
        }
        g2d.drawLine(0, rowHeight * i, width - 1, rowHeight * i)
    }

}

fun drawColumnNames(img: BufferedImage): BufferedImage {
    val columnWidth = img.width / numberOfColumns
    val rowHeight = img.height / numberOfRows

    val imgWithHeaders = BufferedImage(img.width + headerWidth, img.height + headerHeight, img.type)

    val g2d: Graphics2D = imgWithHeaders.createGraphics()
    g2d.color = Color.WHITE
    g2d.font = Font("Dialog", Font.PLAIN, 20)
    g2d.drawImage(img, headerWidth, headerHeight, null)

    val topHeaders = listOf(
        "A",
        "B",
        "C",
        "D",
        "E",
        "F",
        "G",
        "H",
        "I",
        "J",
        "K",
        "L",
        "M",
        "N",
        "O",
        "P",
        "Q",
        "R",
        "S",
        "T",
        "U",
        "V",
        "W",
        "X",
        "Y",
        "Z"
    )
    topHeaders.forEachIndexed { index, it ->
        g2d.drawString(it, headerWidth + (index * columnWidth) + (columnWidth / 2), headerHeight)
    }
    (0..numberOfRows).forEachIndexed { index, it ->
        g2d.drawString("$it", 0, headerHeight + (index * rowHeight) + (rowHeight / 2))
    }
    return imgWithHeaders
}
