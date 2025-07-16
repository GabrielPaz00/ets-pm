package com.example.e2.android

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.camera.core.Preview as CamX
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.rememberImagePainter
import com.google.accompanist.permissions.rememberMultiplePermissionsState


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TitleText("g) Pantalla que permite tomar fotografía")
        Spacer(modifier = Modifier.height(16.dp))
        MainCard()
    }
}

@Composable
fun TitleText(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(16.dp),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )
}
/**
 * Función para capturar una foto.
 *
 * @param imageCapture La instancia configurada de ImageCapture.
 * @param context El contexto actual.
 * @param onImageCaptured Callback que se activa con el URI de la imagen guardada.
 * @param onError Callback que se activa en caso de error, pasando la excepción.
 */
fun capturePhoto(imageCapture: ImageCapture, context: Context, onImageCaptured: (Uri) -> Unit, onError: (Exception) -> Unit) {
    val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        ContentValues().apply {
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
    ).build()

    imageCapture.takePicture(
        outputFileOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                outputFileResults.savedUri?.let(onImageCaptured)
            }
            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}

//Contenedor principal
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainCard() {

    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    var imageCaptureInstance by remember { mutableStateOf<ImageCapture?>(null) }

    // Estado para gestionar los permisos.
    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
        )
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {

        // Botón para activar la cámara y solicitar permisos
        Button(
            onClick = { multiplePermissionsState.launchMultiplePermissionRequest() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Activar Cámara")
        }

        // Vista previa de cámara con botón para capturar imagen
        if (multiplePermissionsState.allPermissionsGranted) {
            CameraPreviewWithButton(
                onImageCaptureReady = { imageCaptureInstance = it },
                onTakePhoto = {
                    imageCaptureInstance?.let { capture ->
                        capturePhoto(capture, context, { uri -> capturedImageUri = uri }, { e ->
                            Log.e("Capture Error", e.message.orEmpty())
                        })
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Mostrar la imagen capturada
        capturedImageUri?.let {
            Image(
                painter = rememberImagePainter(it),
                contentDescription = "Foto Capturada",
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        }
        var tokenInput by remember { mutableStateOf("") }


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            SaveButton( enabledButton = true,
               capturedImageUri = capturedImageUri, tokenInput )
        }

    }
}


@Composable
fun SaveButton(
    enabledButton: Boolean,
    capturedImageUri: Uri?,
    tokenInput: String,
    // Puedes usar un callback para notificarle al padre que la foto se guardó exitosamente
    onPhotoSaved: () -> Unit = {}
) {
    val context = LocalContext.current
    // Estado para almacenar el token generado; vacío significa que aún no se ha generado
    var currentToken by remember { mutableStateOf("") }
    // Estado para almacenar el momento (en milisegundos) en que se generó el token
    var tokenGeneratedTime by remember { mutableStateOf(0L) }

    Button(
        onClick = {
            // Si aún no se ha generado un token, lo generamos y guardamos el tiempo actual
            if (currentToken.isEmpty()) {
                currentToken = generateToken().toString()
                tokenGeneratedTime = System.currentTimeMillis()
                Toast.makeText(context, "Token generado: $currentToken", Toast.LENGTH_LONG).show()
                // En este punto, el usuario debe copiar o ingresar el token en el campo de texto
                return@Button
            }
            // Ya se generó un token; comprobamos que no haya expirado
            val currentTime = System.currentTimeMillis()
            if (currentTime - tokenGeneratedTime > 15000) {
                Toast.makeText(context, "El token ha expirado", Toast.LENGTH_LONG).show()
                // Reiniciamos el token para que, en el siguiente clic, se genere uno nuevo
                currentToken = ""
                tokenGeneratedTime = 0L
                return@Button
            }
            // Verificamos que lo introducido coincida con el token generado
            if (tokenInput != currentToken) {
                Toast.makeText(context, "Token inválido", Toast.LENGTH_LONG).show()
                return@Button
            }
            // Si el token es correcto y válido, procedemos a guardar la foto
            try {
                capturedImageUri?.let { uri ->
                    savePhotoToGallery(context, uri)
                }
                Toast.makeText(context, "Foto guardada con éxito", Toast.LENGTH_LONG).show()
                // Reiniciamos el token para prevenir usos posteriores con el mismo valor.
                currentToken = ""
                tokenGeneratedTime = 0L
                onPhotoSaved()
            } catch (error: Exception) {
                Toast.makeText(context, "Error al guardar la foto: ${error.message}", Toast.LENGTH_LONG).show()
            }
        },
        enabled = enabledButton,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Green,
            contentColor = Color.White
        )
    ) {
        // Modificamos el texto del botón según el estado del token
        Text("Aceptar")
    }
}

fun savePhotoToGallery(context: Context, imageUri: Uri) {
    val resolver = context.contentResolver
    val source = resolver.openInputStream(imageUri) ?: return
    val displayName = "captured_image_${System.currentTimeMillis()}.jpg"

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    }

    val imageUriSaved = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    imageUriSaved?.let { savedUri ->
        resolver.openOutputStream(savedUri)?.use { outputStream ->
            source.copyTo(outputStream)
        }
        source.close()
    }
}




@Composable
fun CameraPreviewWithButton(onImageCaptureReady: (ImageCapture) -> Unit, onTakePhoto: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var imageCaptureInstance by remember { mutableStateOf<ImageCapture?>(null) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView = it }
            },
            modifier = Modifier.fillMaxWidth().height(300.dp)
        )

        LaunchedEffect(previewView) {
            previewView?.let { pView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = CamX.Builder().build().apply {
                            setSurfaceProvider(pView.surfaceProvider)
                        }
                        val imageCapture = ImageCapture.Builder().build()
                        imageCaptureInstance = imageCapture
                        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
                        onImageCaptureReady(imageCapture)
                    } catch (exc: Exception) {
                        Log.e("CameraPreview", "Error al vincular la cámara", exc)
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Button(onClick = onTakePhoto, modifier = Modifier.padding(top = 16.dp)) {
            Text("Tomar Foto")
        }
    }
}


@Composable
fun numericTextField(): String {
    var input by remember { mutableStateOf("") }

    OutlinedTextField(
        value = input,
        onValueChange = {
            if (it.length <= 3 && it.all { char -> char.isDigit() }) {
                input = it
            }
        },
        label = { Text("Token (3 dígitos)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
    return input
}



fun generateToken(): Int {
    return (100..999).random() // Genera un número de tres dígitos aleatorio
}

@Preview(showBackground = true)
@Composable
fun PreviewMainActivity() {
    MainScreen()
}
