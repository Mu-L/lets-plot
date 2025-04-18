/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.commons.interval

import org.jetbrains.letsPlot.commons.interval.DoubleSpan
import kotlin.test.*

class DoubleSpanTest {
    private fun range(lower: Number, upper: Number): DoubleSpan {
        return DoubleSpan(lower.toDouble(), upper.toDouble())
    }

    private fun assertUnion(
        expected: DoubleSpan,
        range0: DoubleSpan,
        range1: DoubleSpan
    ) {
        assertEquals(expected, range0.union(range1))
        assertEquals(expected, range1.union(range0))
    }

    private fun assertIntersection(
        expected: DoubleSpan,
        range0: DoubleSpan,
        range1: DoubleSpan
    ) {
        assertEquals(expected, range0.intersection(range1))
        assertEquals(expected, range1.intersection(range0))
    }


    @Test
    fun illegalEndpoints() {
        assertFailsWith<IllegalStateException> { range(0.0, Double.NaN) }
        assertFailsWith<IllegalStateException> { range(0.0, Double.NEGATIVE_INFINITY) }
        assertFailsWith<IllegalStateException> { range(0.0, Double.POSITIVE_INFINITY) }
        assertFailsWith<IllegalStateException> { range(Double.NaN, 0.0) }
        assertFailsWith<IllegalStateException> { range(Double.NEGATIVE_INFINITY, 0.0) }
        assertFailsWith<IllegalStateException> { range(Double.POSITIVE_INFINITY, 0.0) }
    }

    @Test
    fun endpointsNormalization() {
        val r_01 = range(0.0, 1.0)
        val r_10 = range(1.0, 0.0)
        assertTrue(r_01.equals(r_10))
    }

    @Test
    fun contains() {
        val r = range(-1, 1)
        assertFalse(r.contains(-2.0))
        assertTrue(r.contains(-1.0))
        assertTrue(r.contains(0.0))
        assertTrue(r.contains(1.0))
        assertFalse(r.contains(2.0))
    }

    @Test
    fun isConnected() {
        val r = range(-1, 1)
        assertFalse(r.connected(range(-3, -2)))
        assertTrue(r.connected(range(-3, -1)))
        assertTrue(r.connected(range(-3, 0)))
        assertTrue(r.connected(range(-1, 0)))
        assertTrue(r.connected(range(-2, 0)))
        assertTrue(r.connected(range(-2, 2)))
        assertTrue(r.connected(range(0, 3)))
        assertTrue(r.connected(range(1, 3)))
        assertFalse(r.connected(range(2, 3)))
    }

    @Test
    fun encloses() {
        val r = range(-2, 2)
        assertFalse(r.encloses(range(-3, -3)))
        assertFalse(r.encloses(range(-3, -2)))
        assertFalse(r.encloses(range(-3, 0)))
        assertTrue(r.encloses(range(-1, 0)))
        assertTrue(r.encloses(range(-2, 0)))
        assertTrue(r.encloses(range(-2, 2)))
        assertFalse(r.encloses(range(0, 3)))
        assertFalse(r.encloses(range(2, 3)))
        assertFalse(r.encloses(range(3, 3)))
    }

    @Test
    fun span() {
        val r = range(-2, 2)
        val inner = range(-1, 1)
        assertSame(r, r.union(inner))
        assertSame(r, r.union(r))
        assertSame(r, inner.union(r))

        assertUnion(
            range(-3, 2),
            r,
            range(-3, 1)
        )
        assertUnion(
            range(-2, 3),
            r,
            range(-1, 3)
        )
        assertUnion(
            range(-5, 2),
            r,
            range(-5, -3)
        )
        assertUnion(
            range(-2, 5),
            r,
            range(3, 5)
        )
    }


    @Test
    fun intersection() {
        val r = range(-2, 2)
        val inner = range(-1, 1)
        assertSame(inner, r.intersection(inner))
        assertSame(r, r.intersection(r))
        assertSame(inner, inner.intersection(r))

        assertFailsWith<IllegalArgumentException> {
            val outer = range(-5, -3)
            r.intersection(outer)
        }

        assertFailsWith<IllegalArgumentException> {
            val outer = range(3, 5)
            r.intersection(outer)
        }

        assertIntersection(
            range(-2, 1),
            r,
            range(-3, 1)
        )
        assertIntersection(
            range(-1, 2),
            r,
            range(-1, 3)
        )
    }

    @Test
    fun encloseAll() {
        assertFailsWith<NoSuchElementException> { DoubleSpan.encloseAll(emptyList()) }

        assertEquals(
            range(-3, 0),
            DoubleSpan.encloseAll(listOf(-3.0, 0.0))
        )
    }
}