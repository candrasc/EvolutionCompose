package evolution
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import evolution.DisplayComponents.MetricCard
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

    Row() {


        Column(
            modifier = Modifier.weight(2f)
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color(0xFF86ACEB)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                elevation = 10.dp
            ) {
                Column(
                    modifier = Modifier.padding(15.dp)
                ) {

                    var numStartingUnits by remember {
                        mutableStateOf(environment.startingLiveUnits)
                    }

                    var foodPerSecond by remember {
                        mutableStateOf(environment.foodPerSecond)
                    }

                    Slider(
                        value = numStartingUnits.toFloat(),
                        onValueChange = { sliderValue_ ->
                            numStartingUnits = sliderValue_.toInt()
                        },
                        onValueChangeFinished = {
                            // this is called when the user completed selecting the value
                            environment.startingLiveUnits = numStartingUnits
                        },
                        valueRange = 0f..1000f,
                        steps = 1000,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF4552B8),
                            activeTrackColor = Color(0xFF9FB8E0),
                            inactiveTrackColor = Color(0xFF9FB8E0),
                        )
                    )

                    Text(
                        buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.W900, color = Color(0xFF4552B8))
                            ) {
                                append("Num starting units: ")
                            }
                            withStyle(style = SpanStyle(fontWeight = FontWeight.W900)) {
                                append(numStartingUnits.toString())
                            }
                        }
                    )

                    Slider(
                        value = foodPerSecond.toFloat(),
                        onValueChange = { sliderValue_ ->
                            foodPerSecond = sliderValue_.toInt()
                        },
                        onValueChangeFinished = {
                            // this is called when the user completed selecting the value
                            environment.foodPerSecond = foodPerSecond
                        },
                        valueRange = 1f..100f,
                        steps = 50,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF4552B8),
                            activeTrackColor = Color(0xFF9FB8E0),
                            inactiveTrackColor = Color(0xFF9FB8E0),
                        )
                    )

                    Text(
                        buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.W900, color = Color(0xFF4552B8))
                            ) {
                                append("Food Per Second: ")
                            }
                            withStyle(style = SpanStyle(fontWeight = FontWeight.W900)) {
                                append(foodPerSecond.toString())
                            }
                        }
                    )
                }
            }


        }

        Column(
            modifier = Modifier.weight(5f),
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
                        .size(sizeDp)
                ) {

                    environment.liveUnits.forEach { unit ->
                        drawCircle(
                            alpha = 1.0f,
                            color = unit.color,
                            radius = (unit.size / 100 * canvasHeightPx) / 2,
                            center = Offset(unit.xPos / 100 * canvasHeightPx, unit.yPos / 100 * canvasHeightPx),
                        )
                        if (unit.isActive) {
                            drawCircle(
                                alpha = 0.1f,
                                color = unit.color,
                                radius = ((unit.sight) / 100 * canvasHeightPx),
                                center = Offset(unit.xPos / 100 * canvasHeightPx, unit.yPos / 100 * canvasHeightPx),
                            )
                        }


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

        Column(
            modifier = Modifier.weight(2f)
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color(0xFF86ACEB)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            MetricCard("Number of units", environment.numLiveUnits.toString())
            MetricCard("Average Sight", environment.averageSight.toString())
            MetricCard("Average Efficiency", environment.averageEnergyEfficiency.toString())
        }

    }

}
