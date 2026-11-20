package com.example.campuscare.utils

import android.content.Context
import android.net.Uri
import com.example.campuscare.model.PhotoCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties
import javax.activation.DataHandler
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource

object EmailHelper {

    // Default sender and recipient emails
    const val SENDER_EMAIL = "sheoranpooja726@gmail.com"
    const val RECIPIENT_EMAIL = "poojasheoran825@gmail.com"

    // 16-character Google App Password for sheoranpooja726@gmail.com
    // Paste your 16-character Google App Password here, e.g. "abcd efgh ijkl mnop"
    var SENDER_APP_PASSWORD = "edwuowbuwrnxvcuc"

    fun getAppPassword(context: Context): String {
        if (SENDER_APP_PASSWORD.isNotBlank()) return SENDER_APP_PASSWORD
        val prefs = context.getSharedPreferences("campuscare_email_prefs", Context.MODE_PRIVATE)
        return prefs.getString("sender_app_password", "") ?: ""
    }

    fun saveAppPassword(context: Context, appPassword: String) {
        val prefs = context.getSharedPreferences("campuscare_email_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("sender_app_password", appPassword.trim()).apply()
        SENDER_APP_PASSWORD = appPassword.trim()
    }

    suspend fun autoSendEmail(
        context: Context,
        title: String,
        description: String,
        category: PhotoCategory,
        location: String,
        imageUriString: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val appPassword = getAppPassword(context)

        if (appPassword.isBlank()) {
            return@withContext Result.failure(
                Exception("MISSING_PASSWORD")
            )
        }

        try {
            val formattedDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            val subject = "[CampusCare Report] $title (${category.displayName})"
            val body = buildString {
                appendLine("CampusCare Issue Report")
                appendLine("========================")
                appendLine("From        : $SENDER_EMAIL")
                appendLine("To          : $RECIPIENT_EMAIL")
                appendLine("Category    : ${category.displayName}")
                appendLine("Title       : $title")
                appendLine("Location    : ${location.ifBlank { "Not specified" }}")
                appendLine("Reported At : $formattedDate")
                appendLine()
                appendLine("Description :")
                appendLine(description.ifBlank { "No additional description provided." })
                appendLine()
                appendLine("------------------------")
                appendLine("Sent automatically in background via CampusCare App")
            }

            // Read compressed image bytes (< 400 KB) so JavaMail upload never times out or OOMs
            val imageBytes: ByteArray? = ImageHelper.getCompressedImageBytes(context, imageUriString)

            // Gmail SMTP Configuration
            val props = Properties().apply {
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.port", "465")
                put("mail.smtp.ssl.enable", "true")
                put("mail.smtp.auth", "true")
                put("mail.smtp.socketFactory.port", "465")
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                put("mail.smtp.socketFactory.fallback", "false")
                put("mail.smtp.ssl.protocols", "TLSv1.2")
                put("mail.smtp.connectiontimeout", "30000")
                put("mail.smtp.timeout", "60000")
                put("mail.smtp.writetimeout", "60000")
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(SENDER_EMAIL, appPassword)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(SENDER_EMAIL, "CampusCare App"))
                addRecipient(Message.RecipientType.TO, InternetAddress(RECIPIENT_EMAIL))
                this.subject = subject

                val multipart = MimeMultipart()

                // Text body
                val textPart = MimeBodyPart().apply {
                    setText(body)
                }
                multipart.addBodyPart(textPart)

                // Image attachment
                if (imageBytes != null && imageBytes.isNotEmpty()) {
                    val imagePart = MimeBodyPart().apply {
                        val source = ByteArrayDataSource(imageBytes, "image/jpeg")
                        dataHandler = DataHandler(source)
                        fileName = "campuscare_issue_photo.jpg"
                    }
                    multipart.addBodyPart(imagePart)
                }

                setContent(multipart)
            }

            Transport.send(message)
            android.util.Log.d("EmailHelper", "Email sent successfully to $RECIPIENT_EMAIL")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("EmailHelper", "Failed to send email via SMTP", e)
            Result.failure(e)
        }
    }
}
