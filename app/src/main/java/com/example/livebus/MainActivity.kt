// package com.example.livebus

// import android.os.Bundle
// import androidx.activity.ComponentActivity
// import androidx.activity.compose.setContent
// import androidx.activity.enableEdgeToEdge
// import androidx.compose.foundation.layout.fillMaxSize
// import androidx.compose.foundation.layout.padding
// import androidx.compose.material3.Scaffold
// import androidx.compose.runtime.Composable
// import androidx.compose.ui.Modifier
// import androidx.compose.ui.tooling.preview.Preview
// import androidx.lifecycle.viewmodel.compose.viewModel
// import androidx.navigation.compose.NavHost
// import androidx.navigation.compose.composable
// import androidx.navigation.compose.rememberNavController
// import com.example.livebus.ui.theme.LiveBusTheme

// class MainActivity : ComponentActivity() {
//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)
//         enableEdgeToEdge()
//         setContent {
//             LiveBusTheme {
//                 Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                     AppNavigation(modifier = Modifier.padding(innerPadding))
//                 }
//             }
//         }
//     }
// }

// @Composable
// fun AppNavigation(modifier: Modifier = Modifier) {
//     val navController = rememberNavController()
//     val busViewModel: BusViewModel = viewModel()

//     NavHost(navController = navController, startDestination = "busTracking", modifier = modifier) {
//         composable("busTracking") {
//             BusTrackingScreen(viewModel = busViewModel)
//         }
//         // Add other destinations here
//     }
// }

// @Preview(showBackground = true)
// @Composable
// fun DefaultPreview() {
//     LiveBusTheme {
//         AppNavigation()
//     }
// }









package com.example.livebus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // This launches your entire 4-screen flow
                LiveBusAppNavigation()
            }
        }
    }
}
