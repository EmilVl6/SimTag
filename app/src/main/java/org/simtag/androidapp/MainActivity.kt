@file:Suppress("UNREACHABLE_CODE")

package org.simtag.androidapp

import android.annotation.SuppressLint
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.simtag.androidapp.data.SimTag
import org.simtag.androidapp.ui.theme.SimTagTheme
import org.simtag.androidapp.ui.theme.SimTagViewModel
import org.simtag.androidapp.ui.theme.SimTagViewModelFactory
import kotlin.math.abs
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private val simTagViewModel: SimTagViewModel by viewModels {
        SimTagViewModelFactory((application as SimTagApplication).repository)
    }

    @SuppressLint("DiscouragedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        var isBrandingAnimationRunning = true
        val splashStartTime = System.currentTimeMillis()
        val minSplashDuration = 500L

        splashScreen.setKeepOnScreenCondition { isBrandingAnimationRunning }
        super.onCreate(savedInstanceState)
        setContent {
            SimTagTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SimTagAppScreen(simTagViewModel)
                }
            }
        }
        window.decorView.post {
            val splashIconId = resources.getIdentifier("splashscreen_icon_view", "id", packageName)
            val splashIconView = window.decorView.findViewById<View>(splashIconId)
            val drawable = splashIconView?.background
            if (drawable is AnimatedVectorDrawable) {
                drawable.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable?) {
                        val elapsed = System.currentTimeMillis() - splashStartTime
                        val remaining = minSplashDuration - elapsed
                        if (remaining > 0) {
                            window.decorView.postDelayed({
                                isBrandingAnimationRunning = false
                            }, remaining)
                        } else {
                            isBrandingAnimationRunning = false
                        }
                    }
                })
                drawable.start()
            } else {
                val elapsed = System.currentTimeMillis() - splashStartTime
                val remaining = minSplashDuration - elapsed
                if (remaining > 0) {
                    window.decorView.postDelayed({
                        isBrandingAnimationRunning = false
                    }, remaining)
                } else {
                    isBrandingAnimationRunning = false
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimTagAppScreen(viewModel: SimTagViewModel) {
    val tags by viewModel.allSimTags.collectAsStateWithLifecycle()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var currentTagToEdit by remember { mutableStateOf<SimTag?>(null) }
    val cardWidth = 300.dp
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val totalCards = tags.size + 1
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { totalCards }
    )

    LaunchedEffect(pagerState.currentPage) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "SimTag",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val visibleRange = 2.5f // or even 3f for more overlap
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(
                    horizontal = (LocalConfiguration.current.screenWidthDp.dp - cardWidth) / 2,
                    vertical = 32.dp
                ),
                pageSpacing = 0.dp,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val pageOffset = (page - pagerState.currentPage) + pagerState.currentPageOffsetFraction
                val focusAmount = 1f - abs(pageOffset).coerceIn(0f, 1f)
                val scale = 1f + (0.12f * focusAmount)
                val basePadding = 16.dp
                val extraPadding = 8.dp
                val horizontalPadding = basePadding + (extraPadding * (1f - focusAmount))
                val maxRotationY = 70f
                val maxTranslationZ = -300f
                var rotationY = maxRotationY * pageOffset.coerceIn(-1f, 1f)
                var translationZ = maxTranslationZ * abs(pageOffset.coerceIn(-1f, 1f))
                val isCurrent = page == pagerState.currentPage

                if (abs(pageOffset) <= visibleRange) {
                    val tagShape = SvgTagShape(SVG_PATH_STRING)

                    val cardModifier = Modifier
                        .padding(horizontal = horizontalPadding)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            cameraDistance = 48 * density
                            alpha = 1f
                        }
                        .clip(tagShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(
                                color = Color.White,
                                bounded = true
                            )
                        ) {
                            if (page == tags.size) {
                                if (isCurrent) {
                                    currentTagToEdit = null
                                    showDialog = true
                                } else {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(page)
                                    }
                                }
                            } else if (!isCurrent) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                coroutineScope.launch {
                                    delay(120)
                                    pagerState.animateScrollToPage(page)
                                }
                            }
                        }

                    if (page < tags.size) {
                        SimTagCard(
                            tag = tags[page],
                            onEditClick = {
                                currentTagToEdit = tags[page]
                                showDialog = true
                            },
                            onDeleteClick = { viewModel.delete(tags[page]) },
                            modifier = cardModifier
                        )
                    } else {
                        AddTagCard(
                            onClick = {
                                if (isCurrent) {
                                    currentTagToEdit = null
                                    showDialog = true
                                } else {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(page)
                                    }
                                }
                            },
                            modifier = cardModifier
                        )
                    }
                }
            }
        }
        if (showDialog) {
            SimTagEditDialog(
                tagToEdit = currentTagToEdit,
                onDismiss = { showDialog = false },
                onSave = { tag ->
                    if (tag.id == 0) {
                        viewModel.insert(tag)
                    } else {
                        viewModel.update(tag)
                    }
                    showDialog = false
                    currentTagToEdit = null
                }
            )
        }
    }
}

