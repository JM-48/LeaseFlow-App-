package com.leaseflow.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.leaseflow.app.data.local.storage.UserPreferences
import kotlinx.coroutines.launch

@Composable
fun AppDrawer(
    navController: NavController,
    onCloseDrawer: () -> Unit,
    context: android.content.Context
) {
    val userPreferences = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val userId by userPreferences.userId.collectAsState(initial = null)
    val userRole by userPreferences.userRole.collectAsState(initial = null)
    val userName by userPreferences.userName.collectAsState(initial = null)
    val userEmail by userPreferences.userEmail.collectAsState(initial = null)

    val esArrendatario = userRole?.uppercase() == "ARRENDATARIO"
    val esPropietario = userRole?.uppercase() == "PROPIETARIO"
    val esAdmin = userRole?.uppercase() == "ADMINISTRADOR"

    ModalDrawerSheet(
        drawerContainerColor = Color.Transparent
    ) {
        val drawerGradient = Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.82f)
            )
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .background(drawerGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                Text(
                    text = "LeaseFlow",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.White.copy(alpha = 0.16f)
                )

                if (userId != null) {
                    Text(
                        text = userName ?: "Usuario",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = userEmail ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.78f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.White.copy(alpha = 0.16f)
                )

                DrawerItem(
                    icon = Icons.Default.Home,
                    label = "Inicio",
                    onClick = {
                        navController.navigate(Routes.HOME)
                        onCloseDrawer()
                    }
                )

                if (userId != null) {
                    DrawerItem(
                        icon = Icons.Default.Person,
                        label = "Mi Perfil",
                        onClick = {
                            navController.navigate(Routes.PERFIL)
                            onCloseDrawer()
                        }
                    )
                } else {
                    DrawerItem(
                        icon = Icons.Default.Login,
                        label = "Iniciar Sesion",
                        onClick = {
                            navController.navigate(Routes.LOGIN)
                            onCloseDrawer()
                        }
                    )
                    DrawerItem(
                        icon = Icons.Default.PersonAdd,
                        label = "Registrarse",
                        onClick = {
                            navController.navigate(Routes.REGISTER)
                            onCloseDrawer()
                        }
                    )
                }

                DrawerItem(
                    icon = Icons.Default.Search,
                    label = "Buscar Propiedades",
                    onClick = {
                        navController.navigate(Routes.CATALOGO_PROPIEDADES)
                        onCloseDrawer()
                    }
                )

                // ==========================================
                // SECCIÓN ARRENDATARIO
                // ==========================================
                if (esArrendatario || esAdmin) {

                    // Si es admin, le ponemos un título para que sepa qué sección es esta
                    if (esAdmin) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.White.copy(alpha = 0.16f)
                        )
                        Text(
                            text = "Arrendatario",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.88f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    DrawerItem(
                        icon = Icons.Default.Assignment,
                        label = "Mis Solicitudes",
                        onClick = {
                            // Esto dispara el MODO PERSONAL (Arrendatario)
                            navController.navigate(Routes.SOLICITUDES)
                            onCloseDrawer()
                        }
                    )

                    DrawerItem(
                        icon = Icons.Default.Key,
                        label = "Mis Arriendos",
                        onClick = {
                            navController.navigate(Routes.MIS_ARRIENDOS)
                            onCloseDrawer()
                        }
                    )

                    DrawerItem(
                        icon = Icons.Default.Description,
                        label = "Mis Documentos",
                        onClick = {
                            navController.navigate(Routes.MIS_DOCUMENTOS)
                            onCloseDrawer()
                        }
                    )
                }

                // ==========================================
                // SECCIÓN PROPIETARIO
                // ==========================================
                if (esPropietario || esAdmin) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.White.copy(alpha = 0.16f)
                    )

                    Text(
                        text = "Propietario",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.88f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    DrawerItem(
                        icon = Icons.Default.HomeWork,
                        label = "Mis Propiedades",
                        onClick = {
                            navController.navigate(Routes.MIS_PROPIEDADES)
                            onCloseDrawer()
                        }
                    )

                    DrawerItem(
                        icon = Icons.Default.Add,
                        label = "Agregar Propiedad",
                        onClick = {
                            navController.navigate(Routes.AGREGAR_PROPIEDAD)
                            onCloseDrawer()
                        }
                    )

                    DrawerItem(
                        icon = Icons.Default.AssignmentReturned, // Cambié el icono para diferenciarlo
                        label = "Solicitudes Recibidas",
                        onClick = {
                            // Esto dispara el MODO RECIBIDAS (Propietario)
                            navController.navigate(Routes.SOLICITUDES_RECIBIDAS)
                            onCloseDrawer()
                        }
                    )

                    DrawerItem(
                        icon = Icons.Default.Description,
                        label = "Mis Documentos",
                        onClick = {
                            navController.navigate(Routes.MIS_DOCUMENTOS)
                            onCloseDrawer()
                        }
                    )
                }

                // ==========================================
                // SECCIÓN ADMINISTRADOR
                // ==========================================
                if (esAdmin) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.White.copy(alpha = 0.16f)
                    )

                    Text(
                        text = "Administracion",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.88f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    DrawerItem(
                        icon = Icons.Default.Dashboard,
                        label = "Panel Admin",
                        onClick = {
                            navController.navigate(Routes.ADMIN_PANEL)
                            onCloseDrawer()
                        }
                    )

                    // ---> ¡AQUÍ ESTÁ EL NUEVO GESTOR GLOBAL DE SOLICITUDES! <---
                    DrawerItem(
                        icon = Icons.Default.List,
                        label = "Gestor Solicitudes Global",
                        onClick = {
                            // Esto dispara el MODO ADMIN_GLOBAL
                            navController.navigate(Routes.SOLICITUDES_ADMIN)
                            onCloseDrawer()
                        }
                    )

                    DrawerItem(
                        icon = Icons.Default.People,
                        label = "Gestion Usuarios",
                        onClick = {
                            navController.navigate(Routes.GESTION_USUARIOS)
                            onCloseDrawer()
                        }
                    )

                    DrawerItem(
                        icon = Icons.Default.Business,
                        label = "Gestion Propiedades",
                        onClick = {
                            navController.navigate(Routes.GESTION_PROPIEDADES)
                            onCloseDrawer()
                        }
                    )

                    DrawerItem(
                        icon = Icons.Default.Description,
                        label = "Gestion Documentos",
                        onClick = {
                            navController.navigate(Routes.GESTION_DOCUMENTOS)
                            onCloseDrawer()
                        }
                    )

                    DrawerItem(
                        icon = Icons.Default.ContactMail,
                        label = "Gestion Contacto",
                        onClick = {
                            navController.navigate(Routes.GESTION_CONTACTO)
                            onCloseDrawer()
                        }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.White.copy(alpha = 0.16f)
                )

                DrawerItem(
                    icon = Icons.Default.ContactMail,
                    label = "Contacto",
                    onClick = {
                        navController.navigate(Routes.CONTACT)
                        onCloseDrawer()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.White.copy(alpha = 0.16f)
                )

                if (userId != null) {
                    DrawerItem(
                        icon = Icons.Default.ExitToApp,
                        label = "Cerrar Sesion",
                        onClick = {
                            scope.launch {
                                userPreferences.clearUserSession()
                            }
                            navController.navigate(Routes.WELCOME) {
                                popUpTo(0) { inclusive = true }
                            }
                            onCloseDrawer()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label) },
        selected = false,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = Color.White.copy(alpha = 0.14f),
            unselectedContainerColor = Color.Transparent,
            selectedIconColor = Color.White,
            unselectedIconColor = Color.White.copy(alpha = 0.90f),
            selectedTextColor = Color.White,
            unselectedTextColor = Color.White.copy(alpha = 0.92f)
        ),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}