/*
 * Copyright (c) 2025. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.imagick.canvas

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import org.jetbrains.letsPlot.commons.geometry.Vector
import org.jetbrains.letsPlot.commons.registration.Disposable
import org.jetbrains.letsPlot.commons.values.Bitmap
import org.jetbrains.letsPlot.core.canvas.Canvas
import org.jetbrains.letsPlot.core.canvas.Context2d

class MagickCanvas(
    private val _img: CPointer<ImageMagick.MagickWand>,
    override val size: Vector,
    pixelDensity: Double,
    private val fontManager: MagickFontManager,
) : Canvas {
    // TODO: replace usage in tests with Snapshot
    val img: CPointer<ImageMagick.MagickWand>
        get() {
            val wand = (context2d as MagickContext2d).wand

            if (false) {
                val v = ImageMagick.DrawGetVectorGraphics(wand)
                println(v!!.toKString())
            }

            ImageMagick.MagickDrawImage(_img, wand)
            return _img
        }

    override val context2d: Context2d = MagickContext2d(_img, pixelDensity, fontManager)


    override fun takeSnapshot(): Canvas.Snapshot {
        return MagickSnapshot(img)
    }

    fun saveBmp(filename: String) {
        if (ImageMagick.MagickWriteImage(img, filename) == ImageMagick.MagickFalse) {
            throw RuntimeException("Failed to write image")
        }
    }

    companion object {
        fun create(width: Number, height: Number, pixelDensity: Number, fontManager: MagickFontManager): MagickCanvas {
            return create(Vector(width.toInt(), height.toInt()), pixelDensity, fontManager)
        }

        fun create(size: Vector, pixelDensity: Number, fontManager: MagickFontManager): MagickCanvas {
            val wand = ImageMagick.NewMagickWand() ?: error("MagickCanvas: Failed to create new MagickWand")
            ImageMagick.MagickSetImageAlphaChannel(wand, ImageMagick.AlphaChannelOption.OnAlphaChannel)
            val background = ImageMagick.NewPixelWand()
            ImageMagick.PixelSetColor(background, "transparent")
            ImageMagick.MagickNewImage(wand, size.x.toULong(), size.y.toULong(), background)
            return MagickCanvas(wand, size, pixelDensity = pixelDensity.toDouble(), fontManager = fontManager)
        }
    }

    class MagickSnapshot(
        img: CPointer<ImageMagick.MagickWand>
    ) : Disposable, Canvas.Snapshot {
        val img: CPointer<ImageMagick.MagickWand> = ImageMagick.CloneMagickWand(img) ?: error("MagickSnapshot: Failed to clone image wand")
        override val size: Vector = Vector(
            ImageMagick.MagickGetImageWidth(img).toInt(),
            ImageMagick.MagickGetImageHeight(img).toInt()
        )

        override fun dispose() {
            ImageMagick.DestroyMagickWand(img)
        }

        override fun copy(): Canvas.Snapshot {
            val copiedImg = ImageMagick.CloneMagickWand(img) ?: error("MagickSnapshot: Failed to clone image wand")
            return MagickSnapshot(copiedImg)
        }

        private fun exportPixels(wand: CPointer<ImageMagick.MagickWand>): Pair<UByteArray, Vector> {
            val width = ImageMagick.MagickGetImageWidth(wand).toInt()
            val height = ImageMagick.MagickGetImageHeight(wand).toInt()

            if (width <= 0 || height <= 0) {
                return UByteArray(0) to Vector.ZERO
            }


            val pixels = UByteArray(width * height * 4) // RGBA
            val success = ImageMagick.MagickExportImagePixels(
                wand, 0, 0, width.toULong(), height.toULong(),
                "RGBA",
                ImageMagick.StorageType.CharPixel,
                pixels.refTo(0)
            )
            if (success == ImageMagick.MagickFalse) error("Failed to export pixels")
            return pixels to Vector(width, height)
        }

        companion object {
            fun fromBitmap(bitmap: Bitmap): MagickSnapshot {
                require(bitmap.width > 0 && bitmap.height > 0) { "MagickCanvas: Size must be greater than zero" }
                require(bitmap.argbInts.size == bitmap.width * bitmap.height) {
                    "MagickCanvas: Bitmap pixel array size does not match the specified size"
                }

                val backgroundPixel = ImageMagick.NewPixelWand()
                ImageMagick.PixelSetColor(backgroundPixel, "transparent")

                val img = ImageMagick.NewMagickWand() ?: error("MagickCanvas: Failed to create new MagickWand")
                ImageMagick.MagickNewImage(img, bitmap.width.convert(), bitmap.height.convert(), backgroundPixel)

                val res = ImageMagick.MagickImportImagePixels(
                    img,
                    0,
                    0,
                    bitmap.width.convert(),
                    bitmap.height.convert(),
                    "ARGB",
                    ImageMagick.StorageType.LongPixel,
                    bitmap.argbInts.refTo(0)
                )

                if (res == ImageMagick.MagickFalse) {
                    val err = ImageMagick.MagickGetException(img, null)
                    println("MagickCanvas: Failed to import image pixels: $err")
                    error("MagickCanvas: Failed to import image pixels: $err")
                }

                return MagickSnapshot(img)
            }

            fun fromPixels(rgba: ByteArray, size: Vector): MagickSnapshot {
                require(size.x > 0 && size.y > 0) { "MagickCanvas: Size must be greater than zero" }
                require(rgba.size == size.x * size.y * 4) { // 4 bytes per pixel (RGBA)
                    "MagickCanvas: Byte array size does not match the specified size"
                }

                val backgroundPixel = ImageMagick.NewPixelWand()
                ImageMagick.PixelSetColor(backgroundPixel, "transparent")

                val img = ImageMagick.NewMagickWand() ?: error("MagickCanvas: Failed to create new MagickWand")
                ImageMagick.MagickNewImage(img, size.x.convert(), size.y.convert(), backgroundPixel)

                val res = ImageMagick.MagickImportImagePixels(
                    img,
                    0,
                    0,
                    size.x.convert(),
                    size.y.convert(),
                    "RGBA",
                    ImageMagick.StorageType.CharPixel,
                    rgba.refTo(0) // Convert ByteArray to CPointer<UByte>
                )

                if (res == ImageMagick.MagickFalse) {
                    val err = ImageMagick.MagickGetException(img, null)
                    println("MagickCanvas: Failed to import image pixels: $err")
                    error("MagickCanvas: Failed to import image pixels: $err")
                }

                return MagickCanvas.MagickSnapshot(img)
            }
        }

    }
}
