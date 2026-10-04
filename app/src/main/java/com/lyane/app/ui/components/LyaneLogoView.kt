package com.lyane.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lyane.app.ui.theme.*
import kotlin.math.*

@Composable
fun LyaneLogoView(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    animated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lunarGlow")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w / 2f, h / 2f)
        val radius = min(w, h) * 0.42f

        // Outer glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(LyaneCyan.copy(alpha = 0.25f * (if (animated) pulse else 1f)), Color.Transparent),
                center = center,
                radius = radius * 1.4f
            ),
            radius = radius * 1.3f,
            center = center
        )

        // Orbital Lunar Ring Arc 1
        drawArc(
            color = LyaneCyan,
            startAngle = if (animated) rotation else 45f,
            sweepAngle = 260f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = w * 0.05f, cap = StrokeCap.Round)
        )

        // Secondary Geometric Resonance Arc
        val innerRadius = radius * 0.72f
        drawArc(
            color = LyanePurple,
            startAngle = if (animated) -rotation * 1.5f else 180f,
            sweepAngle = 200f,
            useCenter = false,
            topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
            size = androidx.compose.ui.geometry.Size(innerRadius * 2, innerRadius * 2),
            style = Stroke(width = w * 0.04f, cap = StrokeCap.Round)
        )

        // Central Geometric Diamond-Moon
        val coreSize = radius * 0.45f
        val path = Path().apply {
            moveTo(center.x, center.y - coreSize)
            lineTo(center.x + coreSize, center.y)
            lineTo(center.x, center.y + coreSize)
            lineTo(center.x - coreSize, center.y)
            close()
        }
        drawPath(path, brush = Brush.linearGradient(listOf(LyaneCyan, LyaneCyanGlow, LyanePurple)))

        // 3 Glowing Piano Beams inside core
        val beamW = w * 0.025f
        val beamH = coreSize * 0.9f
        drawRoundRect(
            color = LyaneBlack,
            topLeft = Offset(center.x - beamW * 1.5f, center.y - beamH / 2f),
            size = androidx.compose.ui.geometry.Size(beamW * 3f, beamH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )
        drawLine(
            color = LyaneCyan,
            start = Offset(center.x, center.y - beamH * 0.4f),
            end = Offset(center.x, center.y + beamH * 0.4f),
            strokeWidth = beamW,
            cap = StrokeCap.Round
        )
    }
}
