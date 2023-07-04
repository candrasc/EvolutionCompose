package evolution

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Environment(val refeshRatePerSecond: Int = 60) {
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

}