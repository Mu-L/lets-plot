/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.jfx.mapping.svg

import javafx.application.Platform
import javafx.collections.ListChangeListener
import javafx.geometry.Bounds
import javafx.geometry.Insets
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.control.Hyperlink
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.text.*

// Manually positions text runs along the x-axis, supports super/subscript.

// Limitations:
// Supports only a single line of text.
// Does not allow tspan elements to have their own styles.
internal class TextLine : Region() {
    enum class BaselineShift {
        SUB,
        SUPER
    }

    data class TextRun(
        val text: String,
        val baselineShift: BaselineShift? = null,
        val fontScale: Double? = null,
        // NOTE: resets between runs, yet by standard it alters the baseline for the rest of the text
        val dy: Double? = null,
        val href: String? = null
    )

    init {
        transforms.addListener(
            ListChangeListener {
                rebuild()
            }
        )
    }

    override fun computeMaxHeight(width: Double): Double {
        return super.computeMaxHeight(width).also { println("computeMaxHeight: $it") }
    }

    override fun computeMinHeight(width: Double): Double {
        return super.computeMinHeight(width).also { println("computeMinHeight: $it") }
    }

    override fun computePrefHeight(width: Double): Double {

        return 29.0//super.computePrefHeight(width).also { println("computePrefHeight: $it") }
    }


    var content: List<TextRun> = emptyList()
        set(value) {
            field = value
            rebuild()
        }

    var x: Double = 0.0
        set(value) {
            field = value
            rebuild()
        }

    var y: Double = 0.0
        set(value) {
            field = value
            rebuild()
        }

    var fill: Color? = null
        set(value) {
            field = value
            rebuild()
        }

    var stroke: Color? = null
        set(value) {
            field = value
            rebuild()
        }

    var strokeWidth: Double? = null
        set(value) {
            field = value
            rebuild()
        }

    // May be null - system will use default font
    var fontFamily: String? = null
        set(value) {
            if (value == field) return

            field = value
            rebuildFont()
        }

    var fontSize: Double = -1.0
        set(value) {
            if (value == field) return

            field = value
            rebuildFont()
        }

    var fontWeight: FontWeight? = null
        set(value) {
            if (value == field) return

            field = value
            rebuildFont()
        }

    var fontPosture: FontPosture? = null
        set(value) {
            if (value == field) return

            field = value
            rebuildFont()
        }

    var textOrigin: VPos? = null
        set(value) {
            field = value
            rebuild()
        }

    var textAlignment: TextAlignment? = null
        set(value) {
            field = value
            rebuild()
        }

    private var font: Font? = null
        set(value) {
            field = value
            if (value != null) {
                fontMetric = Text("X").apply { this.font = font }.boundsInLocal
            } else {
                fontMetric = null
            }

            rebuild()
        }

    private var fontMetric: Bounds? = null

    private fun rebuildFont() {
        font = Font.font(fontFamily, fontWeight, fontPosture, fontSize)
        rebuild()
    }

    private fun bbox(textRunElement: Node): Bounds {
        return when (textRunElement) {
            is Text -> textRunElement.boundsInLocal
            is Hyperlink -> textRunElement.graphic.boundsInLocal
            else -> error("Unsupported text run element: $textRunElement")
        }
    }

    private fun relayout() {
        val fontMetric = fontMetric ?: return // wait for font to be set

        content.zip(children).forEach { (textRun, text) ->
            adjustPosition(textRun, text)
        }

        val width = children.sumOf { bbox(it).width }
        val dx = when (textAlignment) {
            TextAlignment.RIGHT -> -width
            TextAlignment.CENTER -> -width / 2
            else -> 0.0
        }

        // Font metrics from Text bounds:
        // lineHeight = fontMetric.height
        // ascent = -fontMetric.minY
        // descent = fontMetric.maxY
        val height = fontMetric.minY
        val dy = when (textOrigin) {
            null -> 0.0
            VPos.BOTTOM -> 0.0
            VPos.TOP -> -height
            VPos.CENTER -> -height / 2
            VPos.BASELINE -> error("VPos.BASELINE is not supported")
        }

        // Arrange runs one after another
        var currentRunPosX = x
        children.forEach { text ->
            text.layoutX = currentRunPosX + dx
            text.layoutY += y + dy + getBaseline(text)
            currentRunPosX += bbox(text).width
        }
    }

