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

    private fun preDelayAction(unit: EUnit) {

        unit.xVelocity *= (1-slowPercentage)
        unit.yVelocity *= (1-slowPercentage)

        unit.size *= (1-shrinkPercentage)
    }

    private fun postDelayAction(unit: EUnit) {
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

        delay(1500)

//        for (i in 1..10) {
//            unit.size *= .90f
//            delay(300)
//        }
        unit.isAlive = false
    }

}
