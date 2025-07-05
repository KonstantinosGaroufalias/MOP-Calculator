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

                append("🔢 Μέσος Όρος Μήνα: ${String.format("%.2f", monthStats.monthMOP)}\n\n")

                append("📈 Καθημερινοί Μ.Ο.Π.:\n")
                if (monthStats.dailyMOP.isNotEmpty()) {
                    monthStats.dailyMOP.forEachIndexed { index, mop ->
                        append("Ημέρα ${index + 1}: ${String.format("%.2f", mop)}\n")
                    }
                } else {
                    append("Δεν υπάρχουν δεδομένα για τον τρέχοντα μήνα.\n")
                }

                append("\n📋 Συνολικές Πληροφορίες:\n")
                append("• Ημέρες με δεδομένα: ${monthStats.dailyMOP.size}\n")
                append("• Μέγιστος Μ.Ο.Π.: ${String.format("%.2f", monthStats.dailyMOP.maxOrNull() ?: 0.0)}\n")
                append("• Ελάχιστος Μ.Ο.Π.: ${String.format("%.2f", monthStats.dailyMOP.minOrNull() ?: 0.0)}\n")

                if (monthStats.monthMOP == 0.0) {
                    append("\n💡 Εισάγετε παραγωγή και ώρες στην καρτέλα ${type}\n")
                }
            }

            statsText.text = statsDisplay
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
