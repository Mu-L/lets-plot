
import org.jetbrains.letsPlot.commons.values.Color
import org.jetbrains.letsPlot.core.canvas.Font
import kotlin.math.PI
import kotlin.test.Ignore
import kotlin.test.Test

/*
 * Copyright (c) 2025. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */


class CanvasPath2dTest : CanvasTestBase() {
    private val w = 100.0
    private val h = 100.0

    private val strokeColor = "#000000"
    private val fillColor = "#000000"
    private val filledStrokeColor = "#000080"
    private val strokedFillColor = "#FFC000"

    @Test
    fun shearedEllipse() {
        val (canvas, ctx) = createCanvas()

        ctx.strokeStyle = strokeColor
        ctx.beginPath()
        ctx.save()
        ctx.transform(sx = 1.0, ry = 0.3420201241970062, rx = 0.0, sy = 1.0, tx = 0.0, ty = -30.0)
        ctx.ellipse(
            x = 50.0,
            y = 60.0,
            radiusX = 45.0,
            radiusY = 20.0,
            rotation = 0.0,
            startAngle = 0.0,
            endAngle = PI * 2,
            anticlockwise = true
        )
        ctx.restore()
        ctx.closePath()
        ctx.stroke()

        assertCanvas("sheared_ellipse.png", canvas)
    }

    @Test
    fun shearedCircularArc() {
        val (canvas, ctx) = createCanvas()

        ctx.strokeStyle = strokeColor
        ctx.beginPath()
        ctx.save()
        ctx.transform(sx = 1.0, ry = 0.3420201241970062, rx = 0.0, sy = 1.0, tx = 0.0, ty = -30.0)
        ctx.moveTo(95.0, 95.0)
        ctx.lineTo(95.0, 5.0)
        ctx.ellipse(
            x = 95.0,
            y = 95.0,
            radiusX = 90.0,
            radiusY = 90.0,
            rotation = 0.0,
            startAngle = -PI / 2,
            endAngle = PI,
            anticlockwise = true
        )
        ctx.restore()
        ctx.closePath()
        ctx.stroke()

        assertCanvas("sheared_circular_arc.png", canvas)
    }

    @Test
    fun nestedTranslates() {
        val (canvas, ctx) = createCanvas()

        ctx.fillStyle = fillColor

        ctx.save()
        ctx.translate(0.0, 50.0)
        ctx.save()
        ctx.translate(33.0, 0.0)
        ctx.beginPath()
        ctx.arc(0.0, 0.0, 5.0, 0.0, 2 * PI)
        ctx.fill()
        ctx.restore()

        ctx.save()
        ctx.translate(66.0, 0.0)
        ctx.beginPath()
        ctx.arc(x = 0.0, y = 0.0, radius = 5.0, startAngle = 0.0, endAngle = 2 * PI)
        ctx.fill()
        ctx.restore()

        ctx.restore()

        assertCanvas("nested_translates.png", canvas)
    }

    @Test
    fun multiPathFill() {
        val (canvas, ctx) = createCanvas()
        ctx.fillStyle = strokedFillColor
        ctx.strokeStyle = filledStrokeColor
        ctx.lineWidth = 5.0

        ctx.moveTo(50.0, 50.0)
        ctx.lineTo(125.0, 125.0)

        ctx.beginPath()
        ctx.moveTo(0.0, 0.0)
        ctx.lineTo(25.0, 25.0)
        ctx.lineTo(50.0, 0.0)
        ctx.closePath()

        ctx.moveTo(100.0, 100.0)
        ctx.lineTo(75.0, 75.0)
        ctx.lineTo(50.0, 100.0)
        ctx.closePath()

        ctx.fill()
        ctx.stroke()

        assertCanvas("multi_path_fill.png", canvas)
    }

    @Test
    fun multiPathStroke() {
        val (canvas, ctx) = createCanvas()

        ctx.strokeStyle = strokeColor
        ctx.lineWidth = 2.0

        ctx.beginPath()
        ctx.moveTo(0.0, 0.0)
        ctx.lineTo(25.0, 25.0)
        ctx.lineTo(50.0, 0.0)
        ctx.closePath()

        ctx.moveTo(100.0, 100.0)
        ctx.lineTo(75.0, 75.0)
        ctx.lineTo(50.0, 100.0)
        ctx.closePath()

        ctx.stroke()

        assertCanvas(
            expectedFileName = "multi_path_stroke.png",
            canvas = canvas,
        )
    }

    @Test
    fun zigZagStroke() {
        val (canvas, ctx) = createCanvas()
        ctx.strokeStyle = strokeColor
        ctx.lineWidth = 3.0

        ctx.beginPath()
        ctx.moveTo(0.0, 0.0)
        ctx.lineTo(50.0, 25.0)
        ctx.lineTo(0.0, 50.0)
        ctx.lineTo(50.0, 75.0)
        ctx.lineTo(0.0, 100.0)

        ctx.moveTo(100.0, 0.0)
        ctx.lineTo(50.0, 25.0)
        ctx.lineTo(100.0, 50.0)
        ctx.lineTo(50.0, 75.0)
        ctx.lineTo(100.0, 100.0)

        ctx.stroke()

        assertCanvas(
            expectedFileName = "zigzag_stroke.png",
            canvas = canvas,
        )
    }

