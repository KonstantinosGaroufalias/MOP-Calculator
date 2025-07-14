package com.example.mop_calculator

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.mop_calculator.AnimationUtils.bounceClick
import com.example.mop_calculator.AnimationUtils.shakeError
import com.example.mop_calculator.AnimationUtils.successFlash
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StatsFragment : Fragment() {
    private lateinit var viewModel: ProductionViewModel
    private lateinit var type: String
    private var selectedDate = LocalDate.now()

    companion object {
        fun newInstance(type: String): StatsFragment {
            val fragment = StatsFragment()
            val args = Bundle()
            args.putString("type", type)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getString("type") ?: "2Φ"

        val factory = ProductionViewModel.Factory(requireActivity().application, type)
        viewModel = ViewModelProvider(this, factory)[ProductionViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stats, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        val statsText = view.findViewById<TextView>(R.id.statsText)
        val exportButton = view.findViewById<Button>(R.id.exportButton)

        // Calendar date change listener - load stats for selected month
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            val yearMonth = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            viewModel.loadMonth(yearMonth)
        }

        // Export button functionality
        exportButton.setOnClickListener {
            // Add bounce animation if AnimationUtils is available
            try {
                it.bounceClick()
            } catch (e: Exception) {
                // Animation not available, continue without it
            }
            exportCurrentMonth()
        }

        // Load current month stats
        val currentMonth = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        viewModel.loadMonth(currentMonth)

        // Observe month stats
        viewModel.monthLive.observe(viewLifecycleOwner) { monthStats ->
            val monthName = getMonthName(selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM")))

            val statsDisplay = buildString {
                append("📊 Στατιστικά ${type} για ${monthName}\n\n")

                // Monthly totals section
                append("📈 ΜΗΝΙΑΙΑ ΣΥΝΟΛΑ:\n")
                append("• Σύνολο Παραγωγής Μήνα για ${type}: ${monthStats.totalMonthlyProduction}\n")
                append("• Σύνολο Ωρών εργασίας ${type}: ${String.format("%.1f", monthStats.totalMonthlyHours)}\n")
                append("• Μέσος Όρος Μήνα: ${String.format("%.2f", monthStats.monthMOP)}\n\n")

                append("📅 ΚΑΘΗΜΕΡΙΝΟΙ Μ.Ο.Π.:\n")
                if (monthStats.dailyMOP.isNotEmpty()) {
                    monthStats.dailyMOP.forEachIndexed { index, mop ->
                        append("Ημέρα ${index + 1}: ${String.format("%.2f", mop)}\n")
                    }
                } else {
                    append("Δεν υπάρχουν δεδομένα για τον επιλεγμένο μήνα.\n")
                }

                append("\n📋 ΑΝΑΛΥΤΙΚΕΣ ΠΛΗΡΟΦΟΡΙΕΣ:\n")
                append("• Ημέρες με δεδομένα: ${monthStats.dailyMOP.size}\n")
                if (monthStats.dailyMOP.isNotEmpty()) {
                    append("• Μέγιστος Μ.Ο.Π.: ${String.format("%.2f", monthStats.dailyMOP.maxOrNull() ?: 0.0)}\n")
                    append("• Ελάχιστος Μ.Ο.Π.: ${String.format("%.2f", monthStats.dailyMOP.minOrNull() ?: 0.0)}\n")

                    if (monthStats.totalMonthlyHours > 0) {
                        val overallMOP = monthStats.totalMonthlyProduction / monthStats.totalMonthlyHours
                        append("• Συνολικός Μ.Ο.Π. μήνα: ${String.format("%.2f", overallMOP)}\n")
                    }
                }

                if (monthStats.monthMOP == 0.0) {
                    append("\n💡 Εισάγετε δεδομένα στην καρτέλα ${type} για να δείτε στατιστικά\n")
                }

                append("\n📊 Πατήστε το κουμπί εξαγωγής για να μοιραστείτε τα δεδομένα!\n")
            }

            statsText.text = statsDisplay
        }
    }

    private fun exportCurrentMonth() {
        val yearMonth = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = Room.databaseBuilder(
                    requireContext(),
                    AppDatabase::class.java,
                    "mop_database"
                ).build()

                val dao = database.shiftDao()
                val data2F = dao.getMonthShifts(yearMonth, "2F")
                val data3F = dao.getMonthShifts(yearMonth, "3F")

                SimpleExportHelper.exportMonthToCSV(
                    requireContext(),
                    yearMonth,
                    data2F,
                    data3F
                ) { success, filePath ->
                    requireActivity().runOnUiThread {
                        if (success && filePath != null) {
                            Toast.makeText(context, "Εξαγωγή επιτυχής! 📊", Toast.LENGTH_SHORT).show()

                            // Add success animation if available
                            try {
                                view?.findViewById<Button>(R.id.exportButton)?.successFlash()
                            } catch (e: Exception) {
                                // Animation not available, continue without it
                            }

                            SimpleExportHelper.shareCSVFile(requireContext(), filePath)
                        } else {
                            Toast.makeText(context, "Σφάλμα κατά την εξαγωγή ❌", Toast.LENGTH_SHORT).show()

                            // Add error animation if available
                            try {
                                view?.findViewById<Button>(R.id.exportButton)?.shakeError()
                            } catch (e: Exception) {
                                // Animation not available, continue without it
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Σφάλμα: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getMonthName(yearMonth: String): String {
        return when (yearMonth.substring(5)) {
            "01" -> "Ιανουάριος ${yearMonth.substring(0, 4)}"
            "02" -> "Φεβρουάριος ${yearMonth.substring(0, 4)}"
            "03" -> "Μάρτιος ${yearMonth.substring(0, 4)}"
            "04" -> "Απρίλιος ${yearMonth.substring(0, 4)}"
            "05" -> "Μάιος ${yearMonth.substring(0, 4)}"
            "06" -> "Ιούνιος ${yearMonth.substring(0, 4)}"
            "07" -> "Ιούλιος ${yearMonth.substring(0, 4)}"
            "08" -> "Αύγουστος ${yearMonth.substring(0, 4)}"
            "09" -> "Σεπτέμβριος ${yearMonth.substring(0, 4)}"
            "10" -> "Οκτώβριος ${yearMonth.substring(0, 4)}"
            "11" -> "Νοέμβριος ${yearMonth.substring(0, 4)}"
            "12" -> "Δεκέμβριος ${yearMonth.substring(0, 4)}"
            else -> yearMonth
        }
    }
}
