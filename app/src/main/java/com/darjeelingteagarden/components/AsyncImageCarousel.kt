package com.darjeelingteagarden.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun AsyncImageCarousel(
    imageUrls: List<String>,
    modifier: Modifier = Modifier
) {
    if (imageUrls.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { imageUrls.size })
    val coroutineScope = rememberCoroutineScope()

    // State to track which image is currently opened in full screen
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            AsyncImage(
                model = imageUrls[page],
                contentDescription = "Carousel image $page",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    // Make the image clickable to open full screen
                    .clickable { fullScreenImageUrl = imageUrls[page] }
            )
        }

        // Left Arrow
        if (pagerState.currentPage > 0) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
                    .size(40.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous", tint = Color.White)
            }
        }

        // Right Arrow
        if (pagerState.currentPage < imageUrls.size - 1) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
                    .size(40.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next", tint = Color.White)
            }
        }
    }

    // Full Screen Dialog Overlay
    if (fullScreenImageUrl != null) {
        Dialog(
            onDismissRequest = { fullScreenImageUrl = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false, // Important to make it truly full screen
                decorFitsSystemWindows = false
            )
        ) {
            FullScreenZoomableImage(
                imageUrl = fullScreenImageUrl!!,
                onClose = { fullScreenImageUrl = null }
            )
        }
    }
}

@Composable
fun FullScreenZoomableImage(
    imageUrl: String,
    onClose: () -> Unit
) {
    // Keep track of zoom (scale) and pan (offset)
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    // 1. Calculate new scale, limit between 1x (normal) and 5x (zoomed in)
                    scale = (scale * zoom).coerceIn(1f, 5f)

                    // 2. Calculate bounds for panning so the image doesn't drag off-screen
                    val maxX = (size.width * (scale - 1)) / 2
                    val maxY = (size.height * (scale - 1)) / 2

                    // 3. Apply pan offset, keeping it within bounds, but only if zoomed in
                    if (scale > 1f) {
                        offsetX = (offsetX + pan.x * scale).coerceIn(-maxX, maxX)
                        offsetY = (offsetY + pan.y * scale).coerceIn(-maxY, maxY)
                    } else {
                        // Reset offsets if fully zoomed out
                        offsetX = 0f
                        offsetY = 0f
                    }
                }
            }
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Full screen zoomed image",
            modifier = Modifier
                .fillMaxSize()
                // Apply the calculated zoom and pan values here
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
        )

        // Close Button in the top right corner
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close full screen", tint = Color.White)
        }
    }
}