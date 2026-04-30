package com.pointlessapps.songbook.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.DynamicThemeColorProviders
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentHeight
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.pointlessapps.songbook.MainActivity
import com.pointlessapps.songbook.R
import kotlin.math.ceil

class SongbookWidget : GlanceAppWidget() {

    companion object {
        const val EXTRA_FILTER_LETTER = "extra_filter_letter"
        const val EXTRA_OPEN_SEARCH = "extra_open_search"
    }

    override val sizeMode = SizeMode.Exact

    private val alphabet = ('A'..'Z').map(Char::toString)
    private val itemSpacing = 5.dp
    private val textPaddingFactor = 0.6f

    private val filterLetterKey = ActionParameters.Key<String>(EXTRA_FILTER_LETTER)
    private val openSearchKey = ActionParameters.Key<Boolean>(EXTRA_OPEN_SEARCH)

    override suspend fun providePreview(context: Context, widgetCategory: Int) = provideContent {
        WidgetContent()
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) = provideContent {
        WidgetContent()
    }

    @Composable
    private fun WidgetContent() {
        val context = LocalContext.current

        GlanceTheme(DynamicThemeColorProviders) {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(
                        color = GlanceTheme.colors.widgetBackground
                            .getColor(context).copy(alpha = 0.5f),
                    )
                    .padding(itemSpacing)
                    .appWidgetBackground()
                    .widgetCornerRadius(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val (width, height, fontSize) = calculateGridDimensions()

                repeat(width) { row ->
                    Row(
                        modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        repeat(height) { col ->
                            val letter = alphabet[row * height + col]
                            LetterCell(
                                letter = letter,
                                fontSize = fontSize,
                                modifier = GlanceModifier.fillMaxHeight().defaultWeight(),
                            )
                        }
                    }
                }

                SearchBar()
            }
        }
    }

    @Composable
    private fun LetterCell(
        letter: String,
        fontSize: TextUnit,
        modifier: GlanceModifier = GlanceModifier,
    ) {
        val context = LocalContext.current
        Box(
            modifier = modifier.padding(itemSpacing),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(
                        color = GlanceTheme.colors.background
                            .getColor(context).copy(alpha = 0.7f),
                    )
                    .widgetCornerRadius()
                    .clickable(
                        actionStartActivity<MainActivity>(
                            actionParametersOf(filterLetterKey to letter),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = letter,
                    style = TextStyle(
                        color = GlanceTheme.colors.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSize,
                        textAlign = TextAlign.Center,
                    ),
                )
            }
        }
    }

    @Composable
    private fun SearchBar() {
        val context = LocalContext.current
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(
                    color = GlanceTheme.colors.background
                        .getColor(context).copy(alpha = 0.7f),
                )
                .clickable(
                    actionStartActivity<MainActivity>(actionParametersOf(openSearchKey to true)),
                )
                .widgetCornerRadius()
                .padding(itemSpacing * 2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                modifier = GlanceModifier.size(18.dp),
                provider = ImageProvider(R.drawable.ic_widget_search),
                contentDescription = null,
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
            )
            Spacer(modifier = GlanceModifier.width(itemSpacing))
            Text(
                text = context.getString(R.string.search_songs),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }

    @Composable
    private fun calculateGridDimensions(): Triple<Int, Int, TextUnit> {
        val size = LocalSize.current
        val totalItems = alphabet.size

        var bestSide = 0.dp
        var bestCols = 1

        for (cols in 1..totalItems) {
            val rows = ceil(totalItems.toDouble() / cols).toInt()
            val cellWidth = size.width / cols
            val cellHeight = size.height / rows

            val side = if (cellWidth < cellHeight) cellWidth else cellHeight

            if (side > bestSide) {
                bestSide = side
                bestCols = cols
            }
        }

        val fontSize = (bestSide.value * textPaddingFactor).sp

        return Triple(totalItems / bestCols, bestCols, fontSize)
    }

    private fun GlanceModifier.widgetCornerRadius(): GlanceModifier {
        val cornerRadiusModifier =
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                GlanceModifier.cornerRadius(android.R.dimen.system_app_widget_background_radius)
            } else {
                GlanceModifier
            }

        return this.then(cornerRadiusModifier)
    }

    private fun getIntent(context: Context, letter: String) =
        Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_FILTER_LETTER, letter)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
}
