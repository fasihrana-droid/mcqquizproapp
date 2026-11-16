package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppTab
import com.example.data.model.Difficulty
import com.example.data.model.QuizQuestion
import com.example.ui.theme.LocalMcqColors

val OPTION_LETTERS = listOf("A", "B", "C", "D")

@Composable
fun McqHeader(modifier: Modifier = Modifier) {
  val colors = LocalMcqColors.current
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(colors.bg)
      .statusBarsPadding(),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(
            Brush.linearGradient(
              colors = listOf(colors.emerald, colors.emeraldDark),
            )
          ),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Rounded.Bolt,
          contentDescription = stringResource(R.string.brand_title),
          tint = Color.White,
          modifier = Modifier.size(28.dp),
        )
      }
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = stringResource(R.string.brand_title),
            style = MaterialTheme.typography.headlineMedium,
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
          )
          Spacer(modifier = Modifier.width(6.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(colors.emeraldBadgeBg)
              .border(1.dp, colors.emeraldBorder, RoundedCornerShape(6.dp))
              .padding(horizontal = 8.dp, vertical = 2.dp),
          ) {
            Text(
              text = stringResource(R.string.brand_badge),
              style = MaterialTheme.typography.labelSmall,
              color = colors.emeraldLight,
              fontWeight = FontWeight.Bold,
            )
          }
        }
        Text(
          text = stringResource(R.string.brand_subtitle),
          style = MaterialTheme.typography.bodySmall,
          color = colors.textMuted,
        )
      }
    }
    HorizontalDivider(color = colors.border, thickness = 1.dp)
  }
}

private fun tabIcon(tab: AppTab, selected: Boolean): ImageVector =
  when (tab) {
    AppTab.STUDIO -> Icons.Rounded.Bolt
    AppTab.BANK -> if (selected) Icons.Rounded.MenuBook else Icons.Outlined.MenuBook
    AppTab.SAVED -> if (selected) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder
    AppTab.UPLOAD -> if (selected) Icons.Rounded.CloudUpload else Icons.Outlined.CloudUpload
    AppTab.STATS -> if (selected) Icons.Rounded.Analytics else Icons.Outlined.Analytics
  }

@Composable
fun McqBottomNavBar(
  currentTab: AppTab,
  isQuizActive: Boolean,
  onSelectTab: (AppTab) -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = LocalMcqColors.current
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(colors.bg)
      .windowInsetsPadding(WindowInsets.navigationBars),
  ) {
    HorizontalDivider(color = colors.border, thickness = 1.dp)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp, horizontal = 4.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      AppTab.entries.forEach { tab ->
        val selected = currentTab == tab && !isQuizActive
        Column(
          modifier = Modifier
            .weight(1f)
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelectTab(tab) }
            .padding(vertical = 4.dp)
            .testTag("nav_${tab.name.lowercase()}"),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Box(
            modifier = Modifier
              .width(56.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(if (selected) colors.emeraldSelectedBg else Color.Transparent)
              .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = tabIcon(tab, selected),
              contentDescription = tab.label,
              tint = if (selected) colors.emeraldLight else colors.textMuted,
              modifier = Modifier.size(22.dp),
            )
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) colors.emeraldLight else colors.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }
}

@Composable
fun McqNavigationRail(
  currentTab: AppTab,
  isQuizActive: Boolean,
  onSelectTab: (AppTab) -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = LocalMcqColors.current
  NavigationRail(
    modifier = modifier.fillMaxHeight(),
    containerColor = colors.bg,
    contentColor = colors.textPrimary,
  ) {
    Spacer(modifier = Modifier.height(12.dp))
    AppTab.entries.forEach { tab ->
      val selected = currentTab == tab && !isQuizActive
      NavigationRailItem(
        selected = selected,
        onClick = { onSelectTab(tab) },
        icon = {
          Icon(
            imageVector = tabIcon(tab, selected),
            contentDescription = tab.label,
          )
        },
        label = {
          Text(
            text = tab.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        },
        colors = NavigationRailItemDefaults.colors(
          selectedIconColor = colors.emeraldLight,
          selectedTextColor = colors.emeraldLight,
          indicatorColor = colors.emeraldSelectedBg,
          unselectedIconColor = colors.textMuted,
          unselectedTextColor = colors.textMuted,
        ),
        modifier = Modifier.testTag("rail_${tab.name.lowercase()}"),
      )
    }
  }
}

@Composable
fun McqCard(
  modifier: Modifier = Modifier,
  borderColor: Color = LocalMcqColors.current.border,
  containerColor: Color = LocalMcqColors.current.card,
  contentPadding: PaddingValues = PaddingValues(16.dp),
  content: @Composable ColumnScope.() -> Unit,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    color = containerColor,
    border = BorderStroke(1.dp, borderColor),
  ) {
    Column(
      modifier = Modifier.padding(contentPadding),
      content = content,
    )
  }
}

