package com.gtreb.cleopattesmanager

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gtreb.cleopattesmanager.data.repository.MockAppRepository
import com.gtreb.cleopattesmanager.theme.AppTheme
import com.gtreb.cleopattesmanager.ui.screens.PlanningScreen
import com.gtreb.cleopattesmanager.ui.viewmodel.PlanningViewModel

@Composable
internal fun App() = AppTheme {
    val repository = remember { MockAppRepository() }
    val planningViewModel = remember { PlanningViewModel(repository) }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        PlanningScreen(
            viewModel = planningViewModel,
            onNavigateToAnimal = { /* TODO: Navigation to animal details */ }
        )
    }
}
