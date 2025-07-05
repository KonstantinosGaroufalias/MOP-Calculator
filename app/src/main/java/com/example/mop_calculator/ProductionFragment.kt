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

        // Production inputs
        val morningInput = view.findViewById<EditText>(R.id.inputMorning)
        val afternoonInput = view.findViewById<EditText>(R.id.inputAfternoon)
        val nightInput = view.findViewById<EditText>(R.id.inputNight)

        // Hours inputs
        val morningHoursInput = view.findViewById<EditText>(R.id.inputMorningHours)
        val afternoonHoursInput = view.findViewById<EditText>(R.id.inputAfternoonHours)
        val nightHoursInput = view.findViewById<EditText>(R.id.inputNightHours)

        val saveBtn = view.findViewById<Button>(R.id.saveBtn)
        val totalText = view.findViewById<TextView>(R.id.totalText)
        val totalHoursText = view.findViewById<TextView>(R.id.totalHoursText)
        val mopText = view.findViewById<TextView>(R.id.mopText)

        // Calendar date change listener
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            viewModel.loadDay(selectedDate)
        }

        // Save button click listener
        saveBtn.setOnClickListener {
            val morning = morningInput.text.toString().toIntOrNull() ?: 0
            val morningHours = morningHoursInput.text.toString().toDoubleOrNull() ?: 0.0

            val afternoon = afternoonInput.text.toString().toIntOrNull() ?: 0
            val afternoonHours = afternoonHoursInput.text.toString().toDoubleOrNull() ?: 0.0

            val night = nightInput.text.toString().toIntOrNull() ?: 0
            val nightHours = nightHoursInput.text.toString().toDoubleOrNull() ?: 0.0

            // Save all shifts
            viewModel.saveShift(selectedDate, "ΠΡΩΙ", morning, morningHours)
            viewModel.saveShift(selectedDate, "ΑΠΟΓ", afternoon, afternoonHours)
            viewModel.saveShift(selectedDate, "ΒΡΑΔ", night, nightHours)
        }

        // Observe day stats
        viewModel.dayLive.observe(viewLifecycleOwner) { stats ->
            // Update production inputs
            morningInput.setText(if (stats.morning > 0) stats.morning.toString() else "")
            afternoonInput.setText(if (stats.afternoon > 0) stats.afternoon.toString() else "")
            nightInput.setText(if (stats.night > 0) stats.night.toString() else "")

            // Update hours inputs
            morningHoursInput.setText(if (stats.morningHours > 0) stats.morningHours.toString() else "")
            afternoonHoursInput.setText(if (stats.afternoonHours > 0) stats.afternoonHours.toString() else "")
            nightHoursInput.setText(if (stats.nightHours > 0) stats.nightHours.toString() else "")

            // Update results
            totalText.text = getString(R.string.total_fmt, stats.total)
            totalHoursText.text = getString(R.string.total_hours_fmt, stats.totalHours)
            mopText.text = getString(R.string.mop_fmt, stats.mop)
        }

        // Load current day data
        viewModel.loadDay(selectedDate)
    }
}
