/*
 * Copyright (c) 2026. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.plot.base.tooltip

import org.jetbrains.letsPlot.core.plot.base.tooltip.text.LinePattern
import org.jetbrains.letsPlot.core.plot.base.tooltip.text.ValueSource

class TooltipSpecification(
    val valueSources: List<ValueSource>,
    val tooltipLinePatterns: List<LinePattern>?,
    val tooltipProperties: TooltipProperties,
    val tooltipTitle: LinePattern?,
    val disableSplitting: Boolean,
    val tooltipGroup: String?,
) {
    class TooltipProperties(
        val anchor: TooltipAnchor?,
        val minWidth: Double?
    ) {
        companion object {
            val NONE = TooltipProperties(
                anchor = null,
                minWidth = null
            )
        }
    }

    fun useDefaultTooltips() = tooltipLinePatterns == null

    fun hideTooltips() = tooltipLinePatterns?.isEmpty() ?: false

    companion object {
        val NONE = TooltipSpecification(
            valueSources = emptyList(),
            tooltipLinePatterns = emptyList(),
            tooltipProperties = TooltipProperties.NONE,
            tooltipTitle = null,
            disableSplitting = false,
            tooltipGroup = null,
        )

        fun defaultTooltip() = TooltipSpecification(
            valueSources = emptyList(),
            tooltipLinePatterns = null,
            tooltipProperties = TooltipProperties.NONE,
            tooltipTitle = null,
            disableSplitting = false,
            tooltipGroup = null,
        )
    }
}