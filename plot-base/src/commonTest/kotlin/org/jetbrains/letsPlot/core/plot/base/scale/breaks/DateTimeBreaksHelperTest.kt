/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.plot.base.scale.breaks

import demoAndTestShared.assertArrayEquals
import org.jetbrains.letsPlot.commons.intern.datetime.*
import org.jetbrains.letsPlot.core.commons.time.interval.NiceTimeInterval
import kotlin.test.Test
import kotlin.test.assertEquals

class DateTimeBreaksHelperTest {

    @Test
    fun milliseconds1() {
        val expected = intArrayOf(10, 20, 30, 40, 50, 60, 70, 80, 90, 100)
        assertMilliseconds(
            expected,
            100,
            10
        )
    }

    @Test
    fun milliseconds2() {
        val expected = intArrayOf(50, 100)
        assertMilliseconds(
            expected,
            100,
            3
        )
    }

    @Test
    fun seconds1() {
        val expected = intArrayOf(8, 9, 10, 11, 12, 13, 14, 15, 16, 17)
        assertSeconds(expected, 10, 10)
    }

    @Test
    fun seconds2() {
        val expected = intArrayOf(10, 15)
        assertSeconds(expected, 10, 3)
    }

    @Test
    fun minutes1() {
        val expected = intArrayOf(8, 9, 10, 11, 12, 13, 14, 15, 16, 17)
        assertMinutes(expected, 10, 10)
    }

    @Test
    fun minutes2() {
        val expected = intArrayOf(10, 15)
        assertMinutes(expected, 10, 3)
    }

    @Test
    fun hours1() {
        val expected = intArrayOf(8, 9, 10, 11, 12, 13, 14, 15, 16, 17)
        assertHours(expected, 10, 10)
    }

    @Test
    fun hours2() {
        val expected = intArrayOf(8, 11, 14, 17)
        assertHours(expected, 10, 3)
    }

