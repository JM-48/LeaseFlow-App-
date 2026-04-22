package com.leaseflow.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.leaseflow.app.navigation.Routes
import com.leaseflow.app.ui.components.AppTopBar
import com.leaseflow.app.ui.components.SolicitudCard

@Composable
fun SolicitudesScreen(navController: NavHostController) {
    val solicitudes = listOf(
        SolicitudUiModel(id = "SOL-001", propiedad = "Depto 45B", estado = "Pendiente"),
        SolicitudUiModel(id = "SOL-002", propiedad = "Casa 123", estado = "Aprobada"),
    )

    Scaffold(
        topBar = { AppTopBar(title = "Solicitudes") },
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(solicitudes) { item ->
                    SolicitudCard(
                        id = item.id,
                        propiedad = item.propiedad,
                        estado = item.estado,
                        onClick = { navController.navigate(Routes.SolicitudDetalle) },
                    )
                }
            }
        }
    }
}

data class SolicitudUiModel(
    val id: String,
    val propiedad: String,
    val estado: String,
)
