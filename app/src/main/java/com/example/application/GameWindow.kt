package com.example.application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.application.ui.theme.ApplicationTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

// Data class for a human die that can be kept.
data class Dice(val value: Int, val kept: Boolean = false)

// Generates a list of 5 new dice for the human player.
fun rollPlayerDice(): List<Dice> = List(5) { Dice((1..6).random()) }

// Re-rolls only the dice that are not kept.
fun reRollDice(diceList: List<Dice>): List<Dice> =
    diceList.map { if (!it.kept) Dice((1..6).random()) else it }

// Returns the sum of the current face values of the human dice (for initial throw only).
fun totalPlayerDice(diceList: List<Dice>): Int = diceList.sumOf { it.value }

// Generates a list of 5 random dice values for the computer.
fun rollDice(): List<Int> = List(5) { (1..6).random() }

// Returns the sum of the computer dice.
fun totalDice(diceList: List<Int>): Int = diceList.sum()

// Returns the drawable resource for the given dice number.
fun fetchDiceImage(diceNumber: Int): Int {
    return when(diceNumber) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        else -> R.drawable.dice_6
    }
}

/**
 * Simulates the computer's turn.
 *
 * Starting from the initial cpuDice, the computer has up to 3 total rolls.
 * It uses a random decision to determine whether to take an optional re-roll.
 * For each re-roll opportunity, for each die the computer randomly decides
 * whether to re-roll that die. For each die re-rolled, its new value is added to the
 * cumulative cpu turn score.
 *
 * @param initialCpuDice The dice from the computer's initial throw.
 * @return Pair(finalCpuDice, cpuTurnScore) where:
 *         - finalCpuDice: the final dice values after re-rolls.
 *         - cpuTurnScore: the cumulative score for the computer’s turn,
 *           computed as the sum of the initial throw plus the sum of new values for any re-rolled dice.
 */
fun simulateCpuTurn(initialCpuDice: List<Int>): Pair<List<Int>, Int> {
    var dice = initialCpuDice
    var rollCount = 1  // Already did initial roll.
    var cumulativeScore = dice.sum()
    // Computer has up to 2 optional re-rolls (i.e. total of 3 rolls maximum).
    while (rollCount < 3) {
        // Random decision: true means computer opts for a re-roll at this opportunity.
        if (!Random.nextBoolean()) {
            break  // Computer decides not to re-roll further.
        }
        // For each die, decide randomly whether to re-roll.
        var added = 0
        val updatedDice = dice.map { oldValue ->
            // Random decision per die: if true, re-roll this die.
            if (Random.nextBoolean()) {
                val newValue = (1..6).random()
                added += newValue
                newValue
            } else {
                oldValue
            }
        }
        cumulativeScore += added
        dice = updatedDice
        rollCount++
    }
    return Pair(dice, cumulativeScore)
}

class GameWindow : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApplicationTheme {
                GameView()
            }
        }
    }
}

