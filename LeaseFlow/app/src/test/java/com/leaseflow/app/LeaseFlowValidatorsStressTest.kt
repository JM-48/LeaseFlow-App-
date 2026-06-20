package com.leaseflow.app

import com.leaseflow.app.domain.validation.validatePhoneChileno
import com.leaseflow.app.domain.validation.validateStrongPassword
import org.junit.Assert.assertEquals
import org.junit.Test

class LeaseFlowValidatorsStressTest {

    @Test
    fun mantiene_resultados_consistentes_en_diez_mil_validaciones_mixtas_de_password() {
        var validas = 0
        var invalidas = 0

        repeat(10_000) { index ->
            val password = if (index % 2 == 0) "Lease1234." else "debil"
            val result = validateStrongPassword(password)

            if (result == null) {
                validas++
            } else {
                invalidas++
            }
        }

        assertEquals(5_000, validas)
        assertEquals(5_000, invalidas)
    }

    @Test
    fun tolera_entradas_extremas_en_telefonos_sin_perder_consistencia() {
        val resultados = (1..2_000).map { index ->
            if (index % 2 == 0) validatePhoneChileno("912345678") else validatePhoneChileno("123")
        }

        assertEquals(1_000, resultados.count { it == null })
        assertEquals(
            1_000,
            resultados.count { it == "Debe tener 9 dígitos (ej: 912345678)" }
        )
    }
}
