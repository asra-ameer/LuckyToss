package com.example.application

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.application.ui.theme.ApplicationTheme
import kotlinx.coroutines.delay

class GameScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApplicationTheme {
                GameScreenContent()
            }
        }
    }
}

@Composable
fun GameScreenContent() {
    var humanDice by remember { mutableStateOf(RandomDiceNo()) }
    var computerDice by remember { mutableStateOf(RandomDiceNo()) }

    var humanScore by remember { mutableStateOf(0) }
    var computerScore by remember { mutableStateOf(0) }
    var winner by remember { mutableStateOf<String?>(null) }

    // New state for target value setup
    var targetInput by remember { mutableStateOf("") }
    var targetValue by remember { mutableStateOf<Int?>(null) }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Main content centered in the screen
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // If target is not set, show input to set it
            if (targetValue == null) {
                OutlinedTextField(
                    value = targetInput,
                    onValueChange = { targetInput = it },
                    label = { Text("Enter target value") },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Button(
                    onClick = {
                        targetValue = targetInput.toIntOrNull() ?: 0
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Set Target", fontSize = 22.sp)
                }
            } else {
                Text("Target: $targetValue", fontSize = 24.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            Text("Human Player", fontSize = 24.sp, modifier = Modifier.padding(bottom = 8.dp))
            LineOfDice(diceValues = humanDice)

            Spacer(modifier = Modifier.height(32.dp))

            Text("Computer Rolls", fontSize = 24.sp, modifier = Modifier.padding(8.dp))
            LineOfDice(diceValues = computerDice)

            Button(
                onClick = {
                    // Allow dice throw only if the game is ongoing and target is set
                    if (winner == null && targetValue != null) {
                        humanDice = RandomDiceNo()
                        computerDice = RandomDiceNo()
                    }
                },
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Text("Throw", fontSize = 22.sp)
            }

            Button(
                onClick = {
                    if (winner == null && targetValue != null) {
                        // Accumulate human's score first
                        humanScore += calculateScore(humanDice)
                        if (humanScore >= targetValue!!) {
                            winner = "Human Wins!"
                        } else {
                            // Then update computer's score
                            computerScore += calculateScore(computerDice)
                            if (computerScore >= targetValue!!) {
                                winner = "Computer Wins!"
                            }
                        }
                    }
                },
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Text("Score", fontSize = 22.sp)
            }

            // Winner announcement when applicable
            if (winner != null) {
                Text(" $winner", fontSize = 26.sp, color = Color.Green, modifier = Modifier.padding(top = 16.dp))
            }
        }

        // Score display in the top-right corner using a Box overlay
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text("Human Score: $humanScore", fontSize = 20.sp)
            Text("Computer Score: $computerScore", fontSize = 20.sp)
        }
    }


    // Auto reset the game when a winner is declared, after a 2-second delay.
    LaunchedEffect(winner) {
        if (winner != null) {
            delay(2000)
            humanDice = RandomDiceNo()
            computerDice = RandomDiceNo()
            humanScore = 0
            computerScore = 0
            winner = null
        }
    }
}

@Composable
fun LineOfDice(diceValues: List<Int>) {
    Row(horizontalArrangement = Arrangement.Center) {
        diceValues.forEach { diceNumber ->
            Image(
                painter = painterResource(id = getDiceImage(diceNumber)),
                contentDescription = "Dice $diceNumber",
                modifier = Modifier.size(60.dp).padding(4.dp)
            )
        }
    }
}

// Random dice roll generator
fun RandomDiceNo(): List<Int> {
    return List(5) { (1..6).random() }
}

// Map number to drawable dice images
fun getDiceImage(diceNumber: Int): Int {
    return when (diceNumber) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        else -> R.drawable.dice_6
    }
}

fun calculateScore(dice: List<Int>): Int {
    return dice.sum()
}
