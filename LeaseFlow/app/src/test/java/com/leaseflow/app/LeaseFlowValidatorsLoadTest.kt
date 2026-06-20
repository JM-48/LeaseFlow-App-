package com.leaseflow.app

import com.leaseflow.app.domain.validation.validateEmail
import com.leaseflow.app.domain.validation.validatePrecio
import org.junit.Assert.assertEquals
import org.junit.Test

class LeaseFlowValidatorsLoadTest {

    @Test
    fun procesa_un_lote_grande_de_emails_validos_sin_generar_errores() {
        val errores = (1..1_000).count { index ->
            validateEmail("usuario$index@leaseflow.cl") != null
        }

        assertEquals(0, errores)
    }

    @Test
    fun procesa_un_lote_grande_de_precios_positivos_sin_fallos() {
        val errores = (1..1_000).count { index ->
            validatePrecio((100_000 + index).toString()) != null
        }

        assertEquals(0, errores)
    }
}
