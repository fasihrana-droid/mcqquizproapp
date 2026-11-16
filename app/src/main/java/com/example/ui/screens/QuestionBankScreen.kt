package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.DifficultyFilter
import com.example.data.model.QuizQuestion
import com.example.ui.McqUiState
import com.example.ui.McqViewModel
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.McqCard
import com.example.ui.components.McqChip
import com.example.ui.components.McqTextField
import com.example.ui.components.QuestionBankCard
import com.example.ui.components.QuestionEditorDialog
import com.example.ui.components.SmallActionButton
import com.example.ui.theme.LocalMcqColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionBankScreen(
  state: McqUiState,
  viewModel: McqViewModel,
  modifier: Modifier = Modifier,
) {
  val colors = LocalMcqColors.current
  val filter = state.bankFilter

  var showAddDialog by remember { mutableStateOf(false) }
  var editingQuestion by remember { mutableStateOf<QuizQuestion?>(null) }
  var deletingQuestion by remember { mutableStateOf<QuizQuestion?>(null) }
  var deletingSubject by remember { mutableStateOf<String?>(null) }

  val filteredQuestions = remember(state.bank, filter) {
    val qLower = filter.searchQuery.trim().lowercase()
    state.bank.filter { item ->
      val subjectMatch = filter.subjectFilter == "All" || item.subject == filter.subjectFilter
      val diffMatch = filter.difficultyFilter.matches(item.difficulty)
      val searchMatch = qLower.isEmpty() ||
        item.question.lowercase().contains(qLower) ||
        item.subject.lowercase().contains(qLower) ||
        item.options.any { it.lowercase().contains(qLower) }
      subjectMatch && diffMatch && searchMatch
    }
  }

  if (showAddDialog) {
    QuestionEditorDialog(
      defaultSubject = if (filter.subjectFilter != "All") filter.subjectFilter else "Pak Studies",
      onSave = { subj, diff, qText, opts, correct, exp ->
        viewModel.addQuestion(subj, diff, qText, opts, correct, exp)
        showAddDialog = false
      },
      onDismiss = { showAddDialog = false },
    )
  }

  editingQuestion?.let { target ->
    QuestionEditorDialog(
      initialQuestion = target,
      onSave = { subj, diff, qText, opts, correct, exp ->
        viewModel.updateQuestion(
          target.copy(
            subject = subj.trim().ifEmpty { "General" },
            difficulty = diff,
            question = qText.trim(),
            options = opts.map { it.trim() },
            correctIndex = correct,
            explanation = exp.trim(),
          )
        )
        editingQuestion = null
      },
      onDismiss = { editingQuestion = null },
    )
  }

  deletingQuestion?.let { target ->
    ConfirmActionDialog(
      title = "Delete MCQ?",
      message = "Delete this MCQ from ${target.subject}?",
      confirmLabel = "Delete",
      isDestructive = true,
      onConfirm = {
        viewModel.deleteQuestion(target.id)
        deletingQuestion = null
      },
      onDismiss = { deletingQuestion = null },
    )
  }

  deletingSubject?.let { subj ->
    val count = state.bank.count { it.subject == subj }
    ConfirmActionDialog(
      title = "Delete Subject?",
      message = "Delete subject \"$subj\" and all $count of its MCQs? This cannot be undone.",
      confirmLabel = "Delete Subject",
      isDestructive = true,
      onConfirm = {
        viewModel.deleteSubject(subj)
        deletingSubject = null
      },
      onDismiss = { deletingSubject = null },
    )
  }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = stringResource(R.string.bank_heading),
          style = MaterialTheme.typography.headlineLarge,
          color = colors.textPrimary,
        )
        Button(
          onClick = { showAddDialog = true },
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = colors.emerald,
            contentColor = colors.emeraldOnPrimary,
          ),
          modifier = Modifier.testTag("add_mcq_button"),
        ) {
          Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Add MCQ", fontWeight = FontWeight.Bold)
        }
      }
    }

    item {
      McqTextField(
        value = filter.searchQuery,
        onValueChange = { viewModel.updateBankSearch(it) },
        placeholder = stringResource(R.string.search_placeholder),
        leadingIcon = {
          Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = colors.textMuted,
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("bank_search_input"),
      )
    }

    item {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        McqChip(
          label = "All Subjects",
          selected = filter.subjectFilter == "All",
          onClick = { viewModel.updateBankSubjectFilter("All") },
        )
        state.subjects.forEach { subj ->
          McqChip(
            label = subj,
            selected = filter.subjectFilter == subj,
            onClick = { viewModel.updateBankSubjectFilter(subj) },
          )
        }
        DifficultyFilter.entries.forEach { diff ->
          McqChip(
            label = if (diff == DifficultyFilter.ALL) "All Difficulties" else diff.label,
            selected = filter.difficultyFilter == diff,
            onClick = { viewModel.updateBankDifficultyFilter(diff) },
          )
        }
      }
    }

    if (state.subjects.isNotEmpty()) {
      item {
        McqCard(contentPadding = PaddingValues(12.dp)) {
          Text(
            text = stringResource(R.string.manage_subjects),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textMuted,
            fontWeight = FontWeight.Bold,
          )
          Spacer(modifier = Modifier.height(6.dp))
          state.subjects.forEach { subj ->
            val count = state.bank.count { it.subject == subj }
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = subj, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "($count MCQs)",
                  style = MaterialTheme.typography.bodyMedium,
                  color = colors.textMuted,
                )
              }
              SmallActionButton(
                onClick = { deletingSubject = subj },
                textColor = colors.red,
                modifier = Modifier.testTag("delete_subject_$subj"),
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    tint = colors.red,
                    modifier = Modifier.size(16.dp),
                  )
                  Text(
                    text = stringResource(R.string.delete_subject),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.red,
                    fontWeight = FontWeight.SemiBold,
                  )
                }
              }
            }
          }
        }
      }
    }

    item {
      Text(
        text = "${filteredQuestions.size} of ${state.bank.size} MCQs shown",
        style = MaterialTheme.typography.bodyMedium,
        color = colors.textMuted,
      )
    }

    if (filteredQuestions.isEmpty()) {
      item {
        McqCard {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
              text = "No matching questions found.",
              style = MaterialTheme.typography.bodyLarge,
              color = colors.textMuted,
            )
          }
        }
      }
    } else {
      items(filteredQuestions, key = { it.id }) { q ->
        QuestionBankCard(
          question = q,
          isSaved = q.id in state.savedIds,
          onToggleSave = { viewModel.toggleSaved(q.id) },
          onEdit = { editingQuestion = q },
          onDelete = { deletingQuestion = q },
        )
      }
    }
  }
}
