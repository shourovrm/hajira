package com.rms.hazira.ui.people

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rms.hazira.domain.Person
import com.rms.hazira.ui.common.PersonAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(onAddPerson: () -> Unit, onEditPerson: (Long) -> Unit) {
    val viewModel: PeopleViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "People") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPerson) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add person")
            }
        },
    ) { innerPadding ->
        if (uiState.isLoaded && uiState.people.isEmpty()) {
            EmptyPeople(onAddPerson = onAddPerson, modifier = Modifier.padding(innerPadding))
        } else {
            PeopleList(
                people = uiState.people,
                onEditPerson = onEditPerson,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun EmptyPeople(onAddPerson: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Add the first person or service you want to track",
            style = MaterialTheme.typography.titleMedium,
        )
        Button(onClick = onAddPerson, modifier = Modifier.padding(top = 16.dp)) {
            Text(text = "Add person")
        }
    }
}

@Composable
private fun PeopleList(people: List<Person>, onEditPerson: (Long) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        // The bottom padding keeps the last row clear of the floating button.
        contentPadding = PaddingValues(bottom = 88.dp),
    ) {
        items(items = people, key = { person -> person.id }) { person ->
            PersonRow(person = person, onClick = { onEditPerson(person.id) })
            HorizontalDivider()
        }
    }
}

@Composable
private fun PersonRow(person: Person, onClick: () -> Unit) {
    val textColour = if (person.isActive) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PersonAvatar(person = person)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = person.name, style = MaterialTheme.typography.titleMedium, color = textColour)
            if (person.role.isNotBlank()) {
                Text(
                    text = person.role,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = summariseSchedule(person),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = describeRate(person),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColour,
            )
            if (!person.isActive) {
                Text(
                    text = "Stopped",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
