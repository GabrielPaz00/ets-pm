package com.example.e2.android

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.compose.ui.text.font.FontWeight
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.io.File
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable

class CamaraViewModel() : ViewModel() {
    var uriImagen by mutableStateOf<Uri?>(null)
        private set

    fun saveURI(uri: Uri) {
        uriImagen = uri
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainScreenPhoto(navController: NavController,
                    viewModel: CamaraViewModel = viewModel()) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(top = 24.dp)
    ) {
        val permissionState = rememberMultiplePermissionsState(
            listOf(Manifest.permission.CAMERA)
        )

        LaunchedEffect(Unit) {
            permissionState.launchMultiplePermissionRequest()
        }


        Spacer(modifier = Modifier.height(16.dp))
        val viewModel: CamaraViewModel = viewModel()
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Captura de Imagen") })
            },
            content = { padding ->
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                ) {
                    ViewCamara(onImageSaved = { uri ->
                        viewModel.saveURI(uri)
                        navController.navigate("detalle/${Uri.encode(uri.toString())}")

                    })
                }
            }
        )
    }
}
@Composable
fun ViewCamara(
    onImageSaved: (Uri) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val imageCapture = remember { ImageCapture.Builder().build() }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    AndroidView(factory = { ctx ->
        val previewView = PreviewView(ctx).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = CamX.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, preview, imageCapture

            )
        }, ContextCompat.getMainExecutor(ctx))

        previewView
    })

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {


        Button(onClick = {
            try {


            val fileName = "captured_image_${System.currentTimeMillis()}.jpg"
            val photoFile = File(context.filesDir, fileName)
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        imageUri = Uri.fromFile(photoFile)
                        onImageSaved(imageUri!!)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(
                            context,
                            "Error al tomar foto: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )}
            catch (e:Exception) {
                Log.e("CameraX", "Excepción al capturar imagen", e)
            }
        }) {
            Text("Tomar Foto")
        }

        Button(onClick = {
            imageUri?.let {
                Toast.makeText(context, "Foto guardada: ${it.path}", Toast.LENGTH_SHORT).show()
                onImageSaved(it) // Puedes usar este callback para pasarlo entre pantallas
            } ?: Toast.makeText(context, "Primero toma una foto", Toast.LENGTH_SHORT).show()
        }) {
            Text("Guardar Foto")
        }
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


