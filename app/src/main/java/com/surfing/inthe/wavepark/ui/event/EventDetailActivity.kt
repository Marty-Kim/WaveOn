package com.surfing.inthe.wavepark.ui.event

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.SimpleAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.surfing.inthe.wavepark.R
import com.surfing.inthe.wavepark.databinding.ActivityEventDetailBinding
import com.surfing.inthe.wavepark.databinding.ActivityEventListBinding
import com.surfing.inthe.wavepark.ui.home.EventItem
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EventDetailActivity : AppCompatActivity() {
    private val viewModel: EventViewModel by viewModels()
    private var _binding: ActivityEventDetailBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val title = intent.getStringExtra("title")
        val date = intent.getStringExtra("date")
        val idx = intent.getStringExtra("idx")
        val webUrl = intent.getStringExtra("webUrl") ?: ""
        binding.eventTitle.text = title
        binding.eventDate.text = date

        val images = intent.getStringArrayListExtra("images") ?: arrayListOf()


        Thread {
            try {
                val url = "https://www.wavepark.co.kr/board/event?act=view/detail/$idx"
                val doc: Document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .get()

                val imageElements = doc.select("article div.con img")
                val imageUrls = imageElements.map { img ->
                    val src = img.attr("src")
                    if (src.startsWith("http")) src
                    else "https://www.wavepark.co.kr$src"
                }

                // 메인 스레드에서 로그 출력 또는 UI 처리
                Handler(Looper.getMainLooper()).post {
                    imageUrls.forEach { url ->
                        Log.d("ImageURL", url)


                        // 여기서 RecyclerView나 Glide 등으로 표시 가능
                    }

                    binding.imageList.adapter = EventDetailImageAdapter(EventItem(
                        event_url = webUrl,
                        title = title ?: "",
                        date = date,
                        event_id = idx,
                        imageList = imageUrls
                    ))
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }


}