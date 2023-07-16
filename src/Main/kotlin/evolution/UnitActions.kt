package evolution

import androidx.compose.ui.graphics.Color
import explosion.randomInRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min


interface CommonUnitAction {
    suspend fun runAction(unit: EUnit)
}
interface LiveUnitAction {

    // provide defaults for methods, so they don't have to be implemented
    suspend fun runAction(unit: LiveUnit)

}

interface FoodUnitAction {

    // provide defaults for methods, so they don't have to be implemented
    suspend fun runAction(unit: FoodUnit)

}

class BounceAction(val durationMilli: Long = 40L,
                   val slowPercentage: Float = .9f,
                   val shrinkPercentage: Float = .12f): CommonUnitAction {


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

class DeathAction: CommonUnitAction {


    override suspend fun runAction(unit: EUnit) {

        unit.xVelocity = 0f
        unit.yVelocity = 0f

        unit.color = Color(0xFF222424) // Dark grey
        unit.isActive = false

        delay(1500)

        repeat(6) {
            unit.size *= .70f
            delay(100)
        }
        unit.isAlive = false
    }

}

class EatAction(private val foodUnit: FoodUnit): LiveUnitAction {
    override suspend fun runAction(unit: LiveUnit) {
        unit.energy = min(100f, unit.energy + foodUnit.energy)
        foodUnit.color = Color.LightGray
        foodUnit.xVelocity = 0f
        foodUnit.yVelocity = 0f
        foodUnit.isActive = false
        delay(1500)
        foodUnit.isAlive = false
    }
}