@Composable
fun McqTextField(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  modifier: Modifier = Modifier,
  singleLine: Boolean = true,
  minLines: Int = 1,
  keyboardType: KeyboardType = KeyboardType.Text,
  leadingIcon: @Composable (() -> Unit)? = null,
) {
  val colors = LocalMcqColors.current
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    singleLine = singleLine,
    minLines = minLines,
    placeholder = {
      Text(
        text = placeholder,
        color = colors.textMuted,
        style = MaterialTheme.typography.bodyMedium,
      )
    },
    leadingIcon = leadingIcon,
    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    shape = RoundedCornerShape(14.dp),
    textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.textPrimary),
    colors = OutlinedTextFieldDefaults.colors(
      focusedContainerColor = colors.inputSurface,
      unfocusedContainerColor = colors.inputSurface,
      focusedBorderColor = colors.emerald,
      unfocusedBorderColor = colors.border,
      cursorColor = colors.emeraldLight,
      focusedTextColor = colors.textPrimary,
      unfocusedTextColor = colors.textPrimary,
    ),
  )
}

@Composable
fun McqChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = LocalMcqColors.current
  Surface(
    onClick = onClick,
    modifier = modifier.minimumInteractiveComponentSize(),
    shape = RoundedCornerShape(999.dp),
    color = if (selected) colors.emeraldSelectedBg else colors.inputSurface,
    border = BorderStroke(1.dp, if (selected) colors.emerald else colors.border),
  ) {
    Box(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = if (selected) colors.emeraldLight else colors.textPrimary,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
      )
    }
  }
}

@Composable
fun SubjectTag(subject: String, modifier: Modifier = Modifier) {
  val colors = LocalMcqColors.current
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(colors.skySurface)
      .padding(horizontal = 10.dp, vertical = 4.dp),
  ) {
    Text(
      text = subject,
      style = MaterialTheme.typography.labelMedium,
      color = colors.sky,
      fontWeight = FontWeight.SemiBold,
    )
  }
}

@Composable
fun DifficultyTag(difficulty: Difficulty, modifier: Modifier = Modifier) {
  val colors = LocalMcqColors.current
  val (bg, fg) = when (difficulty) {
    Difficulty.EASY -> colors.emeraldBadgeBg to colors.emeraldLight
    Difficulty.MEDIUM -> colors.amberSurface to colors.amber
    Difficulty.HARD -> colors.redSurface to colors.red
  }
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(bg)
      .padding(horizontal = 10.dp, vertical = 4.dp),
  ) {
    Text(
      text = difficulty.label,
      style = MaterialTheme.typography.labelMedium,
      color = fg,
      fontWeight = FontWeight.SemiBold,
    )
  }
}

@Composable
fun PassFailPill(
  text: String,
  isPass: Boolean?,
  modifier: Modifier = Modifier,
) {
  val colors = LocalMcqColors.current
  val (bg, fg, border) = when (isPass) {
    true -> Triple(colors.emeraldBadgeBg, colors.emeraldLight, colors.emeraldBorder)
    false -> Triple(colors.redSurface, colors.red, colors.redBorder)
    null -> Triple(colors.inputSurface, colors.textMuted, colors.border)
  }
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(bg)
      .border(1.dp, border, RoundedCornerShape(10.dp))
      .padding(horizontal = 12.dp, vertical = 5.dp),
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelMedium,
      color = fg,
      fontWeight = FontWeight.Bold,
    )
  }
}

