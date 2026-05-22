package com.leaseflow.app.navigation

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.leaseflow.app.data.local.LeaseFlowDatabase
import com.leaseflow.app.data.local.storage.UserPreferences
import com.leaseflow.app.data.remote.RetrofitClient
import com.leaseflow.app.data.remote.dto.UsuarioDTO
import com.leaseflow.app.data.repository.ApplicationRemoteRepository
import com.leaseflow.app.data.repository.ContactRemoteRepository
import com.leaseflow.app.data.repository.DocumentRemoteRepository
import com.leaseflow.app.data.repository.PropertyRemoteRepository
import com.leaseflow.app.data.repository.UserRepository
import com.leaseflow.app.ui.components.AppTopBar
import com.leaseflow.app.ui.screen.*
import com.leaseflow.app.ui.screen.ContactScreen
import com.leaseflow.app.ui.viewmodel.*
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    navController: NavHostController,
    context: Context,
    authViewModel: LeaseFlowAuthViewModel,
    propiedadViewModel: PropiedadViewModel,
    propiedadDetalleViewModel: PropiedadDetalleViewModel,
    solicitudesViewModel: SolicitudesViewModel,
    perfilViewModel: PerfilUsuarioViewModel,
    reviewViewModel: ReviewViewModel
) {
    val userPrefs = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()
    val database = LeaseFlowDatabase.getInstance(context)

    val isLoggedIn by userPrefs.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)
    val userRole by userPrefs.userRole.collectAsStateWithLifecycle(initialValue = null)
    val userId by userPrefs.userId.collectAsStateWithLifecycle(initialValue = null)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Repositorios
    val propertyRepository = remember { PropertyRemoteRepository() }
    val applicationRepository = remember {
        ApplicationRemoteRepository(
            solicitudDao = database.solicitudDao(),
            catalogDao = database.catalogDao()
        )
    }

    // Funciones de navegación existentes
    val goWelcome: () -> Unit = {
        navController.navigate(Routes.WELCOME) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    val goHome: () -> Unit = {
        navController.navigate(Routes.HOME) {
            popUpTo(Routes.HOME) { inclusive = true }
            launchSingleTop = true
        }
    }

    val goLogin: () -> Unit = { navController.navigate(Routes.LOGIN) }
    val goRegister: () -> Unit = { navController.navigate(Routes.REGISTER) }
    val goPropiedades: () -> Unit = { navController.navigate(Routes.CATALOGO_PROPIEDADES) }
    val goPerfil: () -> Unit = { navController.navigate(Routes.PERFIL) }
    val goSolicitudes: () -> Unit = { navController.navigate(Routes.SOLICITUDES) }
    val goMisDocumentos: () -> Unit = { navController.navigate(Routes.MIS_DOCUMENTOS) }

    val goPropiedadDetalle: (Long) -> Unit = { propiedadId ->
        navController.navigate("${Routes.PROPIEDAD_DETALLE}/$propiedadId")
    }

    // CORRECCIÓN: Creamos la función para ir al detalle de la SOLICITUD
    val goSolicitudDetalle: (Long) -> Unit = { solicitudId ->
        navController.navigate("${Routes.SOLICITUD_DETALLE}/$solicitudId")
    }

    val goHomeAfterLogin: () -> Unit = {
        navController.navigate(Routes.HOME) {
            popUpTo(Routes.WELCOME) { inclusive = true }
            launchSingleTop = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                navController = navController,
                onCloseDrawer = { scope.launch { drawerState.close() } },
                context = context
            )
        }
    ) {
        val baseGradient = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.70f)
            ),
            start = Offset(0f, 0f),
            end = Offset(1000f, 1600f)
        )
        val cyanGlow = Brush.radialGradient(
            colors = listOf(
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.20f),
                Color.Transparent
            ),
            center = Offset(120f, 220f),
            radius = 900f
        )
        val purpleGlow = Brush.radialGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                Color.Transparent
            ),
            center = Offset(900f, 1200f),
            radius = 1000f
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(baseGradient)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(cyanGlow))
            Box(modifier = Modifier.fillMaxSize().background(purpleGlow))

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    AppTopBar(
                        isLoggedIn = isLoggedIn,
                        userRole = userRole,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onHome = goHome,
                        onLogin = goLogin,
                        onRegister = goRegister,
                        onPropiedades = goPropiedades,
                        onPerfil = goPerfil,
                        onSolicitudes = goSolicitudes
                    )
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = if (isLoggedIn) Routes.HOME else Routes.WELCOME,
                    modifier = Modifier.padding(innerPadding)
                ) {

                    composable(Routes.WELCOME) {
                        WelcomeScreen(
                            onGoLogin = goLogin,
                            onGoRegister = goRegister
                        )
                    }

                    composable(Routes.HOME) {
                        HomeScreen(
                            onGoPropiedades = goPropiedades,
                            onGoLogin = goLogin,
                            onGoRegister = goRegister
                        )
                    }

                    composable(Routes.LOGIN) {
                        LoginScreenVm(
                            vm = authViewModel,
                            onLoginOkNavigateHome = goHomeAfterLogin,
                            onGoRegister = goRegister
                        )
                    }

                    composable(Routes.REGISTER) {
                        RegisterScreenVm(
                            vm = authViewModel,
                            onRegisteredNavigateHome = goHomeAfterLogin,
                            onGoLogin = goLogin
                        )
                    }

                    // CATALOGO PROPIEDADES
                    composable(Routes.CATALOGO_PROPIEDADES) {
                        val propiedadViewModelFactory = PropiedadViewModelFactory(
                            propiedadDao = database.propiedadDao(),
                            catalogDao = database.catalogDao(),
                            remoteRepository = propertyRepository
                        )

                        CatalogoPropiedadesScreen(
                            viewModelFactory = propiedadViewModelFactory,
                            onVerDetalle = { propiedadId ->
                                goPropiedadDetalle(propiedadId)
                            }
                        )
                    }

                    // PROPIEDAD DETALLE
                    composable(
                        route = "${Routes.PROPIEDAD_DETALLE}/{propiedadId}",
                        arguments = listOf(navArgument("propiedadId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val propiedadId = backStackEntry.arguments?.getLong("propiedadId") ?: 0L

                        val propiedadDetalleViewModelFactory = PropiedadDetalleViewModelFactory(
                            propiedadDao = database.propiedadDao(),
                            catalogDao = database.catalogDao(),
                            propertyRepository = propertyRepository,
                            applicationRepository = applicationRepository
                        )

                        PropiedadDetalleScreen(
                            propiedadId = propiedadId,
                            userPreferences = userPrefs,
                            viewModelFactory = propiedadDetalleViewModelFactory,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToSolicitudes = goSolicitudes
                        )
                    }

                    // PERFIL
                    composable(Routes.PERFIL) {
                        if (!isLoggedIn || userId == null) {
                            LaunchedEffect(Unit) {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(Routes.PERFIL) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        } else {
                            PerfilUsuarioScreen(
                                vm = perfilViewModel,
                                onBack = { navController.popBackStack() },
                                onLogout = goWelcome
                            )
                        }
                    }

                    // 1. SOLICITUDES ENVIADAS (Arrendatario)
                    composable(Routes.SOLICITUDES) {
                        val solicitudesViewModelFactory = SolicitudesViewModelFactory(
                            solicitudDao = database.solicitudDao(),
                            propiedadDao = database.propiedadDao(),
                            catalogDao = database.catalogDao(),
                            remoteRepository = applicationRepository,
                            propertyRepository = propertyRepository
                        )

                        SolicitudesScreen(
                            userPreferences = userPrefs,
                            viewModelFactory = solicitudesViewModelFactory,
                            mode = SolicitudesMode.ARRENDATARIO, // Forzamos el rol inquilino
                            onNavigateToDetalle = goSolicitudDetalle // Redirige al detalle de la solicitud
                        )
                    }

                    // 2. SOLICITUDES RECIBIDAS (Propietario)
                    composable(Routes.SOLICITUDES_RECIBIDAS) {
                        val solicitudesViewModelFactory = SolicitudesViewModelFactory(
                            solicitudDao = database.solicitudDao(),
                            propiedadDao = database.propiedadDao(),
                            catalogDao = database.catalogDao(),
                            remoteRepository = applicationRepository,
                            propertyRepository = propertyRepository
                        )

                        SolicitudesScreen(
                            userPreferences = userPrefs,
                            viewModelFactory = solicitudesViewModelFactory,
                            mode = SolicitudesMode.PROPIETARIO, // Usamos la opción del Enum limpio
                            onNavigateToDetalle = goSolicitudDetalle // Redirige al detalle de la solicitud
                        )
                    }

                    // 3. SOLICITUDES ADMINISTRADOR
                    composable(Routes.SOLICITUDES_ADMIN) {
                        val solicitudesViewModelFactory = SolicitudesViewModelFactory(
                            solicitudDao = database.solicitudDao(),
                            propiedadDao = database.propiedadDao(),
                            catalogDao = database.catalogDao(),
                            remoteRepository = applicationRepository,
                            propertyRepository = propertyRepository
                        )

                        SolicitudesScreen(
                            userPreferences = userPrefs,
                            viewModelFactory = solicitudesViewModelFactory,
                            mode = SolicitudesMode.ADMIN_GLOBAL,
                            onNavigateToDetalle = goSolicitudDetalle // Redirige al detalle de la solicitud
                        )
                    }

                    // MIS DOCUMENTOS
                    composable(Routes.MIS_DOCUMENTOS) {
                        val documentRepository = DocumentRemoteRepository()
                        val misDocumentosViewModel: MisDocumentosViewModel = viewModel(
                            factory = MisDocumentosViewModelFactory(documentRepository)
                        )

                        MisDocumentosScreen(
                            viewModel = misDocumentosViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.MIS_ARRIENDOS) {
                        MisArriendosScreen(
                            userPreferences = userPrefs,
                            onBack = { navController.popBackStack() },
                            onVerPropiedad = goPropiedadDetalle
                        )
                    }

                    // ADMIN PANEL
                    composable(Routes.ADMIN_PANEL) {
                        AdminPanelScreen(
                            onBack = { navController.popBackStack() },
                            onGestionPropiedades = { navController.navigate(Routes.GESTION_PROPIEDADES) },
                            onGestionDocumentos = { navController.navigate(Routes.GESTION_DOCUMENTOS) },
                            onGestionContacto = { navController.navigate(Routes.GESTION_CONTACTO) }
                        )
                    }

                    // GESTION USUARIOS
                    composable(Routes.GESTION_USUARIOS) {
                        val userRepository = UserRepository(RetrofitClient.userServiceApi)
                        val userManagementViewModel: UserManagementViewModel = viewModel(
                            factory = UserManagementViewModelFactory(userRepository)
                        )

                        val users by userManagementViewModel.users.collectAsStateWithLifecycle()
                        val isLoading by userManagementViewModel.isLoading.collectAsStateWithLifecycle()
                        val error by userManagementViewModel.error.collectAsStateWithLifecycle()

                        UserManagementScreen(
                            users = users,
                            isLoading = isLoading,
                            error = error,
                            onBack = { navController.popBackStack() },
                            onUpdateUser = { user: UsuarioDTO -> userManagementViewModel.updateUser(user.id!!, user) },
                            onDeleteUser = { user: UsuarioDTO -> userManagementViewModel.deleteUser(user.id!!) },
                            onRetry = { userManagementViewModel.loadUsers() }
                        )
                    }

                    // GESTION PROPIEDADES
                    composable(Routes.GESTION_PROPIEDADES) {
                        val propiedadViewModelFactory = PropiedadViewModelFactory(
                            propiedadDao = database.propiedadDao(),
                            catalogDao = database.catalogDao(),
                            remoteRepository = propertyRepository
                        )

                        GestionPropiedadesScreen(
                            onBack = { navController.popBackStack() },
                            onVerDetalle = { propiedadId -> goPropiedadDetalle(propiedadId) },
                            viewModelFactory = propiedadViewModelFactory
                        )
                    }

                    // GESTION DOCUMENTOS
                    composable(Routes.GESTION_DOCUMENTOS) {
                        val documentRepository = DocumentRemoteRepository()
                        val gestionDocumentosViewModel: GestionDocumentosViewModel = viewModel(
                            factory = GestionDocumentosViewModelFactory(documentRepository)
                        )

                        GestionDocumentosScreen(
                            viewModel = gestionDocumentosViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // AGREGAR PROPIEDAD
                    composable(Routes.AGREGAR_PROPIEDAD) {
                        val agregarPropiedadViewModelFactory = AgregarPropiedadViewModelFactory(
                            propertyRepository = propertyRepository
                        )

                        AgregarPropiedadScreen(
                            userPreferences = userPrefs,
                            viewModelFactory = agregarPropiedadViewModelFactory,
                            onNavigateBack = { navController.popBackStack() },
                            onPropiedadCreada = { navController.popBackStack() }
                        )
                    }

                    // MIS PROPIEDADES
                    composable(Routes.MIS_PROPIEDADES) {
                        val misPropiedadesViewModelFactory = MisPropiedadesViewModelFactory(
                            propiedadDao = database.propiedadDao(),
                            catalogDao = database.catalogDao(),
                            propertyRepository = propertyRepository,
                            applicationRepository = applicationRepository
                        )

                        MisPropiedadesScreen(
                            userPreferences = userPrefs,
                            viewModelFactory = misPropiedadesViewModelFactory,
                            onNavigateToAgregar = { navController.navigate(Routes.AGREGAR_PROPIEDAD) },
                            onNavigateToDetalle = { propiedadId -> goPropiedadDetalle(propiedadId) }
                        )
                    }

                    // CONTACT
                    composable(Routes.CONTACT) {
                        val contactRepository = ContactRemoteRepository()
                        val contactViewModel: ContactViewModel = viewModel(
                            factory = ContactViewModelFactory(contactRepository)
                        )

                        ContactScreen(
                            contactViewModel = contactViewModel,
                            usuarioId = userId,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.GESTION_CONTACTO) {
                        val contactRepository = ContactRemoteRepository()
                        val contactViewModel: ContactViewModel = viewModel(
                            factory = ContactViewModelFactory(contactRepository)
                        )

                        val adminId = userId
                        if (!isLoggedIn || adminId == null) {
                            LaunchedEffect(Unit) {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(Routes.GESTION_CONTACTO) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        } else {
                            GestionContactoScreen(
                                contactViewModel = contactViewModel,
                                adminId = adminId,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }

                    // SOLICITUD DETALLE
                    composable(
                        route = "${Routes.SOLICITUD_DETALLE}/{solicitudId}",
                        arguments = listOf(navArgument("solicitudId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val solicitudId = backStackEntry.arguments?.getLong("solicitudId") ?: 0L

                        SolicitudDetalleScreen(
                            solicitudId = solicitudId,
                            userPreferences = userPrefs,
                            onBack = { navController.popBackStack() },
                            onVerPropiedad = { propiedadId -> goPropiedadDetalle(propiedadId) }
                        )
                    }
                }
            }
        }
    }
}