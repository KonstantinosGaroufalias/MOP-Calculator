package com.example.mop_calculator

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SimpleExportHelper {

    companion object {
        fun exportMonthToCSV(
            context: Context,
            month: String, // "2025-06"
            data2F: List<ShiftEntry>,
            data3F: List<ShiftEntry>,
            onComplete: (success: Boolean, filePath: String?) -> Unit
        ) {
            try {
                val fileName = "MOP_Αναφορά_${month.replace("-", "_")}.csv"
                val file = File(context.getExternalFilesDir(null), fileName)

                FileWriter(file).use { writer ->
                    // Write header
                    writer.append("Ημερομηνία,Τύπος,Βάρδια,Παραγωγή,Ώρες,Μ.Ο.Π.\n")

                    // Combine and sort all data by date
                    val allData = (data2F + data3F).sortedBy { it.date }

                    // Write data rows
                    allData.forEach { entry ->
                        val mop = if (entry.hours > 0) entry.quantity / entry.hours else 0.0
                        val formattedDate = formatDate(entry.date)

                        writer.append("$formattedDate,${entry.type},${entry.shift},${entry.quantity},${entry.hours},${String.format("%.2f", mop)}\n")
                    }

                    // Add summary section
                    writer.append("\n--- ΣΥΝΟΛΑ ---\n")

                    // 2F Summary
                    val total2F = data2F.sumOf { it.quantity }
                    val hours2F = data2F.sumOf { it.hours }
                    val mop2F = if (hours2F > 0) total2F / hours2F else 0.0
                    writer.append("Σύνολο,2Φ,ΟΛΑ,$total2F,${String.format("%.1f", hours2F)},${String.format("%.2f", mop2F)}\n")

                    // 3F Summary
                    val total3F = data3F.sumOf { it.quantity }
                    val hours3F = data3F.sumOf { it.hours }
                    val mop3F = if (hours3F > 0) total3F / hours3F else 0.0
                    writer.append("Σύνολο,3Φ,ΟΛΑ,$total3F,${String.format("%.1f", hours3F)},${String.format("%.2f", mop3F)}\n")

                    // Final MOP
                    val finalMOP = if (mop2F > 0 && mop3F > 0) (mop2F + mop3F) / 2.0 else 0.0
                    writer.append("ΤΕΛΙΚΟ Μ.Ο.Π.,ΣΥΝΟΛΟ,ΟΛΑ,,${String.format("%.1f", hours2F + hours3F)},${String.format("%.2f", finalMOP)}\n")
                }

                onComplete(true, file.absolutePath)

            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false, null)
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
                date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            } catch (e: Exception) {
                dateString
            }
        }

        fun shareCSVFile(context: Context, filePath: String) {
            try {
                val file = File(filePath)
                val uri = FileProvider.getUriForFile(
                    context,
                    "com.example.mop_calculator.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Μηνιαία Αναφορά Παραγωγής")
                    putExtra(Intent.EXTRA_TEXT, "Μηνιαία αναφορά παραγωγής σε μορφή CSV (ανοίγει με Excel)")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(Intent.createChooser(shareIntent, "Κοινοποίηση Αναφοράς"))

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
