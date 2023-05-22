import androidx.compose.material.MaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
@Preview
fun App() {
    var t = Texty("heloooo")
    var (text, textSetter) = remember { mutableStateOf(t) }

    MaterialTheme {
        Button(onClick = {
            val new: Texty = text.copy()
            new.value = "Hello, I am new"
            textSetter(new)

        })
        {
            Text(text.value)
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

data class Texty(var value: String) {

}