    private fun rebuild() {
        children.clear()
        val fontMetric = fontMetric ?: return // wait for font to be set

        val texts: List<Node> = content.map(::textRunToFxNode)

        val width = texts.sumOf { bbox(it).width }
        val dx = when (textAlignment) {
            TextAlignment.RIGHT -> -width
            TextAlignment.CENTER -> -width / 2
            else -> 0.0
        }

        // Font metrics from Text bounds:
        // lineHeight = fontMetric.height
        // ascent = -fontMetric.minY
        // descent = fontMetric.maxY
        val height = fontMetric.minY
        val dy = when (textOrigin) {
            null -> 0.0
            VPos.BOTTOM -> 0.0
            VPos.TOP -> -height
            VPos.CENTER -> -height / 2
            VPos.BASELINE -> error("VPos.BASELINE is not supported")
        }

        // Arrange runs one after another
        var currentRunPosX = x
        texts.forEach { text ->
            text.layoutX = currentRunPosX + dx
            text.layoutY += y + dy
            currentRunPosX += bbox(text).width
        }

        //layout()
        relayout()
        Platform.runLater {
            relayout()
            children
                .firstOrNull { it is Hyperlink }
                ?.let{ println("Hyperlink bounds: ${it.boundsInLocal}") }

            println("TextLine bounds: $boundsInLocal")
        }
    }

    private fun adjustPosition(textRun: TextRun, text: Node) {
        val bounds = text.boundsInLocal
        val lineHeight = bounds.height

        val dy = textRun.dy?.let { lineHeight * it } ?: 0.0
        val baseline = when (textRun.baselineShift) {
            BaselineShift.SUPER -> lineHeight * 0.4
            BaselineShift.SUB -> lineHeight * -0.4
            else -> 0.0
        }
        val y = -baseline + dy

        when (text) {
            is Hyperlink -> text.layoutY = y
            is Text -> text.layoutY = y
            else -> error("Unsupported text run element: $text")
        }
    }

    private fun textRunToFxNode(textRun: TextRun): Node {
        val font = font ?: error("Font is not specified")
        val scaleFactor = textRun.fontScale ?: 1.0

        val text = Text()
        fill?.let { text.fill = it }
        stroke?.let { text.stroke = it }
        strokeWidth?.let { text.strokeWidth = it }
        text.text = textRun.text
        text.font = when (scaleFactor) {
            1.0 -> font
            else -> {
                val fontWeight = FontWeight.BOLD.takeIf { font.style.contains("bold") }
                val fontPosture = FontPosture.ITALIC.takeIf { font.style.contains("italic") }
                val fontSize = font.size * scaleFactor
                Font.font(font.family, fontWeight, fontPosture, fontSize)
            }
        }

        val bounds = text.boundsInLocal
        val lineHeight = bounds.height

        val dy = textRun.dy?.let { lineHeight * it } ?: 0.0
        val baseline = when (textRun.baselineShift) {
            BaselineShift.SUPER -> lineHeight * 0.4
            BaselineShift.SUB -> lineHeight * -0.4
            else -> 0.0
        }

        val node = if (textRun.href != null) {
            val hyperlink = Hyperlink("", text)
            children.add(hyperlink)

            hyperlink.layout()
            hyperlink.style = "-fx-padding: 0; -fx-margin: 0; -fx-background-insets: 0; -fx-border-insets: 0;"
            hyperlink.maxHeight = 10.0//text.boundsInLocal.height
            hyperlink.prefHeight = 10.0//text.boundsInLocal.height

            hyperlink.setOnAction {
                println("Clicked on ${textRun.href}")
            }
            val y = -baseline + dy
            text.layoutY = y
            hyperlink
        } else {
            children.add(text)
            val y = -baseline + dy
            text.layoutY = y
            text
        }
        return node
    }

    private fun getBaseline(text: Node): Double {
        return when (text) {
            is Text -> 0.0
            is Hyperlink -> text.graphic.boundsInLocal.minY
            else -> error("Unsupported text run element: $text")
        }
    }

    override fun toString(): String {
        return "TextLine(content=$content, fill=$fill, stroke=$stroke, font=$font, textOrigin=$textOrigin, textAlignment=$textAlignment)"
    }
}

class LetsPlotHyperlink : Hyperlink() {
    init {
        border = null
        padding = Insets(0.0)
        setOnAction {
            println("Clicked on $text")
        }
    }


}