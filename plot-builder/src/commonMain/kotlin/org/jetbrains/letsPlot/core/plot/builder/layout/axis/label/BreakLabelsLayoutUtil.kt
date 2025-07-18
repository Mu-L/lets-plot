/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.plot.builder.layout.axis.label

import org.jetbrains.letsPlot.commons.geometry.DoubleRectangle
import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.commons.intern.math.toRadians
import org.jetbrains.letsPlot.commons.interval.DoubleSpan
import org.jetbrains.letsPlot.core.plot.base.layout.Thickness
import org.jetbrains.letsPlot.core.plot.base.scale.ScaleBreaks
import org.jetbrains.letsPlot.core.plot.base.theme.AxisTheme
import org.jetbrains.letsPlot.core.plot.builder.guide.Orientation
import org.jetbrains.letsPlot.core.plot.builder.guide.Orientation.*
import org.jetbrains.letsPlot.core.plot.builder.layout.axis.AxisBreaksProvider
import org.jetbrains.letsPlot.core.plot.builder.presentation.LabelSpec
import kotlin.math.*

internal object BreakLabelsLayoutUtil {

    fun getFlexBreaks(breaksProvider: AxisBreaksProvider, maxCount: Int): ScaleBreaks {
        check(!breaksProvider.isFixedBreaks) { "fixed breaks not expected" }
        check(maxCount > 0) { "maxCount=$maxCount" }
        var breaks = breaksProvider.getBreaks(maxCount)

        if (maxCount == 1 && !breaks.isEmpty) {
            return breaks.withOneBreak()
        }
        var count = maxCount
        while (breaks.size > maxCount) {
            val delta = max(1, (breaks.size - maxCount) / 2)
            count -= delta
            if (count <= 1) {
                breaks = breaksProvider.getBreaks(1)
                break
            }
            breaks = breaksProvider.getBreaks(count)
        }
        return breaks
    }

    fun horizontalCenteredLabelBounds(labelSize: DoubleVector): DoubleRectangle {
        return DoubleRectangle(-labelSize.x / 2.0, 0.0, labelSize.x, labelSize.y)
    }

    fun doLayoutVerticalAxisLabels(
        orientation: Orientation,
        labelSpec: LabelSpec,
        breaks: ScaleBreaks,
        theme: AxisTheme,
        axisDomain: DoubleSpan,
        axisLength: Double,
    ): AxisLabelsLayoutInfo {
        check(!orientation.isHorizontal)

        if (theme.showLabels() && theme.rotateLabels()) {
            return VerticalRotatedLabelsLayout(
                orientation,
                breaks,
                theme
            ).doLayout(axisDomain, axisLength)
        }

        val tickLength = if (theme.showTickMarks()) theme.tickMarkLength() else 0.0
        val axisBounds = when {
            theme.showLabels() -> {
                val labelsBounds = verticalAxisLabelsBounds(
                    breaks,
                    projectedBreaks = breaks.projectOnAxis(axisDomain, axisLength, isHorizontal = false),
                    tickLabelSpec = labelSpec
                )
                applyLabelMargins(
                    labelsBounds,
                    tickLength,
                    theme.tickLabelMargins(),
                    theme.labelSpacing(),
                    orientation
                )
            }

            theme.showTickMarks() -> {
                val labelsBounds = DoubleRectangle.ZERO
                applyLabelMargins(
                    labelsBounds,
                    tickLength,
                    theme.tickLabelMargins(),
                    theme.labelSpacing(),
                    orientation
                )
            }

            else -> DoubleRectangle.ZERO
        }

        return AxisLabelsLayoutInfo.Builder()
            .breaks(breaks)
            .bounds(axisBounds)     // label bounds actually
//            .labelHorizontalAnchor(), // Default anchors,
//            .labelVerticalAnchor()    // see: AxisComponent.TickLabelAdjustments
            .build()
    }

    // Expands to borders with margins
    fun applyLabelMargins(
        bounds: DoubleRectangle,
        tickLength: Double,
        margins: Thickness,
        spacing: Double,
        orientation: Orientation
    ): DoubleRectangle {
        val origin = alignToLabelMargin(bounds, tickLength, margins, spacing, orientation).let {
            val offset = when (orientation){
                TOP -> DoubleVector(0.0, margins.top)
                BOTTOM -> DoubleVector(0.0, margins.top + spacing)
                LEFT -> DoubleVector(margins.left, 0.0)
                RIGHT -> DoubleVector(margins.left + spacing, 0.0)
            }
            it.subtract(offset).origin
        }
        val dimension = bounds.dimension.add(
            when {
                orientation.isHorizontal -> DoubleVector(0.0, margins.height + spacing)
                else -> DoubleVector(margins.width + spacing, 0.0)
            }
        )
        return DoubleRectangle(origin, dimension)
    }

