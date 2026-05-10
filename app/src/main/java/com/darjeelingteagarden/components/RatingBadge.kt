package com.darjeelingteagarden.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

val colorPrimary = Color(0xFF00CCAA)

@Composable
fun RatingBadge(
    rating: String,
    label: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White,
    contentColor: Color = Color.Black
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. The Circle for the Rating
        Box(Modifier.zIndex(1f)){
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = containerColor,
                contentColor = contentColor,
                border = BorderStroke(1.dp, colorPrimary),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = rating,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

        }

        // 2. The Rectangle for the Text
        Surface(
            modifier = Modifier
                .height(24.dp) // Slightly shorter than the circle for visual balance
                .offset(x = (-7).dp), // Slight overlap to "dock" it to the circle
            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
            color = containerColor.copy(alpha = 0.9f), // Slightly lighter or different alpha
            contentColor = contentColor,
            border = BorderStroke(1.dp, colorPrimary)
        ) {
            Box(
                modifier = Modifier.padding(start = 10.dp, end = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}