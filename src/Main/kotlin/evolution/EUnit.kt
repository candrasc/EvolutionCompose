package evolution

import androidx.compose.ui.graphics.Color
import explosion.randomInRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt


abstract class EUnit(
    var xPos: Float,
    var yPos: Float,
    open var speed: Float,
    var xDirection: Float,
    var yDirection: Float,
    var size: Float,
) {
    //private var coroutineScope = CoroutineScope(Dispatchers.Default)
    // All units operate in the bounds of 0 to 100. This is converted to pixels when rendered
    val minYPos = 0
    val minXPos = 0
    val maxYPos = 100
    val maxXPos = 100

    val actionQueue: Queue<CommonUnitAction> = LinkedList()
    var isAlive = true // determines if unit exists in env
    var isActive = true // determines if unit can be interacted with

    var coroutineScope = CoroutineScope(Dispatchers.Default)

    var quadrant: Int = 0
        get() {

            return if (xPos<50 && yPos<50) 0 // Top left
            else if (xPos>=50 && yPos<50) 1 // Top right
            else if (xPos<50 && yPos>=50) 2 // Bottom left
            else 3 // Bottom right
        }

    fun handleWallCollision(): Boolean {
        var isCollision = false

        val unitMaxXPOS: Float = maxXPos - size / 2
        val unitMinXPOS: Float = minXPos + size / 2

        val unitMaxYPOS: Float = maxYPos - size / 2
        val unitMinYPOS: Float = minYPos + size / 2


        if (xPos >= unitMaxXPOS) {
            xPos = unitMaxXPOS
            xDirection *= -1
            isCollision = true

        } else if (xPos <= unitMinXPOS) {
            xPos = unitMinXPOS
            xDirection *= -1
            isCollision = true
        }

        if (yPos >= unitMaxYPOS) {
            yPos = unitMaxYPOS
            yDirection *= -1
            isCollision = true

        } else if (yPos <= unitMinYPOS) {
            yPos = unitMinYPOS
            yDirection *= -1
            isCollision = true
        }
        return isCollision
    }

    fun distance(unit: EUnit): Double {

        return sqrt((unit.xPos - this.xPos).toDouble().pow(2.0) + (unit.yPos - this.yPos).toDouble().pow(2.0))

    }
    fun executeCommonActions() {

        coroutineScope.launch {
            while (!actionQueue.isEmpty()) {
                val action = actionQueue.remove()
                action.runAction(this@EUnit)
                }
            }
        }

    abstract fun executeUniqueActions()
    abstract fun step()



}

data class GeneticAttribute(
    val attribute: Float
) {
    init {
        if (attribute<0 || attribute>100) {
            throw IllegalArgumentException("Attribute must be between 0 and 100")
        }
    }
}

fun Float.toGenetic() = GeneticAttribute(this)

class Colors {
    companion object {
        val defaultColor = Color(0xff4285f4) // Blue
        val huntColor = Color(0xffea4335) // Red
        val fleeColor = Color(0xff34a853) // Green
        val reproduceColor = Color(0xfffbbc05) // Yellow
        val deathColor = Color(0xFF222424) // Dark grey
    }

}

class LiveUnit(xPos: Float,
               yPos: Float,
               speed: Float,
               xDirection: Float,
               yDirection: Float,
               size: Float,
               var energy: Float = 100f,
               var energyEfficiency: Float = 1f,
               var sight: Float = 5f):
    EUnit(xPos,
        yPos,
        speed,
        xDirection,
        yDirection,
        size) {

    var color = Colors.defaultColor
        get() {
            if (target!=null) return Colors.huntColor
            return field
        }
    var target: EUnit? = null
    var predator: EUnit? = null
    val geneticAttributes = mutableSetOf<GeneticAttribute>()
    val uniqueActionQueue: Queue<LiveUnitAction> = LinkedList()


    //TODO: All these attributes need setters so that they can be properly bounded
    // for example, y direction + x direction should not exceed 1
    // energy should not exceed 100
    // energy decay must be greater than 0.1

    override fun step() {
        xPos += xDirection * speed
        yPos += yDirection * speed
        energy -= (1-energyEfficiency)

        if (energy<=0) {
            val deathAction = DeathAction()
            uniqueActionQueue.add(deathAction)
        }

        val collision = handleWallCollision()
        if (collision) {
            val bounceAction = BounceAction()
            actionQueue.add(bounceAction)
        }

        target?.let {
            if (it.isActive) follow(it)
            else target = null
        } // need to null check like this

        executeCommonActions()
        executeUniqueActions()
    }

    override fun executeUniqueActions() {
        coroutineScope.launch {
            while (!uniqueActionQueue.isEmpty()) {
                val action = uniqueActionQueue.remove()
                action.runAction(this@LiveUnit)
            }
        }
    }

    fun eat(food: FoodUnit) {
        val eatAction = EatAction(food)
        uniqueActionQueue.add(eatAction)
    }

    fun mutate(mutationProba: Float) {

        // each trait out of 100
        // then multiply them by some static value
        // rand each out of 100 and softmax it

    }
    fun copy(): LiveUnit {

        return LiveUnit(
            xPos = xPos,
            yPos = yPos,
            speed = speed,
            xDirection = randomInRange(-1f, 1f),
            yDirection = randomInRange(-1f, 1f),
            size = size,
            energy = energy,
            energyEfficiency = energyEfficiency
        )
    }

    fun reproduce(mutationProba: Float = 0.10f): LiveUnit {
        val newUnit = this@LiveUnit.copy()
        newUnit.mutate(mutationProba)

        val newEnergy = energy/2
        newUnit.energy = newEnergy
        this@LiveUnit.energy = newEnergy

        return newUnit

    }

    fun checkFollow(unit: EUnit) {

        val dist = abs(this.distance(unit)) - (this.size + unit.size)/2
        if (dist <= this.sight) {
            this.target = unit
        }
    }

    private fun follow(unit: EUnit) {
        val xDiff = unit.xPos - this.xPos
        val yDiff = unit.yPos - this.yPos

        this.xDirection = xDiff/(abs(xDiff) + abs(yDiff))
        this.yDirection = yDiff/(abs(xDiff) + abs(yDiff))
    }


}



class FoodUnit(xPos: Float,
               yPos: Float,
               speed: Float,
               xDirection: Float,
               yDirection: Float,
               size: Float,
               var energy: Float): EUnit(xPos,
                                    yPos,
                                    speed,
                                    xDirection,
                                    yDirection,
                                    size) {

    var color = Color.Magenta
    val uniqueActionQueue: Queue<FoodUnitAction> = LinkedList()
    override fun step() {
        xPos += xDirection * speed
        yPos += yDirection * speed

        val collision = handleWallCollision()
        if (collision) {
            val bounceAction = BounceAction()
            actionQueue.add(bounceAction)
        }

        executeCommonActions()
        executeUniqueActions()
    }

    override fun executeUniqueActions() {
        coroutineScope.launch {
            while (!uniqueActionQueue.isEmpty()) {
                val action = uniqueActionQueue.remove()
                action.runAction(this@FoodUnit)
            }
        }
    }
}