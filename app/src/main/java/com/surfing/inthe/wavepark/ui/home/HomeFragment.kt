package com.surfing.inthe.wavepark.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.surfing.inthe.wavepark.databinding.FragmentHomeBinding
import com.surfing.inthe.wavepark.R
import com.surfing.inthe.wavepark.ui.event.EventListActivity
import com.surfing.inthe.wavepark.ui.viewmodel.TemperatureViewModel
import com.surfing.inthe.wavepark.ui.viewmodel.WeatherViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Home 화면의 Fragment (MVVM)
 * ViewModel을 Hilt로 주입받아 LiveData를 관찰, UI를 업데이트.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Hilt로 ViewModel 주입 (by viewModels())
    private val homeViewModel: HomeViewModel by activityViewModels<HomeViewModel>()
    private val weatherViewModel: WeatherViewModel by activityViewModels<WeatherViewModel>()
    private val temperatureViewModel: TemperatureViewModel by activityViewModels<TemperatureViewModel>()
    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupEventRecyclerView()
        observeViewModel()
        initViews()

        // WeatherViewModel 수동 호출
        weatherViewModel.fetchWeatherData()

        // TemperatureViewModel 수동 호출 (필요한 경우)
         temperatureViewModel.fetchTemperatureData()
        
        return binding.root
    }

    private fun initViews() {

        binding.eventBtn.setOnClickListener {
            startActivity(Intent(context, EventListActivity::class.java))
        }
        binding.fundingCalendar.setOnClickListener {
            startActivity(Intent(ACTION_VIEW, Uri.parse("https://waveparkcalendar.vercel.app/")))
        }
    }

    // RecyclerView 초기화
    private fun setupEventRecyclerView() {
        eventAdapter = EventAdapter()
        binding.recyclerViewEvents.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = eventAdapter
        }
    }

    // ViewModel의 StateFlow를 관찰하여 UI 업데이트
    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        // 이벤트 데이터 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.events.collect { events ->
                eventAdapter.submitList(events.filter {
                    if (it.title.contains("서핑") ||
                        it.title.contains("surfing",true) ||
                        it.title.contains("night",true) ||
                        it.title.contains("surf pass",true) ||
                        it.title.contains("펀딩") ||
                        it.title.contains("레슨") ) true
                    else false
                })
            }
        }
        
        // 로딩 상태 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.isLoading.collect { isLoading ->
                // TODO: 로딩 UI 표시/숨김
            }
        }
        // 날씨 정보 관찰 및 UI 반영

        weatherViewModel.weatherData.observe(viewLifecycleOwner) { weather ->
            println("Weather ${weather.weatherStatus}")
            binding.textWeatherDesc.text = "${weather.weatherStatus}  ${weather.temper}°C"
            
            // 날씨 상태에 따른 아이콘 설정
            val weatherIconRes = weatherViewModel.getWeatherIconRes(weather.weatherStatus)
            binding.imgWeatherIcon.setImageResource(weatherIconRes)
        }
        temperatureViewModel.temperatureData.observe(viewLifecycleOwner) { temperature ->
            binding.textWaterTemp.text = "${String.format("%.1f", temperature.waterTemperature)}°C"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}