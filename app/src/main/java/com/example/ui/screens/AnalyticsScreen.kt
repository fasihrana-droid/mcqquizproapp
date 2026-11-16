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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ExamMode
import com.example.ui.McqUiState
import com.example.ui.McqViewModel
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.McqCard
import com.example.ui.components.PassFailPill
import com.example.ui.components.SmallActionButton
import com.example.ui.theme.LocalMcqColors
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun AnalyticsScreen(
  state: McqUiState,
  viewModel: McqViewModel,
  modifier: Modifier = Modifier,
) {
  val colors = LocalMcqColors.current
  val history = state.history
  val count = history.size
  val passCount = history.count { it.passed }
  val passRate = if (count > 0) ((passCount.toDouble() / count) * 100.0).roundToInt() else 0
  val avgScore = if (count > 0) history.sumOf { it.percentage } / count else 0.0
  val bestScore = if (count > 0) history.maxOf { it.percentage } else 0.0

  var showClearDialog by remember { mutableStateOf(false) }

  if (showClearDialog) {
    ConfirmActionDialog(
      title = "Clear All History?",
      message = "Delete all recorded test attempts? This cannot be undone.",
      confirmLabel = "Clear History",
      isDestructive = true,
      onConfirm = {
        viewModel.clearAllHistory()
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
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = stringResource(R.string.analytics_heading),
          style = MaterialTheme.typography.headlineLarge,
          color = colors.textPrimary,
        )
        SmallActionButton(
          onClick = { showClearDialog = true },
          textColor = colors.textMuted,
          modifier = Modifier.testTag("clear_history_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.DeleteOutline,
            contentDescription = "Clear all history",
            tint = colors.textMuted,
            modifier = Modifier.size(20.dp),
          )
        }
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = stringResource(R.string.analytics_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = colors.textMuted,
      )
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        StatBox(
          value = count.toString(),
          label = "Tests Taken",
          valueColor = colors.sky,
          modifier = Modifier.weight(1f),
        )
        StatBox(
          value = "$passRate%",
          label = "Pass Rate",
          valueColor = colors.emeraldLight,
          modifier = Modifier.weight(1f),
        )
        StatBox(
          value = String.format(Locale.US, "%.1f%%", avgScore),
          label = "Avg Score",
          valueColor = colors.emeraldLight,
          modifier = Modifier.weight(1f),
        )
        StatBox(
          value = "${bestScore.let { if (it % 1.0 == 0.0) it.toInt().toString() else String.format(Locale.US, "%.1f", it) }}%",
          label = "Best Score",
          valueColor = colors.amber,
          modifier = Modifier.weight(1f),
        )
      }
    }

    item {
      McqCard {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.History,
            contentDescription = null,
            tint = colors.emeraldLight,
            modifier = Modifier.size(22.dp),
          )
          Text(
            text = stringResource(R.string.recent_attempts),
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
          )
        }
        Spacer(modifier = Modifier.height(10.dp))

        if (history.isEmpty()) {
          Text(
            text = stringResource(R.string.no_attempts),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textMuted,
          )
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            history.take(30).forEach { record ->
              Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.card,
                border = BorderStroke(1.dp, colors.border),
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = record.title,
                      style = MaterialTheme.typography.titleMedium,
                      color = colors.textPrimary,
                      fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val modeLabel = if (record.mode == ExamMode.STUDY) "Study & Practice Drill" else "Strict Exam"
                    Text(
                      text = "$modeLabel • ${record.dateLabel}",
                      style = MaterialTheme.typography.bodySmall,
                      color = colors.textMuted,
                    )
                  }

                  TextButton(
                    onClick = { viewModel.retakeAttempt(record) },
                    modifier = Modifier.testTag("retake_${record.id}"),
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.Refresh,
                      contentDescription = null,
                      tint = colors.emeraldLight,
                      modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                      text = "Retake",
                      color = colors.emeraldLight,
                      fontWeight = FontWeight.SemiBold,
                    )
                  }

                  val pctStr = if (record.percentage % 1.0 == 0.0) {
                    record.percentage.toInt().toString()
                  } else {
                    String.format(Locale.US, "%.1f", record.percentage)
                  }
                  PassFailPill(
                    text = "${if (record.passed) "PASS" else "FAIL"} $pctStr%",
                    isPass = record.passed,
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
