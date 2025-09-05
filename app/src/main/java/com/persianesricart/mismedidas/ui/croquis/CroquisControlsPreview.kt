package com.persianesricart.mismedidas.ui.croquis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CroquisControlsMock() {
    var selectedColor by remember { mutableStateOf(Color.Black) }
    var strokeWidth by remember { mutableStateOf(6f) }
    var eraserOn by remember { mutableStateOf(false) }

    // Paleta de colores típica
    val palette = listOf(
        Color.Black, Color.DarkGray, Color.Gray,
        Color(0xFFEF5350), // rojo
        Color(0xFFAB47BC), // morado
        Color(0xFF42A5F5), // azul
        Color(0xFF26A69A), // verde
        Color(0xFFFFCA28), // ámbar
        Color(0xFFFF8A65)  // naranja
    )

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { /* back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                title = { Text("Croquis") },
                actions = {
                    IconButton(onClick = { /* limpiar */ }) {
                        Icon(Icons.Default.Delete, contentDescription = "Limpiar")
                    }
                    IconButton(onClick = { /* guardar */ }) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            // Barra de herramientas
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {

                // Paleta de colores
                //Text("Color del trazo", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    palette.forEach { color ->
                        val selected = color == selectedColor && !eraserOn
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    shape = CircleShape
                                )
                                .clickable {
                                    eraserOn = false
                                    selectedColor = color
                                }
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    // Borrador
                    AssistChip(
                        onClick = { eraserOn = !eraserOn },
                        label = { Text(if (eraserOn) "Borrador (ON)" else "Borrador") },
                        leadingIcon = {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (eraserOn)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                Spacer(Modifier.height(1.dp))

                // Grosor
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Grosor", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(12.dp))
                    Slider(
                        value = strokeWidth,
                        onValueChange = { strokeWidth = it },
                        valueRange = 1f..30f,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("${strokeWidth.toInt()} px")
                }

                Spacer(Modifier.height(1.dp))

                // Deshacer / Rehacer
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { /* deshacer último trazo */ }) {
                        Text("Deshacer")
                    }
                    OutlinedButton(onClick = { /* rehacer */ }) {
                        Text("Rehacer")
                    }
                }
            }

            // Lienzo (mock visual)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, shape = MaterialTheme.shapes.medium)
            ) {
                Text(
                    "Lienzo (preview)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CroquisControlsMockPreview() {
    MaterialTheme {
        CroquisControlsMock()
    }
}