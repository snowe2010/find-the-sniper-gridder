package com.tylerthrailkill.sniper

import java.awt.BorderLayout
import java.awt.Image
import java.awt.ScrollPane
import java.awt.Toolkit
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.WindowConstants

object DebugImage  {
    fun postImage(vararg images: BufferedImage) {
        images.forEach {
            val t = Toolkit.getDefaultToolkit()
            val dimensions = t.screenSize
            val img = ImageDisplay(it)
            img.setSize(dimensions.width, dimensions.height)
            img.isVisible = true
            img.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            img.isResizable = true
        }
    }
}

class ImageDisplay(image: Image) : JFrame("Debug Images") {
    init {
        val pane = ScrollPane()
        val imagePane = JLabel(ImageIcon(image))
        pane.add(imagePane)
        layout = BorderLayout()
        add(pane, BorderLayout.CENTER)
    }
}
