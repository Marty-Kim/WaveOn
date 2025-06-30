package com.surfing.inthe.wavepark.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.surfing.inthe.wavepark.databinding.FragmentHomeBinding
import com.surfing.inthe.wavepark.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Home 화면의 Fragment (MVVM)
 * ViewModel을 Hilt로 주입받아 LiveData를 관찰, UI를 업데이트.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Hilt로 ViewModel 주입 (by viewModels())
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupEventRecyclerView()
        observeViewModel()
        return binding.root
    }

    // RecyclerView 초기화
    private fun setupEventRecyclerView() {
        eventAdapter = EventAdapter()
        binding.recyclerViewEvents.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = eventAdapter
        }
    }

    // ViewModel의 LiveData를 관찰하여 UI 업데이트
    private fun observeViewModel() {
        homeViewModel.events.observe(viewLifecycleOwner) { events ->
            events.forEach {
                println("events ${it.title}" )
            }
            eventAdapter.submitList(events)
        }
        // 날씨 정보 관찰 및 UI 반영
        homeViewModel.weatherInfo.observe(viewLifecycleOwner) { info ->
            binding.imgWeatherIcon.setImageResource(info.iconRes)
            binding.textWeatherDesc.text = info.desc
            binding.textWeatherLocation.text = info.location
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}