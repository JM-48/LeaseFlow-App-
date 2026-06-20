package com.leaseflow.app

import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.safeApiCall
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class SafeApiCallIntegrationTest {

    @Test
    fun respuesta_exitosa_se_convierte_en_success() = runTest {
        val result = safeApiCall { Response.success("ok") }

        assertTrue(result is ApiResult.Success)
        assertEquals("ok", (result as ApiResult.Success).data)
    }

    @Test
    fun error_estructurado_combina_validation_errors_en_un_mensaje_legible() = runTest {
        val errorJson = """
            {
              "timestamp": "2026-06-20T12:00:00",
              "status": 400,
              "error": "Bad Request",
              "message": "Validacion fallida",
              "validationErrors": {
                "email": "Email invalido",
                "asunto": "Asunto obligatorio"
              }
            }
        """.trimIndent()

        val result = safeApiCall {
            Response.error<String>(
                400,
                errorJson.toResponseBody("application/json".toMediaType())
            )
        }

        assertTrue(result is ApiResult.Error)
        result as ApiResult.Error
        assertEquals(400, result.code)
        assertEquals("Email invalido\nAsunto obligatorio", result.message)
    }
}