    @Test
    fun days1() {
        val expected = intArrayOf(2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
        assertDays(expected, 10, 10)
    }

    @Test
    fun days2() {
        val expected = intArrayOf(3, 5, 7, 9, 11)
        assertDays(expected, 10, 3)
    }

    @Test
    fun weeks1() {
//        val expected = intArrayOf(3, 3, 3, 3, 3, 3, 3, 3, 3, 3)
        // New expectation: all Mondays.
        val expected = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        assertWeeks(expected, 10, 10)
    }

    @Test
    fun weeks2() {
        // Note: we don't have a 2-week step in the current implementation: see NiceTimeInterval.kt
//        val expected = intArrayOf(3, 3, 3, 3, 3, 3, 3, 3, 3, 3)
        // New expectation: all Mondays.
        val expected = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        assertWeeks(expected, 10, 5)
    }

    @Test
    fun months1() {
        val expected = intArrayOf(6, 9, 0)
        assertMonths(expected, 10, 5)
    }

    @Test
    fun months2() {
        val expected = intArrayOf(4, 5, 6, 7, 8, 9, 10, 11, 0, 1)
        assertMonths(expected, 10, 10)
    }

    @Test
    fun years1() {
        val expected = intArrayOf(2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023)
        assertYears(expected, 10, 11)
    }

    @Test
    fun years2() {
        val expected = intArrayOf(2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023)
        assertYears(expected, 10, 10)
    }

    @Test
    fun years3() {
        val expected = intArrayOf(2014, 2016, 2018, 2020, 2022)
        assertYears(expected, 10, 5)
    }

    @Test
    fun days_Reversed() {
        // 2-days step backward: 1/1/13, 12/30/12, 28, 26, 24
        val expected = intArrayOf(1, 30, 28, 26, 24)
        assertDays(expected, -10, 3)
    }

    @Test
    fun minInterval_Day_lessTicks() {
        // tick interval can be greater than 1 day
        val expected = intArrayOf(3, 5)
        assertDays(
            expected,
            5,
            3,
            minInterval = NiceTimeInterval.ONE_DAY
        )
    }

    @Test
    fun minInterval_Day_moreTicks() {
        // tick interval cannot be smaller than 1 day
        val expected = intArrayOf(2, 3, 4, 5, 6)
        assertDays(
            expected,
            5,
            10,
            minInterval = NiceTimeInterval.ONE_DAY
        )
    }

    @Test
    fun minInterval_Month_lessTicks() {
        val expected = intArrayOf(6, 9)
        assertMonths(
            expected,
            6,
            2,
            minInterval = NiceTimeInterval.ONE_MONTH
        )
    }

    @Test
    fun minInterval_Month_moreTicks() {
        // tick interval cannot be smaller than 1 month
        val expected = intArrayOf(4, 5, 6, 7, 8)
        assertMonths(
            expected,
            5,
            10,
            minInterval = NiceTimeInterval.ONE_MONTH
        )
    }

    companion object {
        private val TZ_UTC = TimeZone.UTC

        private val BASE_DATE = Date(1, Month.JANUARY, 2013)
        private val BASE_TIME = Time(7, 7, 7, 7)             // 07:07:07.007
        private val BASE_INSTANT: Instant = DateTime(
            BASE_DATE,
            BASE_TIME
        ).toInstant(TZ_UTC)

        private val MILLISECONDS = { instant: Double -> DateTime.ofEpochMilliseconds(instant, TZ_UTC).milliseconds }
        private val SECONDS = { instant: Double -> DateTime.ofEpochMilliseconds(instant, TZ_UTC).seconds }
        private val MINUTES = { instant: Double -> DateTime.ofEpochMilliseconds(instant, TZ_UTC).minutes }
        private val HOURS = { instant: Double -> DateTime.ofEpochMilliseconds(instant, TZ_UTC).hours }
        private val DAYS = { instant: Double -> DateTime.ofEpochMilliseconds(instant, TZ_UTC).day }
        private val WEEKDAYS = { instant: Double -> DateTime.ofEpochMilliseconds(instant, TZ_UTC).weekDay.ordinal }
        private val MONTHS = { instant: Double -> DateTime.ofEpochMilliseconds(instant, TZ_UTC).month.ordinal }
        private val YEARS = { instant: Double -> DateTime.ofEpochMilliseconds(instant, TZ_UTC).year }

        private fun assertMilliseconds(expected: IntArray, msCount: Long, targetBreakCount: Int) {
            val instant2 = BASE_INSTANT.add(Duration.MS.mul(msCount))
            val breaks = computeBreaks(
                BASE_INSTANT.toEpochMilliseconds(),
                instant2.toEpochMilliseconds(),
                targetBreakCount
            )
            assertTimePartEquals(
                expected,
                breaks,
                MILLISECONDS
            )
        }

        private fun assertSeconds(expected: IntArray, sCount: Long, targetBreakCount: Int) {
            val instant2 = BASE_INSTANT.add(Duration.SECOND.mul(sCount))
            val breaks = computeBreaks(
                BASE_INSTANT.toEpochMilliseconds(),
                instant2.toEpochMilliseconds(),
                targetBreakCount
            )
            assertTimePartEquals(
                expected,
                breaks,
                SECONDS
            )
        }

        private fun assertMinutes(expected: IntArray, mCount: Long, targetBreakCount: Int) {
            val instant2 = BASE_INSTANT.add(Duration.MINUTE.mul(mCount))
            val breaks = computeBreaks(
                BASE_INSTANT.toEpochMilliseconds(),
                instant2.toEpochMilliseconds(),
                targetBreakCount
            )
            assertTimePartEquals(
                expected,
                breaks,
                MINUTES
            )
        }

        private fun assertHours(expected: IntArray, mCount: Long, targetBreakCount: Int) {
            val instant2 = BASE_INSTANT.add(Duration.HOUR.mul(mCount))
            val breaks = computeBreaks(
                BASE_INSTANT.toEpochMilliseconds(),
                instant2.toEpochMilliseconds(),
                targetBreakCount
            )
            assertTimePartEquals(
                expected,
                breaks,
                HOURS
            )
        }

        private fun assertDays(
            expected: IntArray,
            dCount: Long,
            targetBreakCount: Int,
            minInterval: NiceTimeInterval? = null
        ) {
            val instant2 = BASE_INSTANT.add(Duration.DAY.mul(dCount))
            val breaks = computeBreaks(
                BASE_INSTANT.toEpochMilliseconds(),
                instant2.toEpochMilliseconds(),
                targetBreakCount,
                minInterval
            )
            assertTimePartEquals(
                expected,
                breaks,
                DAYS
            )
            assertTimesEqual(
                breaks,
                Time.DAY_START
            )
        }

        private fun assertWeeks(expected: IntArray, wCount: Long, targetBreakCount: Int) {
            val instant2 = BASE_INSTANT.add(Duration.WEEK.mul(wCount))
            val breaks = computeBreaks(
                BASE_INSTANT.toEpochMilliseconds(),
                instant2.toEpochMilliseconds(),
                targetBreakCount
            )
            assertTimePartEquals(
                expected,
                breaks,
                WEEKDAYS
            )
            assertTimesEqual(
                breaks,
                Time.DAY_START
            )
        }

        private fun assertMonths(
            expected: IntArray,
            mCount: Int,
            targetBreakCount: Int,
            minInterval: NiceTimeInterval? = null
        ) {
            val date = Date(1, Month.APRIL, 2013)
            val month1 = date.month.ordinal
            val month2 = (month1 + mCount) % 12
            val addYear = (month1 + mCount) / 12
            val dateTime1 = DateTime(
                date,
                BASE_TIME
            )
            val month = Month.entries[month2.toInt()]
            val dateTime2 = DateTime(
                Date(1, month, date.year + addYear),
                BASE_TIME
            )

            val instant1 = dateTime1.toEpochMilliseconds(TZ_UTC)
            val instant2 = dateTime2.toEpochMilliseconds(TZ_UTC)

            val breaks = computeBreaks(
                instant1,
                instant2,
                targetBreakCount,
                minInterval
            )
            assertTimePartEquals(
                expected,
                breaks,
                MONTHS
            )

            assertTimesEqual(
                breaks,
                Time.DAY_START
            )
        }

        private fun assertYears(expected: IntArray, yCount: Int, targetBreakCount: Int) {
            val date = Date(1, Month.APRIL, 2013)
            val dateTime1 = DateTime(
                date,
                BASE_TIME
            )
            val dateTime2 = DateTime(
                Date(1, Month.APRIL, date.year + yCount),
                BASE_TIME
            )

            val instant1 = dateTime1.toEpochMilliseconds(TZ_UTC)
            val instant2 = dateTime2.toEpochMilliseconds(TZ_UTC)

            val breaks = computeBreaks(
                instant1,
                instant2,
                targetBreakCount
            )
            assertTimePartEquals(
                expected,
                breaks,
                YEARS
            )

            assertTimesEqual(
                breaks,
                Time.DAY_START
            )
            assertDaysOfYearEqual(
                breaks,
                0
            )
        }

        private fun computeBreaks(
            fromInstant: Long, toInstant: Long,
            targetBreakCount: Int,
            minInterval: NiceTimeInterval? = null
        ): Array<Double> {
            val helper = DateTimeBreaksHelper(
                fromInstant.toDouble(),
                toInstant.toDouble(),
                targetBreakCount,
                providedFormatter = null,
                minInterval = minInterval,
                maxInterval = null,
                tz = TZ_UTC
            )
            return helper.breaks.toTypedArray()
        }

        private fun assertTimesEqual(dateTimeArr: Array<Double>, time: Time) {
            for ((i, dt) in dateTimeArr.withIndex()) {
                val dateTime = DateTime.ofEpochMilliseconds(dt, TZ_UTC)
                assertEquals(time, dateTime.time, "Index $i")
            }
        }

        private fun assertDaysOfYearEqual(instants: Array<Double>, dayOfTheYear: Int) {
            for ((i, dt) in instants.withIndex()) {
                val dateTime = DateTime.ofEpochMilliseconds(dt, TZ_UTC)
                val daysFromYearStart = dateTime.date.daysFromYearStart()
                assertEquals(dayOfTheYear, daysFromYearStart, "Index $i")
            }
        }

        private fun assertTimePartEquals(
            expected: IntArray, breaks: Array<Double>,
            timePartExtractor: (Double) -> Int
        ) {
            val result = take(
                timePartExtractor,
                breaks
            )
            assertArrayEquals(expected.toTypedArray(), result.toTypedArray())
        }

        private fun take(f: (Double) -> Int, instants: Array<Double>): IntArray {
            val result = IntArray(instants.size)
            var i = 0
            for (instant in instants) {
                result[i++] = f(instant)
            }
            return result
        }
    }
}
