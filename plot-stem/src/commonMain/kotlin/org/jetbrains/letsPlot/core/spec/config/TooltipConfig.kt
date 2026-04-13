/*
 * Copyright (c) 2026. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.spec.config

import org.jetbrains.letsPlot.core.plot.base.Aes
import org.jetbrains.letsPlot.core.plot.base.tooltip.TooltipAnchor
import org.jetbrains.letsPlot.core.plot.base.tooltip.conf.TooltipBehavior
import org.jetbrains.letsPlot.core.plot.builder.VarBinding
import org.jetbrains.letsPlot.core.spec.Option

class TooltipConfig(
    opts: Map<String, Any>,
    constantsMap: Map<Aes<*>, Any>,
    groupingVarNames: List<String>?,
    varBindings: List<VarBinding>
) : LineSpecConfig(opts, constantsMap, groupingVarNames, varBindings) {

    fun createTooltips(): TooltipBehavior {
        return create().run {
            TooltipBehavior(
                valueSources = valueSources,
                tooltipLinePatterns = linePatterns,
                anchor = readAnchor(),
                minWidth = getDouble(Option.Layer.Tooltips.TOOLTIP_MIN_WIDTH),
                tooltipTitle = titleLine,
                disableSplitting = getBoolean(Option.Layer.Tooltips.DISABLE_SPLITTING, def = false),
                tooltipGroup = getString(Option.Layer.Tooltips.TOOLTIP_GROUP)
            )
        }

    }

    private fun readAnchor(): TooltipAnchor? {
        if (!has(Option.Layer.Tooltips.TOOLTIP_ANCHOR)) {
            return null
        }

        return when (val anchor = getString(Option.Layer.Tooltips.TOOLTIP_ANCHOR)) {
            "top_left" -> TooltipAnchor(TooltipAnchor.VerticalAnchor.TOP, TooltipAnchor.HorizontalAnchor.LEFT)
            "top_center" -> TooltipAnchor(TooltipAnchor.VerticalAnchor.TOP, TooltipAnchor.HorizontalAnchor.CENTER)
            "top_right" -> TooltipAnchor(TooltipAnchor.VerticalAnchor.TOP, TooltipAnchor.HorizontalAnchor.RIGHT)
            "middle_left" -> TooltipAnchor(TooltipAnchor.VerticalAnchor.MIDDLE, TooltipAnchor.HorizontalAnchor.LEFT)
            "middle_center" -> TooltipAnchor(
                TooltipAnchor.VerticalAnchor.MIDDLE,
                TooltipAnchor.HorizontalAnchor.CENTER
            )

            "middle_right" -> TooltipAnchor(
                TooltipAnchor.VerticalAnchor.MIDDLE,
                TooltipAnchor.HorizontalAnchor.RIGHT
            )

            "bottom_left" -> TooltipAnchor(TooltipAnchor.VerticalAnchor.BOTTOM, TooltipAnchor.HorizontalAnchor.LEFT)
            "bottom_center" -> TooltipAnchor(
                TooltipAnchor.VerticalAnchor.BOTTOM,
                TooltipAnchor.HorizontalAnchor.CENTER
            )

            "bottom_right" -> TooltipAnchor(
                TooltipAnchor.VerticalAnchor.BOTTOM,
                TooltipAnchor.HorizontalAnchor.RIGHT
            )

            else -> throw IllegalArgumentException(
                "Illegal value $anchor, ${Option.Layer.Tooltips.TOOLTIP_ANCHOR}, expected values are: " +
                        "'top_left'/'top_center'/'top_right'/" +
                        "'middle_left'/'middle_center'/'middle_right'/" +
                        "'bottom_left'/'bottom_center'/'bottom_right'"
            )
        }
    }

}