    // Moves a rectangle on the border (aligns to the margin)
    fun alignToLabelMargin(
        bounds: DoubleRectangle,
        tickLength: Double,
        margins: Thickness,
        spacing: Double,
        orientation: Orientation
    ): DoubleRectangle {
        val offset = tickLength + spacing + when (orientation) {
            LEFT -> margins.right + bounds.width
            TOP -> margins.bottom + bounds.height
            RIGHT -> margins.left
            BOTTOM -> margins.top
        }
        val offsetVector = when (orientation) {
            LEFT -> DoubleVector(-offset, 0.0)
            RIGHT -> DoubleVector(offset, 0.0)
            TOP -> DoubleVector(0.0, -offset)
            BOTTOM -> DoubleVector(0.0, offset)
        }
        return bounds.add(offsetVector)
    }

    fun textBounds(elementRect: DoubleRectangle, margins: Thickness, spacing: Double, orientation: Orientation): DoubleRectangle? {
        if (elementRect.width == 0.0 || elementRect.height == 0.0) {
            return null
        }
        val corrected_margins = margins + when (orientation) {
            LEFT -> Thickness(0.0, spacing, 0.0, 0.0)
            RIGHT -> Thickness(0.0, 0.0, 0.0, spacing)
            TOP -> Thickness(0.0, 0.0, spacing, 0.0)
            BOTTOM -> Thickness(spacing,0.0, 0.0, 0.0)
        }

        return when {
            orientation.isHorizontal -> {
                DoubleRectangle(
                    elementRect.left,
                    elementRect.top + corrected_margins.top,
                    elementRect.width,
                    elementRect.height - corrected_margins.height
                )
            }

            else -> {
                DoubleRectangle(
                    elementRect.left + corrected_margins.left,
                    elementRect.top,
                    elementRect.width - corrected_margins.width,
                    elementRect.height
                )
            }
        }
    }

    fun rotatedLabelBounds(labelNormalSize: DoubleVector, degreeAngle: Double): DoubleRectangle {
        val angle = toRadians(degreeAngle)
        val sin = sin(angle)
        val cos = cos(angle)
        val w = sqrt((labelNormalSize.x * cos).pow(2) + abs(labelNormalSize.y * sin).pow(2))
        val h = sqrt((labelNormalSize.y * cos).pow(2) + abs(labelNormalSize.x * sin).pow(2))
        val x = 0.0
        val y = 0.0

        return DoubleRectangle(x, y, w, h)
    }

    private fun verticalAxisLabelsBounds(
        breaks: ScaleBreaks,
        projectedBreaks: List<Double>,  // coordinates on axis
        tickLabelSpec: LabelSpec
    ): DoubleRectangle {
        val maxLabelWidth = breaks.labels.maxOfOrNull(tickLabelSpec::width) ?: 0.0
        var y1 = 0.0
        var y2 = 0.0
        if (!breaks.isEmpty) {

            y1 = min(projectedBreaks[0], projectedBreaks.last())
            y2 = max(projectedBreaks[0], projectedBreaks.last())
            y1 -= tickLabelSpec.height() / 2
            y2 += tickLabelSpec.height() / 2
        }

        val origin = DoubleVector(0.0, y1)
        val dimensions = DoubleVector(maxLabelWidth, y2 - y1)
        return DoubleRectangle(origin, dimensions)
    }

    fun estimateBreakCountInitial(
        axisLength: Double,
        tickLabelSpec: LabelSpec,
        rotationAngle: Double?,
        side: (DoubleVector) -> Double
    ): Int {
        val initialDim = tickLabelSpec.dimensions(AxisLabelsLayout.INITIAL_TICK_LABEL)
        val dimension = if (rotationAngle != null) {
            rotatedLabelBounds(initialDim, rotationAngle).dimension
        } else {
            initialDim
        }
        return estimateBreakCount(side(dimension), axisLength)
    }

    fun estimateBreakCount(
        labels: List<String>,
        axisLength: Double,
        tickLabelSpec: LabelSpec,
        rotationAngle: Double?,
        side: (DoubleVector) -> Double
    ): Int {
        val dims = labels.map { label ->
            if (rotationAngle != null) {
                rotatedLabelBounds(tickLabelSpec.multilineDimensions(label), rotationAngle).dimension
            } else {
                tickLabelSpec.dimensions(label)
            }
        }
        val longestSide = dims.maxOfOrNull(side) ?: 0.0
        return estimateBreakCount(longestSide, axisLength)
    }

    private fun estimateBreakCount(length: Double, axisLength: Double): Int {
        val tickDistance = length + AxisLabelsLayout.MIN_TICK_LABEL_DISTANCE_AUTO
        return max(1.0, axisLength / tickDistance).toInt()
    }
}
