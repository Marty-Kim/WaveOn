package com.surfing.inthe.wavepark

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.surfing.inthe.wavepark.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import android.view.View
import android.view.View.VISIBLE
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.surfing.inthe.wavepark.ui.dashboard.DashboardFragment
import com.surfing.inthe.wavepark.ui.home.HomeFragment
import com.surfing.inthe.wavepark.ui.home.HomeViewModel
import com.surfing.inthe.wavepark.ui.notifications.ReservationListActivity
import com.surfing.inthe.wavepark.ui.notifications.WebViewFragment
import kotlin.getValue

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val fragmentManager = supportFragmentManager
    private var activeFragment: Fragment? = null

    private val fragments = mapOf(
        R.id.navigation_home to HomeFragment(),
        R.id.navigation_dashboard to DashboardFragment(),
        R.id.navigation_webview to WebViewFragment()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        // 최초에 모든 Fragment add, 나머지는 hide
        fragments.forEach { (id, fragment) ->
            fragmentManager.beginTransaction()
                .add(R.id.nav_host_fragment_activity_main, fragment, id.toString())
                .hide(fragment)
                .commit()
        }
        // 첫 화면만 show
        val firstFragment = fragments[R.id.navigation_home]!!
        fragmentManager.beginTransaction().show(firstFragment).commit()
        activeFragment = firstFragment

        // 바텀 네비게이션 리스너
        navView.setOnItemSelectedListener { item ->
            val selectedFragment = fragments[item.itemId]!!
            if (activeFragment != selectedFragment) {
                fragmentManager.beginTransaction()
                    .hide(activeFragment!!)
                    .show(selectedFragment)
                    .commit()
                activeFragment = selectedFragment
            }
            binding.fabMain.visibility = if (activeFragment is WebViewFragment) View.GONE else VISIBLE
            collapseFabMenu()
            true
        }

        setupFloatingActionButtons()
    }

    private var isFabExpanded = false

    private fun setupFloatingActionButtons() {
        val fabMain = binding.fabMain
        val fabContainer = binding.fabContainer
        val fabCallCenter = binding.fabCallCenter
        val fabReservation = binding.fabReservation
        val fabCarpool = binding.fabCarpool

        fabMain.setOnClickListener {
            toggleFabMenu()
        }
        fabCallCenter.setOnClickListener {
//            Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
            collapseFabMenu()
            val url = "https://open.kakao.com/o/svHiFBeb"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
        fabReservation.setOnClickListener {
//            Toast.makeText(this, "준비중", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, ReservationListActivity::class.java))
            collapseFabMenu()
        }
        fabCarpool.setOnClickListener {
            // 카풀 리스트 화면으로 이동 (add/hide/show 방식으로 통일)
            Toast.makeText(this, "카풀 준비중", Toast.LENGTH_SHORT).show()
//            val carpoolFragment = fragments[R.id.navigation_dashboard]!! // 예시: DashboardFragment로 이동
//            if (activeFragment != carpoolFragment) {
//                fragmentManager.beginTransaction()
//                    .hide(activeFragment!!)
//                    .show(carpoolFragment)
//                    .commit()
//                activeFragment = carpoolFragment
//            }
            collapseFabMenu()
        }
    }

    private fun toggleFabMenu() {
        if (isFabExpanded) {
            collapseFabMenu()
        } else {
            expandFabMenu()
        }
    }

    private fun expandFabMenu() {
        isFabExpanded = true
        binding.fabMain.animate().rotation(45f).setDuration(300).start()
        binding.fabContainer.visibility = View.VISIBLE
        binding.fabContainer.alpha = 0f
        binding.fabContainer.animate().alpha(1f).setDuration(300).start()
        binding.fabCallCenter.animate().translationY(0f).setDuration(300).start()
        binding.fabReservation.animate().translationY(-40f).setDuration(300).start()
        binding.fabCarpool.animate().translationY(-80f).setDuration(300).start()
    }

    private fun collapseFabMenu() {
        isFabExpanded = false
        binding.fabMain.animate().rotation(0f).setDuration(300).start()
        binding.fabContainer.animate().alpha(0f).setDuration(300).withEndAction {
            binding.fabContainer.visibility = View.GONE
        }.start()
        binding.fabCallCenter.animate().translationY(0f).setDuration(300).start()
        binding.fabReservation.animate().translationY(0f).setDuration(300).start()
        binding.fabCarpool.animate().translationY(0f).setDuration(300).start()
    }
}