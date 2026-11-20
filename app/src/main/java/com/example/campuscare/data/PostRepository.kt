package com.example.campuscare.data

import android.content.Context
import android.util.Log
import com.example.campuscare.model.Comment
import com.example.campuscare.model.PhotoCategory
import com.example.campuscare.model.PhotoPost
import com.example.campuscare.model.ReportStatus
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object PostRepository {

    private const val TAG = "PostRepository"
    private const val FILE_NAME = "campuscare_posts.json"

    fun loadPosts(context: Context): List<PhotoPost> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) {
            val initial = SampleData.samplePosts
            savePosts(context, initial)
            return initial
        }

        return try {
            val jsonString = file.readText()
            if (jsonString.isBlank()) {
                return SampleData.samplePosts
            }
            val jsonArray = JSONArray(jsonString)
            val posts = mutableListOf<PhotoPost>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                posts.add(parsePost(obj))
            }
            if (posts.isEmpty()) SampleData.samplePosts else posts
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load posts from storage, returning sample posts", e)
            SampleData.samplePosts
        }
    }

    fun savePosts(context: Context, posts: List<PhotoPost>) {
        try {
            val jsonArray = JSONArray()
            for (post in posts) {
                jsonArray.put(postToJson(post))
            }
            val file = File(context.filesDir, FILE_NAME)
            file.writeText(jsonArray.toString(2))
            Log.d(TAG, "Successfully saved ${posts.size} posts to persistent storage.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save posts to persistent storage", e)
        }
    }

    private fun postToJson(post: PhotoPost): JSONObject {
        return JSONObject().apply {
            put("id", post.id)
            put("title", post.title)
            put("description", post.description)
            put("category", post.category.name)
            put("imageUrl", post.imageUrl)
            put("authorName", post.authorName)
            put("location", post.location)
            put("timestamp", post.timestamp)
            put("status", post.status.name)
            put("likesCount", post.likesCount)
            put("isLiked", post.isLiked)

            val commentsArray = JSONArray()
            for (comment in post.comments) {
                commentsArray.put(commentToJson(comment))
            }
            put("comments", commentsArray)
        }
    }

    private fun commentToJson(comment: Comment): JSONObject {
        return JSONObject().apply {
            put("id", comment.id)
            put("authorName", comment.authorName)
            put("authorRole", comment.authorRole)
            put("text", comment.text)
            put("timestamp", comment.timestamp)
            put("likesCount", comment.likesCount)
            put("isLiked", comment.isLiked)
        }
    }

    private fun parsePost(obj: JSONObject): PhotoPost {
        val category = try {
            PhotoCategory.valueOf(obj.optString("category", PhotoCategory.OTHER.name))
        } catch (_: Exception) {
            PhotoCategory.OTHER
        }

        val status = try {
            ReportStatus.valueOf(obj.optString("status", ReportStatus.PENDING.name))
        } catch (_: Exception) {
            ReportStatus.PENDING
        }

        val comments = mutableListOf<Comment>()
        val commentsArray = obj.optJSONArray("comments")
        if (commentsArray != null) {
            for (i in 0 until commentsArray.length()) {
                val commentObj = commentsArray.getJSONObject(i)
                comments.add(parseComment(commentObj))
            }
        }

        return PhotoPost(
            id = obj.getString("id"),
            title = obj.getString("title"),
            description = obj.getString("description"),
            category = category,
            imageUrl = obj.getString("imageUrl"),
            authorName = obj.optString("authorName", "Anonymous"),
            location = obj.optString("location", "Campus"),
            timestamp = obj.optString("timestamp", "Recently"),
            status = status,
            likesCount = obj.optInt("likesCount", 0),
            isLiked = obj.optBoolean("isLiked", false),
            comments = comments
        )
    }

    private fun parseComment(obj: JSONObject): Comment {
        return Comment(
            id = obj.getString("id"),
            authorName = obj.optString("authorName", "Anonymous"),
            authorRole = obj.optString("authorRole", "Student"),
            text = obj.getString("text"),
            timestamp = obj.optString("timestamp", "Recently"),
            likesCount = obj.optInt("likesCount", 0),
            isLiked = obj.optBoolean("isLiked", false)
        )
    }
}
