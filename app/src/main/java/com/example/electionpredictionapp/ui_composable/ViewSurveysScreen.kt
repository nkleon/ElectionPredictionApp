package com.example.electionpredictionapp.ui_composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.electionpredictionapp.objects.Position
import com.example.electionpredictionapp.objects.SurveyDisplayItem
import com.example.electionpredictionapp.ui.theme.ElectionPredictionAppTheme

@Preview(showBackground = true)
@Composable
private fun PreviewViewSurveysScreen() {
    val allSurveys = listOf(
        SurveyDisplayItem(
            name = "Survey1",
            positionName = "President",
            openStatus = "Closed"
        ),
        SurveyDisplayItem(
            name = "Survey2",
            positionName = "Secretary",
            openStatus = "Open"
        )
    )
    val allPositions = listOf(
        Position(name = "Position1"),
        Position(name = "Position2"),
        Position(name = "Position3"),
    )
    ElectionPredictionAppTheme {
        ViewSurveysScreen(
            allSurveys,
            allPositions,
            { _,_,_->},
            {_,_,_,_->},
            {}
        )
    }
}

@Composable
fun ViewSurveysScreen(
    surveys: List<SurveyDisplayItem>,
    positions: List<Position> = emptyList(),
    onAddSurvey: (String, String, String) -> Unit,
    onEditSurvey: (String, String, String, String) -> Unit,
    onBackClick: () -> Unit
) {
    var showPopupDialog by remember { mutableStateOf(false) }
    var surveyToEdit by remember { mutableStateOf<SurveyDisplayItem?>(null) }

    Scaffold(
        topBar = { MenuTopBar(title = "Surveys", onBackClick = onBackClick) }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding).padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(
                    onClick = {
                        surveyToEdit = null
                        showPopupDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add survey"
                    )
                }
            }

            if (showPopupDialog) {
                AddEditSurveyPopup(
                    initialSurvey = surveyToEdit,
                    positions = positions,
                    onDismissDialog = { showPopupDialog = false },
                    onSaveSurvey = { name, positionName, status ->
                        if (surveyToEdit == null) {
                            onAddSurvey(name, positionName, status)
                        } else {
                            onEditSurvey(surveyToEdit!!.id, name, positionName, status)
                        }
                        showPopupDialog = false
                    }
                )
            }

            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableCell("Name", 160.dp, isHeader = true)
                        TableCell("Position", 120.dp, isHeader = true)
                        TableCell("No. of responses", 150.dp, isHeader = true)
                        TableCell("Status", 120.dp, isHeader = true)
                        TableCell("Action", 100.dp, isHeader = true)
                    }

                    surveys.forEach { survey ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableCell(survey.name, 160.dp)
                            TableCell(survey.positionName, 120.dp)
                            TableCell(survey.responseCount.toString(), 150.dp)
                            TableCell(survey.openStatus, 120.dp)

                            Box(Modifier.width(100.dp), contentAlignment = Alignment.CenterStart) {
                                TextButton(
                                    onClick = {
                                        surveyToEdit = survey
                                        showPopupDialog = true
                                    }
                                ) {
                                    Text("Edit")
                                }
                            }
                        }

                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun TableCell(text: String, width: Dp, isHeader: Boolean = false) {
    Text(
        text,
        Modifier.width(width).padding(horizontal = 8.dp),
        maxLines = 1,
        style = if (isHeader) {
            MaterialTheme.typography.labelLarge
        } else {
            MaterialTheme.typography.bodyMedium
        }
    )
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
private fun AddEditSurveyPopup(
    initialSurvey: SurveyDisplayItem?, positions: List<Position>,
    onDismissDialog: () -> Unit,
    onSaveSurvey: (name: String, positionName: String, openStatus: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusOptions = listOf("Open", "Closed")

    var name by remember { mutableStateOf(initialSurvey?.name ?: "") }
    var selectedPosition by remember {
        mutableStateOf(
            positions.find { it.name == initialSurvey?.positionName } ?: positions.firstOrNull()
        )
    }
    var status by remember {
        mutableStateOf(
            initialSurvey?.openStatus?.takeIf { it in statusOptions } ?: statusOptions.first()
        )
    }

    var positionMenuExpanded by remember { mutableStateOf(false) }
    var statusMenuExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismissDialog) {
        Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(
                Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    if (initialSurvey == null) "Add survey" else "Edit survey",
                    Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Name") }, singleLine = true,
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
                        },
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

                Box(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select status"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(Modifier.matchParentSize().clickable { statusMenuExpanded = true })

                    DropdownMenu(
                        expanded = statusMenuExpanded,
                        onDismissRequest = { statusMenuExpanded = false }
                    ) {
                        statusOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    status = option
                                    statusMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismissDialog) {
                        Text("Cancel")
                    }

                    TextButton(
                        enabled = name.isNotBlank() && selectedPosition != null,
                        onClick = {
                            selectedPosition?.let { pos ->
                                onSaveSurvey(name.trim(), pos.name, status)
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
