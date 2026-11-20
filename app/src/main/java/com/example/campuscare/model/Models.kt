package com.example.campuscare.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class PhotoCategory(
    val displayName: String,
    val icon: ImageVector,
    val badgeColor: Color
) {
    ALL(
        displayName = "All",
        icon = Icons.Default.Category,
        badgeColor = Color(0xFF6750A4)
    ),
    ELECTRICITY(
        displayName = "Electricity",
        icon = Icons.Default.Bolt,
        badgeColor = Color(0xFFE69B00)
    ),
    WATER(
        displayName = "Water Supply",
        icon = Icons.Default.WaterDrop,
        badgeColor = Color(0xFF0288D1)
    ),
    INFRASTRUCTURE(
        displayName = "Infrastructure",
        icon = Icons.Default.Apartment,
        badgeColor = Color(0xFFE65100)
    ),
    CLEANLINESS(
        displayName = "Cleanliness",
        icon = Icons.Default.CleaningServices,
        badgeColor = Color(0xFF2E7D32)
    ),
    SAFETY(
        displayName = "Safety",
        icon = Icons.Default.Security,
        badgeColor = Color(0xFFC2185B)
    ),
    FOOD(
        displayName = "Cafeteria",
        icon = Icons.Default.Restaurant,
        badgeColor = Color(0xFFD84315)
    ),
    ACADEMIC(
        displayName = "Academic",
        icon = Icons.Default.School,
        badgeColor = Color(0xFF512DA8)
    ),
    OTHER(
        displayName = "Other",
        icon = Icons.Default.MoreHoriz,
        badgeColor = Color(0xFF616161)
    );

    companion object {
        val assignableCategories: List<PhotoCategory>
            get() = entries.filter { it != ALL }
    }
}

enum class ReportStatus(val label: String, val color: Color) {
    PENDING("Pending", Color(0xFFF57C00)),
    IN_PROGRESS("In Progress", Color(0xFF1976D2)),
    RESOLVED("Resolved", Color(0xFF388E3C))
}

data class Comment(
    val id: String,
    val authorName: String,
    val authorRole: String = "Student",
    val text: String,
    val timestamp: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false
)

data class PhotoPost(
    val id: String,
    val title: String,
    val description: String,
    val category: PhotoCategory,
    val imageUrl: String,
    val authorName: String,
    val location: String,
    val timestamp: String,
    val status: ReportStatus = ReportStatus.PENDING,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val comments: List<Comment> = emptyList()
)
