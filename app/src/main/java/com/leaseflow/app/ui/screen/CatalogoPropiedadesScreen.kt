package com.leaseflow.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.leaseflow.app.navigation.Routes
import com.leaseflow.app.ui.components.AppTopBar

@Composable
fun CatalogoPropiedadesScreen(navController: NavHostController) {
    val propiedades = listOf(
        "Departamento céntrico",
        "Casa con patio",
        "Estudio amueblado",
    )

    Scaffold(
        topBar = { AppTopBar(title = "Catálogo") },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(propiedades) { nombre ->
                Card(colors = CardDefaults.cardColors()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = nombre)
                        Button(onClick = { navController.navigate(Routes.PropiedadDetalle) }) {
                            Text(text = "Ver detalle")
                        }
                    }
                }
            }
        }
    }
}
