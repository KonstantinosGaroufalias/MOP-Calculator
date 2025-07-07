package com.example.mop_calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.time.LocalDate

class SummaryFragment : Fragment() {
    private lateinit var viewModel: SummaryViewModel
    private var selectedDate = LocalDate.now()

    companion object {
        fun newInstance(): SummaryFragment {
            return SummaryFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = SummaryViewModel.Factory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[SummaryViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        val summaryText = view.findViewById<TextView>(R.id.summaryText)

        // Calendar date change listener
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            viewModel.loadSummary(selectedDate)
        }

        // Observe summary stats
        viewModel.summaryLive.observe(viewLifecycleOwner) { stats ->
            val summaryDisplay = buildString {
                append("📊 ΣΥΝΟΛΙΚΑ ΑΠΟΤΕΛΕΣΜΑΤΑ\n")
                append("${selectedDate.dayOfMonth}/${selectedDate.monthValue}/${selectedDate.year}\n\n")

                append("🔵 2Φ ΠΑΡΑΓΩΓΗ:\n")
                append("• Σύνολο: ${stats.total2F}\n")
                append("• Ώρες: ${String.format("%.1f", stats.totalHours2F)}\n")
                append("• Μ.Ο.Π.: ${String.format("%.2f", stats.mop2F)}\n\n")

                append("🔴 3Φ ΠΑΡΑΓΩΓΗ:\n")
                append("• Σύνολο: ${stats.total3F}\n")
                append("• Ώρες: ${String.format("%.1f", stats.totalHours3F)}\n")
                append("• Μ.Ο.Π.: ${String.format("%.2f", stats.mop3F)}\n\n")

                append("⭐ ΤΕΛΙΚΟ Μ.Ο.Π. ΗΜΕΡΑΣ:\n")
                append("${String.format("%.2f", stats.finalMOP)}\n\n")

                // NEW: Monthly MOP display
                append("📅 ΜΗΝΙΑΙΟ Μ.Ο.Π.:\n")
                append("${String.format("%.2f", stats.monthlyMOP)}\n")
                append("(Μέσος όρος από 1/${selectedDate.monthValue} έως ${selectedDate.dayOfMonth}/${selectedDate.monthValue})\n\n")

                append("📈 ΣΥΝΟΛΙΚΑ:\n")
                append("• Παραγωγή: ${stats.total2F + stats.total3F}\n")
                append("• Ώρες: ${String.format("%.1f", stats.totalHours2F + stats.totalHours3F)}\n")

                if (stats.finalMOP == 0.0) {
                    append("\n💡 Εισάγετε δεδομένα στις καρτέλες 2Φ και 3Φ\n")
                }

                if (stats.monthlyMOP == 0.0 && stats.finalMOP > 0.0) {
                    append("\n📊 Το μηνιαίο Μ.Ο.Π. θα υπολογιστεί όταν υπάρχουν περισσότερες ημέρες με δεδομένα\n")
                }
            }

            summaryText.text = summaryDisplay
        }

        // Load current day data
        viewModel.loadSummary(selectedDate)
    }
}