package com.surfing.inthe.wavepark.ui.dashboard

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.surfing.inthe.wavepark.R
import com.surfing.inthe.wavepark.databinding.FragmentDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dashboard 화면의 Fragment (MVVM)
 * ViewModel을 Hilt로 주입받아 LiveData를 관찰, UI를 업데이트.
 */
@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    // Hilt로 ViewModel 주입 (by viewModels())
    private val dashboardViewModel: DashboardViewModel by viewModels()

    private lateinit var reservationAdapter: ReservationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        setupViews()
        observeViewModel()
        return binding.root
    }

    private fun setupViews() {
        // 날짜 선택 버튼 (ImageButton)
        binding.btnDatePicker.setOnClickListener {
            showDatePicker()
        }
        // 예약 리스트
        reservationAdapter = ReservationAdapter()
        binding.recyclerViewReservations.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reservationAdapter
        }
    }

    // ViewModel의 LiveData를 관찰하여 UI 업데이트
    private fun observeViewModel() {
        dashboardViewModel.reservationData.observe(viewLifecycleOwner) { reservations ->
            reservationAdapter.submitList(reservations)
            updateReservationCount(reservations.size)
        }
        dashboardViewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            binding.textSelectedDate.text = formatDisplayDate(date)
        }
        dashboardViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnDatePicker.isEnabled = !isLoading
        }
        dashboardViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        dashboardViewModel.selectedDate.value?.let { dateStr ->
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                calendar.time = dateFormat.parse(dateStr) ?: Date()
            } catch (_: Exception) {}
        }
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.WaveParkDatePickerDialog,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                dashboardViewModel.setSelectedDate(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    private fun formatDisplayDate(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy년 MM월 dd일 (E)", Locale.KOREAN)
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun updateReservationCount(count: Int) {
        binding.textReservationCount.text = getString(R.string.total_sessions, count)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}