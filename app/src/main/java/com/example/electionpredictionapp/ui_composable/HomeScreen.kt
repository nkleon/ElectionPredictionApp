package com.example.electionpredictionapp.ui_composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import com.example.electionpredictionapp.dataclasses.SurveyDisplayItem
import com.example.electionpredictionapp.ui.theme.ElectionPredictionAppTheme

@Preview(showBackground = true)
@Composable
private fun PreviewHomeScreen() {
    val sampleOpenSurveys = listOf(
        SurveyDisplayItem(name = "Survey 1"),
        SurveyDisplayItem(name = "Survey 2"),
        SurveyDisplayItem(name = "Survey 3")
    )
    ElectionPredictionAppTheme {
        HomeScreen(
            userFirstName = "John",
            userAccountRole = "Supporter",
            openSurveys = sampleOpenSurveys,
            onUserProfile = {},
            onAboutApp = {},
            onLogout = {}
        )
    }
}

@Composable
fun HomeScreen(
    userFirstName: String,
    userAccountRole: String,
    onUserProfile: () -> Unit,
    onAboutApp: () -> Unit,
    onLogout: () -> Unit,
    onUsers: (() -> Unit) = {},
    onPositions: (() -> Unit) = {},
    onSurveys: (() -> Unit) = {},
    onViewOwnCandidacy: (() -> Unit) = {},
    onPredictions: (() -> Unit) = {},
    openSurveys: List<SurveyDisplayItem> = emptyList(),
    onSelectOpenSurvey: (SurveyDisplayItem) -> Unit = {},
    onViewAllCandidacies: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            MenuTopBar("Election Prediction App", onUserProfile, onAboutApp, onLogout)
        }
    ) { innerPadding ->
        Column(
            Modifier.fillMaxSize().padding(innerPadding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Welcome, ${userFirstName}", style = MaterialTheme.typography.headlineMedium)

            if (userAccountRole == "Admin") {
                AdminHomeScreenBody(onUsers, onPositions, onSurveys)
            } else if (userAccountRole == "Candidate") {
                CandidateHomeScreenBody(onViewOwnCandidacy, onPredictions)
            } else if (userAccountRole == "Supporter") {
                SupporterHomeScreenBody(openSurveys, onSelectOpenSurvey, onViewAllCandidacies)
            }
        }
    }
}

@Composable
private fun MenuTopBar(
    menuTitle: String, onUserProfile: () -> Unit, onAboutApp: () -> Unit, onLogout: () -> Unit
) {
    var hamburgerMenuExpanded by remember { mutableStateOf(false) }

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
            Text(menuTitle, style = MaterialTheme.typography.titleLarge)

            Box {
                IconButton(onClick = { hamburgerMenuExpanded = true }) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Open Menu")
                }

                DropdownMenu(
                    expanded = hamburgerMenuExpanded, onDismissRequest = { hamburgerMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Your profile") },
                        onClick = {
                            hamburgerMenuExpanded = false
                            onUserProfile()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("About the app") },
                        onClick = {
                            hamburgerMenuExpanded = false
                            onAboutApp()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Logout") },
                        onClick = {
                            hamburgerMenuExpanded = false
                            onLogout()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CandidateHomeScreenBody(
    onViewOwnCandidacy: () -> Unit,
    onPredictions: () -> Unit,
) {
    Text("Candidates' Portal", style = MaterialTheme.typography.titleLarge)

    NavigationTile(
        title = "View your candidacy",
        onClick = onViewOwnCandidacy
    )
    NavigationTile(
        title = "View current predictions",
        onClick = onPredictions
    )
}

@Composable
private fun AdminHomeScreenBody(
    onUsers: () -> Unit,
    onPositions: () -> Unit,
    onSurveys: () -> Unit,
) {
    Text("Admin Panel", style = MaterialTheme.typography.titleLarge)

    NavigationTile(
        title = "Users",
        onClick = onUsers
    )
    NavigationTile(
        title = "Positions",
        onClick = onPositions
    )
    NavigationTile(
        title = "Surveys",
        onClick = onSurveys
    )
}

@Composable
private fun NavigationTile(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick, modifier.fillMaxWidth().height(100.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(title, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun SupporterHomeScreenBody(
    surveys: List<SurveyDisplayItem>,
    onSelectOpenSurvey: (SurveyDisplayItem) -> Unit,
    onViewAllCandidacies: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("Open Surveys", style = MaterialTheme.typography.titleLarge)

        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Name", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
            Text("Action", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
        }

        HorizontalDivider()

        if (surveys.isNotEmpty()) {
            surveys.forEach { survey ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text(survey.name, modifier = Modifier.weight(1f))
                    Text("Fill",
                        Modifier.weight(1f).padding(start = 8.dp)
                            .clickable { onSelectOpenSurvey(survey) },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            Text(
                "No open surveys",
            )
        }
    }

    HorizontalDivider()

    Spacer(Modifier.height(10.dp))

    Surface(
        onClick = onViewAllCandidacies, Modifier.fillMaxWidth().height(72.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(
            1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text("Candidate profiles", style = MaterialTheme.typography.titleMedium)
        }
    }
}
