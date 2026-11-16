package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.DifficultyFilter
import com.example.data.model.ExamMode
import com.example.data.model.NegativeMarking
import com.example.data.model.SubjectSelection
import com.example.ui.McqUiState
import com.example.ui.McqViewModel
import com.example.ui.components.McqCard
import com.example.ui.components.McqChip
import com.example.ui.components.McqTextField
import com.example.ui.theme.LocalMcqColors
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TestStudioScreen(
  state: McqUiState,
  viewModel: McqViewModel,
  modifier: Modifier = Modifier,
) {
  val colors = LocalMcqColors.current
  val studio = state.studio
  val poolSize = state.poolQuestions.size
  val maxQuestions = max(1, poolSize)

  var countInput by remember(studio.questionCount) { mutableStateOf(studio.questionCount.toString()) }
  var timeInput by remember(studio.timeMinutes) { mutableStateOf(studio.timeMinutes.toString()) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp, vertical = 16.dp),
  ) {
    Text(
      text = stringResource(R.string.studio_heading),
      style = MaterialTheme.typography.headlineLarge,
      color = colors.textPrimary,
    )
    Spacer(modifier = Modifier.height(14.dp))

    // Quick Action Buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Button(
        onClick = { viewModel.launchRapidBlitz() },
        modifier = Modifier
          .weight(1f)
          .testTag("rapid_blitz_button"),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 18.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = colors.amber,
          contentColor = colors.amberOnPrimary,
        ),
      ) {
        Icon(
          imageVector = Icons.Rounded.Bolt,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = stringResource(R.string.rapid_blitz_btn),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
        )
      }

      Button(
        onClick = { viewModel.launchFullMock() },
        modifier = Modifier
          .weight(1f)
          .testTag("full_mock_button"),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 18.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = colors.emerald,
          contentColor = colors.emeraldOnPrimary,
        ),
      ) {
        Icon(
          imageVector = Icons.Rounded.PlayArrow,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = stringResource(R.string.full_mock_btn),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))
    Text(
      text = stringResource(R.string.studio_intro),
      style = MaterialTheme.typography.bodyMedium,
      color = colors.textMuted,
    )

    Spacer(modifier = Modifier.height(18.dp))
    Text(
      text = stringResource(R.string.available_subjects),
      style = MaterialTheme.typography.labelMedium,
      color = colors.textMuted,
      fontWeight = FontWeight.SemiBold,
    )
    Spacer(modifier = Modifier.height(6.dp))

    if (state.subjects.isEmpty()) {
      Text(
        text = "No questions yet. Upload some.",
        style = MaterialTheme.typography.bodyMedium,
        color = colors.textMuted,
      )
    } else {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        state.subjects.forEach { subj ->
          val count = state.bank.count { it.subject == subj }
          McqChip(
            label = "$subj ($count)",
            selected = false,
            onClick = { viewModel.drillSubject(subj) },
            modifier = Modifier.testTag("subject_chip_$subj"),
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Step 1: Select Subjects
    McqCard {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = stringResource(R.string.step_1_title),
          style = MaterialTheme.typography.titleLarge,
          color = colors.textPrimary,
        )
        TextButton(
          onClick = { viewModel.toggleSelectAllSubjects() },
          modifier = Modifier.testTag("select_all_subjects_button"),
        ) {
          val allOn = state.subjects.isNotEmpty() &&
            state.subjects.all { studio.subjectSelections[it]?.enabled ?: true }
          Text(
            text = if (allOn) stringResource(R.string.deselect_all) else stringResource(R.string.select_all),
            color = colors.emeraldLight,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }

      Text(
        text = stringResource(R.string.step_1_hint),
        style = MaterialTheme.typography.bodyMedium,
        color = colors.textMuted,
      )
      Spacer(modifier = Modifier.height(10.dp))

      state.subjects.forEach { subj ->
        val sel = studio.subjectSelections[subj] ?: SubjectSelection()
        val subjCount = state.bank.count { it.subject == subj }
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          shape = RoundedCornerShape(16.dp),
          color = colors.card,
          border = BorderStroke(1.dp, if (sel.enabled) colors.emerald else colors.border),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Checkbox(
              checked = sel.enabled,
              onCheckedChange = { viewModel.toggleSubject(subj, it) },
              colors = CheckboxDefaults.colors(
                checkedColor = colors.emerald,
                uncheckedColor = colors.textMuted,
                checkmarkColor = colors.emeraldOnPrimary,
              ),
              modifier = Modifier.testTag("subject_checkbox_$subj"),
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = subj,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = "$subjCount MCQs",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
              )
            }
            McqTextField(
              value = sel.rangeOrCount,
              onValueChange = { viewModel.updateSubjectRange(subj, it) },
              placeholder = stringResource(R.string.range_count_placeholder),
              modifier = Modifier
                .width(132.dp)
                .testTag("subject_range_$subj"),
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Step 2: Test Format & Scoring Rules
    McqCard {
      Text(
        text = stringResource(R.string.step_2_title),
        style = MaterialTheme.typography.titleLarge,
        color = colors.textPrimary,
      )

      Spacer(modifier = Modifier.height(14.dp))
      Text(
        text = stringResource(R.string.test_title_label),
        style = MaterialTheme.typography.bodyMedium,
        color = colors.textMuted,
      )
      Spacer(modifier = Modifier.height(6.dp))
      McqTextField(
        value = studio.title,
        onValueChange = { viewModel.updateTitle(it) },
        placeholder = stringResource(R.string.test_title_placeholder),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("test_title_input"),
      )

      Spacer(modifier = Modifier.height(14.dp))
      Text(
        text = stringResource(R.string.exam_mode_label),
        style = MaterialTheme.typography.bodyMedium,
        color = colors.textMuted,
      )
      Spacer(modifier = Modifier.height(6.dp))
      ExamMode.entries.forEach { mode ->
        val selected = studio.mode == mode
        Surface(
          onClick = { viewModel.updateMode(mode) },
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("mode_${mode.name.lowercase()}"),
          shape = RoundedCornerShape(14.dp),
          color = if (selected) colors.emeraldSelectedBg else colors.inputSurface,
          border = BorderStroke(1.dp, if (selected) colors.emerald else colors.border),
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = mode.title,
              style = MaterialTheme.typography.titleMedium,
              color = colors.textPrimary,
              fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = mode.subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = colors.textMuted,
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
      Text(
        text = stringResource(R.string.difficulty_filter_label),
        style = MaterialTheme.typography.bodyMedium,
        color = colors.textMuted,
      )
      Spacer(modifier = Modifier.height(6.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        DifficultyFilter.entries.forEach { filter ->
          McqChip(
            label = filter.label,
            selected = studio.difficultyFilter == filter,
            onClick = { viewModel.updateDifficultyFilter(filter) },
            modifier = Modifier.testTag("diff_${filter.name.lowercase()}"),
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
      Text(
        text = stringResource(R.string.negative_marking_label),
        style = MaterialTheme.typography.bodyMedium,
        color = colors.textMuted,
      )
      Spacer(modifier = Modifier.height(6.dp))
      NegativeMarking.entries.forEach { neg ->
        val selected = studio.negativeMarking == neg
        Surface(
          onClick = { viewModel.updateNegativeMarking(neg) },
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("neg_${neg.name.lowercase()}"),
          shape = RoundedCornerShape(14.dp),
          color = if (selected) colors.emeraldSelectedBg else colors.inputSurface,
          border = BorderStroke(1.dp, if (selected) colors.emerald else colors.border),
        ) {
          Text(
            text = neg.label,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textPrimary,
            modifier = Modifier.padding(14.dp),
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "${stringResource(R.string.passing_percentage_label)} (${studio.passPercentage}%)",
          style = MaterialTheme.typography.bodyMedium,
          color = colors.textMuted,
        )
        Text(
          text = stringResource(R.string.standard_pass_label),
          style = MaterialTheme.typography.bodySmall,
          color = colors.sky,
        )
      }
      Slider(
        value = studio.passPercentage.toFloat(),
        onValueChange = { viewModel.updatePassPercentage(it.roundToInt()) },
        valueRange = 33f..60f,
        colors = SliderDefaults.colors(
          thumbColor = colors.emerald,
          activeTrackColor = colors.emerald,
          inactiveTrackColor = colors.inputSurface,
        ),
        modifier = Modifier.testTag("pass_percentage_slider"),
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text("33% (Pass)", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
        Text("60% (Competitive)", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
      }

      Spacer(modifier = Modifier.height(10.dp))
      StudioCheckboxRow(
        label = "Zero Repeats (Unseen First)",
        checked = studio.zeroRepeats,
        onCheckedChange = { viewModel.updateZeroRepeats(it) },
        testTag = "zero_repeats_checkbox",
      )
      StudioCheckboxRow(
        label = "Shuffle Questions",
        checked = studio.shuffleQuestions,
        onCheckedChange = { viewModel.updateShuffleQuestions(it) },
        testTag = "shuffle_questions_checkbox",
      )
      StudioCheckboxRow(
        label = "Randomize Options",
        checked = studio.randomizeOptions,
        onCheckedChange = { viewModel.updateRandomizeOptions(it) },
        testTag = "randomize_options_checkbox",
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Launch Summary Card
    McqCard(borderColor = colors.emeraldBorder) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.PlayArrow,
          contentDescription = null,
          tint = colors.emeraldLight,
          modifier = Modifier.size(22.dp),
        )
        Text(
          text = stringResource(R.string.launch_summary_title),
          style = MaterialTheme.typography.titleLarge,
          color = colors.textPrimary,
        )
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = stringResource(R.string.launch_summary_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = colors.textMuted,
      )

      Spacer(modifier = Modifier.height(14.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = "Number of Questions",
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
          )
          Text("Custom count", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
        }
        NumberBadgeField(
          value = countInput,
          onValueChange = { raw ->
            countInput = raw
            raw.toIntOrNull()?.let { viewModel.setQuestionCount(it) }
          },
          modifier = Modifier
            .width(104.dp)
            .testTag("question_count_input"),
        )
      }
      Slider(
        value = studio.questionCount.toFloat().coerceIn(1f, maxQuestions.toFloat()),
        onValueChange = {
          val v = it.roundToInt()
          countInput = v.toString()
          viewModel.setQuestionCount(v)
        },
        valueRange = 1f..maxQuestions.toFloat().coerceAtLeast(1f),
        enabled = maxQuestions > 1,
        colors = SliderDefaults.colors(
          thumbColor = colors.emerald,
          activeTrackColor = colors.emerald,
          inactiveTrackColor = colors.inputSurface,
        ),
        modifier = Modifier.testTag("question_count_slider"),
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text("1", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
        Text("Max ($poolSize)", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
      }

      Spacer(modifier = Modifier.height(14.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = "Time in Mock (Minutes)",
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
          )
          Text("Custom time", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
        }
        NumberBadgeField(
          value = timeInput,
          onValueChange = { raw ->
            timeInput = raw
            raw.toIntOrNull()?.let { viewModel.setTimeMinutes(it) }
          },
          modifier = Modifier
            .width(104.dp)
            .testTag("time_minutes_input"),
        )
      }
      Slider(
        value = studio.timeMinutes.toFloat().coerceIn(1f, 180f),
        onValueChange = {
          val v = it.roundToInt()
          timeInput = v.toString()
          viewModel.setTimeMinutes(v)
        },
        valueRange = 1f..180f,
        colors = SliderDefaults.colors(
          thumbColor = colors.emerald,
          activeTrackColor = colors.emerald,
          inactiveTrackColor = colors.inputSurface,
        ),
        modifier = Modifier.testTag("time_minutes_slider"),
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text("1 min", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
        Text("180 mins", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = colors.border, thickness = 1.dp)
      Spacer(modifier = Modifier.height(10.dp))

      val selectedSubjectNames = state.subjects
        .filter { studio.subjectSelections[it]?.enabled ?: true }
        .joinToString(", ")
        .ifEmpty { "—" }

      SummaryRow("Mode:", studio.mode.title)
      SummaryRow("Subjects Included:", selectedSubjectNames)
      SummaryRow("Negative Marking:", studio.negativeMarking.label)
      SummaryRow("Pass Criteria:", "${studio.passPercentage}%")

      Spacer(modifier = Modifier.height(14.dp))
      Button(
        onClick = { viewModel.startMockTest() },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("start_mock_test_button"),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = colors.emerald,
          contentColor = colors.emeraldOnPrimary,
        ),
      ) {
        Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = null)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = stringResource(R.string.start_mock_test),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
private fun StudioCheckboxRow(
  label: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  testTag: String,
) {
  val colors = LocalMcqColors.current
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onCheckedChange(!checked) }
      .padding(vertical = 6.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Checkbox(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = CheckboxDefaults.colors(
        checkedColor = colors.emerald,
        uncheckedColor = colors.textMuted,
        checkmarkColor = colors.emeraldOnPrimary,
      ),
    )
    Text(
      text = label,
      style = MaterialTheme.typography.bodyLarge,
      color = colors.textPrimary,
    )
  }
}

@Composable
private fun NumberBadgeField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = LocalMcqColors.current
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    singleLine = true,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    textStyle = MaterialTheme.typography.titleMedium.copy(
      color = colors.emeraldLight,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center,
    ),
    shape = RoundedCornerShape(14.dp),
    colors = OutlinedTextFieldDefaults.colors(
      focusedContainerColor = colors.inputSurface,
      unfocusedContainerColor = colors.inputSurface,
      focusedBorderColor = colors.emerald,
      unfocusedBorderColor = colors.border,
      cursorColor = colors.emeraldLight,
    ),
  )
}

@Composable
private fun SummaryRow(label: String, value: String) {
  val colors = LocalMcqColors.current
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(text = label, style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
    Spacer(modifier = Modifier.width(12.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      color = colors.textPrimary,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.End,
    )
  }
}
