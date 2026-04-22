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
fun PerfilUsuarioScreen(navController: NavHostController) {
    Scaffold(
        topBar = { AppTopBar(title = "Perfil") },
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "Nombre: Usuario")
            Text(text = "Correo: usuario@leaseflow.com")
            Button(onClick = { navController.navigate(Routes.MisDocumentos) }) {
                Text(text = "Ver mis documentos")
            }
            Button(onClick = { navController.navigate(Routes.Login) }) {
                Text(text = "Cerrar sesión")
            }
        }
    }
}
