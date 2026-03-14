package com.pointlessapps.songbook.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentHeight
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.pointlessapps.songbook.MainActivity
import com.pointlessapps.songbook.R

const val EXTRA_FILTER_LETTER = "extra_filter_letter"
const val EXTRA_OPEN_SEARCH = "extra_open_search"

private val ALPHABET_ROW_1 = ('A'..'I').map { it.toString() }
private val ALPHABET_ROW_2 = ('J'..'R').map { it.toString() }
private val ALPHABET_ROW_3 = ('S'..'Z').map { it.toString() }

class SongbookWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                // Outer Column: exactly 10 children — stays within Glance's RemoteViews limit.
                // The Spacer between Divider and SearchBar was removed; SearchBar uses paddingTop instead.
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(Color(0xFF111218))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Header()                                                       // 1
                    Spacer(modifier = GlanceModifier.height(8.dp))                // 2
                    LetterRow(                                                     // 3
                        context = context,
                        letters = ALPHABET_ROW_1,
                        modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))                // 4
                    LetterRow(                                                     // 5
                        context = context,
                        letters = ALPHABET_ROW_2,
                        modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))                // 6
                    LetterRowLastLine(                                             // 7
                        context = context,
                        modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))                // 8
                    Divider()                                                      // 9
                    SearchBar(context = context)                                   // 10 — uses paddingTop instead of a separate Spacer
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        Text(
            text = "Songbook",
            style = TextStyle(
                color = ColorProvider(R.color.widget_title),
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = "Browse by letter",
            style = TextStyle(color = ColorProvider(R.color.widget_subtitle)),
        )
    }
}

// Each Row has exactly 9 children (cells only, no Spacers between them — Glance caps
// containers at 10 children; 9 cells + 8 Spacers = 17, which silently truncates to 5).
// Visual gaps between cells come from the 2dp inset baked into bg_widget_cell.xml.
@Composable
private fun LetterRow(context: Context, letters: List<String>, modifier: GlanceModifier) {
    Row(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        letters.forEach { letter ->
            LetterCell(
                context = context,
                letter = letter,
                modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
            )
        }
    }
}

// Last row: 8 letters + 1 invisible filler = 9 children, aligning column widths with the rows above.
@Composable
private fun LetterRowLastLine(context: Context, modifier: GlanceModifier) {
    Row(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ALPHABET_ROW_3.forEach { letter ->
            LetterCell(
                context = context,
                letter = letter,
                modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
            )
        }
        // 1 filler to match the 9-column width of the rows above
        Box(modifier = GlanceModifier.defaultWeight().fillMaxHeight()) {}
    }
}

@Composable
private fun LetterCell(context: Context, letter: String, modifier: GlanceModifier) {
    val intent = Intent(context, MainActivity::class.java).apply {
        putExtra(EXTRA_FILTER_LETTER, letter)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    Box(
        modifier = modifier
            .background(ImageProvider(R.drawable.bg_widget_cell))
            .clickable(actionStartActivity(intent)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter,
            style = TextStyle(
                color = ColorProvider(R.color.widget_letter),
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(1.dp)
            .background(ColorProvider(R.color.widget_divider)),
    ) {}
}

@Composable
private fun SearchBar(context: Context) {
    val intent = Intent(context, MainActivity::class.java).apply {
        putExtra(EXTRA_OPEN_SEARCH, true)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 8.dp)
            .background(ImageProvider(R.drawable.bg_widget_search))
            .clickable(actionStartActivity(intent))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget_search),
            contentDescription = null,
            modifier = GlanceModifier.size(18.dp),
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
            text = "Search songs\u2026",
            style = TextStyle(color = ColorProvider(R.color.widget_subtitle)),
        )
    }
}
