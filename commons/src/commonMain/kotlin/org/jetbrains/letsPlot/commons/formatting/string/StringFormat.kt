/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.commons.formatting.string

import org.jetbrains.letsPlot.commons.formatting.datetime.DateTimeFormatUtil
import org.jetbrains.letsPlot.commons.formatting.datetime.Pattern.Companion.isDateTimeFormat
import org.jetbrains.letsPlot.commons.formatting.number.NumberFormat
import org.jetbrains.letsPlot.commons.formatting.number.NumberFormat.ExponentNotationType
import org.jetbrains.letsPlot.commons.formatting.string.StringFormat.FormatType.*
import org.jetbrains.letsPlot.commons.intern.datetime.TimeZone

class StringFormat private constructor(
    private val pattern: String,
    private val formatType: FormatType,
    expFormat: ExponentFormat?,
    private val tz: TimeZone?,
) {
    private val placeholders = PLACEHOLDER_REGEX.findAll(pattern).toList()
    private val formatters: List<(Any) -> String>

    enum class FormatType {
        NUMBER_FORMAT,
        DATETIME_FORMAT,
        STRING_FORMAT
    }


    init {
        formatters = when (formatType) {
            NUMBER_FORMAT, DATETIME_FORMAT -> listOf(initFormatter(pattern, formatType, expFormat))
            STRING_FORMAT -> {
                placeholders
                    .map { it.groupValues[TEXT_IN_BRACES] }
                    .map { pattern ->
                        val formatType = detectFormatType(pattern)
                        check(formatType == NUMBER_FORMAT || formatType == DATETIME_FORMAT) {
                            "Can't detect type of pattern '$pattern' used in string pattern '${this.pattern}'"
                        }
                        initFormatter(pattern, formatType, expFormat)
                    }
                    .toList()
            }
        }
    }

    val argsNumber = formatters.size

    fun format(value: Any): String = format(listOf(value))

    fun format(values: List<Any>): String {
        return when (formatType) {
            NUMBER_FORMAT, DATETIME_FORMAT -> {
                // single formatter for single value
                val fmt = formatters.firstOrNull()
                val v = values.firstOrNull()
                return when {
                    fmt != null && v != null -> fmt(v)
                    fmt == null && v != null -> v.toString()
                    fmt != null && v == null -> ""
                    fmt == null && v == null -> ""
                    else -> error("Should not be here")
                }
            }

            STRING_FORMAT -> {
                val formattedParts = formatters.mapIndexed { i, fmt ->
                    if (i < values.size) {
                        fmt(values[i])
                    } else if (i < placeholders.size){
                        // no value to format -> output the pattern itself so that the user can notice the problem
                        placeholders[i].value
                    } else {
                        // should not be here
                        "UNDEFINED"
                    }
                }

                var string = pattern

                placeholders.withIndex().reversed().forEach { (i, placeholder) ->
                    string = string.replaceRange(placeholder.range, formattedParts[i])
                }

                string.replace("{{", "{").replace("}}", "}")
            }
        }
    }

    private fun initFormatter(
        pattern: String,
        formatType: FormatType,
        expFormat: ExponentFormat?,
    ): ((Any) -> String) {
        if (pattern.isEmpty()) {
            return Any::toString
        }
        when (formatType) {
            NUMBER_FORMAT -> {
                val formatSpec = NumberFormat.parseSpec(pattern)

                // override exponent properties if expFormat is set
                val spec = formatSpec.copy(
                    expType = expFormat?.notationType ?: formatSpec.expType,
                    minExp = expFormat?.min ?: formatSpec.minExp,
                    maxExp = expFormat?.max ?: formatSpec.maxExp
                )
                val numberFormatter = NumberFormat(spec)
                return { value: Any ->
                    when (value) {
                        is Number -> numberFormatter.apply(value)
                        is String -> value.toFloatOrNull()?.let(numberFormatter::apply) ?: value
                        else -> error("Failed to format value with type ${value::class.simpleName}. Supported types are Number and String.")
                    }
                }
            }

            DATETIME_FORMAT -> {
                return DateTimeFormatUtil.createInstantFormatter(
                    pattern,
                    tz ?: TimeZone.UTC,
                )
            }

            else -> {
                error("Undefined format pattern $pattern")
            }
        }
    }

    data class ExponentFormat(
        val notationType: ExponentNotationType,
        val min: Int? = null,
        val max: Int? = null
    ) {
        companion object {
            val DEF_EXPONENT_FORMAT = ExponentFormat(ExponentNotationType.E)
        }
    }

    companion object {
        // Format strings contain “replacement fields” surrounded by braces {}.
        // Anything not contained in braces is considered literal text, which is copied unchanged to the output.
        // If you need to include a brace character in the literal text, it can be escaped by doubling: {{ and }}.
        //     "text" -> "text"
        //     "{{text}}" -> "{text}"
        //     "{.1f} -> 1.2
        //     "{{{.1f}}} -> {1.2}
        private val PLACEHOLDER_REGEX = Regex("""(?![^{]|\{\{)(\{([^{}]*)\})(?=[^}]|\}\}|$)""")
        private const val TEXT_IN_BRACES = 2

        fun validate(
            pattern: String,
            type: FormatType? = null,
            formatFor: String? = null,
            expectedArgs: Int = -1,
            expFormat: ExponentFormat? = null,
            tz: TimeZone?,
        ) {
            val fmt = create(pattern, type, formatFor, expFormat = expFormat, tz = tz)
            if (expectedArgs > 0) {
                require(fmt.argsNumber == expectedArgs) {
                    @Suppress("NAME_SHADOWING")
                    val formatFor = formatFor?.let { "to format \'$formatFor\'" } ?: ""
                    "Wrong number of arguments in pattern \'$pattern\' $formatFor. " +
                            "Expected $expectedArgs ${if (expectedArgs > 1) "arguments" else "argument"} " +
                            "instead of ${fmt.argsNumber}"
                }
            }
        }

        fun valueInLinePattern() = "{}"

        fun forOneArg(
            pattern: String,
            type: FormatType? = null,
            formatFor: String? = null,
            expFormat: ExponentFormat = ExponentFormat(ExponentNotationType.POW),
            tz: TimeZone?,
        ): StringFormat {
            return create(
                pattern,
                type,
                formatFor,
                expectedArgs = 1,
                expFormat = expFormat,
                tz
            )
        }

        fun forNArgs(
            pattern: String,
            argCount: Int,
            formatFor: String? = null,
            expFormat: ExponentFormat = ExponentFormat(ExponentNotationType.POW),
            tz: TimeZone?,
        ): StringFormat {
            return create(
                pattern,
                STRING_FORMAT,
                formatFor,
                argCount,
                expFormat = expFormat,
                tz,
            )
        }

        private fun detectFormatType(pattern: String): FormatType {
            return when {
                NumberFormat.isValidPattern(pattern) -> NUMBER_FORMAT
                isDateTimeFormat(pattern) -> DATETIME_FORMAT
                else -> STRING_FORMAT
            }
        }

        internal fun create(
            pattern: String,
            type: FormatType? = null,
            formatFor: String? = null,
            expectedArgs: Int = -1,
            expFormat: ExponentFormat? = null,
            tz: TimeZone?,
        ): StringFormat {
            val formatType = type ?: detectFormatType(pattern)
            return StringFormat(pattern, formatType, expFormat = expFormat, tz)
        }
    }
}