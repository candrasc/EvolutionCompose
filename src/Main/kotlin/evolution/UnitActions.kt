package evolution

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class UnitAction {
    abstract fun runAction(unit: EUnit)
}
abstract class TimedUnitAction(val durationMilli: Long): UnitAction() {

    override fun runAction(unit: EUnit) {

        applyAction(unit)

        var coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {
            delay(durationMilli)
            removeAction(unit)

        }
    }
    abstract fun applyAction(unit: EUnit)

    abstract fun removeAction(unit: EUnit)


}

class BounceAction(durationMilli: Long, val slowPercentage: Float, val shrinkPercentage: Float): TimedUnitAction(durationMilli) {


    override fun applyAction(unit: EUnit) {

        unit.xVelocity *= (1-slowPercentage)
        unit.yVelocity *= (1-slowPercentage)

        unit.size *= (1-shrinkPercentage)
    }

    override fun removeAction(unit: EUnit) {
        unit.xVelocity /= (1-slowPercentage)
        unit.yVelocity /= (1-slowPercentage)

        unit.size /= (1-shrinkPercentage)
    }

}


