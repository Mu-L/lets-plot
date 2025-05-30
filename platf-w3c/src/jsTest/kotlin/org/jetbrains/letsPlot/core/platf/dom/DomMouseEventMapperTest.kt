/*
 * Copyright (c) 2024. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

@file:Suppress("ComplexRedundantLet")

package org.jetbrains.letsPlot.core.platf.dom

import org.jetbrains.letsPlot.commons.event.MouseEvent
import org.jetbrains.letsPlot.commons.event.MouseEventSource
import org.jetbrains.letsPlot.commons.event.MouseEventSpec
import org.jetbrains.letsPlot.commons.event.MouseEventSpec.*
import org.jetbrains.letsPlot.commons.geometry.DoubleRectangle
import org.jetbrains.letsPlot.commons.intern.observable.event.EventHandler
import org.jetbrains.letsPlot.commons.registration.CompositeRegistration
import org.jetbrains.letsPlot.commons.registration.Registration
import org.jetbrains.letsPlot.platf.w3c.dom.events.DomEventType
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.EventTarget
import org.w3c.dom.events.MouseEventInit
import org.w3c.dom.events.WheelEventInit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DomMouseEventMapperTest {
    @Test
    fun wheelEventShouldRaiseEnteredAndWheelEvents() {
        val div = EventTargetAdapter(600, 600)
        val m = div.createEventMapper()

        div.dispatchEvent(DomEventType.MOUSE_WHEEL, 300, 300).let { events ->
            assertEvent(events[m]!![0], MOUSE_ENTERED, 300, 300)
            assertEvent(events[m]!![1], MOUSE_WHEEL_ROTATED, 300, 300) // means preventDefault was called
        }
    }

    /**
     * Sequence of events was captured from the browser
     */
    @Test
    fun click() {
        val div = EventTargetAdapter(600, 600)
        val m = div.createEventMapper()

        div.dispatchEvent(DomEventType.MOUSE_MOVE, 300, 300)

        div.dispatchEvent(DomEventType.MOUSE_DOWN, 300, 300).let { events ->
            assertEvent(events[m]!!.single(), MOUSE_PRESSED, 300, 300)
        }

        div.dispatchEvent(DomEventType.MOUSE_UP, 300, 300).let { events ->
            assertEvent(events[m]!!.single(), MOUSE_RELEASED, 300, 300)
        }

        div.dispatchEvent(DomEventType.CLICK, 300, 300).let { events ->
            assertEvent(events[m]!!.single(), MOUSE_CLICKED, 300, 300)
        }
    }

    /**
     * Sequence of events was captured from the browser
     */
    @Test
    fun doubleClick() {
        val div = EventTargetAdapter(600, 600)
        val m = div.createEventMapper()

        div.dispatchEvent(DomEventType.MOUSE_MOVE, 300, 300)

        div.dispatchEvent(DomEventType.MOUSE_DOWN, 300, 300).let { events ->
            assertEvent(events[m]!!.single(), MOUSE_PRESSED, 300, 300)
        }

        div.dispatchEvent(DomEventType.MOUSE_UP, 300, 300).let { events ->
            assertEvent(events[m]!!.single(), MOUSE_RELEASED, 300, 300)
        }

        div.dispatchEvent(DomEventType.CLICK, 300, 300).let { events ->
            assertEvent(events[m]!!.single(), MOUSE_CLICKED, 300, 300)
        }

        div.dispatchEvent(DomEventType.MOUSE_DOWN, 300, 300).let { events ->
            assertEvent(events[m]!!.single(), MOUSE_PRESSED, 300, 300)
        }

        div.dispatchEvent(DomEventType.MOUSE_UP, 300, 300).let { events ->
            assertEvent(events[m]!!.single(), MOUSE_RELEASED, 300, 300)
        }

        div.dispatchEvent(DomEventType.CLICK, 300, 300).let { events ->
            assertEvent(events[m]!!.single(), MOUSE_CLICKED, 300, 300)
        }

        div.dispatchEvent(DomEventType.DOUBLE_CLICK, 300, 300).let { events ->
            assertEvent(events[m]!!.single(), MOUSE_DOUBLE_CLICKED, 300, 300)
        }
    }

    /**
     * Test the consequence of two click events without any mouse movement
     */
    @Test
    fun twoConsecutiveClicksWithoutMouseMove() {
        val div = EventTargetAdapter(600, 600)
        val m = div.createEventMapper()

        div.dispatchEvent(DomEventType.MOUSE_MOVE, 300, 300).let {
            assertEvent(it[m]!![0], MOUSE_ENTERED, 300, 300)
            assertEvent(it[m]!![1], MOUSE_MOVED, 300, 300)
        }
        div.dispatchEvent(DomEventType.MOUSE_DOWN, 300, 300).let { events ->
            assertEvent(events[m]!!.single(), MOUSE_PRESSED, 300, 300)
        }

        div.dispatchEvent(DomEventType.MOUSE_UP, 300, 300).let { events ->
            assertEvent(events[m]!!.single(), MOUSE_RELEASED, 300, 300)
        }

        div.dispatchEvent(DomEventType.CLICK, 300, 300).let { events ->
            assertEvent(events[m]!!.single(), MOUSE_CLICKED, 300, 300)
        }

        div.dispatchEvent(DomEventType.MOUSE_DOWN, 300, 300).let { events ->
            assertEvent(events[m]!!.single(), MOUSE_PRESSED, 300, 300)
        }

        div.dispatchEvent(DomEventType.MOUSE_UP, 300, 300).let { events ->
            assertEvent(events[m]!!.single(), MOUSE_RELEASED, 300, 300)
        }

        div.dispatchEvent(DomEventType.CLICK, 300, 300).let { events ->
            assertEvent(events[m]!!.single(), MOUSE_CLICKED, 300, 300)
        }
    }


    @Test
    fun dragThresholdShouldSuppressMoveEventsAndGenerateTwoDragEventsWithOneAtTheMouseDownLocation() {
        val div = EventTargetAdapter(600, 600)
        val m = div.createEventMapper()

        div.dispatchEvent(DomEventType.MOUSE_MOVE, 300, 300)

        div.dispatchEvent(DomEventType.MOUSE_DOWN, 300, 300).let { events ->
            assertEvent(events[m]!!.single(), MOUSE_PRESSED, 300, 300)
        }

        div.dispatchEvent(DomEventType.MOUSE_MOVE, 301, 300).let { events ->
            assertNoEvents(m, events)
        }

        div.dispatchEvent(DomEventType.MOUSE_MOVE, 303, 300).let { events ->
            assertNoEvents(m, events)
        }

        div.dispatchEvent(DomEventType.MOUSE_MOVE, 304, 300).let { events ->
            assertEquals(2, events[m]!!.size)
            assertEvent(events[m]!![0], MOUSE_DRAGGED, 300, 300)
            assertEvent(events[m]!![1], MOUSE_DRAGGED, 304, 300)
        }
    }

    @Test
    fun dragShouldWorkOutsideTheTargetBecauseOfDocumentEventsHandling() {
        val div = EventTargetAdapter(600, 600)
        val m = div.createEventMapper()

        div.dispatchEvent(DomEventType.MOUSE_MOVE, 300, 300)
        div.dispatchEvent(DomEventType.MOUSE_DOWN, 300, 300)

        // trigger drag state so mapper starts to listen document events
        div.dispatchEvent(DomEventType.MOUSE_MOVE, 310, 300).let { events ->
            assertEquals(2, events[m]!!.size)
            assertEvent(events[m]!![0], MOUSE_DRAGGED, 300, 300)
            assertEvent(events[m]!![1], MOUSE_DRAGGED, 310, 300)
        }

        div.dispatchDocumentEvent(DomEventType.MOUSE_MOVE, 900, 900).let { events ->
            assertEvent(events[m]!!.single(), MOUSE_DRAGGED, 900, 900)
        }
    }

    /**
     * Ignore MOUSE_CLICK event after drag release to prevent ghost clicks (e.g. on buttons in a livemap)
     */
    @Test
    fun dragReleaseShouldNotTriggerClickEvent() {
        val div = EventTargetAdapter(600, 600)
        val m = div.createEventMapper()

        div.dispatchEvent(DomEventType.MOUSE_MOVE, 300, 300)
        div.dispatchEvent(DomEventType.MOUSE_DOWN, 300, 300)
        div.dispatchEvent(DomEventType.MOUSE_MOVE, 310, 310)

        // In drag state the whole document is monitored to handle dragging outside the target area,
        // so we need to dispatch event on the document
        div.dispatchDocumentEvent(DomEventType.MOUSE_UP, 310, 310).let { events ->
            assertEvent(events[m]!!.single(), MOUSE_RELEASED, 310, 310)
        }

        // click is still sent from the target, not the document
        div.dispatchEvent(DomEventType.CLICK, 310, 310).let { events ->
            assertNoEvents(m, events) // mapper should not generate click event
        }
    }

    /**
     * MOUSE_MOVED not generated if MOUSE_LEFT is generated
     * Coordinate of MOUSE_LEFT event can be outside the event area
     */
    @Test
    fun withSubAreas_mouseLeftDoesntGenerateMouseMoveEvent() {
        val targetAdapter = EventTargetAdapter(1200, 600)

        val leftArea = targetAdapter.createEventMapper(DoubleRectangle.XYWH(0, 0, 600, 600))
        val rightArea = targetAdapter.createEventMapper(DoubleRectangle.XYWH(600, 0, 600, 600))

        targetAdapter.dispatchEvent(DomEventType.MOUSE_ENTER, 300, 300).let { events ->
            assertEvent(events[leftArea]!!.single(), MOUSE_ENTERED, 300, 300)
            assertNoEvents(rightArea, events)
        }

        targetAdapter.dispatchEvent(DomEventType.MOUSE_MOVE, 900, 300).let { events ->
            // MOUSE_LEFT coord is outside the event area
            // no MOUSE_MOVED event is generated
            assertEvent(events[leftArea]!!.single(), MOUSE_LEFT, 900, 300)

            assertEvent(events[rightArea]!![0], MOUSE_ENTERED, 300, 300)
            assertEvent(events[rightArea]!![1], MOUSE_MOVED, 300, 300)
        }
    }

    @Test
    fun singleDivWithTwoEventAreas() {
        val targetAdapter = EventTargetAdapter(1200, 600)

        val leftArea = targetAdapter.createEventMapper(DoubleRectangle.XYWH(0, 0, 599, 600))
        val rightArea = targetAdapter.createEventMapper(DoubleRectangle.XYWH(600, 0, 600, 600))

        targetAdapter.dispatchEvent(DomEventType.MOUSE_ENTER, 0, 300).let { events ->
            assertEvent(events[leftArea]!!.single(), MOUSE_ENTERED, 0, 300)
            assertNoEvents(rightArea, events)
        }

        targetAdapter.dispatchEvent(DomEventType.MOUSE_MOVE, 300, 300).let { events ->
            assertEvent(events[leftArea]!!.single(), MOUSE_MOVED, 300, 300)
            assertNoEvents(rightArea, events)
        }

        targetAdapter.dispatchEvent(DomEventType.MOUSE_MOVE, 600, 300).let { events ->
            printEvents(events)
            assertEvent(events[leftArea]!!.single(), MOUSE_LEFT, 600, 300)
            assertEvent(events[rightArea]!![0], MOUSE_ENTERED, 0, 300)
            assertEvent(events[rightArea]!![1], MOUSE_MOVED, 0, 300)
        }

        targetAdapter.dispatchEvent(DomEventType.MOUSE_MOVE, 601, 300).let { events ->
            printEvents(events)
            assertNoEvents(leftArea, events)
            assertEvent(events[rightArea]!!.single(), MOUSE_MOVED, 1, 300)
        }

        targetAdapter.dispatchEvent(DomEventType.MOUSE_MOVE, 650, 300).let { events ->
            assertNoEvents(leftArea, events)
            assertEvent(events[rightArea]!!.single(), MOUSE_MOVED, 50, 300)
        }
    }

    private fun assertEvent(
        actualEvent: Pair<MouseEventSpec, MouseEvent>,
        expectedSpec: MouseEventSpec,
        expectedX: Int,
        expectedY: Int
    ) {
        val (spec, event) = actualEvent
        // Triple is used to provide more informative error message - it shows all three values together
        assertEquals(Triple(expectedSpec, expectedX, expectedY), Triple(spec, event.x, event.y))
    }

    private fun assertNoEvents(id: DomMouseEventMapper, events: Map<DomMouseEventMapper, List<Pair<MouseEventSpec, MouseEvent>>>) {
        val msg = events[id]?.joinToString { (spec, event) -> "[$spec at (${event.x}, ${event.y})]" } ?: ""
        assertTrue(msg) { id !in events }
    }

    private fun printEvents(events: Map<DomMouseEventMapper, List<Pair<MouseEventSpec, MouseEvent>>>) {
        events.forEach { (id, list) ->
            println("Events for $id:")
            list.forEach { (spec, event) ->
                println("  $spec at (${event.x}, ${event.y})")
            }
        }
    }

    private class EventTargetAdapter(
        private val w: Int,
        private val h: Int
    ) {
        val target = kotlinx.browser.document.createElement("div") as HTMLDivElement
        private val mappers = mutableListOf<DomMouseEventMapper>()
        private val events = mutableMapOf<DomMouseEventMapper, MutableList<Pair<MouseEventSpec, MouseEvent>>>()
        private val regs = CompositeRegistration()

        init {
            target.style.width = "${w}px"
            target.style.height = "${h}px"
        }

        fun createEventMapper(r: DoubleRectangle? = null): DomMouseEventMapper {
            val area = r ?: DoubleRectangle.XYWH(0, 0, w, h)
            val eventMapper = DomMouseEventMapper(target, eventArea = area)
            mappers.add(eventMapper)

            MouseEventSpec.entries.forEach { spec ->
                val reg = eventMapper.on(spec, eventHandler = { events.getOrPut(eventMapper) { mutableListOf() } += spec to it })
                regs.add(reg)
            }

            return eventMapper
        }

        fun dispatchEvent(type: DomEventType<*>, x: Number, y: Number): Map<DomMouseEventMapper, List<Pair<MouseEventSpec, MouseEvent>>> {
            return dispatchEvent(target, type, x, y)
        }

        fun dispatchDocumentEvent(type: DomEventType<*>, x: Number, y: Number): Map<DomMouseEventMapper, List<Pair<MouseEventSpec, MouseEvent>>> {
            return dispatchEvent(kotlinx.browser.document, type, x, y)
        }

        private fun dispatchEvent(eventTarget: EventTarget, type: DomEventType<*>, x: Number, y: Number): Map<DomMouseEventMapper, MutableList<Pair<MouseEventSpec, MouseEvent>>> {
            val event = if (type == DomEventType.MOUSE_WHEEL) {
                org.w3c.dom.events.WheelEvent(type.name, object : WheelEventInit {
                    override var clientX: Int? = x.toInt()
                    override var clientY: Int? = y.toInt()
                })
            } else {
                org.w3c.dom.events.MouseEvent(type.name, object : MouseEventInit {
                    override var clientX: Int? = x.toInt()
                    override var clientY: Int? = y.toInt()
                })
            }

            events.clear()
            eventTarget.dispatchEvent(event)
            return events.toMap() // copy
        }
    }

    companion object {
        fun MouseEventSource.on(eventSpec: MouseEventSpec, eventHandler: (MouseEvent) -> Unit): Registration {
            return addEventHandler(eventSpec, object : EventHandler<MouseEvent> {
                override fun onEvent(event: MouseEvent) {
                    eventHandler(event)
                }
            })
        }
    }
}
