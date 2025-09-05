package com.persianesricart.mismedidas.ui.croquis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Undo
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
fun CroquisTopBarMock() {
    var expanded by remember { mutableStateOf(false) }
    var eraserOn by remember { mutableStateOf(false) }
    var strokeColor by remember { mutableStateOf(Color.Black) }
    var strokeWidth by remember { mutableStateOf(6f) }

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
                    // Menú de color/grosor/eraser
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.Palette, contentDescription = "Color y grosor")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Herramientas") },
                            leadingIcon = { Icon(Icons.Default.Brush, contentDescription = null) },
                            onClick = { /* no-op; cabecera */ },
                            enabled = false
                        )
                        // Paleta de colores
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text("Color", style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                palette.forEach { c ->
                                    val selected = c == strokeColor && !eraserOn
                                    Box(
                                        modifier = Modifier
                                            .size(if (selected) 28.dp else 24.dp)
                                            .clip(CircleShape)
                                            .background(c)
                                            .then(
                                                if (selected) Modifier.border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = CircleShape
                                                ) else Modifier
                                            )
                                            .clickable {
                                                eraserOn = false
                                                strokeColor = c
                                            }
                                    )
                                }
                            }
                        }
                        // Grosor
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text("Grosor", style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Slider(
                                    value = strokeWidth,
                                    onValueChange = { strokeWidth = it },
                                    valueRange = 1f..30f,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("${strokeWidth.toInt()} px")
                            }
                        }
                        // Borrador
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(checked = eraserOn, onCheckedChange = { eraserOn = it })
                                Spacer(Modifier.width(8.dp))
                                Text(if (eraserOn) "Borrador (ON)" else "Borrador")
                            }
                        }
                    }

                    IconButton(onClick = { /* deshacer */ }) {
                        Icon(Icons.Default.Undo, contentDescription = "Deshacer")
                    }
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
        // Lienzo placeholder para ver espacios
        Box(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Preview del lienzo",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CroquisTopBarMockPreview() {
    MaterialTheme {
        CroquisTopBarMock()
    }
}