@Composable
fun GameView() {
    // Human player's dice (selectable).
    var playerDice by remember { mutableStateOf(rollPlayerDice()) }
    // CPU dice: rolled on human’s initial throw.
    var cpuDice by remember { mutableStateOf(rollDice()) }
    var playerTotal by remember { mutableStateOf(0) }
    var cpuTotal by remember { mutableStateOf(0) }
    var currentWinner by remember { mutableStateOf<String?>(null) }
    // Human turn: number of rolls taken (1 = initial, maximum 3).
    var humanRollCount by remember { mutableStateOf(0) }
    // Cumulative human turn score: initial sum + additional re-roll values.
    var humanTurnScore by remember { mutableStateOf(0) }

    // Target value states.
    var targetText by remember { mutableStateOf("") }
    var finalTarget by remember { mutableStateOf<Int?>(null) }

    // Automatic scoring if human takes all three rolls.
    LaunchedEffect(humanRollCount) {
        if (humanRollCount == 3 && currentWinner == null && finalTarget != null) {
            // Finalize human turn.
            playerTotal += humanTurnScore
            // Now simulate computer turn.
            val (finalCpuDice, cpuTurnScore) = simulateCpuTurn(cpuDice)
            cpuDice = finalCpuDice
            cpuTotal += cpuTurnScore
            if (playerTotal >= finalTarget!!) {
                currentWinner = "Player Wins!"
            } else if (cpuTotal >= finalTarget!!) {
                currentWinner = "CPU Wins!"
            }
            delay(2000)
            // Reset for next turn.
            playerDice = rollPlayerDice()
            cpuDice = rollDice() // New CPU dice for next turn.
            humanRollCount = 0
            humanTurnScore = 0
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Target setup.
            if (finalTarget == null) {
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Enter Target Score (default 101)") },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Button(
                    onClick = { finalTarget = targetText.toIntOrNull() ?: 101 },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Set Target", fontSize = 22.sp)
                }
            } else {
                Text("Target Score: $finalTarget", fontSize = 24.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            // Display human player's dice (selectable).
            Text("Player's Dice", fontSize = 24.sp, modifier = Modifier.padding(bottom = 8.dp))
            PlayerDiceRow(diceList = playerDice, onDiceClick = { index ->
                playerDice = playerDice.mapIndexed { i, dice ->
                    if (i == index) dice.copy(kept = !dice.kept) else dice
                }
            })

            Spacer(modifier = Modifier.height(32.dp))

            // Display CPU's dice (non-selectable).
            Text("CPU's Dice", fontSize = 24.sp, modifier = Modifier.padding(8.dp))
            DiceRow(diceList = cpuDice)

            // Action buttons arranged in a horizontal row.
            Row(
                modifier = Modifier.padding(top = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (humanRollCount) {
                    0 -> {
                        // Initial throw button.
                        Button(onClick = {
                            if (currentWinner == null && finalTarget != null) {
                                playerDice = rollPlayerDice()
                                cpuDice = rollDice()  // CPU dice are rolled only once.
                                humanRollCount = 1
                                humanTurnScore = totalPlayerDice(playerDice)
                            }
                        }) {
                            Text("Throw", fontSize = 22.sp)
                        }
                    }
                    in 1..2 -> {
                        // Re-roll button for human.
                        Button(onClick = {
                            if (currentWinner == null && finalTarget != null && humanRollCount < 3) {
                                // Re-roll only the non-kept dice.
                                val newDice = reRollDice(playerDice)
                                // For each die that was re-rolled (i.e. not kept), add its new value to the cumulative turn score.
                                val addedSum = newDice.zip(playerDice) { new, old ->
                                    if (!old.kept) new.value else 0
                                }.sum()
                                humanTurnScore += addedSum
                                playerDice = newDice
                                humanRollCount += 1
                            }
                        }) {
                            Text("Re-roll(${3 - humanRollCount})", fontSize = 22.sp)
                        }
                        // Score button for human turn.
                        Button(onClick = {
                            if (currentWinner == null && finalTarget != null) {
                                // Finalize human turn.
                                playerTotal += humanTurnScore
                                // Simulate computer turn.
                                val (finalCpuDice, cpuTurnScore) = simulateCpuTurn(cpuDice)
                                cpuDice = finalCpuDice
                                cpuTotal += cpuTurnScore
                                if (playerTotal >= finalTarget!!) {
                                    currentWinner = "Player Wins!"
                                } else if (cpuTotal >= finalTarget!!) {
                                    currentWinner = "CPU Wins!"
                                }
                                humanRollCount = 0
                                humanTurnScore = 0
                            }
                        }) {
                            Text("Score", fontSize = 22.sp)
                        }
                    }
                }
            }

            // Display winner announcement (inline, if any).
            if (currentWinner != null) {
                val announcementColor = if (currentWinner!!.contains("Player")) Color.Green else Color.Red
                Text(
                    currentWinner!!,
                    fontSize = 26.sp,
                    color = announcementColor,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
        // Top-right scoreboard overlay.
        Column(
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text("Player Score: $playerTotal", fontSize = 20.sp)
            Text("CPU Score: $cpuTotal", fontSize = 20.sp)
        }
    }

    // Winner pop-up AlertDialog.
    if (currentWinner != null) {
        val winMessage = if (currentWinner!!.contains("Player")) "You win!" else "You lose"
        val winColor = if (currentWinner!!.contains("Player")) Color.Green else Color.Red
        AlertDialog(
            onDismissRequest = { /* Disable dismiss on outside touch */ },
            title = { Text("Game Over", fontSize = 24.sp) },
            text = { Text(winMessage, fontSize = 20.sp, color = winColor) },
            confirmButton = {

            }
        )
    }

    // Reset entire game state after a winner is declared.
    LaunchedEffect(currentWinner) {
        if (currentWinner != null) {
            delay(2000)
            playerDice = rollPlayerDice()
            cpuDice = rollDice()
            playerTotal = 0
            cpuTotal = 0
            currentWinner = null
        }
    }
}

@Composable
fun PlayerDiceRow(diceList: List<Dice>, onDiceClick: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.Center) {
        diceList.forEachIndexed { index, dice ->
            Image(
                painter = painterResource(id = fetchDiceImage(dice.value)),
                contentDescription = "Dice ${dice.value}",
                modifier = Modifier
                    .size(60.dp)
                    .padding(4.dp)
                    .clickable { onDiceClick(index) }
                    .then(
                        if (dice.kept) Modifier.border(BorderStroke(2.dp, Color.Blue))
                        else Modifier
                    )
            )
        }
    }
}

@Composable
fun DiceRow(diceList: List<Int>) {
    Row(horizontalArrangement = Arrangement.Center) {
        diceList.forEach { diceValue ->
            Image(
                painter = painterResource(id = fetchDiceImage(diceValue)),
                contentDescription = "Dice $diceValue",
                modifier = Modifier.size(60.dp).padding(4.dp)
            )
        }
    }
}
