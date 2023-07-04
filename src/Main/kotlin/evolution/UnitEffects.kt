package evolution

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class UnitEffect(val durationMilli: Long) {

    fun runEffect(unit: EUnit) {

        applyEffect(unit)

        var coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {
            delay(durationMilli)
            removeEffect(unit)

        }
    }
    abstract fun applyEffect(unit: EUnit)

    abstract fun removeEffect(unit: EUnit)


}

class BounceEffect(durationMilli: Long, val slowPercentage: Float, val shrinkPercentage: Float): UnitEffect(durationMilli) {


    override fun applyEffect(unit: EUnit) {

        unit.xVelocity *= (1-slowPercentage)
        unit.yVelocity *= (1-slowPercentage)

        unit.size *= (1-shrinkPercentage)
    }

    override fun removeEffect(unit: EUnit) {
        unit.xVelocity /= (1-slowPercentage)
        unit.yVelocity /= (1-slowPercentage)

        unit.size /= (1-shrinkPercentage)
    }

}