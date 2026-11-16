package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.PlayArrow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.McqUiState
import com.example.ui.McqViewModel
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.McqCard
import com.example.ui.components.QuestionBankCard
import com.example.ui.theme.LocalMcqColors

@Composable
fun SavedScreen(
  state: McqUiState,
  viewModel: McqViewModel,
  modifier: Modifier = Modifier,
) {
  val colors = LocalMcqColors.current
  val savedQuestions = remember(state.bank, state.savedIds) {
    val byId = state.bank.associateBy { it.id }
    state.savedIds.mapNotNull { byId[it] }
  }
  var showClearDialog by remember { mutableStateOf(false) }

  if (showClearDialog) {
    ConfirmActionDialog(
      title = "Clear Saved MCQs?",
      message = "Remove all saved bookmarks?",
      confirmLabel = "Clear All",
      isDestructive = true,
      onConfirm = {
        viewModel.clearAllSaved()
        showClearDialog = false
      },
      onDismiss = { showClearDialog = false },
    )
  }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Text(
        text = stringResource(R.string.saved_heading),
        style = MaterialTheme.typography.headlineLarge,
        color = colors.textPrimary,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = stringResource(R.string.saved_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = colors.textMuted,
      )
      Spacer(modifier = Modifier.height(14.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Button(
          onClick = { viewModel.practiceSaved() },
          enabled = savedQuestions.isNotEmpty(),
          modifier = Modifier
            .weight(1f)
            .testTag("practice_saved_button"),
          shape = RoundedCornerShape(14.dp),
          contentPadding = PaddingValues(vertical = 12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = colors.emerald,
            contentColor = colors.emeraldOnPrimary,
            disabledContainerColor = colors.emerald.copy(alpha = 0.35f),
            disabledContentColor = colors.emeraldOnPrimary.copy(alpha = 0.5f),
          ),
        ) {
          Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Practice Saved (${savedQuestions.size})", fontWeight = FontWeight.Bold)
        }

        Button(
          onClick = { showClearDialog = true },
          enabled = savedQuestions.isNotEmpty(),
          modifier = Modifier
            .weight(1f)
            .testTag("clear_saved_button"),
          shape = RoundedCornerShape(14.dp),
          contentPadding = PaddingValues(vertical = 12.dp),
          border = BorderStroke(1.dp, colors.border),
          colors = ButtonDefaults.buttonColors(
            containerColor = colors.inputSurface,
            contentColor = colors.red,
            disabledContainerColor = colors.inputSurface.copy(alpha = 0.4f),
            disabledContentColor = colors.red.copy(alpha = 0.4f),
          ),
        ) {
          Text("Clear all", fontWeight = FontWeight.SemiBold)
        }
      }
    }

    if (savedQuestions.isEmpty()) {
      item {
        McqCard {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.BookmarkBorder,
              contentDescription = null,
              tint = colors.textMuted,
              modifier = Modifier.size(36.dp),
            )
            Text(
              text = stringResource(R.string.saved_empty),
              style = MaterialTheme.typography.bodyMedium,
              color = colors.textMuted,
              textAlign = TextAlign.Center,
            )
          }
        }
      }
    } else {
      items(savedQuestions, key = { it.id }) { q ->
        QuestionBankCard(
          question = q,
          isSaved = true,
          onToggleSave = { viewModel.toggleSaved(q.id) },
        )
      }
    }
  }
}
