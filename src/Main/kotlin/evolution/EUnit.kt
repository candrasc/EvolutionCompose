package evolution

import androidx.compose.ui.graphics.Color


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

    private fun reverseDirection() {

        if (xPos >= maxXPos || xPos<=minXPos) {
            xVelocity *= -1
        }

        if (yPos >= maxYPos || yPos<=minYPos) {
            yVelocity *= -1
        }
    }
    fun step() {
        xPos += xVelocity
        yPos += yVelocity
        reverseDirection()
    }

}
