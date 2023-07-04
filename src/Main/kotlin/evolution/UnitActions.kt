package evolution

import androidx.compose.ui.graphics.Color
import explosion.randomInRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class UnitAction {
    abstract suspend fun runAction(unit: EUnit)
}

class BounceAction(val durationMilli: Long, val slowPercentage: Float, val shrinkPercentage: Float): UnitAction() {


    override suspend fun runAction(unit: EUnit) {

    preDelayAction(unit)
    delay(durationMilli)
    postDelayAction(unit)

    }

    fun preDelayAction(unit: EUnit) {

        unit.xVelocity *= (1-slowPercentage)
        unit.yVelocity *= (1-slowPercentage)

        unit.size *= (1-shrinkPercentage)
    }

    fun postDelayAction(unit: EUnit) {
        unit.xVelocity /= (1-slowPercentage)
        unit.yVelocity /= (1-slowPercentage)

        unit.size /= (1-shrinkPercentage)
    }

}

class DeathAction: UnitAction() {


    override suspend fun runAction(unit: EUnit) {

        unit.xVelocity = 0f
        unit.yVelocity = 0f

        unit.color = Color(0xFF222424) // Dark grey

        delay(2000)

        for (i in 1..20) {
            unit.size *= .75f
            delay(100)
        }
        unit.isAlive = false
    }

}
