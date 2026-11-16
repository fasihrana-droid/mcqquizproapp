package com.example.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SampleData
import com.example.ui.McqViewModel
import com.example.ui.components.McqCard
import com.example.ui.components.McqTextField
import com.example.ui.components.SmallActionButton
import com.example.ui.theme.LocalMcqColors
import com.example.ui.theme.MonoFontFamily

@Composable
fun UploadScreen(
  viewModel: McqViewModel,
  modifier: Modifier = Modifier,
) {
  val colors = LocalMcqColors.current
  val context = LocalContext.current
  val clipboard = LocalClipboardManager.current

  var showPasteDialog by remember { mutableStateOf(false) }
  var pastedJson by remember { mutableStateOf("") }

  val openFileLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument(),
  ) { uri: Uri? ->
    if (uri != null) {
      runCatching {
        val fileName = queryFileName(context, uri) ?: uri.lastPathSegment.orEmpty()
        val isCsv = fileName.endsWith(".csv", ignoreCase = true) ||
          (context.contentResolver.getType(uri)?.contains("csv", ignoreCase = true) == true)
        val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
          ?: error("Unable to open selected file")
        viewModel.importContent(content, isCsv = isCsv)
      }.onFailure { err ->
        viewModel.notifyMessage("Could not read that file: ${err.message}")
      }
    }
  }

  val exportFileLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/json"),
  ) { uri: Uri? ->
    if (uri != null) {
      runCatching {
        val json = viewModel.exportBankAsJson()
        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(json) }
          ?: error("Unable to write to destination")
        viewModel.notifyMessage("Exported question bank to JSON.")
      }.onFailure { err ->
        viewModel.notifyMessage("Export failed: ${err.message}")
      }
    }
  }

  if (showPasteDialog) {
    AlertDialog(
      onDismissRequest = { showPasteDialog = false },
      containerColor = colors.card,
      titleContentColor = colors.textPrimary,
      title = { Text("Paste JSON Here", style = MaterialTheme.typography.titleLarge) },
      text = {
        McqTextField(
          value = pastedJson,
          onValueChange = { pastedJson = it },
          placeholder = "[{\"subject\": \"...\", ...}]",
          singleLine = false,
          minLines = 6,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("paste_json_input"),
        )
      },
      confirmButton = {
        Button(
          onClick = {
            if (pastedJson.isNotBlank()) {
              viewModel.importContent(pastedJson, isCsv = false)
            }
            pastedJson = ""
            showPasteDialog = false
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = colors.emerald,
            contentColor = colors.emeraldOnPrimary,
          ),
          shape = RoundedCornerShape(12.dp),
        ) {
          Text("Import", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showPasteDialog = false }) {
          Text("Cancel", color = colors.textMuted)
        }
      },
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Column {
      Text(
        text = stringResource(R.string.upload_heading),
        style = MaterialTheme.typography.headlineLarge,
        color = colors.textPrimary,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = stringResource(R.string.upload_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = colors.textMuted,
      )
    }

    // Import Questions Card
    McqCard {
      Text(
        text = stringResource(R.string.import_questions),
        style = MaterialTheme.typography.titleLarge,
        color = colors.textPrimary,
      )
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "JSON: a list of objects with subject, difficulty, question, options (list of 4), correct_index, explanation.\nCSV: columns subject, difficulty, question, option_a, option_b, option_c, option_d, correct_index, explanation.",
        style = MaterialTheme.typography.bodyMedium,
        color = colors.textMuted,
      )
      Spacer(modifier = Modifier.height(14.dp))

      Button(
        onClick = {
          openFileLauncher.launch(
            arrayOf("application/json", "text/csv", "text/comma-separated-values", "text/plain", "*/*")
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("choose_file_button"),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = colors.emerald,
          contentColor = colors.emeraldOnPrimary,
        ),
      ) {
        Icon(imageVector = Icons.Rounded.UploadFile, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(R.string.choose_file), fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Button(
          onClick = { showPasteDialog = true },
          modifier = Modifier
            .weight(1f)
            .testTag("paste_json_button"),
          shape = RoundedCornerShape(14.dp),
          contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp),
          border = BorderStroke(1.dp, colors.border),
          colors = ButtonDefaults.buttonColors(
            containerColor = colors.inputSurface,
            contentColor = colors.textPrimary,
          ),
        ) {
          Icon(imageVector = Icons.Rounded.Code, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(stringResource(R.string.paste_json), maxLines = 1)
        }

        Button(
          onClick = { viewModel.loadSamplePack() },
          modifier = Modifier
            .weight(1f)
            .testTag("load_sample_pack_button"),
          shape = RoundedCornerShape(14.dp),
          contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp),
          border = BorderStroke(1.dp, colors.border),
          colors = ButtonDefaults.buttonColors(
            containerColor = colors.inputSurface,
            contentColor = colors.emeraldLight,
          ),
        ) {
          Text(stringResource(R.string.load_sample), maxLines = 1, fontWeight = FontWeight.SemiBold)
        }
      }
    }

    // Export Questions Card
    McqCard {
      Text(
        text = stringResource(R.string.export_questions),
        style = MaterialTheme.typography.titleLarge,
        color = colors.textPrimary,
      )
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "Save the entire question bank to a single JSON file you can back up, share, or re-import.",
        style = MaterialTheme.typography.bodyMedium,
        color = colors.textMuted,
      )
      Spacer(modifier = Modifier.height(14.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Button(
          onClick = { exportFileLauncher.launch("mcq-bank.json") },
          modifier = Modifier
            .weight(1f)
            .testTag("export_json_button"),
          shape = RoundedCornerShape(14.dp),
          contentPadding = PaddingValues(vertical = 14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = colors.emerald,
            contentColor = colors.emeraldOnPrimary,
          ),
        ) {
          Icon(imageVector = Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(stringResource(R.string.export_all_json), fontWeight = FontWeight.Bold)
        }

        Button(
          onClick = {
            clipboard.setText(AnnotatedString(viewModel.exportBankAsJson()))
            viewModel.notifyCopied()
          },
          modifier = Modifier.testTag("copy_export_json_button"),
          shape = RoundedCornerShape(14.dp),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
          border = BorderStroke(1.dp, colors.border),
          colors = ButtonDefaults.buttonColors(
            containerColor = colors.inputSurface,
            contentColor = colors.textPrimary,
          ),
        ) {
          Icon(
            imageVector = Icons.Rounded.ContentCopy,
            contentDescription = "Copy exported JSON",
            modifier = Modifier.size(20.dp),
          )
        }
      }
    }

    // Sample JSON Template Card
    McqCard {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = stringResource(R.string.sample_json_template),
          style = MaterialTheme.typography.titleLarge,
          color = colors.textPrimary,
        )
        SmallActionButton(
          onClick = {
            clipboard.setText(AnnotatedString(SampleData.SAMPLE_JSON_TEMPLATE))
            viewModel.notifyCopied()
          },
          modifier = Modifier.testTag("copy_sample_template_button"),
        ) {
          Text("Copy", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(colors.inputSurface)
          .horizontalScroll(rememberScrollState())
          .padding(12.dp),
      ) {
        Text(
          text = SampleData.SAMPLE_JSON_TEMPLATE,
          style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = MonoFontFamily,
            fontSize = 12.sp,
            lineHeight = 18.sp,
          ),
          color = colors.codeText,
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

private fun queryFileName(context: android.content.Context, uri: Uri): String? {
  return context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
    ?.use { cursor ->
      if (cursor.moveToFirst()) cursor.getString(0) else null
    }
}
