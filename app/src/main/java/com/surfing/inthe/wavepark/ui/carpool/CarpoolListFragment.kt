package com.surfing.inthe.wavepark.ui.carpool

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.surfing.inthe.wavepark.databinding.FragmentCarpoolListBinding

class CarpoolListFragment : Fragment() {
    private var _binding: FragmentCarpoolListBinding? = null
    private val binding get() = _binding!!

    private lateinit var carpoolAdapter: CarpoolAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarpoolListBinding.inflate(inflater, container, false)
        setupTabs()
        setupRecyclerView()
        return binding.root
    }

    private fun setupTabs() {
        binding.tabCarpoolType.addTab(binding.tabCarpoolType.newTab().setText("카풀 제공"))
        binding.tabCarpoolType.addTab(binding.tabCarpoolType.newTab().setText("카풀 요청"))
        binding.tabCarpoolType.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                carpoolAdapter.setType(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        carpoolAdapter = CarpoolAdapter()
        binding.recyclerViewCarpool.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewCarpool.adapter = carpoolAdapter
        carpoolAdapter.submitList(CarpoolSampleData.getList(0))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class CarpoolAdapter : androidx.recyclerview.widget.ListAdapter<CarpoolData, CarpoolViewHolder>(CarpoolDiffCallback()) {
        private var type = 0 // 0: 제공, 1: 요청
        fun setType(type: Int) {
            this.type = type
            submitList(CarpoolSampleData.getList(type))
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarpoolViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = com.surfing.inthe.wavepark.databinding.ItemCarpoolCardBinding.inflate(inflater, parent, false)
            return CarpoolViewHolder(binding)
        }
        override fun onBindViewHolder(holder: CarpoolViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }

    inner class CarpoolViewHolder(private val binding: com.surfing.inthe.wavepark.databinding.ItemCarpoolCardBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(data: CarpoolData) {
            binding.textCarpoolType.text = data.type
            binding.textCarpoolTime.text = data.time
            binding.textCarpoolRoute.text = data.route
            binding.textCarpoolPeople.text = data.people
            binding.textCarpoolBoard.text = data.board
            binding.textCarpoolCost.text = data.cost
        }
    }
}

// 샘플 데이터 및 데이터 클래스
object CarpoolSampleData {
    fun getList(type: Int): List<CarpoolData> = if (type == 0) 제공 else 요청
    private val 제공 = listOf(
        CarpoolData("왕복", "출발 08:00 / 복귀 18:00", "서울 → 웨이브파크", "탑승 3명", "보드 캐리 가능", "비용 10,000원"),
        CarpoolData("편도", "출발 09:00", "인천 → 웨이브파크", "탑승 2명", "보드 캐리 불가", "비용 7,000원")
    )
    private val 요청 = listOf(
        CarpoolData("왕복", "출발 10:00 / 복귀 19:00", "수원 → 웨이브파크", "요청 1명", "보드 캐리 요청", "비용 12,000원"),
        CarpoolData("편도", "출발 11:00", "부천 → 웨이브파크", "요청 2명", "보드 캐리 무관", "비용 8,000원")
    )
}
data class CarpoolData(
    val type: String,
    val time: String,
    val route: String,
    val people: String,
    val board: String,
    val cost: String
)
class CarpoolDiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<CarpoolData>() {
    override fun areItemsTheSame(oldItem: CarpoolData, newItem: CarpoolData) = oldItem === newItem
    override fun areContentsTheSame(oldItem: CarpoolData, newItem: CarpoolData) = oldItem == newItem
} 