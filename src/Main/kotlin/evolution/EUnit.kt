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
    var target: EUnit? = null
    var targeter: EUnit? = null
    open var color: Color = Colors.defaultColor


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
    var value: Float,
    val scaler: Float
) {

    init {
        if (value<0 || value>100) {
            throw IllegalArgumentException("Attribute must be between 0 and 100")
        }
    }

    val scaledValue get() = value * scaler

}

class Colors {
    companion object {
        val defaultColor = Color(0xff4285f4) // Blue
        val huntColor = Color(0xffea4335) // Red
        val huntFoodColor = Color(0xff34a853) // Green
        val fleeColor = Color(0xfffbbc05) // Yellow
        val deathColor = Color(0xFF222424) // Dark grey
        val newBorn = Color(0xFF959ADE)
    }

}

class LiveUnit(xPos: Float,
               yPos: Float,
               speed: Float,
               xDirection: Float,
               yDirection: Float,
               size: Float,
               var energyLossPerStep: Float,
               var energy: Float = 100f,
               var energyEfficiency: GeneticAttribute,
               var sight: GeneticAttribute,
               var strength: GeneticAttribute):
    EUnit(xPos,
        yPos,
        speed,
        xDirection,
        yDirection,
        size) {

    override var color = Colors.defaultColor
        get() {

            if (targeter!=null) return Colors.fleeColor

            else if (target!=null) {
                if (target!!::class == LiveUnit::class) return Colors.huntColor
                else if (target!!::class == FoodUnit::class) return Colors.huntFoodColor
            }

            return field
        }

    val geneticAttributes = mutableSetOf(energyEfficiency, sight, strength)
    val uniqueActionQueue: Queue<LiveUnitAction> = LinkedList()

    init {
        normalizeGeneticAttributes()
        normalizeDirection()
    }

    override fun step() {
        xPos += xDirection * speed
        yPos += yDirection * speed
        energy -= (1-energyEfficiency.scaledValue) * energyLossPerStep

        if (energy<=0) {
            uniqueActionQueue.add(DeathAction())
        }

        val collision = handleWallCollision()
        if (collision) {
            actionQueue.add(BounceAction())
        }

        if (targeter!=null) {
            targeter?.let {
                if (it.isActive) flee(it)
                else targeter = null
            }
        }
        else if (target!=null) {
            target?.let {
                if (it.isActive) follow(it)
                else target = null
            } // need to null check like this
        }

        executeCommonActions()
        executeUniqueActions()

        target = null
        targeter = null
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
        val eatAction = EatFoodAction(food)
        uniqueActionQueue.add(eatAction)
    }

    fun eat(prey: LiveUnit) {
        this.energy += prey.energy
        prey.energy = 0f
        prey.uniqueActionQueue.add(DeathAction())

    }

    fun mutate() {
    //Mutate a random trait


        val geneticAttribute = geneticAttributes.random()
        geneticAttribute.value = randomInRange(1f, 100f)

        normalizeGeneticAttributes()

        uniqueActionQueue.add(MutateAnimationAction())

    }

    private fun normalizeGeneticAttributes() {
        val totalValue = geneticAttributes.sumOf { it.value.toDouble() }
        geneticAttributes.forEach {
            it.value = (it.value/totalValue * 100).toFloat()
        }

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
            energyLossPerStep = energyLossPerStep,
            energyEfficiency = energyEfficiency.copy(),
            sight = sight.copy(),
            strength = strength.copy()
        )
    }

    fun reproduce(mutationProba: Float = 0.10f): LiveUnit {
        val newUnit = this@LiveUnit.copy()
        if (randomInRange(0f,1f) < mutationProba) {
            newUnit.mutate()
        }

        val newEnergy = energy/2
        newUnit.energy = newEnergy
        this@LiveUnit.energy = newEnergy

        newUnit.actionQueue.add(InactiveAction(2000L))
        return newUnit

    }

    fun checkFollow(unit: EUnit) {

        if (canSee(unit))this.target = unit

    }

    fun checkFlee(unit: EUnit) {

        if (canSee(unit)) targeter = unit

    }

    private fun canSee(unit: EUnit): Boolean {
        val dist = abs(this.distance(unit)) - (this.size + unit.size)/2
        return dist <= this.sight.scaledValue
    }

    private fun normalizeDirection() {
        val xDir = xDirection/(abs(xDirection) + abs(yDirection))
        val yDir = yDirection/(abs(xDirection) + abs(yDirection))
        xDirection = xDir
        yDirection = yDir
    }
    private fun follow(unit: EUnit) {
        xDirection = unit.xPos - this.xPos
        yDirection = unit.yPos - this.yPos

        normalizeDirection()


    }

    private fun flee(unit: EUnit) {
        // Flee is just the opposite of follow
        follow(unit)
        this.xDirection *=-1
        this.yDirection *=-1
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

    override var color = Color.Magenta
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