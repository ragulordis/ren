package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.R

/**
 * Universal property image renderer.
 * Automatically detects whether `imageSource` is:
 * 1. A remote URL (http://, https://) or local content URI (content://) -> Loads with Coil SubcomposeAsyncImage
 * 2. A bundled drawable resource name (prop_1, prop_house_kottakuppam) -> Loads via getDrawableResForName
 * Includes graceful placeholder shimmer and fallback icon.
 */
@Composable
fun PropertyImage(
    imageSource: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val isRemoteOrContent = imageSource.startsWith("http://", ignoreCase = true) ||
            imageSource.startsWith("https://", ignoreCase = true) ||
            imageSource.startsWith("content://", ignoreCase = true)

    if (isRemoteOrContent) {
        val context = LocalContext.current
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageSource)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            error = {
                // Fallback to local default drawable if remote download fails
                Image(
                    painter = painterResource(id = R.drawable.prop_house_kottakuppam),
                    contentDescription = contentDescription,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )
            }
        )
    } else {
        // Local bundled drawable
        Image(
            painter = painterResource(id = getDrawableResForName(imageSource)),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}
