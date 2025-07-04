package com.example.mop_calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StatsFragment : Fragment() {
    private lateinit var viewModel: ProductionViewModel
    private lateinit var type: String

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
        type = arguments?.getString("type") ?: "2F"

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val statsText = view.findViewById<TextView>(R.id.statsText)

        // Load current month stats
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        viewModel.loadMonth(currentMonth)

        // Observe month stats
        viewModel.monthLive.observe(viewLifecycleOwner) { monthStats ->
            val statsDisplay = buildString {
                append("📊 Στατιστικά ${type} για ${getMonthName(currentMonth)}\n\n")

                append("🔢 Μέσος Όρος Μήνα: ${String.format("%.1f", monthStats.monthAvg)}\n\n")

                append("📈 Καθημερινοί Μέσοι Όροι:\n")
                if (monthStats.dailyAverages.isNotEmpty()) {
                    monthStats.dailyAverages.forEachIndexed { index, avg ->
                        append("Ημέρα ${index + 1}: ${String.format("%.1f", avg)}\n")
                    }
                } else {
                    append("Δεν υπάρχουν δεδομένα για τον τρέχοντα μήνα.\n")
                }

                append("\n📋 Συνολικές Πληροφορίες:\n")
                append("• Ημέρες με δεδομένα: ${monthStats.dailyAverages.size}\n")
                append("• Μέγιστος Μ.Ο.: ${String.format("%.1f", monthStats.dailyAverages.maxOrNull() ?: 0.0)}\n")
                append("• Ελάχιστος Μ.Ο.: ${String.format("%.1f", monthStats.dailyAverages.minOrNull() ?: 0.0)}\n")
            }

            statsText.text = statsDisplay
        }
    }

    private fun getMonthName(yearMonth: String): String {
        return when (yearMonth.substring(5)) {
            "01" -> "Ιανουάριος"
            "02" -> "Φεβρουάριος"
            "03" -> "Μάρτιος"
            "04" -> "Απρίλιος"
            "05" -> "Μάιος"
            "06" -> "Ιούνιος"
            "07" -> "Ιούλιος"
            "08" -> "Αύγουστος"
            "09" -> "Σεπτέμβριος"
            "10" -> "Οκτώβριος"
            "11" -> "Νοέμβριος"
            "12" -> "Δεκέμβριος"
            else -> yearMonth
        }
    }
}
