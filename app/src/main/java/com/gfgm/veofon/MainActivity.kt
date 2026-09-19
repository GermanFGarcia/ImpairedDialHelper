package com.gfgm.veofon

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.ui.platform.LocalView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Forcem el fons clar per a tota l'aplicació
            MaterialTheme(
                colorScheme = lightColorScheme(
                    background = Color.White,
                    surface = Color(0xFFF0F0F0)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    PantallaMarcadorGran()
                }
            }
        }
    }
}

// Funció per iniciar l'acció de trucada obrint el marcador del telèfon
fun ferTrucada(context: Context, numero: String) {
    if (numero.isNotEmpty()) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${Uri.encode(numero)}")
        }
        context.startActivity(intent)
    }
}

@Composable
fun PantallaMarcadorGran() {
    var numeroMarcat by remember { mutableStateOf("") }
    val context = LocalContext.current
    val view = LocalView.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // -------------------------------------------------------------
        // 1. PANTALLA ON ES VEU EL NÚMERO MARCAT
        // -------------------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp)
                .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (numeroMarcat.isEmpty()) "Marca un número" else numeroMarcat,
                fontSize = if (numeroMarcat.length > 10) 36.sp else 44.sp, // S'ajusta si és molt llarg
                fontWeight = FontWeight.Bold,
                color = if (numeroMarcat.isEmpty()) Color.Gray else Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }

        // -------------------------------------------------------------
        // 2. TECLAT NUMÈRIC GEGANT (GRID 3x4)
        // -------------------------------------------------------------
        val tecles = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("*", "0", "#")
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (fila in tecles) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (dgit in fila) {
                        BotoTeclat(
                            text = dgit,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (numeroMarcat.length < 15) {
                                    numeroMarcat += dgit
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // -------------------------------------------------------------
        // 3. BOTONS D'ACCIÓ (ESBORRAR I TRUCAR)
        // -------------------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Botó Esborrar (Gris/Taronja)
            Button(
                onClick = {
                    reproduirSoIVibracio(view) // <-- So i vibració
                    if (numeroMarcat.isNotEmpty()) {
                        numeroMarcat = numeroMarcat.dropLast(1)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(80.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0))
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Esborrar",
                    tint = Color.Black,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Botó Trucar (Verd Gran)
            Button(
                onClick = {
                    reproduirSoIVibracio(view) // <-- So i vibració
                    ferTrucada(context, numeroMarcat)
                          },
                modifier = Modifier
                    .weight(2f)
                    .height(80.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)) // Verd d'alt contrast
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Trucar",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// Component reutilitzable per a cada botó del teclat numèric
@Composable
fun BotoTeclat(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Button(
        onClick = {
            reproduirSoIVibracio(view) // <-- Executa el so i la vibració
            onClick()                  // <-- Executa la lògica d'afegir el número
        },
        modifier = modifier.height(75.dp), // Mida vertical molt alta
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE3F2FD) // Blau molt clar per destacar
        )
    ) {
        Text(
            text = text,
            fontSize = 38.sp, // Font molt gran
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0D47A1) // Blau fosc d'alt contrast
        )
    }
}

// Funció que reprodueix el so de clic del sistema i genera la vibració estàndard
fun reproduirSoIVibracio(view: android.view.View) {
    // 1. So natiu de clic/tecla de l'mòbil
    view.playSoundEffect(SoundEffectConstants.CLICK)

    // 2. Vibració o resposta hàptica
    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
}

// Vista prèvia per a Android Studio amb fons blanc
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewMarcador() {
    MaterialTheme {
        PantallaMarcadorGran()
    }
}