package com.musabber.pomofocus.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object AppIcons {
    val iconMap = mapOf<String, ImageVector>(
        "Book" to Icons.Filled.Book,
        "MenuBook" to Icons.Filled.MenuBook,
        "Psychology" to Icons.Filled.Psychology,
        "WorkspacePremium" to Icons.Filled.WorkspacePremium,
        "Laptop" to Icons.Filled.Laptop,
        "Edit" to Icons.Filled.Edit,
        "AutoStories" to Icons.Filled.AutoStories,
        "Lightbulb" to Icons.Filled.Lightbulb,
        "School" to Icons.Filled.School,
        "Computer" to Icons.Filled.Computer,
        "Desk" to Icons.Filled.Desk,
        "Chair" to Icons.Filled.Chair,
        "Headset" to Icons.Filled.Headset,
        "Timer" to Icons.Filled.Timer,
        "Favorite" to Icons.Filled.Favorite,
        "Star" to Icons.Filled.Star,
        "Bolt" to Icons.Filled.Bolt,
        "LocalLibrary" to Icons.Filled.LocalLibrary,
        "EmojiObjects" to Icons.Filled.EmojiObjects,
        "Brush" to Icons.Filled.Brush
    )

    fun getIcon(name: String): ImageVector {
        return iconMap[name] ?: Icons.Filled.Book
    }
}