package com.leaseflow.app

import com.leaseflow.app.data.local.storage.UserSessionData
import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.dto.MensajeContactoDTO
import com.leaseflow.app.data.repository.ContactRemoteRepository
import com.leaseflow.app.ui.viewmodel.ContactViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.flow.flowOf
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ContactViewModelMockTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository: ContactRemoteRepository = mock()
    private val userPreferences = flowOf(
        UserSessionData(
            isLoggedIn = true,
            userId = 10L,
            userRole = 1
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun crear_mensaje_exitoso_actualiza_el_estado_de_exito() = runTest {
        val dto = MensajeContactoDTO(
            id = 99L,
            nombre = "Ana",
            email = "ana@leaseflow.cl",
            asunto = "Consulta de arriendo",
            mensaje = "Necesito informacion adicional sobre la propiedad."
        )
        whenever(
            repository.crearMensaje(
                nombre = "Ana",
                email = "ana@leaseflow.cl",
                asunto = "Consulta de arriendo",
                mensaje = "Necesito informacion adicional sobre la propiedad.",
                numeroTelefono = null,
                usuarioId = null
            )
        ).thenReturn(ApiResult.Success(dto))

        val viewModel = ContactViewModel(repository, userPreferences)
        viewModel.crearMensaje(
            nombre = "Ana",
            email = "ana@leaseflow.cl",
            asunto = "Consulta de arriendo",
            mensaje = "Necesito informacion adicional sobre la propiedad."
        )

        advanceUntilIdle()

        assertEquals(
            "Mensaje enviado exitosamente. Le responderemos pronto.",
            viewModel.successMessage.value
        )
        assertNull(viewModel.errorMessage.value)
        verify(repository, times(1)).crearMensaje(
            nombre = "Ana",
            email = "ana@leaseflow.cl",
            asunto = "Consulta de arriendo",
            mensaje = "Necesito informacion adicional sobre la propiedad.",
            numeroTelefono = null,
            usuarioId = null
        )
    }
}
