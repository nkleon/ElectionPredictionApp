package com.example.electionpredictionapp.ui_composable

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.electionpredictionapp.ui.theme.ElectionPredictionAppTheme

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun PreviewHomeScreen()
{
    ElectionPredictionAppTheme {
        HomeScreen( "John", {}, {})
    }
}

@Composable
fun HomeScreen(userFirstName: String, onYourProfile: () -> Unit, onAboutApp: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 10.dp
            ) {
                Row(
                    Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Election Prediction App", style = MaterialTheme.typography.titleLarge)

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Open Menu")
                        }

                        DropdownMenu(
                            expanded = menuExpanded, onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Your profile") },
                                onClick = {
                                    menuExpanded = false
                                    onYourProfile()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("About the app") },
                                onClick = {
                                    menuExpanded = false
                                    onAboutApp()
                                }
                            )
                        }
                    }
                }
            }
        }
    ) {
        innerPadding ->
        Column(
            Modifier.fillMaxSize().padding(innerPadding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Welcome, $userFirstName", style = MaterialTheme.typography.headlineMedium)

            Text("Activity 1", style = MaterialTheme.typography.titleLarge)

            Surface(
                Modifier.fillMaxWidth().height(180.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Placeholder")
                }
            }
        }
    }
}