    @Test
    fun zigZagFill() {
        val (canvas, ctx) = createCanvas()
        ctx.fillStyle = fillColor
        ctx.lineWidth = 1.0

        ctx.beginPath()
        ctx.moveTo(0.0, 0.0)
        ctx.lineTo(50.0, 25.0)
        ctx.lineTo(0.0, 50.0)
        ctx.lineTo(50.0, 75.0)
        ctx.lineTo(0.0, 100.0)
        ctx.closePath()

        ctx.moveTo(50.0, 0.0)
        ctx.lineTo(100.0, 25.0)
        ctx.lineTo(50.0, 50.0)
        ctx.lineTo(100.0, 75.0)
        ctx.lineTo(50.0, 100.0)
        ctx.closePath()

        ctx.fill()

        assertCanvas(
            expectedFileName = "zigzag_fill.png",
            canvas = canvas,
        )
    }

    @Test
    fun circleStroke() {
        val (canvas, ctx) = createCanvas()
        ctx.strokeStyle = strokeColor
        ctx.lineWidth = 1.0

        ctx.beginPath()
        ctx.arc(x = 50.0, y = 50.0, radius = 40.0, startAngle = -PI, endAngle = 0.0)
        ctx.stroke()

        assertCanvas(
            expectedFileName = "circle_stroke.png",
            canvas = canvas
        )
    }

    @Test
    fun circleFill() {
        val (canvas, ctx) = createCanvas()
        ctx.fillStyle = fillColor
        ctx.lineWidth = 1.0

        ctx.beginPath()
        ctx.arc(x = 50.0, y = 50.0, radius = 40.0, startAngle = -PI, endAngle = 0.0)
        ctx.closePath()

        ctx.fill()

        assertCanvas(
            expectedFileName = "circle_fill.png",
            canvas = canvas
        )
    }

    @Test
    fun circleFillStroke() {
        val (canvas, ctx) = createCanvas()
        ctx.beginPath()
        ctx.arc(x = 50.0, y = 50.0, radius = 40.0, startAngle = -PI, endAngle = 0.0)
        ctx.closePath()

        ctx.fillStyle = strokedFillColor
        ctx.fill()

        ctx.strokeStyle = filledStrokeColor
        ctx.setLineWidth(2.0)
        ctx.stroke()

        assertCanvas(
            expectedFileName = "circle_fill_stroke.png",
            canvas = canvas
        )
    }

    @Test
    fun ellipse() {
        val (canvas, ctx) = createCanvas()

        ctx.fillStyle = fillColor

        ctx.beginPath()
        ctx.ellipse(
            x = 50.0,
            y = 50.0,
            radiusX = 20.0,
            radiusY = 50.0,
            rotation = 0.0,
            startAngle = -PI,
            endAngle = -2 * PI,
            anticlockwise = true
        )
        ctx.closePath()
        ctx.fill()

        assertCanvas(
            expectedFileName = "ellipse.png",
            canvas = canvas
        )
    }

    @Test
    fun rotatedEllipse() {
        val (canvas, ctx) = createCanvas()

        ctx.fillStyle = fillColor
        ctx.beginPath()
        ctx.ellipse(
            x = 50.0,
            y = 50.0,
            radiusX = 20.0,
            radiusY = 50.0,
            rotation = PI / 4,
            startAngle = -PI,
            endAngle = -2 * PI,
            anticlockwise = true
        )
        ctx.closePath()
        ctx.fill()

        assertCanvas(
            expectedFileName = "rotated_ellipse.png",
            canvas = canvas
        )
    }


    @Test
    fun pathTransformOnBuild() {
        val (canvas, ctx) = createCanvas()
        ctx.strokeStyle = strokeColor
        ctx.lineWidth = 2.0

        ctx.save()
        ctx.translate(50.0, 50.0)
        ctx.rotate(PI / 2)
        ctx.scale(0.5, 0.5)
        ctx.beginPath()
        ctx.moveTo(0.0, 0.0)
        ctx.lineTo(50.0, 50.0)
        ctx.restore()
        ctx.stroke()

        assertCanvas(
            expectedFileName = "path_transform_on_build.png",
            canvas = canvas
        )
    }

    @Test
    fun arcTransformsAfterRestore() {
        val (canvas, ctx) = createCanvas()
        ctx.fillStyle = fillColor

        ctx.save()
        ctx.translate(50.0, 50.0)
        ctx.beginPath()
        ctx.scale(1.0, 0.5)
        ctx.arc(0.0, 0.0, 50.0, 0.0, 2 * PI)
        ctx.restore()

        ctx.fill()

        assertCanvas(
            expectedFileName = "arc_transform_after_restore.png",
            canvas = canvas
        )
    }

