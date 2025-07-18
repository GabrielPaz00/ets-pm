package com.example.e2.android

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
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
                    NavigationApp()
                }
            }
        }
    }
}


@Composable
fun BackButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    label: String = "Volver"
) {
    Box(modifier = Modifier
        .size(width = 160.dp, height = 80.dp)
        .padding(8.dp)) {
        Button(
            onClick = onClick,
            modifier = modifier
                .padding(16.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(label)
        }
    }

}



@Composable
fun NavigationApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "foto") {
        composable("foto") { MainScreenPhoto(navController) }
        composable("imagen") { MainScreenImage(navController) }
        composable("detalle/{uri}") { backStackEntry ->
            val uri = backStackEntry.arguments?.getString("uri")?.let { Uri.decode(it) }
            DetailScreen(uri = uri, navController)
        }
    }
}