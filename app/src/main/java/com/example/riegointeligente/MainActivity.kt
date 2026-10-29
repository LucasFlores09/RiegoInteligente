
package com.example.riegointeligente

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.riegointeligente.ui.theme.RiegoInteligenteTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val SERVIDOR = "http://127.0.0.1:8000/"
private const val API_KEY = "RiegoIoT-2026-demo"
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RiegoInteligenteTheme {
                RiegoApp()
            }
        }
    }
}

@Composable
fun RiegoApp() {
    var usuario by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var errorConexion by remember { mutableStateOf("") }
    var sesionIniciada by remember { mutableStateOf(false) }

    var humedad by remember { mutableFloatStateOf(35f) }
    var riegoActivo by remember { mutableStateOf(false) }
    var conectado by remember { mutableStateOf(false) }
    var editandoHumedad by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(sesionIniciada) {
        if (sesionIniciada) {
            while (true) {
                try {
                    val datos = withContext(Dispatchers.IO) {
                        consultarServidor()
                    }

                    conectado = true
                    errorConexion = ""

                    if (!editandoHumedad) {
                        humedad = datos.getInt("humedad").toFloat()
                    }

                    riegoActivo = datos.getBoolean("riego")

                } catch (e: Exception) {
                    conectado = false
                    errorConexion = e.message ?: "No se pudo conectar"
                }

                delay(3000)
            }
        }
    }

    MaterialTheme {
        if (!sesionIniciada) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.height(48.dp))

                Text(
                    text = "💧",
                    style = MaterialTheme.typography.displayLarge
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Riego Inteligente",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Control y monitoreo del agua",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(32.dp))

                OutlinedTextField(
                    value = usuario,
                    onValueChange = { usuario = it },
                    label = { Text("Usuario") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = clave,
                    onValueChange = { clave = it },
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (usuario == "admin" && clave == "1234") {
                            mensaje = ""
                            sesionIniciada = true
                        } else {
                            mensaje = "Usuario o contraseña incorrectos"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Iniciar sesión")
                }

                if (mensaje.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        mensaje,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "Proyecto IoT · Sistema de riego",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(48.dp))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = "Riego Inteligente",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Panel de control",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Conexión con el servidor",
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                if (conectado) {
                                    "Conectado · Actualización cada 3 s"
                                } else {
                                    "Desconectado"
                                },
                                color = if (conectado) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Text(
                            if (conectado) "●" else "●",
                            color = if (conectado) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                if (errorConexion.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = errorConexion,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Humedad del suelo",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "${humedad.toInt()}%",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Slider(
                            value = humedad,
                            onValueChange = {
                                humedad = it
                                editandoHumedad = true
                            },
                            valueRange = 0f..100f,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0%")
                            Text("100%")
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = if (humedad < 40) {
                                "Suelo seco: necesita riego"
                            } else {
                                "Humedad adecuada"
                            },
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Umbral de riego: 40%",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            "Estado de la bomba",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            if (riegoActivo) {
                                "RIEGO ACTIVADO"
                            } else {
                                "RIEGO DESACTIVADO"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (riegoActivo) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val nuevoEstado = !riegoActivo

                                scope.launch {
                                    try {
                                        val respuesta = withContext(Dispatchers.IO) {
                                            enviarDatos(
                                                humedad.toInt(),
                                                nuevoEstado
                                            )
                                        }

                                        humedad = respuesta
                                            .getInt("humedad").toFloat()
                                        riegoActivo = respuesta
                                            .getBoolean("riego")
                                        editandoHumedad = false
                                        conectado = true
                                        errorConexion = ""

                                    } catch (e: Exception) {
                                        conectado = false
                                        errorConexion =
                                            e.message ?: "Error al enviar datos"
                                    }
                                }
                            },
                            enabled = conectado,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                if (riegoActivo) {
                                    "Desactivar riego"
                                } else {
                                    "Activar riego"
                                }
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                val nuevoEstado = humedad < 40

                                scope.launch {
                                    try {
                                        val respuesta = withContext(Dispatchers.IO) {
                                            enviarDatos(
                                                humedad.toInt(),
                                                nuevoEstado
                                            )
                                        }

                                        humedad = respuesta
                                            .getInt("humedad").toFloat()
                                        riegoActivo = respuesta
                                            .getBoolean("riego")
                                        editandoHumedad = false
                                        conectado = true
                                        errorConexion = ""

                                    } catch (e: Exception) {
                                        conectado = false
                                        errorConexion =
                                            e.message ?: "Error al enviar datos"
                                    }
                                }
                            },
                            enabled = conectado,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Aplicar riego automático")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "La humedad se simula con el control deslizante.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        sesionIniciada = false
                        usuario = ""
                        clave = ""
                        editandoHumedad = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar sesión")
                }
            }
        }
    }
}

private fun consultarServidor(): JSONObject {
    val conexion =
        URL(SERVIDOR).openConnection() as HttpURLConnection

    conexion.requestMethod = "GET"
    conexion.connectTimeout = 5000
    conexion.readTimeout = 5000
    conexion.setRequestProperty("X-API-Key", API_KEY)
    try {
        val respuesta = conexion.inputStream
            .bufferedReader()
            .use { it.readText() }

        return JSONObject(respuesta)
    } finally {
        conexion.disconnect()
    }
}

private fun enviarDatos(
    humedad: Int,
    riego: Boolean
): JSONObject {
    val conexion =
        URL(SERVIDOR).openConnection() as HttpURLConnection

    conexion.requestMethod = "POST"
    conexion.connectTimeout = 5000
    conexion.readTimeout = 5000
    conexion.doOutput = true

    conexion.setRequestProperty(
        "Content-Type",
        "application/json; charset=UTF-8"
    )

    conexion.setRequestProperty("X-API-Key", API_KEY)
    try {
        val datos = JSONObject().apply {
            put("humedad", humedad)
            put("riego", riego)
        }

        conexion.outputStream.use {
            it.write(datos.toString().toByteArray(Charsets.UTF_8))
        }

        val codigo = conexion.responseCode
        val flujo = if (codigo in 200..299) {
            conexion.inputStream
        } else {
            conexion.errorStream
                ?: throw Exception("Error HTTP $codigo")
        }

        val respuesta = flujo.bufferedReader()
            .use { it.readText() }

        if (codigo !in 200..299) {
            throw Exception("Error HTTP $codigo: $respuesta")
        }

        return JSONObject(respuesta)
    } finally {
        conexion.disconnect()
    }
}
