package net.ifmain.hwanultoktok.kmp.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 *
 * @author gayoung.
 * @since 2025. 8. 14.
 */
class FavoriteExchangeRateWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FavoriteExchangeRateWidget()
}