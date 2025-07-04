package com.example.mop_calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.time.LocalDate

class ProductionFragment : Fragment() {
    private lateinit var viewModel: ProductionViewModel
    private lateinit var type: String
    private var selectedDate = LocalDate.now()

    companion object {
        fun newInstance(type: String): ProductionFragment {
            val fragment = ProductionFragment()
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
        return inflater.inflate(R.layout.fragment_production, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        val morningInput = view.findViewById<EditText>(R.id.inputMorning)
        val afternoonInput = view.findViewById<EditText>(R.id.inputAfternoon)
        val nightInput = view.findViewById<EditText>(R.id.inputNight)
        val saveBtn = view.findViewById<Button>(R.id.saveBtn)
        val totalText = view.findViewById<TextView>(R.id.totalText)
        val avgText = view.findViewById<TextView>(R.id.avgText)

        // Calendar date change listener
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            viewModel.loadDay(selectedDate)
        }

        // Save button click listener
        saveBtn.setOnClickListener {
            val morning = morningInput.text.toString().toIntOrNull() ?: 0
            val afternoon = afternoonInput.text.toString().toIntOrNull() ?: 0
            val night = nightInput.text.toString().toIntOrNull() ?: 0

            viewModel.saveShift(selectedDate, "ΠΡΩΙ", morning)
            viewModel.saveShift(selectedDate, "ΑΠΟΓ", afternoon)
            viewModel.saveShift(selectedDate, "ΒΡΑΔ", night)

            // Reload data to update UI
            viewModel.loadDay(selectedDate)
        }

        // Observe day stats
        viewModel.dayLive.observe(viewLifecycleOwner) {stats ->
            morningInput.setText(if (stats.morning > 0) stats.morning.toString() else "")
            afternoonInput.setText(if (stats.afternoon > 0) stats.afternoon.toString() else "")
            nightInput.setText(if (stats.night > 0) stats.night.toString() else "")

            totalText.text = getString(R.string.total_fmt, stats.total)
            avgText.text = getString(R.string.avg_fmt, stats.avg)
        }
    }
}
