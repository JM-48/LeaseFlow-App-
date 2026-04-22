package com.leaseflow.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.leaseflow.app.ui.components.AppTopBar

@Composable
fun AgregarPropiedadScreen(navController: NavHostController) {
    val nombre = remember { mutableStateOf("") }
    val direccion = remember { mutableStateOf("") }

    Scaffold(
        topBar = { AppTopBar(title = "Agregar propiedad") },
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = nombre.value,
                onValueChange = { nombre.value = it },
                label = { Text(text = "Nombre") },
                singleLine = true,
            )
            OutlinedTextField(
                value = direccion.value,
                onValueChange = { direccion.value = it },
                label = { Text(text = "Dirección") },
                singleLine = true,
            )
            Button(onClick = { navController.popBackStack() }) {
                Text(text = "Guardar")
            }
        }
    }
}