@Composable
fun SimTagCard(
    tag: SimTag,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tagColor = Color(tag.color.toLong())
    val tagShape = SvgTagShape(SVG_PATH_STRING)
    Card(
        modifier = modifier
            .width(300.dp)
            .fillMaxHeight(0.85f)
            .then(Modifier.shadow(8.dp, tagShape, clip = false)) // Custom shadow, not affected by scaling
            .clip(tagShape)
            .drawBehind {
                val outline = tagShape.createOutline(size, layoutDirection, this)
                if (outline is Outline.Generic) {
                    val strokeWidth = 4.dp.toPx()
                    val dashLength = 12.dp.toPx()
                    val gapLength = 8.dp.toPx()
                    drawPath(
                        path = outline.path,
                        color = tagColor.copy(alpha = 0.5f),
                        style = Stroke(
                            width = strokeWidth,
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(
                                    dashLength,
                                    gapLength
                                ), 0f
                            )
                        )
                    )
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // No built-in shadow
        shape = tagShape,
        colors = CardDefaults.cardColors(
            containerColor = tagColor.copy(alpha = 0.15f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Use procedural paper texture instead of image
            PaperTexture(
                modifier = Modifier
                    .matchParentSize()
                    .clip(tagShape)
                    .alpha(0.35f)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 18.dp)
            ) {
                Text(
                    tag.tagName.take(18),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 56.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    tag.fullName?.let { Text("Name: $it", style = MaterialTheme.typography.bodyMedium) }
                    tag.email?.let { Text("Email: $it", style = MaterialTheme.typography.bodyMedium) }
                    tag.phoneNumber?.let { Text("Phone: $it", style = MaterialTheme.typography.bodyMedium) }
                }
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Tag")
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Tag")
                    }
                }
            }
        }
    }
}

@Composable
fun AddTagCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tagShape = SvgTagShape(SVG_PATH_STRING)
    OutlinedCard(
        onClick = onClick,
        modifier = modifier
            .width(300.dp)
            .fillMaxHeight(0.85f)
            .clip(tagShape)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = tagShape
            ),
        shape = tagShape,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Tag",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Add Tag",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimTagEditDialog(
    tagToEdit: SimTag?,
    onDismiss: () -> Unit,
    onSave: (SimTag) -> Unit
) {
    var tagName by rememberSaveable(tagToEdit) { mutableStateOf(tagToEdit?.tagName ?: "") }
    var fullName by rememberSaveable(tagToEdit) { mutableStateOf(tagToEdit?.fullName ?: "") }
    var email by rememberSaveable(tagToEdit) { mutableStateOf(tagToEdit?.email ?: "") }
    var phoneNumber by rememberSaveable(tagToEdit) { mutableStateOf(tagToEdit?.phoneNumber ?: "") }
    val color by rememberSaveable(tagToEdit) { mutableStateOf(tagToEdit?.color ?: 0xFF828148.toInt()) } // <-- fix here
    var tagNameError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (tagToEdit == null) "Add New SimTag" else "Edit SimTag",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = tagName,
                    onValueChange = {
                        if (it.length <= 18) {
                            tagName = it
                            tagNameError = null
                        }
                    },
                    label = { Text("Tag Name*") },
                    isError = tagNameError != null,
                    supportingText = { tagNameError?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(onClick = {
                        if (tagName.isBlank()) {
                            tagNameError = "Tag name is required"
                        } else {
                            val resolvedColor = if (tagToEdit == null) {
                                when (tagName.trim()) {
                                    "Personal" -> 0xFF828148.toInt()
                                    "Work" -> 0xFFC7B793.toInt()
                                    else -> 0xFFFFFFFF.toInt()
                                }
                            } else {
                                color
                            }
                            onSave(
                                SimTag(
                                    id = tagToEdit?.id ?: 0,
                                    tagName = tagName,
                                    fullName = fullName.ifBlank { null },
                                    email = email.ifBlank { null },
                                    phoneNumber = phoneNumber.ifBlank { null },
                                    color = resolvedColor
                                )
                            )
                        }
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun PaperTexture(
    modifier: Modifier = Modifier,
    baseColor: Color = Color(0xFFF8F5E4),
    noiseAlpha: Float = 0.08f
) {
    val scaleFactor = 1.3f
    Canvas(modifier = modifier) {
        // Fill with base color
        drawRect(baseColor)

        // Add subtle noise
        val noiseCount = (size.width * size.height / (120 / (scaleFactor * scaleFactor))).toInt()
        val rnd = kotlin.random.Random(hashCode())
        repeat(noiseCount) {
            val x = rnd.nextFloat() * size.width
            val y = rnd.nextFloat() * size.height
            val radius = (rnd.nextFloat() * 1.5f + 0.5f) * scaleFactor
            drawCircle(
                color = Color.Black.copy(alpha = noiseAlpha * rnd.nextFloat()),
                radius = radius,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }

        // Add a few subtle horizontal lines for paper grain
        val lineCount = 6
        repeat(lineCount) { i ->
            val y = size.height * (i + 1) / (lineCount + 1)
            drawLine(
                color = Color.White.copy(alpha = 0.10f),
                start = androidx.compose.ui.geometry.Offset(0f, y),
                end = androidx.compose.ui.geometry.Offset(size.width, y),
                strokeWidth = 1.5f * scaleFactor
            )
        }
    }
}

class SvgTagShape(
    private val svgPath: String
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = PathParser().parsePathString(svgPath).toPath()
        path.fillType = PathFillType.EvenOdd
        val bounds = path.getBounds()
        if (bounds.width == 0f || bounds.height == 0f) {
            return Outline.Rectangle(Rect(0f, 0f, size.width, size.height))
        }
        val scaleX = size.width / bounds.width
        val scaleY = size.height / bounds.height
        val matrix = Matrix()
        matrix.translate(-bounds.left, -bounds.top)
        matrix.scale(scaleX, scaleY)
        path.transform(matrix)
        return Outline.Generic(path)
    }
}

const val SVG_PATH_STRING = "M0 20 c0 0 1 -4 5 -5 A15 15 0 0 0 15 5 c0 0 1 -4 5 -5 H80 c0 0 4 1 5 5 A15 15 0 0 0 95 15 c0 0 4 1 5 5 V175 c0 0 0 5 -5 5 H5 c0 0 -5 0 -5 -5 Z"