    @Test
    fun text_SkewTransform() {
        val (canvas, ctx) = createCanvas()

        ctx.fillStyle = strokeColor
        ctx.strokeStyle = strokeColor
        ctx.setFont(Font(fontFamily = "serif", fontSize = 30.0))

        ctx.transform(sx = 1.0, ry = 0.0, rx = -0.33, sy = 1.0, tx = 0.0, ty = 0.0)

        ctx.beginPath()
        ctx.moveTo(w * 0.5, h * 0.5)
        ctx.lineTo(w * 0.5, h * 0.5 - 30)
        ctx.lineTo(w * 0.5 + 35, h * 0.5 - 30)
        ctx.lineTo(w * 0.5 + 35, h * 0.5)
        ctx.closePath()
        ctx.stroke()
        //ctx.clip()


        ctx.fillText("Test", w * 0.5, h * 0.5)

        assertCanvas("text_skew_transform.png", canvas)
    }


    @Test
    fun simpleBezierCurve() {
        val (canvas, ctx) = createCanvas()

        ctx.strokeStyle = strokeColor
        ctx.lineWidth = 2.0

        ctx.beginPath()
        ctx.moveTo(0.0, 0.0)
        ctx.bezierCurveTo(50.0, 0.0, 50.0, 100.0, 100.0, 100.0)
        ctx.stroke()

        assertCanvas(
            expectedFileName = "simple_bezier_curve.png",
            canvas = canvas
        )
    }

    @Test
    fun bezierCurveInsidePath() {
        val (canvas, ctx) = createCanvas()

        ctx.strokeStyle = strokeColor
        ctx.lineWidth = 2.0

        ctx.beginPath()
        ctx.moveTo(0, 20)
        ctx.lineTo(20, 20)
        ctx.bezierCurveTo(20, 80, 80, 80, 80, 20)
        ctx.lineTo(100, 20)
        ctx.stroke()

        assertCanvas(
            expectedFileName = "bezier_curve_inside_path.png",
            canvas = canvas
        )
    }

    @Test
    fun ellipseInsidePath() {
        val (canvas, ctx) = createCanvas()

        ctx.strokeStyle = strokeColor
        ctx.lineWidth = 2.0

        ctx.beginPath()
        ctx.moveTo(100, 10)
        ctx.lineTo(80, 10)
        ctx.ellipse(
            x = 50,
            y = 20,
            radiusX = 20,
            radiusY = 20,
            rotation = 0,
            startAngle = 0,
            endAngle = -PI,
            anticlockwise = false
        )
        ctx.lineTo(20, 10)
        ctx.lineTo(0, 10)
        ctx.stroke()

        assertCanvas(
            expectedFileName = "ellipse_inside_path.png",
            canvas = canvas
        )
    }

    @Test
    fun roundedRectWithCurves() {
        val (canvas, ctx) = createCanvas()

        ctx.lineWidth = 2.0
        ctx.strokeStyle = filledStrokeColor
        ctx.fillStyle = strokedFillColor

        ctx.translate(5, 5)
        ctx.scale(3.0)
        ctx.beginPath()
        ctx.moveTo(25.6086387569017, 21.0)
        ctx.bezierCurveTo(25.6086387569017, 21.0, 28.7586387569017, 21.0, 28.7586387569017, 17.85)
        ctx.lineTo(28.7586387569017, 3.15)
        ctx.bezierCurveTo(28.7586387569017, 3.15, 28.7586387569017, 0.0, 25.6086387569017, 0.0)
        ctx.lineTo(3.37605456734872, 0.0)
        ctx.bezierCurveTo(3.37605456734872, 0.0, 0.2260545673487, 0.0, 0.2260545673487, 3.15)
        ctx.lineTo(0.2260545673487, 17.85)
        ctx.bezierCurveTo(0.2260545673487, 17.85, 0.2260545673487, 21.0, 3.37605456734872, 21.0)
        ctx.closePath()
        ctx.fill()
        ctx.stroke()

        assertCanvas(
            expectedFileName = "rounded_rect_with_curves.png",
            canvas = canvas
        )
    }

    @Ignore
    @Test
    fun perf_5_000_points() {
        val (canvas, ctx) = createCanvas()

        ctx.fillStyle = fillColor
        ctx.strokeStyle = strokeColor
        ctx.lineWidth = 1.0

        for (i in 0 until 3_200) {
            ctx.setFillStyle(Color.HOT_PINK)
            ctx.setStrokeStyle(Color.TRANSPARENT)
            ctx.beginPath()
            ctx.arc(
                x = 25.0,
                y = 25.0,
                radius = 1.0,
                startAngle = 0.0,
                endAngle = 2 * PI,
                anticlockwise = true
            )
            ctx.closePath()
            ctx.fill()
            ctx.setStrokeStyle(Color.HOT_PINK)
        }

        assertCanvas(
            expectedFileName = "perf_5_000_points.png",
            canvas = canvas
        )
    }
}
