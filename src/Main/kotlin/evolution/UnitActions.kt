package evolution

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
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

        unit.speed *= (1-slowPercentage)


        unit.size *= (1-shrinkPercentage)
    }

    private fun postDelayAction(unit: EUnit) {
        unit.speed /= (1-slowPercentage)

        unit.size /= (1-shrinkPercentage)
    }

}

class DeathAction: LiveUnitAction {


    override suspend fun runAction(unit: LiveUnit) {

        unit.speed = 0f

        unit.color = Colors.deathColor // Dark grey
        unit.isActive = false

        delay(1500)

        repeat(10) {
            unit.size *= .80f
            delay(100)
        }
        unit.isAlive = false
    }

}

class EatAction(private val foodUnit: FoodUnit): LiveUnitAction {
    override suspend fun runAction(unit: LiveUnit) {
        unit.energy = min(100f, unit.energy + foodUnit.energy)
        foodUnit.color = Color.LightGray
        foodUnit.speed = 0f
        foodUnit.isActive = false
        delay(1500)
        foodUnit.isAlive = false
    }
}

class MutateAnimationAction: LiveUnitAction {

    override suspend fun runAction(unit: LiveUnit) {

        unit.isActive = false
        val originalSpeed = unit.speed
        val originalEnergy = unit.energy

        unit.speed = 0f

        repeat(6) {
            unit.color = Color.Magenta
            delay(100)
            unit.color = Color.Blue
            delay(100)
            unit.color = Color.Gray
            delay(100)
            unit.color = Color.Cyan
            delay(100)
        }
        unit.speed = originalSpeed
        unit.energy = originalEnergy
        unit.color = Colors.defaultColor
        unit.isActive = true
    }
}
