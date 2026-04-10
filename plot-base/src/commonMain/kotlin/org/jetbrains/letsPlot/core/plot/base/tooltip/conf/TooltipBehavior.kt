/*
 * Copyright (c) 2026. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.plot.base.tooltip.conf

import org.jetbrains.letsPlot.core.plot.base.Aes
import org.jetbrains.letsPlot.core.plot.base.tooltip.GeomTargetLocator.LookupSpec
import org.jetbrains.letsPlot.core.plot.base.tooltip.TooltipSpecification

data class TooltipBehavior(
    val lookupSpec: LookupSpec,
    val axisAesFromFunctionKind: List<Aes<*>>,
    val axisTooltipEnabled: Boolean,
    val isCrosshairEnabled: Boolean,
    val ignoreInvisibleTargets: Boolean,
    val tooltipSpec: TooltipSpecification,
)
