package com.github.secretx33.kotlinplayground.image

import com.github.secretx33.kotlinplayground.createFileIfNotExists
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.extension

fun Path.resizeImage(scale: Double, destination: Path): Path = ImageIO.read(toFile())
    .resize(scale)
    .writeTo(destination, extension)

fun Path.resizeImage(x: Int, y: Int, destination: Path): Path = ImageIO.read(toFile())
    .resize(x, y)
    .writeTo(destination, extension)

fun BufferedImage.writeTo(destination: Path, extension: String): Path = destination.apply {
    ImageIO.write(this@writeTo, extension, destination.createFileIfNotExists().toFile())
}

fun BufferedImage.resize(x: Int, y: Int): BufferedImage =
    resize(DimensionTuple(dimension, Dimension(x, y)))

fun BufferedImage.resize(scale: Double): BufferedImage {
    val scaled = dimension.withScale(scale).toScaledDimensionTuple()
    return resize(scaled)
}

fun BufferedImage.resize(dimensionTuple: DimensionTuple): BufferedImage {
    val widthScaled = dimensionTuple.modified.getWidth().toInt()
    val heightScaled = dimensionTuple.modified.getHeight().toInt()

    val resizedImage = BufferedImage(widthScaled, heightScaled, colorModel.hasAlpha()).graphics {
        setRenderingHints(RENDERING_HINTS)
        transform = transform.apply {
            setToScale(dimensionTuple.widthRatio, dimensionTuple.heightRatio)
        }
        drawImage(this@resize, 0, 0, null)
    }

    return resizedImage
}

private fun <T> BufferedImage.graphics(block: Graphics2D.() -> T): BufferedImage = apply {
    val graphics = createGraphics()
    try {
        graphics.block()
    } finally {
        graphics.dispose()
    }
}

private fun BufferedImage(
    width: Int,
    height: Int,
    hasAlpha: Boolean,
): BufferedImage = BufferedImage(
    width,
    height,
    if (hasAlpha) BufferedImage.TYPE_INT_ARGB else BufferedImage.TYPE_INT_RGB,
)

val BufferedImage.dimension: ScalableDimension
    get() = ScalableDimension(width, height)

private val RENDERING_HINTS = mapOf(
    RenderingHints.KEY_ANTIALIASING to RenderingHints.VALUE_ANTIALIAS_ON,
    RenderingHints.KEY_ALPHA_INTERPOLATION to RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY,
    RenderingHints.KEY_COLOR_RENDERING to RenderingHints.VALUE_COLOR_RENDER_QUALITY,
    RenderingHints.KEY_DITHERING to RenderingHints.VALUE_DITHER_DISABLE,
    RenderingHints.KEY_FRACTIONALMETRICS to RenderingHints.VALUE_FRACTIONALMETRICS_ON,
    RenderingHints.KEY_INTERPOLATION to RenderingHints.VALUE_INTERPOLATION_BICUBIC,
    RenderingHints.KEY_RENDERING to RenderingHints.VALUE_RENDER_QUALITY,
    RenderingHints.KEY_STROKE_CONTROL to RenderingHints.VALUE_STROKE_PURE,
    RenderingHints.KEY_TEXT_ANTIALIASING to RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
)