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

@Composable
fun BentoCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = horizontalAlignment,
            content = content
        )
    }
}

@Composable
fun StatusBadge(
    label: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        contentColor = color,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatItem(
    label: String, 
    value: String, 
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label, 
            style = MaterialTheme.typography.labelSmall, 
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value, 
            style = MaterialTheme.typography.titleLarge, 
            fontWeight = FontWeight.Bold, 
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionButtons(
    onEdit: (() -> Unit)? = null,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    editLabel: String = stringResource(R.string.common_edit),
    deleteLabel: String = stringResource(R.string.common_delete),
    accentColor: Color = MaterialTheme.colorScheme.primary,
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
                        containerColor = accentColor.copy(alpha = 0.1f),
                        contentColor = accentColor
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
                    containerColor = deleteColor.copy(alpha = 0.1f),
                    contentColor = deleteColor
                ),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}
