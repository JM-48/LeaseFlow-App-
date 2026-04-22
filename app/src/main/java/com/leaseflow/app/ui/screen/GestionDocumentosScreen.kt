package com.leaseflow.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.leaseflow.app.ui.components.AppTopBar

@Composable
fun GestionDocumentosScreen(navController: NavHostController) {
    Scaffold(
        topBar = { AppTopBar(title = "Gestión de documentos") },
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = "Revisión/validación de documentos (pendiente).")
            Button(onClick = { navController.popBackStack() }) {
                Text(text = "Volver")
            }
        }
    }
}
