// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import evolution.Display
import androidx.compose.material.MaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import evolution.EUnit
import evolution.Environment
import evolution.runGame
import explosion.randomInRange

@Composable
@Preview
fun App() {

    MaterialTheme {
        //ControlledExplosion()
        runGame(sizeDisplayDP = 650.dp)
    }

}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}