package evolution

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


abstract class EUnit(
    var xPos: Float,
    var yPos: Float,
    var xVelocity: Float,
    var yVelocity: Float,
    var size: Float,
    var color: Color,
) {
    private var coroutineScope = CoroutineScope(Dispatchers.Default)
    // All units operate in the bounds of 0 to 100. This is converted to pixels when rendered
    val minYPos = 0
    val minXPos = 0
    val maxYPos = 100
    val maxXPos = 100

    val actionQueue: Queue<UnitAction> = LinkedList()
    var isAlive = true

    fun handleWallCollision(): Boolean {
        var isCollision = false

        val unitMaxXPOS: Float = maxXPos - size / 2
        val unitMinXPOS: Float = minXPos + size / 2

        val unitMaxYPOS: Float = maxYPos - size / 2
        val unitMinYPOS: Float = minYPos + size / 2


        if (xPos >= unitMaxXPOS) {
            xPos = unitMaxXPOS
            xVelocity *= -1
            isCollision = true

        } else if (xPos <= unitMinXPOS) {
            xPos = unitMinXPOS
            xVelocity *= -1
            isCollision = true
        }

        if (yPos >= unitMaxYPOS) {
            yPos = unitMaxYPOS
            yVelocity *= -1
            isCollision = true

        } else if (yPos <= unitMinYPOS) {
            yPos = unitMinYPOS
            yVelocity *= -1
            isCollision = true
        }
        return isCollision
    }

    fun executeActions() {
        while (!actionQueue.isEmpty()) {
            val action = actionQueue.remove()
            coroutineScope.launch {
                action.runAction(this@EUnit)
            }
        }
    }

    abstract fun step()

}

class LiveUnit(xPos: Float,
               yPos: Float,
               xVelocity: Float,
               yVelocity: Float,
               size: Float,
               color: Color,
               var energy: Float = 100f,
               var energyDecay: Float = 1f):
    EUnit(xPos,
        yPos,
        xVelocity,
        yVelocity,
        size,
        color) {
    override fun step() {
        xPos += xVelocity
        yPos += yVelocity
        energy -= 1*energyDecay

        if (energy<=0) {
            val deathAction = DeathAction()
            actionQueue.add(deathAction)
        }

        val collision = handleWallCollision()
        if (collision) {
            val bounceAction = BounceAction(40L, .90f, shrinkPercentage = .12f)
            actionQueue.add(bounceAction)
        }

        executeActions()
    }
}


class FoodUnit(xPos: Float,
               yPos: Float,
               xVelocity: Float,
               yVelocity: Float,
               size: Float,
               color: Color): EUnit(xPos,
                                    yPos,
                                    xVelocity,
                                    yVelocity,
                                    size,
                                    color) {

    override fun step() {
        xPos += xVelocity
        yPos += yVelocity

        val collision = handleWallCollision()
        if (collision) {
            val bounceAction = BounceAction(40L, .90f, shrinkPercentage = .12f)
            actionQueue.add(bounceAction)
        }

        executeActions()
    }
}