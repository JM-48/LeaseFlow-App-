package com.leaseflow.app.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun AppDrawer(
    navController: NavHostController,
    currentRoute: String?,
    onDestinationSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "LeaseFlow",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        DrawerItem(
            label = "Inicio",
            route = Routes.Home,
            navController = navController,
            currentRoute = currentRoute,
            onDestinationSelected = onDestinationSelected,
        )
        DrawerItem(
            label = "Catálogo de Propiedades",
            route = Routes.CatalogoPropiedades,
            navController = navController,
            currentRoute = currentRoute,
            onDestinationSelected = onDestinationSelected,
        )
        DrawerItem(
            label = "Solicitudes",
            route = Routes.Solicitudes,
            navController = navController,
            currentRoute = currentRoute,
            onDestinationSelected = onDestinationSelected,
        )
        DrawerItem(
            label = "Mis Documentos",
            route = Routes.MisDocumentos,
            navController = navController,
            currentRoute = currentRoute,
            onDestinationSelected = onDestinationSelected,
        )
        DrawerItem(
            label = "Perfil",
            route = Routes.PerfilUsuario,
            navController = navController,
            currentRoute = currentRoute,
            onDestinationSelected = onDestinationSelected,
        )
        DrawerItem(
            label = "Contacto",
            route = Routes.Contact,
            navController = navController,
            currentRoute = currentRoute,
            onDestinationSelected = onDestinationSelected,
        )
        DrawerItem(
            label = "Admin",
            route = Routes.AdminPanel,
            navController = navController,
            currentRoute = currentRoute,
            onDestinationSelected = onDestinationSelected,
        )
    }
}

@Composable
private fun DrawerItem(
    label: String,
    route: String,
    navController: NavHostController,
    currentRoute: String?,
    onDestinationSelected: () -> Unit,
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = label,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        selected = currentRoute == route,
        onClick = {
            onDestinationSelected()
            navController.navigate(route) {
                launchSingleTop = true
            }
        },
    )
}
