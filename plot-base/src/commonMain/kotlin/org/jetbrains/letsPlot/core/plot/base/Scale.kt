/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.plot.base

import org.jetbrains.letsPlot.commons.formatting.string.StringFormat.ExponentFormat
import org.jetbrains.letsPlot.commons.intern.datetime.TimeZone
import org.jetbrains.letsPlot.core.commons.data.DataType
import org.jetbrains.letsPlot.core.plot.base.scale.BreaksGenerator
import org.jetbrains.letsPlot.core.plot.base.scale.ScaleBreaks

interface Scale {
    val name: String

    /**
     * TRUE if both, domain and range are continuous
     */
    val isContinuous: Boolean

    val isContinuousDomain: Boolean

    val multiplicativeExpand: Double

    val additiveExpand: Double

    val transform: Transform

    val userFormatter: ((Any) -> String)?

    fun hasBreaks(): Boolean

    fun getBreaksGenerator(): BreaksGenerator

    fun getScaleBreaks(): ScaleBreaks

    // For axis and legend (truncated labels). For tooltips the getScaleBreaks functions should be used (full labels).
    fun getShortenedScaleBreaks(): ScaleBreaks

    fun with(): Builder

    interface Builder {
        fun name(v: String): Builder

        fun breaks(l: List<Any>): Builder

        fun labels(l: List<String>): Builder

        fun scaleBreaks(v: ScaleBreaks): Builder

        fun labelLengthLimit(v: Int): Builder

        fun labelFormatter(v: (Any) -> String): Builder

        fun dataType(v: DataType): Builder

        fun timeZone(v: TimeZone): Builder

        fun exponentFormat(v: ExponentFormat): Builder

        fun multiplicativeExpand(v: Double): Builder

        fun additiveExpand(v: Double): Builder

        fun continuousTransform(v: ContinuousTransform): Builder

        fun discreteTransform(v: DiscreteTransform): Builder

        fun breaksGenerator(v: BreaksGenerator): Builder

        fun build(): Scale
    }
}
