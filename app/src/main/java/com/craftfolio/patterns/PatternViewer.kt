package com.craftfolio.patterns

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource as s
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class RenderedPage(val bitmap: Bitmap, val pages: Int)
fun renderPattern(pattern: Pattern, index: Int, maxWidth: Int = 1600, maxHeight: Int = 2000): RenderedPage {
    if (pattern.mime == "application/pdf") {
        PdfRenderer(ParcelFileDescriptor.open(File(pattern.path), ParcelFileDescriptor.MODE_READ_ONLY)).use { renderer ->
            renderer.openPage(index.coerceIn(0, renderer.pageCount - 1)).use { page ->
                val scale = minOf(maxWidth.toFloat() / page.width, maxHeight.toFloat() / page.height)
                val bitmap = Bitmap.createBitmap((page.width * scale).toInt().coerceAtLeast(1), (page.height * scale).toInt().coerceAtLeast(1), Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(AndroidColor.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                return RenderedPage(bitmap, renderer.pageCount)
            }
        }
    }
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(pattern.path, bounds)
    var sample = 1
    while (bounds.outWidth / sample > maxWidth || bounds.outHeight / sample > maxHeight) sample *= 2
    val image = requireNotNull(BitmapFactory.decodeFile(pattern.path, BitmapFactory.Options().apply { inSampleSize = sample }))
    return RenderedPage(image, 1)
}

@Composable fun PatternCover(pattern: Pattern) {
    val cover by produceState<Bitmap?>(null, pattern.id) {
        value = withContext(Dispatchers.IO) { runCatching { renderPattern(pattern, 0, 240, 320).bitmap }.getOrNull() }
    }
    cover?.let { Image(it.asImageBitmap(), contentDescription = null, modifier = Modifier.width(88.dp).height(120.dp).background(Color.White)) }
}

@Composable fun PatternViewer(pattern: Pattern, project: Project, vm: CraftViewModel, modifier: Modifier) {
    var page by remember(project.id) { mutableIntStateOf(project.page) }
    var zoom by remember(project.id) { mutableFloatStateOf(project.zoom) }
    var panX by remember(project.id) { mutableFloatStateOf(project.panX) }
    var panY by remember(project.id) { mutableFloatStateOf(project.panY) }
    var rendered by remember(pattern.id) { mutableStateOf<RenderedPage?>(null) }
    var error by remember(pattern.id) { mutableStateOf<String?>(null) }
    fun persist() {
        val p = page; val z = zoom; val x = panX; val y = panY
        if (project.id.isEmpty()) vm.position("pattern:${pattern.id}") { vm.repo.dao.patternPosition(pattern.id, p, z, x, y) }
        else vm.position(project.id) { vm.repo.dao.position(project.id, p, z, x, y, System.currentTimeMillis()) }
    }
    LaunchedEffect(pattern.id, page) {
        error = null
        rendered = null
        runCatching { withContext(Dispatchers.IO) { renderPattern(pattern, page) } }
            .onSuccess { rendered = it }.onFailure { error = it.message ?: "Unable to read pattern" }
    }
    Column(modifier.padding(vertical = 12.dp)) {
        Row(Modifier.fillMaxWidth().horizontalScrollCompat(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TextButton(enabled = page > 0, onClick = { page--; zoom = 1f; panX = 0f; panY = 0f; persist() }) { Text(s(R.string.previous)) }
            Text(s(R.string.page_number, page + 1, rendered?.pages ?: 1), Modifier.padding(top = 14.dp))
            TextButton(enabled = page + 1 < (rendered?.pages ?: 1), onClick = { page++; zoom = 1f; panX = 0f; panY = 0f; persist() }) { Text(s(R.string.next)) }
            TextButton(onClick = { zoom = 1f; panX = 0f; panY = 0f; persist() }) { Text(s(R.string.fit)) }
            TextButton(onClick = { zoom = (zoom + 0.5f).coerceAtMost(5f); persist() }) { Text(s(R.string.zoom_in)) }
        }
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).clipToBounds().background(Color(0xFF414A44))) {
            val width = constraints.maxWidth.toFloat(); val height = constraints.maxHeight.toFloat()
            val bitmap = rendered?.bitmap
            if (error != null) Text(s(R.string.viewer_error), color = Color.White, modifier = Modifier.padding(24.dp))
            else if (bitmap == null) CircularProgressIndicator(Modifier.padding(24.dp))
            else Image(bitmap.asImageBitmap(), contentDescription = s(R.string.pattern_page, page + 1), modifier = Modifier.fillMaxSize()
                .pointerInput(pattern.id, page) {
                    detectTransformGestures { _, pan, factor, _ ->
                        zoom = (zoom * factor).coerceIn(1f, 5f)
                        panX = (panX + pan.x / width).coerceIn(-(zoom - 1) / 2, (zoom - 1) / 2)
                        panY = (panY + pan.y / height).coerceIn(-(zoom - 1) / 2, (zoom - 1) / 2)
                        persist()
                    }
                }.graphicsLayer { scaleX = zoom; scaleY = zoom; translationX = panX * width; translationY = panY * height })
        }
    }
}
@Composable private fun Modifier.horizontalScrollCompat(): Modifier = this.then(Modifier.horizontalScroll(androidx.compose.foundation.rememberScrollState()))
