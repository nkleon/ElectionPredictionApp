package com.example.electionpredictionapp.ui_composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.electionpredictionapp.dataclasses.CandidacyDisplayItem
import com.example.electionpredictionapp.dataclasses.Prediction
import com.example.electionpredictionapp.ui.theme.ElectionPredictionAppTheme
import java.util.Locale

@Preview(showBackground = true, uiMode = 32)
@Composable
private fun PreviewViewPredictionsScreen() {
    val sampleCandidates = listOf(
        CandidacyDisplayItem(
            id = "1",
            firstName = "Amina",
            lastName = "Kamau",
            positionName = "President",
            createdTimestamp = "2026-11-01T09:00:00Z"
        ),
        CandidacyDisplayItem(
            id = "2",
            firstName = "David",
            lastName = "Ochieng",
            positionName = "President",
            createdTimestamp = "2026-11-02T10:00:00Z"
        ),
        CandidacyDisplayItem(
            id = "3",
            firstName = "Grace",
            lastName = "Wanjiku",
            positionName = "Secretary",
            createdTimestamp = "2026-11-01T11:00:00Z"
        ),
        CandidacyDisplayItem(
            id = "4",
            firstName = "Brian",
            lastName = "Mutua",
            positionName = "Secretary",
            createdTimestamp = "2026-11-03T14:00:00Z"
        )
    )
    val samplePredictions = listOf(
        Prediction(
            id = "President",
            shares = mapOf(
                1 to 0.44,
                2 to 0.31,
                7 to 0.05,
                8 to 0.12,
                9 to 0.08
            )
        ),
        Prediction(
            id = "Secretary",
            shares = mapOf(
                1 to 0.52,
                2 to 0.25,
                7 to 0.03,
                8 to 0.14,
                9 to 0.06
            )
        )
    )
    ElectionPredictionAppTheme {
        ViewPredictionsScreen(
            candidates = sampleCandidates,
            predictions = samplePredictions,
            onBackClick = {}
        )
    }
}

@Composable
fun ViewPredictionsScreen(
    candidates: List<CandidacyDisplayItem>,
    predictions: List<Prediction>,
    onBackClick: () -> Unit
) {
    val nonCandidateKeyMap = remember {
        listOf(
            7 to "Other",
            8 to "I'm not sure",
            9 to "No one"
        )
    }
    val predictionsByPosition = remember(predictions) {
        predictions.associateBy { it.id }
    }

    val groupedCandidates = remember(candidates) {
        candidates.groupBy { it.positionName.ifBlank { "General" } }
    }

    val positions = remember(groupedCandidates, predictionsByPosition) {
        (groupedCandidates.keys + predictionsByPosition.keys).distinct()
    }

    Scaffold(
        topBar = {
            MenuTopBar("Predicted Vote Shares", onBackClick)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = positions,
                key = { it }
            ) { position ->
                val shareMap = predictionsByPosition[position]?.shares.orEmpty()
                val indexedCandidates = groupedCandidates[position]
                    .orEmpty()
                    .sortedBy { it.createdTimestamp }
                    .mapIndexed { index, candidate ->
                        val slotKey = index + 1
                        candidate to (shareMap[slotKey]?.toFloat() ?: 0f)
                    }
                    .sortedByDescending { (_, share) -> share }

                val candidateSharesById = indexedCandidates.associate { (candidate, share) ->
                    candidate.id to share
                }

                val nonCandidateShares = nonCandidateKeyMap.mapNotNull { (key, label) ->
                    shareMap[key]?.let { label to it.toFloat() }
                }

                PositionProportionCard(
                    positionName = position,
                    candidates = indexedCandidates.map { it.first },
                    candidateSharesById = candidateSharesById,
                    nonCandidateShares = nonCandidateShares
                )
            }
        }
    }
}

@Composable
private fun PositionProportionCard(
    positionName: String,
    candidates: List<CandidacyDisplayItem>,
    candidateSharesById: Map<String, Float>,
    nonCandidateShares: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = positionName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            candidates.forEach { candidate ->
                val fullName = listOf(candidate.firstName.trim(), candidate.lastName.trim())
                    .filter { it.isNotEmpty() }.joinToString(" ")
                    .ifEmpty { "Unnamed Candidate" }

                ProportionRow(
                    label = fullName,
                    share = candidateSharesById[candidate.id] ?: 0f,
                    isNonCandidate = false
                )
            }

            if (candidates.isNotEmpty() && nonCandidateShares.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }

            nonCandidateShares.forEach { (label, share) ->
                ProportionRow(
                    label = label,
                    share = share,
                    isNonCandidate = true
                )
            }
        }
    }
}

@Composable
private fun ProportionRow(
    label: String,
    share: Float,
    isNonCandidate: Boolean,
    modifier: Modifier = Modifier
) {
    val clampedShare = share.coerceIn(0f, 1f)
    val percentageText = String.format(Locale.getDefault(), "%.1f%%", clampedShare * 100f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isNonCandidate) FontWeight.Normal else FontWeight.Medium,
                color = if (isNonCandidate) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = percentageText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        LinearProgressIndicator(
            progress = { clampedShare },
            modifier = Modifier.fillMaxWidth().height(8.dp)
                .clip(MaterialTheme.shapes.small),
            color = if (isNonCandidate) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.primary
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
