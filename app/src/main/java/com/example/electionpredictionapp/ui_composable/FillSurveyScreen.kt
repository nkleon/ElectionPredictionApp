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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.electionpredictionapp.objects.CandidacyDisplayItem
import com.example.electionpredictionapp.objects.Position
import com.example.electionpredictionapp.objects.SurveyResponse
import com.example.electionpredictionapp.ui.theme.ElectionPredictionAppTheme

@Preview(showBackground = true, uiMode = 32)
@Composable
private fun PreviewFillSurveyScreen() {
    val sampleCandidacies = listOf(
        CandidacyDisplayItem("", "John", "Smith", "Gov"),
        CandidacyDisplayItem("", "Jane", "Doe", "Gov"),
        CandidacyDisplayItem("", "Jack", "Cork", "Sen"),
        CandidacyDisplayItem("", "Juli", "Crock", "Sen"),
    )
    val samplePositions = listOf(Position("", "Gov"), Position("", "Sen"))
    ElectionPredictionAppTheme {
        FillSurveyScreen(
            sampleCandidacies, samplePositions, {}, { _->}
        )
    }
}

@Composable
fun FillSurveyScreen(
    candidacies: List<CandidacyDisplayItem>,
    positions: List<Position>,
    onCancel: () -> Unit,
    onSubmit: (SurveyResponse) -> Unit
) {
    val interestOptions = listOf(
        "Most of the time",
        "Some of the time",
        "Only now and then",
        "Hardly at all",
        "Don't know"
    )
    val lastElectionOptions = listOf("Yes", "No")
    val intendParticipateOptions = listOf(
        "Yes, definitely",
        "Probably",
        "No",
        "Undecided"
    )
    val approvalOptions = listOf(
        "Strongly approve",
        "Somewhat approve",
        "Somewhat disapprove",
        "Strongly disapprove",
        "Not sure"
    )
    val nonCandidateOptions = listOf(
        "Other",
        "I'm not sure",
        "No one"
    )

    var interestLevel by remember { mutableStateOf<String?>(null) }
    var pastParticipation by remember { mutableStateOf<String?>(null) }
    var willParticipate by remember { mutableStateOf<String?>(null) }
    var leadershipApproval by remember { mutableStateOf<String?>(null) }
    val preferredCandidates = remember { mutableStateMapOf<String, String>() }

    val positionsWithCandidates = remember(positions, candidacies) {
        positions.filter { position ->
            candidacies.any { it.positionName == position.name }
        }
    }
    val isFormValid by remember {
        derivedStateOf {
            interestLevel != null && pastParticipation != null &&
                    willParticipate != null && leadershipApproval != null &&
                    positionsWithCandidates.all { position ->
                        preferredCandidates[position.name] != null
                    }
        }
    }

    Scaffold(
        topBar = { MenuTopBar(title = "Fill Survey", onBackClick = onCancel) }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding).padding(20.dp)) {
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item(key = "interest") {
                    RadioQuestionSection(
                        question = "How interested are you?",
                        options = interestOptions,
                        selectedOption = interestLevel,
                        onOptionSelected = { interestLevel = it }
                    )
                }

                item(key = "last_election") {
                    RadioQuestionSection(
                        question = "Did you participate in any way in the last election?",
                        options = lastElectionOptions,
                        selectedOption = pastParticipation,
                        onOptionSelected = { pastParticipation = it }
                    )
                }

                item(key = "intend_participate") {
                    RadioQuestionSection(
                        question = "Do you intend to participate in any way in this year's process?",
                        options = intendParticipateOptions,
                        selectedOption = willParticipate,
                        onOptionSelected = { willParticipate = it }
                    )
                }

                item(key = "leadership_approval") {
                    RadioQuestionSection(
                        question = "How well do you approve of the current leadership's performance?",
                        options = approvalOptions,
                        selectedOption = leadershipApproval,
                        onOptionSelected = { leadershipApproval = it }
                    )
                }

                items(items = positions, key = { position -> position.id }) { position ->
                    val candidateNamesForPosition = remember(
                        candidacies, position.name
                    ) {
                        val realCandidates = candidacies
                            .filter { it.positionName == position.name }
                            .map { "${it.firstName} ${it.lastName}".trim() }
                        realCandidates + nonCandidateOptions
                    }
                    RadioQuestionSection(
                        question = "Who is your preferred candidate for ${position.name}?",
                        options = candidateNamesForPosition,
                        selectedOption = preferredCandidates[position.name],
                        onOptionSelected = { selectedName ->
                            preferredCandidates[position.name] = selectedName
                        }
                    )
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onCancel) {
                    Text(text = "Cancel")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    enabled = isFormValid,
                    onClick = {
                        onSubmit(
                            SurveyResponse(
                                interestLevel = interestLevel!!,
                                pastParticipation = pastParticipation!!,
                                willParticipate = willParticipate!!,
                                leadershipApproval = leadershipApproval!!,
                                preferredCandidates = preferredCandidates.toMap()
                            )
                        )
                    }
                ) {
                    Text(text = "Submit")
                }
            }
        }
    }
}

@Composable
private fun RadioQuestionSection(
    question: String,
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().selectableGroup()) {
        Text(
            text = question,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        options.forEach { option ->
            val isSelected = option == selectedOption
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    .selectable(
                        selected = isSelected,
                        onClick = { onOptionSelected(option) },
                        role = Role.RadioButton
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = null // Handled by Row's selectable modifier for better touch & accessibility
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
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
