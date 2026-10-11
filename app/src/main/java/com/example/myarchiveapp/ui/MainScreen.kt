package com.example.myarchiveapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.myarchiveapp.model.AppModel
import com.example.myarchiveapp.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onArchiveClicked: (Set<String>) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Archive App") },
                actions = {
                    if (uiState.selectedPackages.isNotEmpty()) {
                        Text("${uiState.selectedPackages.size} selected", modifier = Modifier.padding(end = 16.dp))
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.selectedPackages.isNotEmpty()) {
                FloatingActionButton(onClick = { 
                    onArchiveClicked(uiState.selectedPackages) 
                    viewModel.clearSelection()
                }) {
                    Icon(Icons.Filled.Archive, contentDescription = "Archive Selected")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.apps.isEmpty()) {
                Text(
                    text = "No apps found.",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.apps, key = { it.packageName }) { app ->
                        val isSelected = uiState.selectedPackages.contains(app.packageName)
                        AppListItem(
                            app = app,
                            isSelected = isSelected,
                            onToggleSelect = { viewModel.toggleSelection(app.packageName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppListItem(
    app: AppModel,
    isSelected: Boolean,
    onToggleSelect: () -> Unit
) {
    ListItem(
        headlineContent = { Text(app.appName) },
        supportingContent = { Text(app.packageName) },
        leadingContent = {
            if (app.icon != null) {
                Image(
                    bitmap = app.icon.toBitmap().asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Box(modifier = Modifier.size(48.dp))
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (app.isArchived) {
                    Text("Archived", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(end = 8.dp))
                }
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() }
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSelect() }
    )
}
