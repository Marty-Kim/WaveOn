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
import android.widget.ImageView
import android.widget.Button

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

    private lateinit var dailySessionAdapter: DailySessionAdapter
    private lateinit var emptySessionLayout: View
    private lateinit var btnNextDate: Button

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptySessionLayout = binding.layoutEmptySession
        btnNextDate = binding.btnNextDate
        btnNextDate.setOnClickListener {
            moveToNextDate()
        }
    }

    private fun setupViews() {
        // 날짜 선택 버튼 (ImageButton)
        binding.btnDatePicker.setOnClickListener {
            showDatePicker()
        }
        binding.btnRefresh.setOnClickListener {
            dashboardViewModel.refreshSessions()
        }
        // 세션 리스트
        dailySessionAdapter = DailySessionAdapter()
        binding.recyclerViewReservations.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = dailySessionAdapter
        }
    }

    // ViewModel의 LiveData를 관찰하여 UI 업데이트
    private fun observeViewModel() {
        dashboardViewModel.selectedSessionPairs.observe(viewLifecycleOwner) { pairs ->
            dailySessionAdapter.submitList(pairs)
            binding.recyclerViewReservations.scrollToPosition(0)
            if (pairs.isNullOrEmpty()) {
                binding.recyclerViewReservations.visibility = View.GONE
                emptySessionLayout.visibility = View.VISIBLE
            } else {
                binding.recyclerViewReservations.visibility = View.VISIBLE
                emptySessionLayout.visibility = View.GONE
            }
        }
        dashboardViewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            binding.textSelectedDate.text = formatDisplayDate(date)
        }
        dashboardViewModel.lastRefreshTime.observe(viewLifecycleOwner) { time ->
            binding.textLastRefreshTime.text = "마지막 갱신: $time"
        }
        dashboardViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnDatePicker.isEnabled = !isLoading
            binding.btnRefresh.isEnabled = !isLoading
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
//            R.style.WaveParkDatePickerDialog,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                dashboardViewModel.setSelectedDate(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() + 13 * 24 * 60 * 60 * 1000
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

    private fun moveToNextDate() {
        val currentDate = dashboardViewModel.selectedDate.value ?: return
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val date = sdf.parse(currentDate) ?: return
        val cal = java.util.Calendar.getInstance().apply { time = date }
        cal.add(java.util.Calendar.DATE, 1)
        val nextDate = sdf.format(cal.time)
        dashboardViewModel.setSelectedDate(nextDate)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}