/*
 * Copyright (c) 2024. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.util.sizing

import org.jetbrains.letsPlot.commons.geometry.DoubleRectangle
import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.commons.logging.PortableLogging
import org.jetbrains.letsPlot.core.util.sizing.SizingMode.*
import org.jetbrains.letsPlot.core.util.sizing.SizingOption.HEIGHT
import org.jetbrains.letsPlot.core.util.sizing.SizingOption.HEIGHT_MODE
import org.jetbrains.letsPlot.core.util.sizing.SizingOption.WIDTH
import org.jetbrains.letsPlot.core.util.sizing.SizingOption.WIDTH_MODE
import kotlin.math.max
import kotlin.math.min

private val LOG = PortableLogging.logger("Lets-Plot SizingPolicy")

/**
 * Controls the sizing behavior of plots by managing width and height dimensions
 * according to different sizing modes.
 *
 * The policy determines how a plot's dimensions should be calculated based on:
 * - The default figure size
 * - The container size (if available)
 * - The specified width and height values
 * - The sizing modes for both width and height
 *
 * Sizing modes:
 *
 * 1. FIXED mode:
 *    - Uses the explicitly provided width/height values
 *    - Falls back to the default figure size if no values provided
 *    - Not responsive to container size
 *
 * 2. MIN mode:
 *    Applies the smallest dimension among:
 *    - The default figure size
 *    - The specified width/height (if provided)
 *    - The container size (if available)
 *
 * 3. FIT mode:
 *    Uses either:
 *    - The specified width/height if provided
 *    - Otherwise uses container size if available
 *    - Falls back to default figure size if neither is available
 *
 * 4. SCALED mode:
 *    - Always preserves the figure's aspect ratio
 *    - Typical usage: one dimension (usually width) uses FIXED/MIN/FIT mode
 *      and SCALED height adjusts to maintain aspect ratio
 *    - Special case: when both width and height are SCALED:
 *      * Requires container size to be available
 *      * Fits figure within container while preserving aspect ratio
 *      
 */
class SizingPolicy(
    val widthMode: SizingMode,
    val heightMode: SizingMode,
    val width: Double? = null,
    val height: Double? = null,
) {

    // avoid division by zero
    private fun normalize(v: Double): Double = max(1.0, v)

    fun isFixedSize(): Boolean {
        // 'fixed size' - no need to know figure size or container sise.
        return widthMode == FIXED && heightMode == FIXED
                && width != null && height != null
    }

    fun getFixedSize(): DoubleVector {
        check(isFixedSize()) {
            "Not a fixed size policy: $this"
        }

        return DoubleVector(
            x = normalize(width!!),
            y = normalize(height!!)
        )
    }

    fun resize(figureSizeDefault: DoubleVector, containerSize: DoubleVector?): DoubleVector {
        @Suppress("NAME_SHADOWING")
        val containerSize = containerSize ?: figureSizeDefault

        // width, height if provided by the policy overrides the container size.
        val policyWidth = width ?: containerSize.x
        val policyHeight = height ?: containerSize.y

        return if (widthMode == SCALED && heightMode == SCALED) {
            // Fit in the container and preserve the figure aspect ratio.
            return DoubleRectangle(DoubleVector.ZERO, DoubleVector(policyWidth, policyHeight))
                .shrinkToAspectRatio(figureSizeDefault)
                .dimension
        } else {
            val widthFixed = when (widthMode) {
                FIXED -> width ?: figureSizeDefault.x
                FIT -> policyWidth
                MIN -> min(figureSizeDefault.x, policyWidth)
                SCALED -> null
            }

            val heightFixed = when (heightMode) {
                FIXED -> height ?: figureSizeDefault.y
                FIT -> policyHeight
                MIN -> min(figureSizeDefault.y, policyHeight)
                SCALED -> null
            }

            if (widthFixed != null && heightFixed != null) {
                DoubleVector(widthFixed, heightFixed)
            } else if (widthFixed != null) {
                // scale height
                val height = widthFixed / normalize(figureSizeDefault.x) * figureSizeDefault.y
                DoubleVector(widthFixed, height)
            } else if (heightFixed != null) {
                // scale width
                val width = heightFixed / normalize(figureSizeDefault.y) * figureSizeDefault.x
                DoubleVector(width, heightFixed)
            } else {
                // Never occurs.
                throw IllegalArgumentException("Unable to determine size with sizing policy: $this")
            }
        }
    }

    fun withUpdate(
        options: Map<*, *>,
    ): SizingPolicy {
        val widthMode = sizingMode(options, WIDTH_MODE) ?: this.widthMode
        val heightMode = sizingMode(options, HEIGHT_MODE) ?: this.heightMode
        val width = (options[WIDTH] as? Number)?.toDouble() ?: this.width
        val height = (options[HEIGHT] as? Number)?.toDouble() ?: this.height
        return SizingPolicy(widthMode, heightMode, width, height)
    }

    override fun toString(): String {
        return "SizingPolicy(widthMode=$widthMode, heightMode=$heightMode, width=$width, height=$height)"
    }

    companion object {
        // Notebook policy
        private val NOTEBOOK_WIDTH_MODE = MIN
        private val NOTEBOOK_HEIGHT_MODE = SCALED

        fun notebookCell(): SizingPolicy {
            return SizingPolicy(
                NOTEBOOK_WIDTH_MODE, NOTEBOOK_HEIGHT_MODE,
                width = null,
                height = null,
            )
        }

        fun dataloreReportCell(width: Double): SizingPolicy {
            return SizingPolicy(
                widthMode = FIXED,
                width = width,
                heightMode = SCALED,
                height = null,
            )
        }

        fun fixed(width: Double, height: Double): SizingPolicy {
            return SizingPolicy(
                widthMode = FIXED,
                heightMode = FIXED,
                width = max(0.0, width),
                height = max(0.0, height),
            )
        }

        fun keepFigureDefaultSize(): SizingPolicy {
            return SizingPolicy(
                widthMode = FIXED,
                heightMode = FIXED,
                width = null,
                height = null,
            )
        }

        fun fitContainerSize(preserveAspectRatio: Boolean): SizingPolicy {
            val mode = if (preserveAspectRatio) SCALED else FIT
            return SizingPolicy(
                widthMode = mode,
                heightMode = mode,
                width = null,
                height = null,
            )
        }

        fun create(
            options: Map<*, *>,
        ): SizingPolicy {
            val width = (options[WIDTH] as? Number)?.toDouble()
            val height = (options[HEIGHT] as? Number)?.toDouble()

            val widthMode = sizingMode(options, WIDTH_MODE) ?: if (width != null) FIXED else null
            val heightMode = sizingMode(options, HEIGHT_MODE) ?: if (height != null) FIXED else null

            return SizingPolicy(
                widthMode = widthMode ?: NOTEBOOK_WIDTH_MODE,
                heightMode = heightMode ?: NOTEBOOK_HEIGHT_MODE,
                width = width,
                height = height,
            )
        }

        private fun sizingMode(options: Map<*, *>, option: String): SizingMode? {
            return options[option]?.let { value ->
                SizingMode.byNameIgnoreCase(value as String) ?: let {
                    LOG.info { "Option $option: unexpected value '$value'" }
                    null
                }
            }
        }
    }
}