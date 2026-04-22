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
fun HomeScreen(navController: NavHostController) {
    Scaffold(
        topBar = { AppTopBar(title = "Inicio") },
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Button(onClick = { navController.navigate(Routes.CatalogoPropiedades) }) {
                Text(text = "Ver catálogo de propiedades")
            }
            Button(onClick = { navController.navigate(Routes.MisPropiedades) }) {
                Text(text = "Mis propiedades")
            }
            Button(onClick = { navController.navigate(Routes.Solicitudes) }) {
                Text(text = "Solicitudes")
            }
            Button(onClick = { navController.navigate(Routes.MisDocumentos) }) {
                Text(text = "Mis documentos")
            }
            Button(onClick = { navController.navigate(Routes.PerfilUsuario) }) {
                Text(text = "Perfil")
            }
            Button(onClick = { navController.navigate(Routes.Contact) }) {
                Text(text = "Contacto")
            }
            Button(onClick = { navController.navigate(Routes.AdminPanel) }) {
                Text(text = "Panel Admin")
            }
        }
    }
}
