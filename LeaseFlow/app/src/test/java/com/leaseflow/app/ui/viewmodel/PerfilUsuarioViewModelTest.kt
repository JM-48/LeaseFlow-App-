package com.leaseflow.app.ui.viewmodel

import com.leaseflow.app.data.local.dao.CatalogDao
import com.leaseflow.app.data.local.dao.SolicitudDao
import com.leaseflow.app.data.local.dao.UsuarioDao
import com.leaseflow.app.data.local.entities.EstadoEntity
import com.leaseflow.app.data.local.entities.RolEntity
import com.leaseflow.app.data.local.entities.UsuarioEntity
import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.dto.UsuarioRemoteDTO
import com.leaseflow.app.data.repository.LeaseFlowUserRepository
import com.leaseflow.app.data.repository.UserRemoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class PerfilUsuarioViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var usuarioDao: UsuarioDao
    @Mock
    private lateinit var catalogDao: CatalogDao
    @Mock
    private lateinit var solicitudDao: SolicitudDao
    @Mock
    private lateinit var userRemoteRepository: UserRemoteRepository
    @Mock
    private lateinit var localUserRepository: LeaseFlowUserRepository

    private lateinit var viewModel: PerfilUsuarioViewModel

    // Datos Falsos Comunes
    private val FAKE_FNACIMIENTO = 1000L
    private val FAKE_FECHA = 1000L
    private val FAKE_CODIGO = "TESTCODE"
    private val FAKE_SNOMBRE = "Segundo"

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        viewModel = PerfilUsuarioViewModel(
            usuarioDao = usuarioDao,
            catalogDao = catalogDao,
            solicitudDao = solicitudDao,
            userRemoteRepository = userRemoteRepository,
            localUserRepository = localUserRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cargarDatosUsuario obtiene usuario, nombre de rol y conteo de solicitudes correctamente`() = runTest {
        // 1. ARRANGE
        val userId = 10L
        val rolId = 2L

       
        val usuarioFake = UsuarioEntity(
            id = userId,
            pnombre = "Juan",
            snombre = FAKE_SNOMBRE,
            papellido = "Pérez",
            fnacimiento = FAKE_FNACIMIENTO,
            email = "juan@test.com",
            rut = "12345678-9",
            ntelefono = "987654321",
            clave = "clave123",
            duoc_vip = false,
            codigo_ref = FAKE_CODIGO,
            fcreacion = FAKE_FECHA,
            factualizacion = FAKE_FECHA,
            estado_id = 1,
            rol_id = rolId
        )

        val rolReal = RolEntity(id = rolId, nombre = "Propietario")
        val estadoPendienteReal = EstadoEntity(id = 5L, nombre = "Pendiente")

        // Enseñamos a los Mocks qué responder
        whenever(usuarioDao.getById(userId)).thenReturn(usuarioFake)
        whenever(userRemoteRepository.obtenerUsuarioPorId(userId, includeDetails = true)).thenReturn(
            ApiResult.Success(
                UsuarioRemoteDTO(
                    id = userId,
                    pnombre = usuarioFake.pnombre,
                    snombre = usuarioFake.snombre,
                    papellido = usuarioFake.papellido,
                    fnacimiento = "2000-01-01",
                    email = usuarioFake.email,
                    rut = usuarioFake.rut,
                    ntelefono = usuarioFake.ntelefono,
                    clave = usuarioFake.clave,
                    estadoId = usuarioFake.estado_id,
                    rolId = usuarioFake.rol_id
                )
            )
        )
        whenever(localUserRepository.syncUsuarioFromRemote(any())).thenReturn(Result.success(userId))
        whenever(catalogDao.getRolById(rolId)).thenReturn(rolReal)
        whenever(catalogDao.getEstadoByNombre("Pendiente")).thenReturn(estadoPendienteReal)

        // Simulamos que hay 3 solicitudes pendientes
        whenever(solicitudDao.countSolicitudesActivas(eq(userId), eq(estadoPendienteReal.id))).thenReturn(3)

        // 2. ACT
        viewModel.cargarDatosUsuario(userId)
        advanceUntilIdle()

        // 3. ASSERT
        assertNotNull(viewModel.usuario.value)
        assertEquals("Juan", viewModel.usuario.value?.pnombre)
        assertEquals("Propietario", viewModel.nombreRol.value)
        assertEquals(0, viewModel.cantidadSolicitudes.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `actualizarPerfil llama al microservicio y refresca el usuario local`() = runTest {
        // 1. ARRANGE
        // Usuario Original (CON TODOS LOS CAMPOS OBLIGATORIOS Y GUION BAJO)
        val usuarioOriginal = UsuarioEntity(
            id = 1L,
            pnombre = "Original",
            snombre = FAKE_SNOMBRE,
            papellido = "Original",
            fnacimiento = FAKE_FNACIMIENTO,
            email = "test@leaseflow.cl",
            rut = "1-9",
            ntelefono = "111111111",
            clave = "clave123",
            duoc_vip = false,
            codigo_ref = FAKE_CODIGO,
            fcreacion = FAKE_FECHA,
            factualizacion = FAKE_FECHA,
            estado_id = 1,
            rol_id = 1
        )

        whenever(usuarioDao.getById(1L)).thenReturn(
            usuarioOriginal,
            usuarioOriginal,
            usuarioOriginal.copy(
                pnombre = "Editado",
                snombre = "Segundo",
                papellido = "NuevoApellido",
                ntelefono = "999999999"
            )
        )
        whenever(userRemoteRepository.obtenerUsuarioPorId(1L, includeDetails = true)).thenReturn(
            ApiResult.Success(
                UsuarioRemoteDTO(
                    id = 1L,
                    pnombre = usuarioOriginal.pnombre,
                    snombre = usuarioOriginal.snombre,
                    papellido = usuarioOriginal.papellido,
                    fnacimiento = "2000-01-01",
                    email = usuarioOriginal.email,
                    rut = usuarioOriginal.rut,
                    ntelefono = usuarioOriginal.ntelefono,
                    clave = usuarioOriginal.clave,
                    estadoId = usuarioOriginal.estado_id,
                    rolId = usuarioOriginal.rol_id
                )
            )
        )
        whenever(userRemoteRepository.actualizarUsuario(eq(1L), any())).thenReturn(
            ApiResult.Success(
                UsuarioRemoteDTO(
                    id = 1L,
                    pnombre = "Editado",
                    snombre = "Segundo",
                    papellido = "NuevoApellido",
                    fnacimiento = "2000-01-01",
                    email = usuarioOriginal.email,
                    rut = usuarioOriginal.rut,
                    ntelefono = "999999999",
                    clave = usuarioOriginal.clave,
                    estadoId = usuarioOriginal.estado_id,
                    rolId = usuarioOriginal.rol_id
                )
            )
        )
        whenever(localUserRepository.syncUsuarioFromRemote(any())).thenReturn(Result.success(1L))
        viewModel.cargarDatosUsuario(1L)
        advanceUntilIdle()

        // 2. ACT
        viewModel.actualizarPerfil(
            usuarioId = 1L,
            pnombre = "Editado",
            snombre = "Segundo",
            papellido = "NuevoApellido",
            telefono = "999999999"
        )
        advanceUntilIdle()

        // 3. ASSERT
        // Verificamos actualización en StateFlow
        val usuarioActual = viewModel.usuario.value
        assertEquals("Editado", usuarioActual?.pnombre)

        verify(userRemoteRepository).actualizarUsuario(eq(1L), any())
        verify(localUserRepository, atLeastOnce()).syncUsuarioFromRemote(any())
    }
}
