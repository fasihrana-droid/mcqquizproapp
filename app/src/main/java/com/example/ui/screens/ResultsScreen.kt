package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AppTab
import com.example.data.model.QuizQuestion
import com.example.data.model.QuizResultState
import com.example.ui.McqViewModel
import com.example.ui.components.McqCard
import com.example.ui.components.OPTION_LETTERS
import com.example.ui.components.PassFailPill
import com.example.ui.components.SmallActionButton
import com.example.ui.theme.LocalMcqColors
import java.util.Locale

@Composable
fun ResultsScreen(
  result: QuizResultState,
  bank: List<QuizQuestion>,
  savedIds: Set<String>,
  viewModel: McqViewModel,
  modifier: Modifier = Modifier,
) {
  val colors = LocalMcqColors.current
  val byId = bank.associateBy { it.id }
  val incorrectTotal = result.wrongIds.size + result.skippedIds.size

  BackHandler {
    viewModel.selectTab(AppTab.STUDIO)
  }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Text(
        text = stringResource(R.string.results_heading),
        style = MaterialTheme.typography.headlineLarge,
        color = colors.textPrimary,
      )
    }

    item {
      McqCard {
        Text(
          text = String.format(Locale.US, "%.1f%%", result.percentage),
          style = MaterialTheme.typography.displayLarge,
          color = if (result.passed) colors.emeraldLight else colors.red,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
          PassFailPill(
            text = if (result.passed) "PASS" else "FAIL",
            isPass = result.passed,
          )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          StatBox(
            value = result.correctCount.toString(),
            label = "Correct",
            valueColor = colors.emeraldLight,
            modifier = Modifier.weight(1f),
          )
          StatBox(
            value = result.wrongCount.toString(),
            label = "Wrong",
            valueColor = colors.red,
            modifier = Modifier.weight(1f),
          )
          StatBox(
            value = result.skippedCount.toString(),
            label = "Skipped",
            valueColor = colors.textPrimary,
            modifier = Modifier.weight(1f),
          )
          StatBox(
            value = String.format(Locale.US, "%.2f", result.score),
            label = "Score",
            valueColor = colors.textPrimary,
            modifier = Modifier.weight(1f),
          )
        }
      }
    }

    item {
      McqCard {
        Text(
          text = stringResource(R.string.practice_more_title),
          style = MaterialTheme.typography.titleLarge,
          color = colors.textPrimary,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = stringResource(R.string.practice_more_subtitle),
          style = MaterialTheme.typography.bodyMedium,
          color = colors.textMuted,
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
          onClick = { viewModel.reattemptIncorrect() },
          enabled = incorrectTotal > 0,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("reattempt_incorrect_button"),
          shape = RoundedCornerShape(14.dp),
          contentPadding = PaddingValues(vertical = 14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = colors.amber,
            contentColor = colors.amberOnPrimary,
            disabledContainerColor = colors.amber.copy(alpha = 0.35f),
            disabledContentColor = colors.amberOnPrimary.copy(alpha = 0.5f),
          ),
        ) {
          Icon(imageVector = Icons.Rounded.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Re-attempt Incorrect MCQs ($incorrectTotal)", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))
        Button(
          onClick = { viewModel.practiceSaved() },
          enabled = savedIds.isNotEmpty(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("practice_saved_from_results_button"),
          shape = RoundedCornerShape(14.dp),
          contentPadding = PaddingValues(vertical = 14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = colors.emerald,
            contentColor = colors.emeraldOnPrimary,
            disabledContainerColor = colors.emerald.copy(alpha = 0.35f),
            disabledContentColor = colors.emeraldOnPrimary.copy(alpha = 0.5f),
          ),
        ) {
          Icon(imageVector = Icons.Rounded.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Practice Saved MCQs (${savedIds.size})", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))
        Button(
          onClick = { viewModel.selectTab(AppTab.STUDIO) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("back_to_studio_button"),
          shape = RoundedCornerShape(14.dp),
          contentPadding = PaddingValues(vertical = 14.dp),
          border = BorderStroke(1.dp, colors.border),
          colors = ButtonDefaults.buttonColors(
            containerColor = colors.inputSurface,
            contentColor = colors.textPrimary,
          ),
        ) {
          Text(stringResource(R.string.back_to_studio), fontWeight = FontWeight.SemiBold)
        }
      }
    }

    item {
      Text(
        text = stringResource(R.string.review_heading),
        style = MaterialTheme.typography.titleLarge,
        color = colors.textPrimary,
        modifier = Modifier.padding(top = 6.dp),
      )
    }

    itemsIndexed(result.questions) { idx, item ->
      val q = byId[item.questionId] ?: return@itemsIndexed
      val ans = result.answers.getOrNull(idx)
      val correctIdx = item.options.indexOfFirst { it.isCorrect }
      val isSaved = q.id in savedIds

      val statusLabel = when {
        ans == null -> "Skipped"
        ans == correctIdx -> "Correct"
        else -> "Wrong"
      }
      val statusPass = when {
        ans == null -> null
        ans == correctIdx -> true
        else -> false
      }

      McqCard {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Q${idx + 1}",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textMuted,
            fontWeight = FontWeight.Bold,
          )
          PassFailPill(text = statusLabel, isPass = statusPass)
          SmallActionButton(
            onClick = { viewModel.toggleSaved(q.id) },
            textColor = if (isSaved) colors.amber else colors.textMuted,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Icon(
                imageVector = if (isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                contentDescription = null,
                tint = if (isSaved) colors.amber else colors.textMuted,
                modifier = Modifier.size(15.dp),
              )
              Text(
                text = if (isSaved) "Saved" else "Save",
                style = MaterialTheme.typography.bodySmall,
                color = if (isSaved) colors.amber else colors.textMuted,
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = q.question,
          style = MaterialTheme.typography.titleMedium,
          color = colors.textPrimary,
          fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))

        item.options.forEachIndexed { j, opt ->
          val (bg, border, fg) = when {
            opt.isCorrect -> Triple(colors.emeraldBadgeBg, colors.emerald, colors.emeraldLight)
            ans == j -> Triple(colors.redSurface, colors.red, colors.red)
            else -> Triple(colors.inputSurface, colors.border, colors.textPrimary)
          }
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 3.dp),
            shape = RoundedCornerShape(14.dp),
            color = bg,
            border = BorderStroke(1.dp, border),
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Box(
                modifier = Modifier
                  .size(30.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(colors.optionBadge),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = OPTION_LETTERS.getOrElse(j) { "?" },
                  style = MaterialTheme.typography.labelLarge,
                  color = fg,
                  fontWeight = FontWeight.Bold,
                )
              }
              Text(
                text = opt.text,
                style = MaterialTheme.typography.bodyMedium,
                color = fg,
                modifier = Modifier.weight(1f),
              )
            }
          }
        }

        if (q.explanation.isNotBlank()) {
          Spacer(modifier = Modifier.height(8.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(colors.inputSurface)
              .border(1.dp, colors.border, RoundedCornerShape(14.dp))
              .padding(12.dp),
          ) {
            Text(
              text = q.explanation,
              style = MaterialTheme.typography.bodyMedium,
              color = colors.textMuted,
            )
          }
        }
      }
    }
  }
}

@Composable
fun StatBox(
  value: String,
  label: String,
  valueColor: Color,
  modifier: Modifier = Modifier,
) {
  val colors = LocalMcqColors.current
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    color = colors.card,
    border = BorderStroke(1.dp, colors.border),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = value,
        style = MaterialTheme.typography.titleLarge,
        color = valueColor,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = colors.textMuted,
        maxLines = 1,
      )
    }
  }
}
