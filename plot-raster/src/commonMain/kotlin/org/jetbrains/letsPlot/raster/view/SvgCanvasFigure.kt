/*
 * Copyright (c) 2025 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.raster.view

import org.jetbrains.letsPlot.commons.geometry.DoubleRectangle
import org.jetbrains.letsPlot.commons.geometry.Rectangle
import org.jetbrains.letsPlot.commons.intern.observable.property.ReadableProperty
import org.jetbrains.letsPlot.commons.intern.observable.property.ValueProperty
import org.jetbrains.letsPlot.commons.registration.Registration
import org.jetbrains.letsPlot.core.canvas.*
import org.jetbrains.letsPlot.core.canvasFigure.CanvasFigure
import org.jetbrains.letsPlot.datamodel.mapping.framework.MappingContext
import org.jetbrains.letsPlot.datamodel.svg.dom.*
import org.jetbrains.letsPlot.datamodel.svg.event.SvgAttributeEvent
import org.jetbrains.letsPlot.raster.mapping.svg.SvgCanvasPeer
import org.jetbrains.letsPlot.raster.mapping.svg.SvgSvgElementMapper
import org.jetbrains.letsPlot.raster.mapping.svg.TextMeasurer
import org.jetbrains.letsPlot.raster.shape.Container
import org.jetbrains.letsPlot.raster.shape.Element
import kotlin.math.ceil

class SvgCanvasFigure(svg: SvgSvgElement = SvgSvgElement()) : CanvasFigure {
    private var needRedraw: Boolean = true
    private var nodeContainer: SvgNodeContainer? = null
    private var canvasPeer: SvgCanvasPeer? = null
    private var contentCanvas: Canvas? = null
    private var textMeasureCanvas: Canvas? = null

    var svgSvgElement: SvgSvgElement = svg
        set(value) {
            field = value
            mapSvgSvgElement(value)
        }

    val width get() = svgSvgElement.width().get()?.let { ceil(it).toInt() } ?: 0
    val height get() = svgSvgElement.height().get()?.let { ceil(it).toInt() } ?: 0

    internal lateinit var rootMapper: SvgSvgElementMapper // = SvgSvgElementMapper(svgSvgElement, canvasPeer)
    private val myBounds = ValueProperty(Rectangle(0, 0, width, height))

    override fun bounds(): ReadableProperty<Rectangle> {
        return myBounds
    }

    override fun mapToCanvas(canvasControl: CanvasControl): Registration {
        val resizeReg = canvasControl.onResize { size ->
            val oldCanvas = contentCanvas ?: error("Should not happen - canvas is null")
            val newCanvas = canvasControl.createCanvas(size)
            contentCanvas = newCanvas

            canvasControl.removeChild(oldCanvas)
            canvasControl.addChild(newCanvas)
        }

        textMeasureCanvas = canvasControl.createCanvas(0, 0)
        canvasControl.addChild(textMeasureCanvas ?: error("Should not happen - textMeasureCanvas is null"))

        contentCanvas = canvasControl.createCanvas(width * canvasControl.pixelDensity, height * canvasControl.pixelDensity)
        canvasControl.addChild(contentCanvas!!)

        val textMeasurer = TextMeasurer.create(textMeasureCanvas ?: error("Should not happen - textMeasureCanvas is null"))
        canvasPeer = SvgCanvasPeer(textMeasurer, canvasControl)
        mapSvgSvgElement(svgSvgElement)

        // TODO: for native export. There is no timer to trigger redraw, draw explicitly on attach to canvas.
        renderElement(rootMapper.target, contentCanvas!!)

        val anim = canvasControl.createAnimationTimer(object : AnimationProvider.AnimationEventHandler {
            override fun onEvent(millisTime: Long): Boolean {
                val canvas = contentCanvas ?: return false

                if (!needRedraw) {
                    return false
                }

                canvas.context2d.clearRect(DoubleRectangle(0.0, 0.0, width.toDouble(), height.toDouble()))
                renderElement(rootMapper.target, canvas)
                needRedraw = false

                return true
            }
        })
        anim.start()

        return object : Registration() {
            override fun doRemove() {
                canvasControl.removeChild(contentCanvas ?: error("Should not happen - canvas is null"))
                canvasControl.removeChild(textMeasureCanvas ?: error("Should not happen - textMeasureCanvas is null"))
                rootMapper.detachRoot()
                anim.stop()
                resizeReg.dispose()
            }
        }
    }

    private fun mapSvgSvgElement(value: SvgSvgElement) {
        val canvasPeer = canvasPeer ?: return // not yet attached

        nodeContainer = SvgNodeContainer(value)  // attach root
        nodeContainer!!.addListener(object : SvgNodeContainerListener {
            override fun onAttributeSet(element: SvgElement, event: SvgAttributeEvent<*>) = requestRedraw()
            override fun onNodeAttached(node: SvgNode) = requestRedraw()
            override fun onNodeDetached(node: SvgNode) = requestRedraw()
        })
        rootMapper = SvgSvgElementMapper(svgSvgElement, canvasPeer)
        rootMapper.attachRoot(MappingContext())

        contentCanvas?.let {
            renderElement(rootMapper.target, it)
        }
    }

    private fun render(elements: List<Element>, canvas: Canvas) {
        elements.forEach { element ->
            renderElement(element, canvas)
        }
    }

    private fun renderElement(element: Element, canvas: Canvas) {
        if (!element.isVisible) {
            return
        }

        val ctx = canvas.context2d

        var needRestore = false
        if (!element.transform.isIdentity) {
            if (!needRestore) {
                ctx.save()
                needRestore = true
            }
            ctx.affineTransform(element.transform)
        }

        element.clipPath?.let { clipPath ->
            if (!needRestore) {
                ctx.save()
                needRestore = true
            }
            ctx.beginPath()
            ctx.applyPath(clipPath.getCommands())
            ctx.closePath()
            canvas.context2d.clip()
        }

        //val globalAlphaSet = element.opacity?.let {
        //    val paint = Paint().apply {
        //        setAlphaf(it)
        //    }
        //    ctx.saveLayer(null, paint)
        //}

        element.render(canvas)
        if (element is Container) {
            render(element.children, canvas)
        }

        //globalAlphaSet?.let { canvas.restore() }

        if (needRestore) {
            ctx.restore()
        }
    }

    private fun requestRedraw() {
        needRedraw = true
    }
}
