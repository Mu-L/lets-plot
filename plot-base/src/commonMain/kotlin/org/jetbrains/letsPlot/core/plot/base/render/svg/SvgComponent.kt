/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.plot.base.render.svg

import org.jetbrains.letsPlot.commons.event.Event
import org.jetbrains.letsPlot.commons.geometry.DoubleRectangle
import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.commons.intern.observable.event.EventHandler
import org.jetbrains.letsPlot.commons.registration.CompositeRegistration
import org.jetbrains.letsPlot.commons.registration.Registration
import org.jetbrains.letsPlot.commons.values.Color
import org.jetbrains.letsPlot.datamodel.svg.dom.*
import org.jetbrains.letsPlot.datamodel.svg.dom.SvgGraphicsElement.Companion.CLIP_BOUNDS_JFX
import org.jetbrains.letsPlot.datamodel.svg.dom.SvgGraphicsElement.Companion.CLIP_CIRCLE_JFX
import org.jetbrains.letsPlot.datamodel.svg.event.SvgEventHandler
import org.jetbrains.letsPlot.datamodel.svg.event.SvgEventSpec

abstract class SvgComponent {
    protected var isBuilt: Boolean = false
        private set
    private var myIsBuilding: Boolean = false
    private val myRootGroup = SvgGElement()
    private val myChildComponents = ArrayList<SvgComponent>()
    private var myOrigin = DoubleVector.ZERO
    private var myRotationAngle = 0.0
    private var myCompositeRegistration = CompositeRegistration()

    protected val childComponents: List<SvgComponent>
        get() {
            require(isBuilt) { "Plot has not yet built" }
            return ArrayList(myChildComponents)
        }

    val rootGroup: SvgGElement
        get() {
            ensureBuilt()
            return myRootGroup
        }

    fun ensureBuilt() {
        if (!(isBuilt || myIsBuilding)) {
            buildComponentIntern()
        }
    }

    private fun buildComponentIntern() {
        try {
            myIsBuilding = true
            buildComponent()
        } finally {
            myIsBuilding = false
            isBuilt = true
        }
    }

    protected abstract fun buildComponent()

    protected fun <EventT> rebuildHandler(): EventHandler<EventT> {
        return object : EventHandler<EventT> {
            override fun onEvent(event: EventT) {
                needRebuild()
            }
        }
    }

    protected fun needRebuild() {
        if (isBuilt) {
            clear()
            buildComponentIntern()
        }
    }

    protected fun reg(r: Registration) {
        myCompositeRegistration.add(r)
    }

    open fun clear() {
        isBuilt = false
        for (child in myChildComponents) {
            child.clear()
        }
        myChildComponents.clear()
        myRootGroup.children().clear()
        myCompositeRegistration.remove()
        myCompositeRegistration = CompositeRegistration()
    }

    fun add(child: SvgComponent) {
        myChildComponents.add(child)
        add(child.rootGroup)
    }

    fun add(node: SvgNode) {
        myRootGroup.children().add(node)
    }

    fun moveTo(p: DoubleVector) {
        myOrigin = p
        myRootGroup.transform().set(
            buildTransform(
                myOrigin,
                myRotationAngle
            )
        )
    }

    fun moveTo(x: Double, y: Double) {
        moveTo(DoubleVector(x, y))
    }

    /**
     * @param angle in degrees
     */
    fun rotate(angle: Double) {
        myRotationAngle = angle
        myRootGroup.transform().set(
            buildTransform(
                myOrigin,
                myRotationAngle
            )
        )
    }

    fun toRelativeCoordinates(location: DoubleVector): DoubleVector {
        return rootGroup.pointToTransformedCoordinates(location)
    }

    fun toAbsoluteCoordinates(location: DoubleVector): DoubleVector {
        return rootGroup.pointToAbsoluteCoordinates(location)
    }

    fun clipBounds(rect: DoubleRectangle) {
        val clipPathElement = SvgClipPathElement().apply {
            id().set(SvgUID.get(CLIP_PATH_ID_PREFIX))
            children().add(SvgRectElement().apply {
                x().set(rect.left)
                y().set(rect.top)
                width().set(rect.width)
                height().set(rect.height)
            }
            )
        }
        val defs = SvgDefsElement().apply {
            children().add(clipPathElement)
        }
        add(defs)

        rootGroup.clipPath().set(SvgIRI(clipPathElement.id().get()!!))
        rootGroup.setAttribute(CLIP_BOUNDS_JFX, rect) // JFX workaround
    }

    fun clipCircle(center: DoubleVector, radius: Double) {
        val clipPathElement = SvgClipPathElement().apply {
            id().set(SvgUID.get(CLIP_PATH_ID_PREFIX))
            children().add(SvgCircleElement().apply {
                cx().set(center.x)
                cy().set(center.y)
                r().set(radius)
            }
            )
        }
        val defs = SvgDefsElement().apply {
            children().add(clipPathElement)
        }
        add(defs)

        rootGroup.clipPath().set(SvgIRI(clipPathElement.id().get()!!))
        rootGroup.setAttribute(
            CLIP_CIRCLE_JFX,
            DoubleRectangle.XYWH(center.x - radius, center.y - radius, radius * 2, radius * 2)
        ) // JFX workaround
    }

    open fun addClassName(className: String) {
        myRootGroup.addClass(className)
    }

    fun drawDebugRect(r: DoubleRectangle, color: Color, message: String? = null) {
        val rect = SvgRectElement(r)
        rect.strokeColor().set(color)
        rect.strokeWidth().set(1.0)
        rect.fillOpacity().set(0.0)
        message?.run {
            onMouseMove(rect, "$message: $r")
        }
        add(rect)
    }

    /**
     * Only used when DEBUG_DRAWING is ON.
     */
    private fun onMouseMove(e: SvgElement, message: String) {
        e.addEventHandler(SvgEventSpec.MOUSE_MOVE, object :
            SvgEventHandler<Event> {
            override fun handle(node: SvgNode, e: Event) {
                println(message)
            }
        })
    }


    companion object {
        const val CLIP_PATH_ID_PREFIX = "c"

        fun buildTransform(origin: DoubleVector, rotationAngle: Double): SvgTransform {
            val transformBuilder = SvgTransformBuilder()
            if (origin != DoubleVector.ZERO) {
                transformBuilder.translate(origin.x, origin.y)
            }
            if (rotationAngle != 0.0) {
                transformBuilder.rotate(rotationAngle)
            }
            return transformBuilder.build()
        }
    }
}
