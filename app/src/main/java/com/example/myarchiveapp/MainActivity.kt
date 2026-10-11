package com.example.myarchiveapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.myarchiveapp.repository.AppRepository
import com.example.myarchiveapp.ui.MainScreen
import com.example.myarchiveapp.ui.theme.MyArchiveAppTheme
import com.example.myarchiveapp.viewmodel.MainViewModel

private data object MainRoute

class MainActivity : ComponentActivity() {

    private lateinit var archiveManager: ArchiveManager
    private var mainViewModel: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        archiveManager = ArchiveManager(this)
        archiveManager.onArchiveSuccess = { _ ->
            mainViewModel?.loadApps()
        }
        archiveManager.register()

        val repository = AppRepository(this)

        setContent {
            MyArchiveAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val backStack = remember { mutableStateListOf<Any>(MainRoute) }

                    NavDisplay(
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        entryProvider = { key ->
                            when (key) {
                                is MainRoute -> NavEntry(key) {
                                    val factory = viewModelFactory {
                                        initializer {
                                            MainViewModel(repository)
                                        }
                                    }
                                    val vm: MainViewModel = viewModel(factory = factory)
                                    mainViewModel = vm
                                    
                                    MainScreen(
                                        viewModel = vm,
                                        onArchiveClicked = { selectedPackages ->
                                            if (Build.VERSION.SDK_INT >= 35) {
                                                selectedPackages.forEach { packageName ->
                                                    archiveManager.requestArchive(packageName)
                                                }
                                            }
                                        }
                                    )
                                }
                                else -> error("Unknown route: $key")
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        archiveManager.unregister()
    }
}
