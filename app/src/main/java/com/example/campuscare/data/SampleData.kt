package com.example.campuscare.data

import com.example.campuscare.model.Comment
import com.example.campuscare.model.PhotoCategory
import com.example.campuscare.model.PhotoPost
import com.example.campuscare.model.ReportStatus

object SampleData {
    val samplePosts = listOf(
        PhotoPost(
            id = "post-1",
            title = "Water Leakage Near Science Lab",
            description = "Pipe burst causing water to pool along the walkway near Lab B-12. Could cause slipping hazard.",
            category = PhotoCategory.WATER,
            imageUrl = "https://images.unsplash.com/photo-1542013936693-884638332954?auto=format&fit=crop&w=800&q=80",
            authorName = "Aman Sharma",
            location = "Science Block, 1st Floor",
            timestamp = "15 mins ago",
            status = ReportStatus.IN_PROGRESS,
            likesCount = 8,
            comments = listOf(
                Comment(
                    id = "c1",
                    authorName = "Rohan Verma",
                    authorRole = "Student",
                    text = "I almost slipped here this morning! Thanks for posting.",
                    timestamp = "10 mins ago",
                    likesCount = 3
                ),
                Comment(
                    id = "c2",
                    authorName = "Mr. K. Sharma",
                    authorRole = "Maintenance Staff",
                    text = "Plumbing team has been notified and valve is being shut down.",
                    timestamp = "5 mins ago",
                    likesCount = 5
                )
            )
        ),
        PhotoPost(
            id = "post-2",
            title = "Street Lights Flickering at West Gate",
            description = "Three street lights have been flickering and one is completely dark, making the pathway unsafe at night.",
            category = PhotoCategory.ELECTRICITY,
            imageUrl = "https://images.unsplash.com/photo-1509114397022-ed747cca3f65?auto=format&fit=crop&w=800&q=80",
            authorName = "Priya Patel",
            location = "West Gate Pathway",
            timestamp = "2 hours ago",
            status = ReportStatus.PENDING,
            likesCount = 14,
            comments = listOf(
                Comment(
                    id = "c3",
                    authorName = "Ananya Das",
                    authorRole = "Student",
                    text = "Yes, please fix this ASAP, it gets pitch black after 8 PM.",
                    timestamp = "1 hour ago",
                    likesCount = 4
                )
            )
        ),
        PhotoPost(
            id = "post-3",
            title = "Litter Overflow Behind Hostel C",
            description = "Dustbins are overflowing since yesterday and trash is scattering around the garden area.",
            category = PhotoCategory.CLEANLINESS,
            imageUrl = "https://images.unsplash.com/photo-1611284446314-60a58ac0deb9?auto=format&fit=crop&w=800&q=80",
            authorName = "Karan Singh",
            location = "Hostel C Courtyard",
            timestamp = "4 hours ago",
            status = ReportStatus.RESOLVED,
            likesCount = 19,
            comments = listOf(
                Comment(
                    id = "c4",
                    authorName = "Campus Care Admin",
                    authorRole = "Admin",
                    text = "Sanitation crew visited and emptied all bins at 1 PM.",
                    timestamp = "2 hours ago",
                    likesCount = 7
                ),
                Comment(
                    id = "c5",
                    authorName = "Karan Singh",
                    authorRole = "Student",
                    text = "Confirmed clean now, thank you for quick action!",
                    timestamp = "1 hour ago",
                    likesCount = 2
                )
            )
        ),
        PhotoPost(
            id = "post-4",
            title = "Damaged Desks in Lecture Hall 204",
            description = "Two desk benches have broken wooden supports and exposed screws.",
            category = PhotoCategory.INFRASTRUCTURE,
            imageUrl = "https://images.unsplash.com/photo-1580582932707-520aed937b7b?auto=format&fit=crop&w=800&q=80",
            authorName = "Deepak Rao",
            location = "Academic Block A, Room 204",
            timestamp = "Yesterday",
            status = ReportStatus.PENDING,
            likesCount = 6,
            comments = emptyList()
        ),
        PhotoPost(
            id = "post-5",
            title = "Water Cooler Filter Replacement Needed",
            description = "The water dispenser filter light in Cafeteria 2 is blinking red.",
            category = PhotoCategory.FOOD,
            imageUrl = "https://images.unsplash.com/photo-1527661591475-527312dd65f5?auto=format&fit=crop&w=800&q=80",
            authorName = "Sneha Sen",
            location = "Central Cafeteria",
            timestamp = "Yesterday",
            status = ReportStatus.IN_PROGRESS,
            likesCount = 11,
            comments = listOf(
                Comment(
                    id = "c6",
                    authorName = "Chef Mohan",
                    authorRole = "Cafeteria Staff",
                    text = "New RO filter cartridges have arrived, will be swapped today.",
                    timestamp = "3 hours ago",
                    likesCount = 3
                )
            )
        )
    )

    val quickSamplePhotos = listOf(
        Pair("Water Leak", "https://images.unsplash.com/photo-1542013936693-884638332954?auto=format&fit=crop&w=800&q=80"),
        Pair("Electrical", "https://images.unsplash.com/photo-1509114397022-ed747cca3f65?auto=format&fit=crop&w=800&q=80"),
        Pair("Cleanliness", "https://images.unsplash.com/photo-1611284446314-60a58ac0deb9?auto=format&fit=crop&w=800&q=80"),
        Pair("Classroom", "https://images.unsplash.com/photo-1580582932707-520aed937b7b?auto=format&fit=crop&w=800&q=80"),
        Pair("Cafeteria", "https://images.unsplash.com/photo-1527661591475-527312dd65f5?auto=format&fit=crop&w=800&q=80"),
        Pair("Campus Gate", "https://images.unsplash.com/photo-1562774053-701939374585?auto=format&fit=crop&w=800&q=80")
    )
}