@Composable
fun SmallActionButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  textColor: Color = LocalMcqColors.current.textPrimary,
  containerColor: Color = LocalMcqColors.current.inputSurface,
  borderColor: Color = LocalMcqColors.current.border,
  content: @Composable () -> Unit,
) {
  Surface(
    onClick = onClick,
    modifier = modifier.minimumInteractiveComponentSize(),
    shape = RoundedCornerShape(10.dp),
    color = containerColor,
    border = BorderStroke(1.dp, borderColor),
    contentColor = textColor,
  ) {
    Box(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      contentAlignment = Alignment.Center,
    ) {
      content()
    }
  }
}

@Composable
fun QuestionBankCard(
  question: QuizQuestion,
  isSaved: Boolean,
  onToggleSave: () -> Unit,
  onEdit: (() -> Unit)? = null,
  onDelete: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  val colors = LocalMcqColors.current
  McqCard(modifier = modifier) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f),
      ) {
        SubjectTag(question.subject)
        DifficultyTag(question.difficulty)
      }
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SmallActionButton(
          onClick = onToggleSave,
          textColor = if (isSaved) colors.amber else colors.textMuted,
          modifier = Modifier.testTag("save_btn_${question.id}"),
        ) {
          Icon(
            imageVector = if (isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
            contentDescription = if (isSaved) "Remove bookmark" else "Bookmark question",
            tint = if (isSaved) colors.amber else colors.textMuted,
            modifier = Modifier.size(18.dp),
          )
        }
        if (onEdit != null) {
          SmallActionButton(
            onClick = onEdit,
            modifier = Modifier.testTag("edit_btn_${question.id}"),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "Edit question",
                modifier = Modifier.size(15.dp),
              )
              Text("Edit", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
          }
        }
        if (onDelete != null) {
          SmallActionButton(
            onClick = onDelete,
            textColor = colors.red,
            modifier = Modifier.testTag("delete_btn_${question.id}"),
          ) {
            Icon(
              imageVector = Icons.Rounded.DeleteOutline,
              contentDescription = "Delete question",
              tint = colors.red,
              modifier = Modifier.size(18.dp),
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))
    Text(
      text = question.question,
      style = MaterialTheme.typography.titleMedium,
      color = colors.textPrimary,
      fontWeight = FontWeight.Bold,
    )

    Spacer(modifier = Modifier.height(10.dp))
    question.options.forEachIndexed { idx, opt ->
      val isCorrect = idx == question.correctIndex
      Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        if (isCorrect) {
          Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = "Correct option",
            tint = colors.emeraldLight,
            modifier = Modifier.size(16.dp),
          )
        }
        Text(
          text = "${OPTION_LETTERS.getOrElse(idx) { "?" }}. $opt",
          style = MaterialTheme.typography.bodyMedium,
          color = if (isCorrect) colors.emeraldLight else colors.textMuted,
          fontWeight = if (isCorrect) FontWeight.SemiBold else FontWeight.Normal,
        )
      }
    }

    if (question.explanation.isNotBlank()) {
      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(colors.inputSurface)
          .border(1.dp, colors.border, RoundedCornerShape(14.dp))
          .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
      ) {
        Icon(
          imageVector = Icons.Rounded.Lightbulb,
          contentDescription = "Explanation",
          tint = colors.amber,
          modifier = Modifier.size(18.dp),
        )
        Text(
          text = question.explanation,
          style = MaterialTheme.typography.bodyMedium,
          color = colors.textMuted,
        )
      }
    }
  }
}

