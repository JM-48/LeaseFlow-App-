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
import com.leaseflow.app.navigation.Routes
import com.leaseflow.app.ui.components.AppTopBar

@Composable
fun AdminPanelScreen(navController: NavHostController) {
    Scaffold(
        topBar = { AppTopBar(title = "Panel Admin") },
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Button(onClick = { navController.navigate(Routes.GestionUsuarios) }) {
                Text(text = "Gestión de usuarios")
            }
            Button(onClick = { navController.navigate(Routes.GestionPropiedades) }) {
                Text(text = "Gestión de propiedades")
            }
            Button(onClick = { navController.navigate(Routes.GestionDocumentos) }) {
                Text(text = "Gestión de documentos")
            }
            Button(onClick = { navController.navigate(Routes.UserManagement) }) {
                Text(text = "User Management")
            }
        }
    }
}
