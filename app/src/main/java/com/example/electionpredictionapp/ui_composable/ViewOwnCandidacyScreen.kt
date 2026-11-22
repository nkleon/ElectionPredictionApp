package com.example.electionpredictionapp.ui_composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.electionpredictionapp.objects.Candidacy
import com.example.electionpredictionapp.objects.Position
import com.example.electionpredictionapp.ui.theme.ElectionPredictionAppTheme

@Preview(showBackground = true)
@Composable
private fun PreviewViewOwnCandidacyScreen() {
    val sampleCandidacy = Candidacy(
        positionName = "President",
        manifesto = "Build resilient, accessible software that respects user attention, " +
                "favors clarity over cleverness, and scales cleanly across teams."
    )
    val samplePositions = listOf(
        Position(name = "Position1"),
        Position(name = "Position2"),
        Position(name = "Position3"),
    )
    ElectionPredictionAppTheme {
        ViewOwnCandidacyScreen(
            sampleCandidacy,
            samplePositions,
            {_,_,_->},
            {}
        )
    }
}

@Composable
fun ViewOwnCandidacyScreen(
    candidacy: Candidacy,
    positions: List<Position>,
    onEditCandidacy: (String, String, String) -> Unit, onBackClick: () -> Unit
) {
    var showPopupDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { MenuTopBar("Your Candidacy", onBackClick) }
    ) { innerPadding ->
        Column(
            Modifier.padding(innerPadding).padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Box(Modifier.fillMaxWidth().padding(16.dp)) {
                    IconButton(
                        { showPopupDialog = true },
                        Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit position and manifesto",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (showPopupDialog) {
                        EditCandidacyPopup(
                            candidacy,
                            positions,
                            { showPopupDialog = false},
                            { positionName, manifesto ->
                                onEditCandidacy(candidacy.id, positionName, manifesto)
                                showPopupDialog = false
                            }
                        )
                    }

                    Column(
                        Modifier.fillMaxWidth().padding(end = 48.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Position",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = candidacy.positionName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Manifesto",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = candidacy.manifesto,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
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

@Composable
private fun EditCandidacyPopup(
    candidacy: Candidacy, positions: List<Position>,
    onDismissDialog: () -> Unit,
    onSaveManifesto: (positionName: String, manifesto: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPosition by remember {
        mutableStateOf(
            positions.find { it.name == candidacy.positionName } ?: positions.firstOrNull()
        )
    }
    var manifesto by remember { mutableStateOf(candidacy.manifesto) }
    var positionMenuExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismissDialog) {
        Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(
                Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit manifesto",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineSmall
                )

                Box(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedPosition?.name ?: "",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        label = { Text("Position") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select position"
                            )
                        }
                    )

                    Box(
                        Modifier.matchParentSize().clickable(enabled = positions.isNotEmpty()) {
                                positionMenuExpanded = true
                            }
                    )

                    DropdownMenu(
                        expanded = positionMenuExpanded,
                        onDismissRequest = { positionMenuExpanded = false }
                    ) {
                        positions.forEach { position ->
                            DropdownMenuItem(
                                text = { Text(position.name) },
                                onClick = {
                                    selectedPosition = position
                                    positionMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = manifesto,
                    onValueChange = { manifesto = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Manifesto") },
                    singleLine = false,
                    minLines = 3,
                    maxLines = 6
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismissDialog) {
                        Text("Cancel")
                    }

                    TextButton(
                        enabled = selectedPosition != null && manifesto.isNotBlank(),
                        onClick = {
                            selectedPosition?.let { pos ->
                                onSaveManifesto(pos.name, manifesto.trim())
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
