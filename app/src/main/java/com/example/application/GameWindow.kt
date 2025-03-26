package com.example.application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.application.ui.theme.ApplicationTheme
import kotlin.random.Random

// Data class for a human die that can be kept.
data class Dice(val value: Int, val kept: Boolean = false)

// Generates a list of 5 new dice for the human player.
fun rollPlayerDice(): List<Dice> = List(5) { Dice((1..6).random()) }

// Re-rolls only the dice that are not kept.
fun reRollDice(diceList: List<Dice>): List<Dice> =
    diceList.map { if (!it.kept) Dice((1..6).random()) else it }

// Returns the sum of the current face values of the human dice.
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
 * Simulates the computer's turn using a simple random re-roll strategy.
 *
 * After the initial roll, the computer may take up to 2 optional re-rolls.
 * In this implementation, the decision to re-roll each die is random.
 *
 * @param initialCpuDice The dice from the computer's initial throw.
 * @return Pair(finalCpuDice, cpuTurnScore) where:
 *         - finalCpuDice: the final dice values after re-rolls.
 *         - cpuTurnScore: the cumulative score for the computer’s turn,
 *           computed as the sum of the initial throw plus any additional re-rolled values.
 */
fun simulateCpuTurn(initialCpuDice: List<Int>): Pair<List<Int>, Int> {
    var dice = initialCpuDice
    var rollCount = 1  // initial roll already done.
    var cumulativeScore = dice.sum()
    while (rollCount < 3) {
        if (!Random.nextBoolean()) {
            break
        }
        var added = 0
        val updatedDice = dice.map { oldValue ->
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
    // Use rememberSaveable to preserve state across configuration changes.
    var playerDice by rememberSaveable { mutableStateOf(rollPlayerDice()) }
    var cpuDice by rememberSaveable { mutableStateOf(rollDice()) }
    var playerTotal by rememberSaveable { mutableStateOf(0) }
    var cpuTotal by rememberSaveable { mutableStateOf(0) }
    var isHumanWinner by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var humanRollCount by rememberSaveable { mutableStateOf(0) }
    var humanTurnScore by rememberSaveable { mutableStateOf(0) }
    var targetText by rememberSaveable { mutableStateOf("") }
    var finalTarget by rememberSaveable { mutableStateOf<Int?>(null) }
    val customFontFamily = FontFamily(Font(R.font.font4))
    val diceWordFontFamily= FontFamily(Font(R.font.aboutfont))
    val backgroundColor = Color(0xFF10BF1E)
    var showWarning by rememberSaveable { mutableStateOf(false) }


    // Automatic scoring if human takes all three rolls.
    LaunchedEffect(humanRollCount) {
        if (humanRollCount == 3 && isHumanWinner == null && finalTarget != null) {
            // Finalize human turn.
            playerTotal += humanTurnScore
            // Simulate computer turn.
            val (finalCpuDice, cpuTurnScore) = simulateCpuTurn(cpuDice)
            cpuDice = finalCpuDice
            cpuTotal += cpuTurnScore
            // Check tie-breaker condition.
            if (playerTotal >= finalTarget!! && cpuTotal >= finalTarget!! && playerTotal == cpuTotal) {
                var tieBreakerWinner: Boolean? = null
                do {
                    val tiePlayerDice = rollPlayerDice()
                    val tieCpuDice = rollDice()
                    val tiePlayerScore = totalPlayerDice(tiePlayerDice)
                    val tieCpuScore = totalDice(tieCpuDice)
                    if (tiePlayerScore > tieCpuScore) {
                        tieBreakerWinner = true
                    } else if (tieCpuScore > tiePlayerScore) {
                        tieBreakerWinner = false
                    }
                } while (tieBreakerWinner == null)
                isHumanWinner = tieBreakerWinner
            } else {
                if (playerTotal >= finalTarget!!) {
                    isHumanWinner = true
                } else if (cpuTotal >= finalTarget!!) {
                    isHumanWinner = false
                }
            }
            // If no winner, reset turn state for next attempt.
            if (isHumanWinner == null) {
                playerDice = rollPlayerDice()
                cpuDice = rollDice()
                humanRollCount = 0
                humanTurnScore = 0
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        // Background image.
        Image(
            painter = painterResource(id = R.drawable.lucky_toss_bg),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Main content.
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
                    label = { Text("Enter Target Score (default value: 101)") },

                    modifier = Modifier.width(500.dp).padding(top=0.dp).padding(bottom = 8.dp).padding(8.dp)
                )

                Button(
                    onClick = {
                        if (targetText.isEmpty()) {
                            finalTarget = 101
                        } else {
                            val input = targetText.toIntOrNull()
                            if (input == null || input <= 0) {
                                showWarning = true
                            } else {
                                finalTarget = input
                            }
                        }
                    },
                    modifier = Modifier.padding(bottom = 16.dp).padding(top=6.dp).padding(20.dp).height(70.dp).width(300.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor =  backgroundColor
                    )
                )
                {
                    Text("Set Target", fontSize = 32.sp,fontFamily = customFontFamily)
                }
            }
            // (The target score is now only displayed in the top-right overlay.)

            // Display human player's dice (selectable).
            Text("Player's Dice", fontSize = 24.sp, fontFamily = diceWordFontFamily,fontWeight = FontWeight.W900, modifier = Modifier.padding(bottom = 8.dp))
            PlayerDiceRow(diceList = playerDice, onDiceClick = { index ->
                playerDice = playerDice.mapIndexed { i, dice ->
                    if (i == index) dice.copy(kept = !dice.kept) else dice
                }
            })

            Spacer(modifier = Modifier.height(32.dp))

            // Display CPU's dice (non-selectable).
            Text("CPU's Dice", fontSize = 24.sp, fontFamily = diceWordFontFamily, fontWeight = FontWeight.W900, modifier = Modifier.padding(8.dp))
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
                            if (isHumanWinner == null && finalTarget != null) {
                                playerDice = rollPlayerDice()
                                cpuDice = rollDice()  // CPU dice are rolled only once per attempt.
                                humanRollCount = 1
                                humanTurnScore = totalPlayerDice(playerDice)
                            }
                        }, modifier = Modifier.height(70.dp).width(150.dp),colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = backgroundColor
                        )) {
                            Text("Throw", fontSize = 30.sp,fontFamily = customFontFamily)
                        }
                    }

                    in 1..2 -> {
                        // Re-roll button for human.
                        Button(onClick = {
                            if (isHumanWinner == null && finalTarget != null && humanRollCount < 3) {
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
                        }, modifier = Modifier .height(70.dp).width(190.dp),colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor =  backgroundColor
                        )) {
                            Text("Re-roll(${3 - humanRollCount})", fontSize = 30.sp,fontFamily = customFontFamily)
                        }
                        // Score button for human turn.
                        Button(onClick = {
                            if (isHumanWinner == null && finalTarget != null) {
                                // Finalize human turn.
                                playerTotal += humanTurnScore
                                // Simulate computer turn.
                                val (finalCpuDice, cpuTurnScore) = simulateCpuTurn(cpuDice)
                                cpuDice = finalCpuDice
                                cpuTotal += cpuTurnScore
                                if (playerTotal >= finalTarget!!) {
                                    isHumanWinner = true
                                } else if (cpuTotal >= finalTarget!!) {
                                    isHumanWinner = false
                                }
                                humanRollCount = 0
                                humanTurnScore = 0
                            }
                        } , modifier = Modifier.height(70.dp).width(190.dp),colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = backgroundColor
                        )) {
                            Text("Score", fontSize = 30.sp,fontFamily = customFontFamily)
                        }
                    }
                }
            }
        }
        // Top-left scoreboard overlay for player and CPU scores.
        Row(
            modifier = Modifier.align(Alignment.TopStart).padding(10.dp)
        ) {
            Text("H: $playerTotal", fontSize =25.sp,fontWeight = FontWeight.W900)
            Text(" /C: $cpuTotal", fontSize = 25.sp ,fontWeight = FontWeight.W900)
        }
        // Top-right target overlay.
        if (finalTarget != null) {
            Text(
                "Target: $finalTarget",
                fontSize = 25.sp, fontWeight = FontWeight.W900,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            )
        }
    }

}

    // Warning pop-up AlertDialog for invalid target input.
    if (showWarning) {
        AlertDialog(
            onDismissRequest = { showWarning = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
            title = { Text("Warning", fontSize = 24.sp, color = Color.Red) },
            text = { Text("Enter a valid number", fontSize = 20.sp, color = Color.Red) },
            confirmButton = {
                Button(onClick = { showWarning = false },colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor =  backgroundColor
                ),) {
                    Text("OK", fontSize = 18.sp,)
                }
            }
        )
    }

    // Winner pop-up AlertDialog.
    if (isHumanWinner != null) {
        val winMessage = if (isHumanWinner == true) "You win!" else "You lose"
        val winColor = if (isHumanWinner == true) Color.Green else Color.Red
        val activity = LocalActivity.current
        AlertDialog(
            onDismissRequest = { activity?.finish() },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            ),
            title = { Text("Game Over", fontSize = 26.sp, fontWeight = FontWeight.W500) },
            text = { Text(text = winMessage, fontSize = 34.sp, color = winColor,fontWeight = FontWeight.W800) },
            confirmButton = { }
        )
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
                    .size(70.dp)
                    .padding(4.dp)
                    .clickable { onDiceClick(index) }
                    .then(
                        if (dice.kept) Modifier.border(BorderStroke(6.dp, Color.Blue))
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
                modifier = Modifier.size(70.dp).padding(4.dp)
            )
        }
    }
}
