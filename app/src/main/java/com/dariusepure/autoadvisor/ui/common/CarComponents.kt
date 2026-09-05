package com.dariusepure.autoadvisor.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dariusepure.autoadvisor.R
import com.dariusepure.autoadvisor.ui.theme.statusExpiredRed

/**
 * Text component that automatically scales down font size to fit in 1 line.
 */
@Composable
fun AutoSizeText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier,
    minFontSize: TextUnit = 10.sp,
    maxLines: Int = 1,
    softWrap: Boolean = maxLines > 1,
    textAlign: TextAlign? = null,
    color: Color = Color.Unspecified
) {
    Text(
        text = text,
        modifier = modifier,
        style = style,
        maxLines = maxLines,
        softWrap = softWrap,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        color = color
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionButtons(
    onEdit: (() -> Unit)? = null,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    editLabel: String = stringResource(R.string.common_edit),
    deleteLabel: String = stringResource(R.string.common_delete),
    accentColor: Color = Color(0xFF1A73E8),
    deleteColor: Color = statusExpiredRed,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically
) {
    Row(
        modifier = modifier,
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement
    ) {
        if (onEdit != null) {
            val editTooltipState = rememberTooltipState()
            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = { PlainTooltip { Text(editLabel) } },
                state = editTooltipState
            ) {
                FilledIconButton(
                    onClick = onEdit,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = accentColor,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                }
            }
        }
        
        val deleteTooltipState = rememberTooltipState()
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = { PlainTooltip { Text(deleteLabel) } },
            state = deleteTooltipState
        ) {
            FilledIconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = deleteColor,
                    contentColor = Color.White
                ),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}
