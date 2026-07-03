package com.leaseflow.app.ui.viewmodel

import com.leaseflow.app.data.local.storage.UserSessionData
import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.dto.ComunaRemoteDTO
import com.leaseflow.app.data.remote.dto.PropertyRemoteDTO
import com.leaseflow.app.data.repository.PropertyRemoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AgregarPropiedadViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val userPreferences = flowOf(
        UserSessionData(
            isLoggedIn = true,
            userId = 25L,
            userRole = 2
        )
    )

    @Mock
    private lateinit var propertyRepository: PropertyRemoteRepository

    private lateinit var viewModel: AgregarPropiedadViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        runBlocking {
            whenever(propertyRepository.listarTipos()).thenReturn(ApiResult.Success(emptyList()))
            whenever(propertyRepository.listarRegiones()).thenReturn(ApiResult.Success(emptyList()))
            whenever(propertyRepository.listarComunas()).thenReturn(ApiResult.Success(emptyList()))
            whenever(propertyRepository.listarCategorias()).thenReturn(ApiResult.Success(emptyList()))
        }

        viewModel = AgregarPropiedadViewModel(propertyRepository, userPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cargarComunasPorRegion filtra correctamente`() = runTest {
        val regionIdTarget = 5L
        val comunaCorrecta = ComunaRemoteDTO(
            id = 1L,
            nombre = "Comuna 1",
            regionId = 5L,
            region = null
        )

        whenever(propertyRepository.listarComunas())
            .thenReturn(ApiResult.Success(listOf(comunaCorrecta)))

        viewModel.cargarCatalogos()
        advanceUntilIdle()
        viewModel.cargarComunasPorRegion(regionIdTarget)
        advanceUntilIdle()

        assertEquals("Debería haber 1 comuna filtrada", 1, viewModel.comunasFiltradas.value.size)
        assertEquals("Comuna 1", viewModel.comunasFiltradas.value[0].nombre)
    }

    @Test
    fun `crearPropiedad activa loading y guarda exitosamente`() = runTest {
        val propiedadCreadaFake = PropertyRemoteDTO(
            id = 777L,
            codigo = "COD-TEST",
            titulo = "Casa Test",
            precioMensual = 500000.0,
            divisa = "CLP",
            m2 = 80.0,
            nHabit = 3,
            nBanos = 2,
            petFriendly = true,
            direccion = "Calle Test 123",
            tipoId = 1L,
            comunaId = 1L,
            propietarioId = 1L,
            fotos = emptyList(),
            tipo = null,
            comuna = null
        )

        whenever(propertyRepository.crearPropiedad(any(), any(), any()))
            .thenAnswer { invocation ->
                val propiedadEnviada = invocation.getArgument<PropertyRemoteDTO>(2)
                assertTrue("El ViewModel debería estar guardando (isSaving=true)", viewModel.isSaving.value)
                assertEquals("Casa Test", propiedadEnviada.titulo)
                assertEquals(500000.0, propiedadEnviada.precioMensual, 0.0)
                assertEquals("CLP", propiedadEnviada.divisa)
                assertEquals(80.0, propiedadEnviada.m2, 0.0)
                assertEquals(3, propiedadEnviada.nHabit)
                assertEquals(2, propiedadEnviada.nBanos)
                assertTrue(propiedadEnviada.petFriendly)
                assertEquals("Calle Test 123", propiedadEnviada.direccion)
                assertEquals(1L, propiedadEnviada.tipoId)
                assertEquals(1L, propiedadEnviada.comunaId)
                assertEquals(1L, propiedadEnviada.propietarioId)
                ApiResult.Success(propiedadCreadaFake)
            }

        viewModel.crearPropiedad(
            titulo = "Casa Test",
            precioMensual = 500000.0,
            m2 = 80.0,
            nHabit = 3,
            nBanos = 2,
            petFriendly = true,
            direccion = "Calle Test 123",
            tipoId = 1L,
            comunaId = 1L,
            propietarioId = 1L
        )

        advanceUntilIdle()

        assertFalse("Al terminar, isSaving debe ser false", viewModel.isSaving.value)
        assertNotNull("La propiedad creada no debe ser nula", viewModel.propiedadCreada.value)
        assertEquals(777L, viewModel.propiedadCreada.value?.id)
        assertNotNull("Debe haber un mensaje de éxito", viewModel.successMsg.value)
    }

    @Test
    fun `crearPropiedad maneja errores del servidor`() = runTest {
        val mensajeError = "Error de conexión"

        whenever(propertyRepository.crearPropiedad(any(), any(), any()))
            .thenReturn(ApiResult.Error(mensajeError))

        viewModel.crearPropiedad(
            titulo = "X",
            precioMensual = 0.0,
            m2 = 0.0,
            nHabit = 0,
            nBanos = 0,
            petFriendly = false,
            direccion = "X",
            tipoId = 1L,
            comunaId = 1L,
            propietarioId = 1L
        )
        advanceUntilIdle()

        assertNull("No se debió crear la propiedad", viewModel.propiedadCreada.value)
        assertEquals(mensajeError, viewModel.errorMsg.value)
        assertFalse("isSaving debe apagarse tras el error", viewModel.isSaving.value)
    }
}
