package com.example.electionpredictionapp.ui_composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.electionpredictionapp.dataclasses.CandidacyDisplayItem
import com.example.electionpredictionapp.ui.theme.ElectionPredictionAppTheme

@Preview(showBackground = true)
@Composable
private fun PreviewViewAllCandidaciesScreen() {
    val sampleCandidacies = listOf(
        CandidacyDisplayItem(
            id = "1",
            firstName = "Amara",
            lastName = "Okafor",
            positionName = "President",
            manifesto = "Transparent budget reporting every semester, " +
                    "expanded student housing grants, and weekly open town halls."
        ),
        CandidacyDisplayItem(
            id = "2",
            firstName = "Liam",
            lastName = "Chen",
            positionName = "President",
            manifesto = "24/7 library study access during exam weeks, " +
                    "faster transit shuttle frequencies, and mental health peer support."
        ),
        CandidacyDisplayItem(
            id = "3",
            firstName = "Sofia",
            lastName = "Reyes",
            positionName = "Secretary",
            manifesto = "Publish meeting minutes within 48 hours and " +
                    "build an accessible archive of club funding decisions."
        )
    )
    ElectionPredictionAppTheme {
        ViewAllCandidaciesScreen(
            onBackClick = {},
            candidacies = sampleCandidacies
        )
    }
}

@Composable
fun ViewAllCandidaciesScreen(
    candidacies: List<CandidacyDisplayItem>,
    onBackClick: () -> Unit
) {
    val groupedByPosition = remember(candidacies) {
        candidacies.groupBy { it.positionName }
    }

    Scaffold(
        topBar = { MenuTopBar("All Candidacies & Manifestos", onBackClick) }
    ) { innerPadding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedByPosition.forEach { (positionName, candidates) ->
                item(key = "position_$positionName") {
                    PositionCandidatesGroup(
                        positionName = positionName,
                        candidates = candidates
                    )
                }
            }
        }
    }
}

@Composable
private fun PositionCandidatesGroup(
    positionName: String,
    candidates: List<CandidacyDisplayItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = positionName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            candidates.forEachIndexed { index, candidate ->
                CandidateManifestoRow(
                    candidate = candidate
                )
                if (index < candidates.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun CandidateManifestoRow(
    candidate: CandidacyDisplayItem,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember(candidate.id) { mutableStateOf(false) }
    val fullName = remember(candidate.firstName, candidate.lastName) {
        listOf(candidate.firstName, candidate.lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                .clickable(
                    role = Role.Button,
                    onClick = { isExpanded = !isExpanded }
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = fullName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Hide manifesto" else "Show manifesto",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Text(
                text = candidate.manifesto,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp, end = 8.dp)
            )
        }
    }
}

@Composable
private fun MenuTopBar(title: String, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 10.dp
    ) {
        Row(
            Modifier.statusBarsPadding().fillMaxWidth().height(64.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go back"
                )
            }
            Text(
                title, Modifier.padding(start = 12.dp),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}
