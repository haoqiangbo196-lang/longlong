package com.elegant.reader.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elegant.reader.reader.FontFamilyOption
import com.elegant.reader.reader.PageTurnMode
import com.elegant.reader.reader.ReaderSettings
import com.elegant.reader.reader.ReaderTheme
import com.elegant.reader.ui.theme.TextPrimary
import com.elegant.reader.ui.theme.TextSecondary
import com.elegant.reader.ui.theme.Terracotta
import com.elegant.reader.ui.theme.TerracottaLight
import com.elegant.reader.ui.theme.WarmSurface

/**
 * 阅读设置面板：使用 Material3 ModalBottomSheet，
 * 自带半透明遮罩、下滑关闭、返回键关闭等标准行为。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsPanel(
    settings: ReaderSettings,
    onSettingsChange: (ReaderSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = WarmSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // 字号
            SettingSliderRow(
                title = "字号",
                value = settings.fontSizeSp,
                range = 12f..30f,
                onValueChange = { onSettingsChange(settings.copy(fontSizeSp = it)) }
            )

            // 行间距
            SettingSliderRow(
                title = "行间距",
                value = settings.lineHeightMultiplier,
                range = 1.2f..2.5f,
                onValueChange = { onSettingsChange(settings.copy(lineHeightMultiplier = it)) }
            )

            // 段间距
            SettingSliderRow(
                title = "段间距",
                value = settings.paragraphSpacing,
                range = 0f..30f,
                onValueChange = { onSettingsChange(settings.copy(paragraphSpacing = it)) }
            )

            Spacer(Modifier.height(8.dp))

            // 字体
            Text(
                text = "字体",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FontFamilyOption.entries.forEach { option ->
                    OptionChip(
                        text = option.displayName,
                        selected = settings.fontFamily == option,
                        onClick = { onSettingsChange(settings.copy(fontFamily = option)) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // 主题
            Text(
                text = "阅读主题",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ReaderTheme.entries.forEach { theme ->
                    ThemeDot(
                        theme = theme,
                        selected = settings.theme == theme,
                        onClick = { onSettingsChange(settings.copy(theme = theme)) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // 翻页动画
            Text(
                text = "翻页动画",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PageTurnMode.entries.forEach { mode ->
                    OptionChip(
                        text = mode.displayName,
                        selected = settings.pageTurnMode == mode,
                        onClick = { onSettingsChange(settings.copy(pageTurnMode = mode)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingSliderRow(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(60.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = Terracotta,
                activeTrackColor = Terracotta,
                inactiveTrackColor = TerracottaLight.copy(alpha = 0.3f)
            )
        )
        Text(
            text = String.format("%.1f", value),
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.width(48.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
private fun OptionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) {
                    Brush.linearGradient(listOf(TerracottaLight, Terracotta))
                } else {
                    Brush.linearGradient(listOf(Color(0xFFF0EAE0), Color(0xFFF0EAE0)))
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun ThemeDot(
    theme: ReaderTheme,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(theme.background))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(theme.textColor).copy(alpha = 0.7f))
            )
        }
    }
}
