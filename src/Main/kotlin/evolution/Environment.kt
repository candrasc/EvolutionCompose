package evolution

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import explosion.randomInRange
import explosion.round
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

// Notes

/*
Notes

Break interactions in 4 quadrants to reduce n2 issues
- Have all the unit actions get dispatched by the environment rather than dispatched by each unit individually

Adding attacking.
- Units can have a strength score.
- If a unit is stronger than another it tries to kill it and turns red while chasing
- If a unit is weaker than another, it tries to run away and turns green
- All units are blue by default
* */

class Environment {

    var startingLiveUnits = 50
    val startFoodUnits = 1
    val foodValue = 30f
    var foodPerSecond = 4

    val energyReproductionThreshold = 80f
    val mutationProba = 0.10f

    var liveUnits = mutableSetOf<LiveUnit>()
    var foodUnits = mutableSetOf<FoodUnit>()

    var deadUnits = mutableSetOf<LiveUnit>()
    var eatenFoodUnits = mutableSetOf<FoodUnit>()


    private var isActive = false
    private var coroutineScope = CoroutineScope(Dispatchers.Default)

    var onUpdate by mutableStateOf(0)

    var numLiveUnits: Int = 0
        get() = liveUnits.size

    var averageSight: Double = 0.0
        get() = liveUnits.sumOf { it.sight.toDouble()/numLiveUnits }.round()

    var averageEnergyEfficiency: Double = 0.0
        get() = liveUnits.sumOf { it.energyEfficiency.toDouble()/numLiveUnits }.round()

    fun step() {

        liveUnits.forEach {
            if (!it.isAlive) {
                deadUnits.add(it)
            }
            it.step()

        }

        foodUnits.forEach {
            if (!it.isAlive) eatenFoodUnits.add(it)
            it.step()
        }

        liveUnits.removeAll(deadUnits)
        foodUnits.removeAll(eatenFoodUnits)

        val newUnits = mutableSetOf<LiveUnit>()
        liveUnits.forEach { liveUnit ->

            if (!liveUnit.isActive) {
                return@forEach // this is equivalent to "continue"
            }

            if (liveUnit.energy>=this.energyReproductionThreshold) {
                val newUnit = liveUnit.reproduce(mutationProba = mutationProba)
                newUnits.add(newUnit)
            }

            foodUnits.forEach FoodLoop@ { food ->

                if (!food.isActive) (return@FoodLoop)

                if (checkUnitCollision(food, liveUnit)) {
                    liveUnit.eat(food)
                }

                liveUnit.checkFollow(food)
            }

        }

        liveUnits.addAll(newUnits)

    }

    fun start() {
        if(isActive) return

        coroutineScope.launch {

            this@Environment.isActive = true
            while(this@Environment.isActive) {
                delay(1000L/60) // 60 frames a second
                step()
                onUpdate = (0..1000).random()

            }
        }

        coroutineScope.launch {

            this@Environment.isActive = true
            while(this@Environment.isActive) {
                delay(1000/2)
                spawnfood(max(foodPerSecond/2, 1))

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
        spawnfood()
    }

    fun spawnLiveUnit() {
        val unit = LiveUnit(
            color = Color(listOf(0xffea4335, 0xff4285f4, 0xfffbbc05, 0xff34a853).random()),
            xPos = randomInRange(5f, 95f), // keep the edges from clipping through side
            yPos = randomInRange(5f, 95f),
            speed = randomInRange(0.5f, 1f),
            xDirection = randomInRange(-1f, 1f),
            yDirection = randomInRange(-1f, 1f),
            size = 2f,
            energy = energyReproductionThreshold - 1,
            energyEfficiency = randomInRange(0.1f, 0.9f),
            sight = randomInRange(0.1f, 5f)
        )
        liveUnits.add(unit)
    }

    fun spawnLiveUnits(numUnits: Int = startingLiveUnits) {
        repeat(numUnits) {
            spawnLiveUnit()
        }
    }

    fun spawnFood() {
        val food = FoodUnit(
            color = Color.Magenta,
            xPos = randomInRange(5f, 95f), // keep the edges from clipping through side
            yPos = randomInRange(5f, 95f),
            speed = randomInRange(0.2f, 0.5f),
            xDirection = randomInRange(-1f, 1f),
            yDirection = randomInRange(-1f, 1f),
            size = 2f,
            energy = foodValue,
        )
        foodUnits.add(food)

    }

    fun spawnfood(numUnits: Int = startFoodUnits) {
        repeat(numUnits) {
            spawnFood()
        }
    }

    fun checkUnitCollision(unit: EUnit, unit1: EUnit): Boolean {

        val distance = sqrt(((unit.xPos - unit1.xPos).pow(2) + ((unit.yPos - unit1.yPos).pow(2))).toDouble())
        return distance <= (unit1.size / 2 + unit.size /2) && (distance >= abs(unit1.size/2 - unit.size/2))

    }


}