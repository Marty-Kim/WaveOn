package com.surfing.inthe.wavepark

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
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        setupFloatingActionButtons()
    }

    private var isFabExpanded = false

    private fun setupFloatingActionButtons() {
        val fabMain = binding.fabMain
        val fabContainer = binding.fabContainer
        val fabEvent = binding.fabEvent
        val fabReservation = binding.fabReservation
        val fabCarpool = binding.fabCarpool

        fabMain.setOnClickListener {
            toggleFabMenu()
        }
        fabEvent.setOnClickListener {
            Toast.makeText(this, "이벤트 기능", Toast.LENGTH_SHORT).show()
            collapseFabMenu()
        }
        fabReservation.setOnClickListener {
            Toast.makeText(this, "예약 기능", Toast.LENGTH_SHORT).show()
            collapseFabMenu()
        }
        fabCarpool.setOnClickListener {
            // 카풀 리스트 화면으로 이동
            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            navController.navigate(R.id.navigation_carpool_list)
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
        binding.fabEvent.animate().translationY(-80f).setDuration(300).start()
        binding.fabReservation.animate().translationY(-160f).setDuration(300).start()
        binding.fabCarpool.animate().translationY(-240f).setDuration(300).start()
    }

    private fun collapseFabMenu() {
        isFabExpanded = false
        binding.fabMain.animate().rotation(0f).setDuration(300).start()
        binding.fabContainer.animate().alpha(0f).setDuration(300).withEndAction {
            binding.fabContainer.visibility = View.GONE
        }.start()
        binding.fabEvent.animate().translationY(0f).setDuration(300).start()
        binding.fabReservation.animate().translationY(0f).setDuration(300).start()
        binding.fabCarpool.animate().translationY(0f).setDuration(300).start()
    }
}