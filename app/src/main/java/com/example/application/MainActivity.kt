package com.example.application

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.application.ui.theme.ApplicationTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ApplicationTheme {
             MainScreen( onNavigateToGameWindow = {
                 startActivity(Intent(this, GameWindow::class.java))
             })
            }
        }
    }
}

@Composable
fun MainScreen(onNavigateToGameWindow:()->Unit) {
    var showDialog by remember { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Background image
            Image(
                painter = painterResource(id = R.drawable.lucky_toss),
                contentDescription = "Background Image",
                modifier = Modifier
                    .fillMaxSize(),

                contentScale = ContentScale.Crop // This will cover the entire area, cropping the excess
            )
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .align(Alignment.Center),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Create a FontFamily with your custom font
                val customFontFamily = FontFamily(Font(R.font.font2))
                val backgroundColor = Color(0xFF10BF1E)

                //New game button
                Button(
                    onClick = { onNavigateToGameWindow()/* Handle new game action */ },
                    modifier = Modifier
                        .width(450.dp)
                        .padding(top = 600.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = backgroundColor,  // Background color of the button
                        contentColor = Color.Black       // Text and icon color inside the button
                    )

                ) {
                    Text("New Game",
                        modifier = Modifier.padding(8.dp),
                        fontSize = 45.sp,
                        fontFamily = customFontFamily)
                }

                //About Button
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .width(450.dp)
                        .padding(top = 45.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = backgroundColor,
                        contentColor = Color.Black
                    )
                ) {
                    Text("About",
                        modifier = Modifier.padding(5.dp), fontSize = 45.sp, fontFamily = customFontFamily)
                }
            }
            if (showDialog) {
                AboutDialog(showDialog = showDialog, onDismiss = { showDialog = false })
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun PreviewHomeScreen() {
//    ApplicationTheme {
//        HomeScreen()
//    }
//}
@Composable
fun AboutDialog(showDialog: Boolean, onDismiss: () -> Unit) {
    val customFontFamilyGaming = FontFamily(Font(R.font.aboutfont))
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "About",fontFamily = customFontFamilyGaming,fontSize = 22.sp) },
            text = {
                Text("StudentID: w2055319 / 20232437 \nName: Asra Ameer\nI confirm that I understand what plagiarism is and have read and understood the section on Assessment Offences in the Essential Information for Students. The work that I have submitted is entirely my own. Any work from other authors is duly referenced and acknowledged."
                    ,fontFamily = customFontFamilyGaming,fontSize = 15.sp,
                    textAlign = TextAlign.Justify)
            },
            confirmButton = {
                val backgroundColor = Color(0xFF10BF1E)
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor =  backgroundColor
                    ),
                )
                {
                    Text("OK" ,
                        modifier = Modifier.padding(5.dp),
                        fontSize = 18.sp, fontFamily = customFontFamilyGaming)
                }
            },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        )
    }
}

