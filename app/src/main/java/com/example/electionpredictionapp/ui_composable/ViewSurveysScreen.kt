package com.example.electionpredictionapp.ui_composable

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.electionpredictionapp.objects.Survey

@Preview(showBackground = true)
@Composable
private fun PreviewViewSurveysScreen() {
    ViewSurveysScreen(emptyList(), {}, {_->}, {})
}

@Composable
fun ViewSurveysScreen(
    surveys: List<Survey>,
    onAddClick: () -> Unit,
    onEditClick: (Survey) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = { MenuTopBar(title = "Surveys", onBackClick = onBackClick) }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding).padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onAddClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add survey"
                    )
                }
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
                            TableCell("survey.position", 120.dp)
                            TableCell("survey.responseCount".toString(), 150.dp)
                            TableCell(survey.status.toString(), 120.dp)

                            Box(Modifier.width(100.dp), contentAlignment = Alignment.CenterStart) {
                                TextButton(onClick = { onEditClick(survey) }) {
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
            Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 4.dp),
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