@Composable
fun ConfirmActionDialog(
  title: String,
  message: String,
  confirmLabel: String,
  isDestructive: Boolean = false,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  val colors = LocalMcqColors.current
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = colors.card,
    titleContentColor = colors.textPrimary,
    textContentColor = colors.textMuted,
    title = { Text(title, style = MaterialTheme.typography.titleLarge) },
    text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
    confirmButton = {
      Button(
        onClick = onConfirm,
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isDestructive) colors.red else colors.emerald,
          contentColor = if (isDestructive) colors.bg else colors.emeraldOnPrimary,
        ),
        shape = RoundedCornerShape(12.dp),
      ) {
        Text(confirmLabel, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = colors.textMuted)
      }
    },
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionEditorDialog(
  initialQuestion: QuizQuestion? = null,
  defaultSubject: String = "Pak Studies",
  onSave: (subject: String, difficulty: Difficulty, question: String, options: List<String>, correctIndex: Int, explanation: String) -> Unit,
  onDismiss: () -> Unit,
) {
  val colors = LocalMcqColors.current
  var subject by remember { mutableStateOf(initialQuestion?.subject ?: defaultSubject) }
  var questionText by remember { mutableStateOf(initialQuestion?.question ?: "") }
  var optA by remember { mutableStateOf(initialQuestion?.options?.getOrNull(0) ?: "") }
  var optB by remember { mutableStateOf(initialQuestion?.options?.getOrNull(1) ?: "") }
  var optC by remember { mutableStateOf(initialQuestion?.options?.getOrNull(2) ?: "") }
  var optD by remember { mutableStateOf(initialQuestion?.options?.getOrNull(3) ?: "") }
  var correctIdx by remember { mutableIntStateOf(initialQuestion?.correctIndex ?: 0) }
  var difficulty by remember { mutableStateOf(initialQuestion?.difficulty ?: Difficulty.MEDIUM) }
  var explanation by remember { mutableStateOf(initialQuestion?.explanation ?: "") }

  val isValid = subject.isNotBlank() &&
    questionText.isNotBlank() &&
    optA.isNotBlank() &&
    optB.isNotBlank() &&
    optC.isNotBlank() &&
    optD.isNotBlank()

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = colors.card,
    titleContentColor = colors.textPrimary,
    title = {
      Text(
        text = if (initialQuestion == null) "Add New MCQ" else "Edit MCQ",
        style = MaterialTheme.typography.titleLarge,
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        McqTextField(
          value = subject,
          onValueChange = { subject = it },
          placeholder = "Subject (e.g. Pak Studies)",
          modifier = Modifier.fillMaxWidth(),
        )
        McqTextField(
          value = questionText,
          onValueChange = { questionText = it },
          placeholder = "Question text",
          singleLine = false,
          minLines = 2,
          modifier = Modifier.fillMaxWidth(),
        )
        McqTextField(
          value = optA,
          onValueChange = { optA = it },
          placeholder = "Option A",
          modifier = Modifier.fillMaxWidth(),
        )
        McqTextField(
          value = optB,
          onValueChange = { optB = it },
          placeholder = "Option B",
          modifier = Modifier.fillMaxWidth(),
        )
        McqTextField(
          value = optC,
          onValueChange = { optC = it },
          placeholder = "Option C",
          modifier = Modifier.fillMaxWidth(),
        )
        McqTextField(
          value = optD,
          onValueChange = { optD = it },
          placeholder = "Option D",
          modifier = Modifier.fillMaxWidth(),
        )
        Text("Correct Option", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OPTION_LETTERS.forEachIndexed { idx, letter ->
            McqChip(
              label = letter,
              selected = correctIdx == idx,
              onClick = { correctIdx = idx },
            )
          }
        }
        Text("Difficulty", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Difficulty.entries.forEach { d ->
            McqChip(
              label = d.label,
              selected = difficulty == d,
              onClick = { difficulty = d },
            )
          }
        }
        McqTextField(
          value = explanation,
          onValueChange = { explanation = it },
          placeholder = "Explanation / Rationale (optional)",
          singleLine = false,
          minLines = 2,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(
            subject,
            difficulty,
            questionText,
            listOf(optA, optB, optC, optD),
            correctIdx,
            explanation,
          )
        },
        enabled = isValid,
        colors = ButtonDefaults.buttonColors(
          containerColor = colors.emerald,
          contentColor = colors.emeraldOnPrimary,
        ),
        shape = RoundedCornerShape(12.dp),
      ) {
        Text(if (initialQuestion == null) "Add MCQ" else "Save", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = colors.textMuted)
      }
    },
  )
}
