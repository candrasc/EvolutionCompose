package evolution
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import explosion.randomInRange
import explosion.toPx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.math.max

@Composable
fun runGame(sizeDisplayDP: Dp = 500.dp) {

    val environment = remember { Environment() }
    environment.reset()

    Display(environment, sizeDisplayDP)
}
@Composable
fun Display(environment: Environment, sizeDp: Dp) {

    val coroutineScope = CoroutineScope(Dispatchers.Main)

    // Get local density from composable
    val localDensity = LocalDensity.current

    // Create element height in pixel state
    var canvasHeightPx by remember {
        mutableStateOf(0f)
    }

    // Create element height in dp state
    var canvasHeightDp by remember {
        mutableStateOf(0.dp)
    }


    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = environment.onUpdate.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            color = Color.Black
        )

        Spacer(Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Canvas(
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        // Set column height using the LayoutCoordinates
                        canvasHeightPx = coordinates.size.height.toFloat()
                        canvasHeightDp = with(localDensity) { coordinates.size.height.toDp() }
                    }
                    .border(width = 1.dp, color = Color(0x26000000))
                    .size(sizeDp - 30.dp)
            ) {
                drawLine(
                    color = Color.Black,
                    start = Offset(canvasHeightPx / 2, 0f),
                    end = Offset(canvasHeightPx / 2, canvasHeightPx),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = Color.Black,
                    start = Offset(0f, canvasHeightPx / 2),
                    end = Offset(canvasHeightPx, canvasHeightPx / 2),
                    strokeWidth = 2.dp.toPx()
                )

                environment.liveUnits.forEach { unit ->
                    drawCircle(
                        alpha = 1.0f,
                        color = unit.color,
                        radius = (unit.size / 100 * canvasHeightPx) / 2,
                        center = Offset(unit.xPos / 100 * canvasHeightPx, unit.yPos / 100 * canvasHeightPx),
                    )
                }

                environment.foodUnits.forEach { unit ->
                    drawCircle(
                        alpha = 1.0f,
                        color = unit.color,
                        radius = (unit.size / 100 * canvasHeightPx) / 2,
                        center = Offset(unit.xPos / 100 * canvasHeightPx, unit.yPos / 100 * canvasHeightPx),
                        style = Stroke(3f)
                    )
                }



            }
        }
            Spacer(Modifier.height(16.dp))
            Row(
            ) {
                Button(environment::start) {
                    Text("Start")
                }
                Spacer(Modifier.width(16.dp))
                Button(environment::pause) {
                    Text("Pause")
                }
                Spacer(Modifier.width(16.dp))
                Button(environment::reset) {
                    Text("Reset")
                }

            }

        }

}
