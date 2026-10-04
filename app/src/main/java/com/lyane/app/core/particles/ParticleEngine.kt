package com.lyane.app.core.particles

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.lyane.app.data.model.ParticleConfig
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.*
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Int,
    var size: Float,
    var maxLife: Float,
    var age: Float = 0.0f,
    var alpha: Float = 1.0f
) {
    val isDead: Boolean
        get() = age >= maxLife
}

class ParticleEngine {

    private val particles = CopyOnWriteArrayList<Particle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    fun emitNoteHit(
        x: Float,
        y: Float,
        colorHex: String,
        velocity: Int,
        config: ParticleConfig
    ) {
        if (!config.enabled) return

        val parsedColor = try {
            Color.parseColor(colorHex)
        } catch (_: Exception) {
            Color.CYAN
        }

        val baseCount = (config.count * (velocity / 127.0f)).toInt().coerceIn(10, 200)

        for (i in 0 until baseCount) {
            val angle = Random.nextDouble(-Math.PI, 0.0).toFloat() * config.spread
            val speed = (Random.nextFloat() * 4.0f + 1.5f) * config.speed * (velocity / 100.0f)

            val vx = cos(angle) * speed + (Random.nextFloat() - 0.5f) * config.turbulence * 2.0f
            val vy = sin(angle) * speed

            val size = (Random.nextFloat() * 6.0f + 3.0f) * (config.size / 8.0f)
            val lifetime = (Random.nextFloat() * 0.8f + 0.6f) * config.lifetime

            particles.add(
                Particle(
                    x = x + (Random.nextFloat() - 0.5f) * 12.0f,
                    y = y,
                    vx = vx,
                    vy = vy,
                    color = parsedColor,
                    size = size,
                    maxLife = lifetime
                )
            )
        }
    }

    fun emitChordExplosion(
        x: Float,
        y: Float,
        colorHex: String,
        chordSize: Int,
        config: ParticleConfig
    ) {
        if (!config.enabled) return

        val count = (config.count * chordSize * 0.8f).toInt().coerceIn(40, 300)
        val parsedColor = try { Color.parseColor(colorHex) } catch (_: Exception) { Color.MAGENTA }

        for (i in 0 until count) {
            val angle = (Random.nextFloat() * 2.0 * Math.PI).toFloat()
            val speed = (Random.nextFloat() * 6.0f + 2.0f) * config.speed
            val vx = cos(angle) * speed
            val vy = sin(angle) * speed

            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    color = parsedColor,
                    size = (Random.nextFloat() * 8.0f + 4.0f) * (config.size / 8.0f),
                    maxLife = config.lifetime * 1.5f
                )
            )
        }
    }

    fun update(deltaTimeSec: Float, config: ParticleConfig) {
        if (!config.enabled && particles.isEmpty()) return

        val iterator = particles.iterator()
        val deadList = mutableListOf<Particle>()

        for (p in iterator) {
            p.age += deltaTimeSec
            if (p.isDead) {
                deadList.add(p)
                continue
            }

            // Physics update
            p.x += p.vx * 60.0f * deltaTimeSec
            p.y += p.vy * 60.0f * deltaTimeSec
            p.vy += config.gravity * 60.0f * deltaTimeSec

            // Drag / air turbulence
            p.vx *= (1.0f - 0.02f * config.turbulence)
            p.vy *= (1.0f - 0.02f * config.turbulence)

            val progress = p.age / p.maxLife
            p.alpha = (1.0f - progress).coerceIn(0.0f, 1.0f) * config.opacity
        }

        particles.removeAll(deadList.toSet())
    }

    fun draw(canvas: Canvas) {
        for (p in particles) {
            val a = (p.alpha * 255).toInt().coerceIn(0, 255)
            val r = Color.red(p.color)
            val g = Color.green(p.color)
            val b = Color.blue(p.color)
            paint.color = Color.argb(a, r, g, b)

            // Draw glowing particle
            canvas.drawCircle(p.x, p.y, p.size, paint)
        }
    }

    fun clear() {
        particles.clear()
    }
}
