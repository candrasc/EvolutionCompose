package evolution

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class EUnit(
    var xPos: Float,
    var yPos: Float,
    var xVelocity: Float,
    var yVelocity: Float,
    val size: Float,
    val color: Color,
    var health: Float = 100f,
    var energy: Float = 100f
) {
    // All units operate in the bounds of 0 to 100. This is converted to pixels when rendered
    val minYPos = 0
    val minXPos = 0
    val maxYPos = 100
    val maxXPos = 100

    private fun checkWallCollision() {
        val unitMaxXPOS: Float = maxXPos - size/2
        val unitMinXPOS: Float = minXPos + size/2

        val unitMaxYPOS: Float = maxYPos - size/2
        val unitMinYPOS: Float = minYPos + size/2

        val slowEffect = SlowEffect(1000L, .8f)

        if (xPos >= unitMaxXPOS )  {
            xPos = unitMaxXPOS
            xVelocity *= -1

            slowEffect.runEffect(this)


        } else if (xPos <= unitMinXPOS) {
            xPos = unitMinXPOS
            xVelocity *= -1

            slowEffect.runEffect(this)
        }

        if (yPos >= unitMaxYPOS )  {
            yPos = unitMaxYPOS
            yVelocity *= -1

            slowEffect.runEffect(this)

        } else if (yPos <= unitMinYPOS) {
            yPos = unitMinYPOS
            yVelocity *= -1

            slowEffect.runEffect(this)
        }
    }
    fun step() {
        xPos += xVelocity
        yPos += yVelocity

        checkWallCollision()
    }

}
