package com.pointlessapps.songbook.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class SongbookWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = SongbookWidget()
}
