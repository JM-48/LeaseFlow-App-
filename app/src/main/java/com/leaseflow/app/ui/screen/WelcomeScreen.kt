package com.leaseflow.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.leaseflow.app.navigation.Routes

@Composable
fun WelcomeScreen(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "LeaseFlow", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Bienvenido/a", style = MaterialTheme.typography.titleMedium)

        Button(onClick = { navController.navigate(Routes.Login) }) {
            Text(text = "Iniciar sesión")
        }
        Button(onClick = { navController.navigate(Routes.Register) }) {
            Text(text = "Crear cuenta")
        }
        Button(onClick = { navController.navigate(Routes.Home) }) {
            Text(text = "Continuar")
        }
    }
}
