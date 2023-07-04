package evolution

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Environment(val refeshRatePerSecond: Int = 60) {

    var units: List<EUnit> = emptyList()
    private var isActive = false
    private var coroutineScope = CoroutineScope(Dispatchers.Main)

    var onUpdate by mutableStateOf(0)

    fun addUnits(newUnits: List<EUnit>) {
        units = units.plus(newUnits)
    }

    fun step() {
        units.forEach { it.step() }
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