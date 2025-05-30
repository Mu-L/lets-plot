/*
 * Copyright (c) 2024 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */


package org.jetbrains.letsPlot.raster.mapping.svg.attr

import org.jetbrains.letsPlot.core.canvas.FontStyle
import org.jetbrains.letsPlot.core.canvas.FontWeight
import org.jetbrains.letsPlot.datamodel.svg.dom.SvgShape
import org.jetbrains.letsPlot.datamodel.svg.dom.SvgTSpanElement
import org.jetbrains.letsPlot.datamodel.svg.dom.SvgTextContent
import org.jetbrains.letsPlot.raster.mapping.svg.SvgUtils.toColor
import org.jetbrains.letsPlot.raster.shape.TSpan
import org.jetbrains.letsPlot.raster.shape.Text.BaselineShift
import org.jetbrains.letsPlot.raster.shape.Text.Companion.DEFAULT_FONT_FAMILY

internal object SvgTSpanElementAttrMapping : SvgShapeMapping<TSpan>() {
    override fun setAttribute(target: TSpan, name: String, value: Any?) {
        when (name) {
            SvgShape.FILL.name -> target.fill = toColor(value)
            //SvgShape.FILL_OPACITY.name -> target.fillOpacity = value!!.asFloat
            SvgShape.STROKE.name -> target.stroke = toColor(value)
            //SvgShape.STROKE_OPACITY.name -> target.strokeOpacity = value!!.asFloat
            SvgShape.STROKE_WIDTH.name -> target.strokeWidth = value!!.asFloat

            SvgTextContent.FONT_FAMILY.name -> target.fontFamily = value?.asFontFamily ?: DEFAULT_FONT_FAMILY
            SvgTextContent.FONT_SIZE.name -> {
                require(value is String) { "font-size: only string value is supported" }
                target.fontScale = when {
                    "em" in value -> value.removeSuffix("em").toFloat()
                    "%" in value -> value.removeSuffix("%").toFloat() / 100.0f
                    else -> 1f
                }
            }
            SvgTextContent.FONT_WEIGHT.name -> target.fontWeight = when (value) {
                "normal" -> FontWeight.NORMAL
                "bold" -> FontWeight.BOLD
                else -> error("Unexpected font-weight value: $value")
            }

            SvgTextContent.FONT_STYLE.name -> target.fontStyle = when (value) {
                "normal" -> FontStyle.NORMAL
                "italic" -> FontStyle.ITALIC
                else -> error("Unexpected font-style value: $value")
            }
            SvgTextContent.TEXT_DY.name -> {
                require(value is String) { "dy: only string value is supported" }
                target.dy = when {
                    "em" in value -> value.removeSuffix("em").toFloat()
                    "%" in value -> value.removeSuffix("%").toFloat() / 100.0f
                    else -> 0f
                }
            }

            SvgTSpanElement.BASELINE_SHIFT.name -> target.baselineShift = when (value) {
                "sub" -> BaselineShift.SUB
                "super" -> BaselineShift.SUPER
                else -> error("Unexpected baseline-shift value: $value")
            }

            else -> super.setAttribute(target, name, value)
        }
    }

    fun setAttributes(target: TSpan, tspan: SvgTSpanElement) {
        tspan.attributeKeys.forEach { key ->
            setAttribute(target, key.name, tspan.getAttribute(key).get())
        }
    }

}
