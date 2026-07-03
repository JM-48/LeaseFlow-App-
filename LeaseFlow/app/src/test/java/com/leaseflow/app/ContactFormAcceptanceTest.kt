package com.leaseflow.app

import com.leaseflow.app.data.local.storage.UserSessionData
import com.leaseflow.app.data.repository.ContactRemoteRepository
import com.leaseflow.app.ui.viewmodel.ContactViewModel
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock

class ContactFormAcceptanceTest {

    private val userPreferences = flowOf(UserSessionData())
    private val viewModel = ContactViewModel(
        mock<ContactRemoteRepository>(),
        userPreferences
    )

    @Test
    fun formulario_valido_cumple_los_criterios_de_aceptacion() {
        val email = viewModel.validarEmail("cliente@leaseflow.cl")
        val asunto = viewModel.validarAsunto("Consulta sobre disponibilidad")
        val mensaje = viewModel.validarMensaje("Necesito saber si la propiedad admite mascotas.")

        assertTrue(email.first)
        assertNull(email.second)
        assertTrue(asunto.first)
        assertNull(asunto.second)
        assertTrue(mensaje.first)
        assertNull(mensaje.second)
    }

    @Test
    fun formulario_invalido_rechaza_los_campos_que_no_cumplen_las_reglas() {
        val email = viewModel.validarEmail("cliente-sin-dominio")
        val asunto = viewModel.validarAsunto("")
        val mensaje = viewModel.validarMensaje("Muy corto")

        assertEquals(false, email.first)
        assertEquals("El email no es valido", email.second)
        assertEquals(false, asunto.first)
        assertEquals("El asunto es obligatorio", asunto.second)
        assertEquals(false, mensaje.first)
        assertEquals("El mensaje debe tener al menos 10 caracteres", mensaje.second)
    }
}
