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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
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
import com.example.electionpredictionapp.ui.theme.ElectionPredictionAppTheme

@Preview(showBackground = true)
@Composable
private fun PreviewViewPositionsScreen() {
    val positions = listOf(
        Position(name = "Position1"),
        Position(name = "Position2"),
        Position(name = "Position3"),
    )
    ElectionPredictionAppTheme {
        ViewPositionsScreen(positions, {}, {_,_->}, {})
    }
}

@Composable
fun ViewPositionsScreen(
    positions: List<Position>, onAddPosition: (String) -> Unit,
    onEditPosition: (String, String) -> Unit, onBackClick: () -> Unit
) {
    var showPopupDialog by remember { mutableStateOf(false) }
    var positionToEdit by remember { mutableStateOf<Position?>(null) }

    Scaffold(
        topBar = { MenuTopBar(title = "Positions", onBackClick = onBackClick) }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding).padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(
                    onClick = {
                        positionToEdit = null
                        showPopupDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add position"
                    )
                }
            }

            if (showPopupDialog) {
                AddEditPositionPopup(
                    positionToEdit?.name ?: "",
                    { showPopupDialog = false },
                    { newName ->
                        if (positionToEdit == null) {
                            onAddPosition(newName)
                        } else {
                            onEditPosition(positionToEdit!!.id, newName)
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
                        TableCell("Name", 250.dp, isHeader = true)
                        TableCell("Action", 100.dp, isHeader = true)
                    }

                    positions.forEach { position ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableCell(position.name, 250.dp)

                            Box(Modifier.width(100.dp), contentAlignment = Alignment.CenterStart) {
                                TextButton(onClick = {
                                    positionToEdit = position
                                    showPopupDialog = true
                                }) {
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
            Modifier.statusBarsPadding().fillMaxWidth()
                .height(64.dp).padding(horizontal = 4.dp),
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
private fun AddEditPositionPopup(
    initialName: String, onDismissDialog: () -> Unit,
    onSaveName: (String) -> Unit, modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(initialName) }

    Dialog(onDismissRequest = onDismissDialog) {
        Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(
                Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Position name",
                    Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismissDialog) {
                        Text("Cancel")
                    }

                    TextButton(
                        onClick = {
                            onSaveName(name)
                            onDismissDialog()
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}