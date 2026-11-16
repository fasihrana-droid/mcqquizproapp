package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ActiveQuizState
import com.example.data.model.ExamMode
import com.example.data.model.QuizQuestion
import com.example.ui.McqViewModel
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.DifficultyTag
import com.example.ui.components.McqCard
import com.example.ui.components.OPTION_LETTERS
import com.example.ui.components.SmallActionButton
import com.example.ui.components.SubjectTag
import com.example.ui.theme.LocalMcqColors
import com.example.ui.theme.MonoFontFamily

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuizScreen(
  quiz: ActiveQuizState,
  bank: List<QuizQuestion>,
  savedIds: Set<String>,
  viewModel: McqViewModel,
  modifier: Modifier = Modifier,
) {
  val colors = LocalMcqColors.current
  var showQuitDialog by remember { mutableStateOf(false) }
  var showSubmitDialog by remember { mutableStateOf(false) }

  BackHandler {
    showQuitDialog = true
  }

  val activeItem = quiz.questions.getOrNull(quiz.currentIndex)
  val question = activeItem?.let { item -> bank.find { it.id == item.questionId } }
  val chosenIndex = quiz.answers.getOrNull(quiz.currentIndex)
  val isStudy = quiz.config.mode == ExamMode.STUDY
  val isLocked = isStudy && chosenIndex != null
  val isSaved = question != null && question.id in savedIds
  val answeredCount = quiz.answers.count { it != null }

  val mins = (quiz.remainingSeconds / 60).toString().padStart(2, '0')
  val secs = (quiz.remainingSeconds % 60).toString().padStart(2, '0')

  if (showQuitDialog) {
    ConfirmActionDialog(
      title = stringResource(R.string.quiz_quit_confirm_title),
      message = stringResource(R.string.quiz_quit_confirm_body),
      confirmLabel = stringResource(R.string.quiz_quit),
      isDestructive = true,
      onConfirm = {
        showQuitDialog = false
        viewModel.quitQuizToStudio()
      },
      onDismiss = { showQuitDialog = false },
    )
  }

  if (showSubmitDialog) {
    ConfirmActionDialog(
      title = stringResource(R.string.quiz_submit_confirm_title),
      message = stringResource(R.string.quiz_submit_confirm_body),
      confirmLabel = "Submit",
      onConfirm = {
        showSubmitDialog = false
        viewModel.finishQuiz()
      },
      onDismiss = { showSubmitDialog = false },
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .navigationBarsPadding()
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Top Quiz Header Card
    McqCard {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (isStudy) "STUDY & PRACTICE" else "STRICT EXAM SIMULATOR",
            style = MaterialTheme.typography.labelLarge,
            color = colors.emeraldLight,
            fontWeight = FontWeight.Bold,
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Question ${quiz.currentIndex + 1} of ${quiz.questions.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textMuted,
          )
        }
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(colors.inputSurface)
              .border(1.dp, colors.border, RoundedCornerShape(10.dp))
              .padding(horizontal = 12.dp, vertical = 6.dp),
          ) {
            Text(
              text = "$mins:$secs",
              style = MaterialTheme.typography.labelLarge.copy(fontFamily = MonoFontFamily),
              color = colors.amber,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.testTag("quiz_timer"),
            )
          }
          SmallActionButton(
            onClick = { showQuitDialog = true },
            textColor = colors.red,
            modifier = Modifier.testTag("quiz_quit_button"),
          ) {
            Text(
              text = stringResource(R.string.quiz_quit),
              style = MaterialTheme.typography.bodySmall,
              color = colors.red,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }
      }
    }

    // Progress Bar
    LinearProgressIndicator(
      progress = { (quiz.currentIndex + 1).toFloat() / quiz.questions.size.coerceAtLeast(1) },
      modifier = Modifier
        .fillMaxWidth()
        .height(10.dp)
        .clip(RoundedCornerShape(9.dp)),
      color = colors.emerald,
      trackColor = colors.card,
    )

    // Active Question Card
    if (question != null && activeItem != null) {
      McqCard {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SubjectTag(question.subject)
            DifficultyTag(question.difficulty)
          }
          SmallActionButton(
            onClick = { viewModel.toggleSaved(question.id) },
            textColor = if (isSaved) colors.amber else colors.textMuted,
            modifier = Modifier.testTag("quiz_save_button"),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Icon(
                imageVector = if (isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                contentDescription = null,
                tint = if (isSaved) colors.amber else colors.textMuted,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = if (isSaved) "Saved" else "Save",
                style = MaterialTheme.typography.bodySmall,
                color = if (isSaved) colors.amber else colors.textMuted,
                fontWeight = FontWeight.SemiBold,
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = question.question,
          style = MaterialTheme.typography.titleLarge,
          color = colors.textPrimary,
        )
        Spacer(modifier = Modifier.height(12.dp))

        activeItem.options.forEachIndexed { idx, opt ->
          val (bg, border, textColor) = when {
            isLocked && opt.isCorrect -> Triple(colors.emeraldBadgeBg, colors.emerald, colors.emeraldLight)
            isLocked && chosenIndex == idx -> Triple(colors.redSurface, colors.red, colors.red)
            !isLocked && chosenIndex == idx -> Triple(colors.emeraldSelectedBg, colors.emerald, colors.textPrimary)
            else -> Triple(colors.inputSurface, colors.border, colors.textPrimary)
          }
          Surface(
            onClick = { viewModel.pickOption(idx) },
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              .testTag("quiz_option_$idx"),
            shape = RoundedCornerShape(14.dp),
            color = bg,
            border = BorderStroke(1.dp, border),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(colors.optionBadge),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = OPTION_LETTERS.getOrElse(idx) { "?" },
                  style = MaterialTheme.typography.labelLarge,
                  color = textColor,
                  fontWeight = FontWeight.Bold,
                )
              }
              Text(
                text = opt.text,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                modifier = Modifier.weight(1f),
              )
            }
          }
        }

        if (isLocked) {
          val pickedCorrect = chosenIndex?.let { activeItem.options.getOrNull(it)?.isCorrect } == true
          val correctOptionText = activeItem.options.firstOrNull { it.isCorrect }?.text.orEmpty()
          Spacer(modifier = Modifier.height(10.dp))
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(colors.inputSurface)
              .border(1.dp, colors.border, RoundedCornerShape(14.dp))
              .padding(12.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = if (pickedCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                contentDescription = null,
                tint = if (pickedCorrect) colors.emeraldLight else colors.red,
                modifier = Modifier.size(18.dp),
              )
              Text(
                text = if (pickedCorrect) "Correct!" else "Incorrect.",
                style = MaterialTheme.typography.bodyMedium,
                color = if (pickedCorrect) colors.emeraldLight else colors.red,
                fontWeight = FontWeight.Bold,
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Correct answer: $correctOptionText",
              style = MaterialTheme.typography.bodyMedium,
              color = colors.textPrimary,
              fontWeight = FontWeight.SemiBold,
            )
            if (question.explanation.isNotBlank()) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = question.explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted,
              )
            }
          }
        }
      }
    }

    // Previous / Clear / Next Navigation Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Button(
        onClick = { viewModel.goToQuestion(quiz.currentIndex - 1) },
        enabled = quiz.currentIndex > 0,
        modifier = Modifier
          .weight(1f)
          .testTag("quiz_prev_button"),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        border = BorderStroke(1.dp, colors.border),
        colors = ButtonDefaults.buttonColors(
          containerColor = colors.inputSurface,
          contentColor = colors.textPrimary,
          disabledContainerColor = colors.inputSurface.copy(alpha = 0.4f),
          disabledContentColor = colors.textMuted.copy(alpha = 0.5f),
        ),
      ) {
        Text("← Previous", maxLines = 1)
      }

      Button(
        onClick = { viewModel.clearChoice() },
        enabled = !isLocked && chosenIndex != null,
        modifier = Modifier
          .weight(1f)
          .testTag("quiz_clear_button"),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        border = BorderStroke(1.dp, colors.border),
        colors = ButtonDefaults.buttonColors(
          containerColor = colors.inputSurface,
          contentColor = colors.textPrimary,
          disabledContainerColor = colors.inputSurface.copy(alpha = 0.4f),
          disabledContentColor = colors.textMuted.copy(alpha = 0.5f),
        ),
      ) {
        Text("Clear Choice", maxLines = 1)
      }

      Button(
        onClick = { viewModel.goToQuestion(quiz.currentIndex + 1) },
        enabled = quiz.currentIndex < quiz.questions.lastIndex,
        modifier = Modifier
          .weight(1f)
          .testTag("quiz_next_button"),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = colors.emerald,
          contentColor = colors.emeraldOnPrimary,
          disabledContainerColor = colors.emerald.copy(alpha = 0.35f),
          disabledContentColor = colors.emeraldOnPrimary.copy(alpha = 0.5f),
        ),
      ) {
        Text("Next →", maxLines = 1, fontWeight = FontWeight.Bold)
      }
    }

    // Question Palette Card
    McqCard {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Question Palette",
          style = MaterialTheme.typography.bodyMedium,
          color = colors.textMuted,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = "$answeredCount / ${quiz.questions.size} Answered",
          style = MaterialTheme.typography.bodyMedium,
          color = colors.emeraldLight,
          fontWeight = FontWeight.Bold,
        )
      }

      Spacer(modifier = Modifier.height(12.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        quiz.questions.forEachIndexed { idx, item ->
          val answered = quiz.answers[idx] != null
          val isCurrent = idx == quiz.currentIndex
          val saved = item.questionId in savedIds
          val amberBarColor = colors.amber

          Surface(
            onClick = { viewModel.goToQuestion(idx) },
            modifier = Modifier
              .size(48.dp)
              .testTag("palette_$idx"),
            shape = RoundedCornerShape(12.dp),
            color = if (answered) colors.emeraldBadgeBg else colors.inputSurface,
            border = BorderStroke(
              width = if (isCurrent) 2.dp else 1.dp,
              color = when {
                isCurrent -> Color.White
                answered -> colors.emerald
                else -> colors.border
              },
            ),
          ) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                  if (saved) {
                    drawRect(
                      color = amberBarColor,
                      topLeft = Offset(0f, size.height - 4.dp.toPx()),
                      size = Size(size.width, 4.dp.toPx()),
                    )
                  }
                },
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = "${idx + 1}",
                style = MaterialTheme.typography.labelLarge,
                color = if (answered) colors.emeraldLight else colors.textMuted,
                fontWeight = FontWeight.SemiBold,
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
      Button(
        onClick = { showSubmitDialog = true },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("quiz_submit_button"),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = colors.emerald,
          contentColor = colors.emeraldOnPrimary,
        ),
      ) {
        Text(
          text = stringResource(R.string.quiz_submit_btn),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}
