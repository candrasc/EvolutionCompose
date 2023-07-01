package explosion
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.*
import androidx.compose.ui.unit.*
import java.util.*

fun Float.mapInRange(inMin: Float, inMax: Float, outMin: Float, outMax: Float): Float {
    return outMin + (((this - inMin) / (inMax - inMin)) * (outMax - outMin))
}


// Can only be used within context of a Composable function
@Composable
fun Dp.toPx() = with(LocalDensity.current) { this@toPx.toPx() }
//https://stackoverflow.com/questions/65921799/how-to-convert-dp-to-pixels-in-android-jetpack-compose

// For use outside Composable scope...
// Long term need to refactor so never need to use outside of Composable scope
fun dpToPixel(dp: Double, dpRatio: Float) = dp * dpRatio
fun dpToPixel(dp: Int, dpRatio: Float) = dp * dpRatio


private val random = Random()
fun Float.randomTillZero() = this * random.nextFloat()
fun randomInRange(min:Float,max:Float) = min + (max - min).randomTillZero()
fun randomBoolean(trueProbabilityPercentage: Int) = random.nextFloat() < trueProbabilityPercentage/100f
