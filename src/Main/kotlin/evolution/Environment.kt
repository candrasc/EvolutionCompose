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

    var startingLiveUnits = 30
    val startFoodUnits = 1
    var foodValue = 60
    var foodPerSecond = 4

    val energyLossPerStep = 0.7f
    val energyReproductionThreshold = 80f
    val mutationProba = 0.2f

    var liveUnits = mutableSetOf<LiveUnit>()
    var foodUnits = mutableSetOf<FoodUnit>()

    var deadUnits = mutableSetOf<LiveUnit>()
    var eatenFoodUnits = mutableSetOf<FoodUnit>()


    private var isActive = false
    private var coroutineScope = CoroutineScope(Dispatchers.Main)

    var onUpdate by mutableStateOf(0)

    var numLiveUnits: Int = 0
        get() = liveUnits.size

    var averageSight: Double = 0.0
        get() = liveUnits.sumOf { it.sight.value.toDouble()/numLiveUnits }.round()

    var averageEnergyEfficiency: Double = 0.0
        get() = liveUnits.sumOf { it.energyEfficiency.value.toDouble()/numLiveUnits }.round()

    var averageStrength: Double = 0.0
        get() = liveUnits.sumOf { it.strength.value.toDouble()/numLiveUnits }.round()


    fun stepQuadrant(quadLiveUnits: List<LiveUnit>, quadFoodUnits: List<FoodUnit>) {

        quadLiveUnits.forEach {
            if (!it.isAlive) {
                deadUnits.add(it)
            }
            it.step()

        }

        quadFoodUnits.forEach {
            if (!it.isAlive) {
                eatenFoodUnits.add(it)
            }
            it.step()
        }

        val newUnits = mutableSetOf<LiveUnit>()
        quadLiveUnits.forEach { liveUnit ->

            if (!liveUnit.isActive) {
                return@forEach // this is equivalent to "continue"
            }

            if (liveUnit.energy>=this.energyReproductionThreshold) {
                val newUnit = liveUnit.reproduce(mutationProba = mutationProba)
                newUnits.add(newUnit)
            }

            quadFoodUnits.forEach FoodLoop@ { food ->

                if (!food.isActive) (return@FoodLoop)

                if (checkUnitCollision(food, liveUnit)) {
                    liveUnit.eat(food)
                }

                liveUnit.checkFollow(food)

            }

            quadLiveUnits.forEach LiveLoop@ { subLiveUnit ->
                if (!subLiveUnit.isActive || subLiveUnit==liveUnit) (return@LiveLoop)

                /*
                If liveUnit is stronger than sub unit, eat it if possible and if not try to follow.
                If weaker, the sub unit will instead check if it needs to flee
                 */
                if (liveUnit.strength.value>subLiveUnit.strength.value) {
                    if (checkUnitCollision(subLiveUnit, liveUnit)) {
                        liveUnit.eat(subLiveUnit)
                    }
                    else {
                        liveUnit.checkFollow(subLiveUnit)
                        subLiveUnit.checkFlee(liveUnit)
                    }
                }
            }

        }

        liveUnits.addAll(newUnits)

    }

    fun step() {

        val liveQuadrants = liveUnits.groupBy { it.quadrant }
        val foodQuadrants = foodUnits.groupBy { it.quadrant }

        for (quadrant in 0..4) {
            //coroutineScope.launch{
                var liveQuad = liveQuadrants[quadrant]
                var foodQuad = foodQuadrants[quadrant]

                if (liveQuad==null) {
                    liveQuad = emptyList()
                }
                if (foodQuad==null) {
                    foodQuad = emptyList()
                }

                stepQuadrant(liveQuad, foodQuad)

            //}
        }

        liveUnits.removeAll(deadUnits)
        foodUnits.removeAll(eatenFoodUnits)

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
                delay(1000)
                spawnfood(max(foodPerSecond, 1))

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

            xPos = randomInRange(5f, 95f), // keep the edges from clipping through side
            yPos = randomInRange(5f, 95f),
            speed = 1f,
            xDirection = randomInRange(-1f, 1f),
            yDirection = randomInRange(-1f, 1f),
            size = 2f,
            energy = energyReproductionThreshold - 1,
            energyLossPerStep = energyLossPerStep,
            energyEfficiency = GeneticAttribute(randomInRange(50f, 100f), 0.00999f),
            sight = GeneticAttribute(randomInRange(1f, 100f), 0.09f),
            strength = GeneticAttribute(randomInRange(1f, 100f), 1f)
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
            xPos = randomInRange(5f, 95f), // keep the edges from clipping through side
            yPos = randomInRange(5f, 95f),
            speed = randomInRange(0.2f, 0.5f),
            xDirection = randomInRange(-1f, 1f),
            yDirection = randomInRange(-1f, 1f),
            size = 2f,
            energy = foodValue.toFloat(),
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