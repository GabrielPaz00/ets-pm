package com.example.e2.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.camera.core.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavegationApp()
                }
            }
        }
    }
}


@Composable
fun PantallaDetalle(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxSize().padding(top = 24.dp)
        , horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Pantalla de Detalle")
        Button(onClick = { navController.navigate("foto") }) {
            Text("Volver")
        }
    }
}


@Composable
fun NavegationApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "detalle") {
        composable("foto") { MainScreenPhoto(navController) }
        composable("imagen") { MainScreenImage(navController) }
        composable("detalle") { PantallaDetalle(navController) }
    }
}