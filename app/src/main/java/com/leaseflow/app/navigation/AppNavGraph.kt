package com.leaseflow.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.leaseflow.app.ui.screen.AdminPanelScreen
import com.leaseflow.app.ui.screen.AgregarPropiedadScreen
import com.leaseflow.app.ui.screen.CatalogoPropiedadesScreen
import com.leaseflow.app.ui.screen.ContactScreen
import com.leaseflow.app.ui.screen.GestionDocumentosScreen
import com.leaseflow.app.ui.screen.GestionPropiedadesScreen
import com.leaseflow.app.ui.screen.GestionUsuariosScreen
import com.leaseflow.app.ui.screen.HomeScreen
import com.leaseflow.app.ui.screen.LoginScreen
import com.leaseflow.app.ui.screen.MisDocumentosScreen
import com.leaseflow.app.ui.screen.MisPropiedadesScreen
import com.leaseflow.app.ui.screen.PerfilUsuarioScreen
import com.leaseflow.app.ui.screen.PropiedadDetalleScreen
import com.leaseflow.app.ui.screen.RegisterScreen
import com.leaseflow.app.ui.screen.SolicitudDetalleScreen
import com.leaseflow.app.ui.screen.SolicitudesScreen
import com.leaseflow.app.ui.screen.UserManagementScreen
import com.leaseflow.app.ui.screen.WelcomeScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.Welcome,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Routes.Welcome) { WelcomeScreen(navController) }
        composable(Routes.Home) { HomeScreen(navController) }
        composable(Routes.Login) { LoginScreen(navController) }
        composable(Routes.Register) { RegisterScreen(navController) }

        composable(Routes.CatalogoPropiedades) { CatalogoPropiedadesScreen(navController) }
        composable(Routes.PropiedadDetalle) { PropiedadDetalleScreen(navController) }

        composable(Routes.MisPropiedades) { MisPropiedadesScreen(navController) }
        composable(Routes.AgregarPropiedad) { AgregarPropiedadScreen(navController) }

        composable(Routes.Solicitudes) { SolicitudesScreen(navController) }
        composable(Routes.SolicitudDetalle) { SolicitudDetalleScreen(navController) }

        composable(Routes.PerfilUsuario) { PerfilUsuarioScreen(navController) }
        composable(Routes.MisDocumentos) { MisDocumentosScreen(navController) }

        composable(Routes.AdminPanel) { AdminPanelScreen(navController) }
        composable(Routes.GestionUsuarios) { GestionUsuariosScreen(navController) }
        composable(Routes.GestionPropiedades) { GestionPropiedadesScreen(navController) }
        composable(Routes.GestionDocumentos) { GestionDocumentosScreen(navController) }
        composable(Routes.UserManagement) { UserManagementScreen(navController) }

        composable(Routes.Contact) { ContactScreen(navController) }
    }
}
