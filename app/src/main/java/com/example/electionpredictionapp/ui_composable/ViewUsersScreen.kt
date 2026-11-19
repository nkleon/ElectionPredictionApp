package com.example.electionpredictionapp.ui_composable

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.electionpredictionapp.objects.User
import com.example.electionpredictionapp.ui.theme.ElectionPredictionAppTheme

@Preview(showBackground = true)
@Composable
private fun PreviewViewUsersScreen() {
    val allUsers = listOf(
        User(
            "", "User 1", "Apple", "1-1-2001",
            "Male", "Candidate", "u1apple@example.edu"
        ),
        User(
            "", "User 2", "Berry", "2-2-2002",
            "Female", "Candidate", "u2berry@example.edu"
        ),
        User(
            "", "User 3", "Cabbage", "3-3-2003",
            "Male", "Supporter", "u3cabbage@example.edu"
        ),
        User(
            "", "User 4", "Date", "4-4-2004",
            "Female", "Supporter", "u4date@example.edu"
        )
    )
    ElectionPredictionAppTheme {
        ViewUsersScreen(
            allUsers,
            {}
        )
    }
}

@Composable
fun ViewUsersScreen(
    users: List<User>,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = { MenuTopBar(title = "User Management", onBackClick = onBackClick) }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding).padding(20.dp)) {

            Text(
                "All users", Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(24.dp))

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
                        TableCell("First Name", 120.dp, isHeader = true)
                        TableCell("Last Name", 120.dp, isHeader = true)
                        TableCell("Date of Birth", 100.dp, isHeader = true)
                        TableCell("Gender", 70.dp, isHeader = true)
                        TableCell("Role", 80.dp, isHeader = true)
                        TableCell("Email", 250.dp, isHeader = true)
                        TableCell("Action", width = 60.dp, isHeader = true)
                    }

                    users.forEach { user ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableCell(user.firstName, 120.dp)
                            TableCell(user.lastName, 120.dp)
                            TableCell(user.dateOfBirth, 100.dp)
                            TableCell(user.gender, 70.dp)
                            TableCell(user.accountRole, 80.dp)
                            TableCell(user.email, 250.dp)

                            Box(Modifier.width(60.dp), contentAlignment = Alignment.CenterStart) {
                                TextButton(onClick = {  }) {
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
