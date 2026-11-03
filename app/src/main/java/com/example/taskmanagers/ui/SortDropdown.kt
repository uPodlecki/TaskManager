package com.example.taskmanagers.ui

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.taskmanagers.R
import com.example.taskmanagers.viewmodel.SortOption


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortDropdown(currentSort: SortOption, onSortSelected: (SortOption) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    val currentSortText = when(currentSort) {
        SortOption.DATE_CREATED -> stringResource(R.string.sort_date_created)
        SortOption.DUE_DATE -> stringResource(R.string.sort_due_date)
        SortOption.PRIORITY -> stringResource(R.string.sort_priority)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = currentSortText,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.sort_by)) },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SortOption.entries.forEach { option ->
                val optionText = when(option) {
                    SortOption.DATE_CREATED -> stringResource(R.string.sort_date_created)
                    SortOption.DUE_DATE -> stringResource(R.string.sort_due_date)
                    SortOption.PRIORITY -> stringResource(R.string.sort_priority)
                }
                DropdownMenuItem(
                    text = { Text(optionText) },
                    onClick = {
                        onSortSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}