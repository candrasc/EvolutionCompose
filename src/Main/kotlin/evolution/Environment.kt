package evolution

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import explosion.randomInRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class Environment(val refeshRatePerSecond: Int = 60) {

    val numLiveUnits = 100
    val maxFoodUnits = 10
    val foodValue = 30f
    val foodsPerSecond = 1



    var liveUnits = mutableSetOf<LiveUnit>()
    var foodUnits = mutableSetOf<FoodUnit>()

    var deadUnits = mutableSetOf<LiveUnit>()
    var eatenFoodUnits = mutableSetOf<FoodUnit>()


    private var isActive = false
    private var coroutineScope = CoroutineScope(Dispatchers.Main)

    var onUpdate by mutableStateOf(0)


    fun step() {

        liveUnits.forEach {
            if (!it.isAlive) {
                deadUnits.add(it)
            }
            if (it.isActive) {
                it.step()
            }
        }

        foodUnits.forEach {
            if (!it.isAlive) eatenFoodUnits.add(it)
            if (it.isActive) {
                it.step()
            }
        }

        liveUnits.removeAll(deadUnits)
        foodUnits.removeAll(eatenFoodUnits)

        liveUnits.forEach { liveUnit ->

            if (!liveUnit.isActive) {
                return@forEach // this is equivalent to "continue"
            }

            foodUnits.forEach FoodLoop@ { food ->

                if (!food.isActive) (return@FoodLoop)

                if (checkUnitCollision(food, liveUnit)) {
                    liveUnit.eat(food)
                }
            }
        }


    }

    fun start() {
        if(isActive) return

        coroutineScope.launch {

            this@Environment.isActive = true
            while(this@Environment.isActive) {
                delay(1000L/refeshRatePerSecond)
                step()
                onUpdate = (0..100).random()

            }
        }

        val foodDelay = (1000L/foodsPerSecond).toLong()
        coroutineScope.launch {

            this@Environment.isActive = true
            while(this@Environment.isActive) {
                delay(foodDelay)
                spawnFoods(1)

            }
        }
    }

    fun pause() {
        isActive = false
    }

    fun reset() {
        isActive = false
        liveUnits = mutableSetOf()
        foodUnits = mutableSetOf()
        deadUnits = mutableSetOf()
        eatenFoodUnits = mutableSetOf()
        spawnLiveUnits()
        spawnFoods()
    }

    fun spawnLiveUnit() {
        val unit = LiveUnit(
            color = Color(listOf(0xffea4335, 0xff4285f4, 0xfffbbc05, 0xff34a853).random()),
            xPos = randomInRange(5f, 95f), // keep the edges from clipping through side
            yPos = randomInRange(5f, 95f),
            xVelocity = randomInRange(-1f, 1f),
            yVelocity = randomInRange(-1f, 1f),
            size = randomInRange(1f, 2.5f),
            energy = 100f,
            energyDecay = randomInRange(0.1f, 1f)
        )
        liveUnits.add(unit)
    }

    fun spawnLiveUnits(numUnits: Int = numLiveUnits) {
        repeat(numUnits) {
            spawnLiveUnit()
        }
    }

    fun spawnFood() {
        val food = FoodUnit(
            color = Color.Magenta,
            xPos = randomInRange(5f, 95f), // keep the edges from clipping through side
            yPos = randomInRange(5f, 95f),
            xVelocity = randomInRange(-0.2f, 0.2f),
            yVelocity = randomInRange(-0.2f, 0.2f),
            size = 3f,
            energy = foodValue,
        )
        foodUnits.add(food)

    }

    fun spawnFoods(numUnits: Int = maxFoodUnits) {
        repeat(numUnits) {
            spawnFood()
        }
    }

    fun checkUnitCollision(unit: EUnit, unit1: EUnit): Boolean {

        val distance = sqrt(((unit.xPos - unit1.xPos).pow(2) + ((unit.yPos - unit1.yPos).pow(2))).toDouble())
        return distance <= (unit1.size / 2 + unit.size /2) && (distance >= abs(unit1.size/2 - unit.size/2))

    }


}