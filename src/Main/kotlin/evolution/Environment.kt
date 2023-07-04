package evolution

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import explosion.randomInRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Environment(val numLiveUnits: Int, val refeshRatePerSecond: Int = 60) {

    var deadUnits = mutableSetOf<LiveUnit>()
    var liveUnits = mutableSetOf<LiveUnit>()
    private var isActive = false
    private var coroutineScope = CoroutineScope(Dispatchers.Main)

    var onUpdate by mutableStateOf(0)

    fun addLiveUnits(newUnits: MutableSet<LiveUnit>) {
        liveUnits = liveUnits.plus(newUnits) as MutableSet<LiveUnit>
    }

    fun step() {
        deadUnits = mutableSetOf()
        liveUnits.forEach {
            if (!it.isAlive) deadUnits.add(it)
            it.step() }

        liveUnits.removeAll(deadUnits)
    }

    fun start() {
        if(isActive) return

        coroutineScope.launch {

            this@Environment.isActive = true
            while(this@Environment.isActive) {
                delay(1000L/refeshRatePerSecond)
                step()
                onUpdate = (0..1_000_000).random()

            }
        }
    }

    fun pause() {
        isActive = false
    }

    fun reset() {
        isActive = false
        liveUnits = mutableSetOf()
        deadUnits = mutableSetOf()
        spawnLiveUnits()
    }

    fun spawnLiveUnits() {
        val units = List(numLiveUnits) {
            LiveUnit(
                color = Color(listOf(0xffea4335, 0xff4285f4, 0xfffbbc05, 0xff34a853).random()),
                xPos = randomInRange(5f, 95f), // keep the edges from clipping through side
                yPos = randomInRange(5f, 95f),
                xVelocity = randomInRange(-1f, 1f),
                yVelocity = randomInRange(-1f, 1f),
                size = randomInRange(0.2f, 6f),
                energy = 100f,
                energyDecay = randomInRange(0.1f, 1f)
            )

        }.toMutableSet()
        liveUnits.addAll(units)
    }

}