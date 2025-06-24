package com.surfing.inthe.wavepark.ui.notifications

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.webkit.*
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.surfing.inthe.wavepark.databinding.FragmentWebviewBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * MVVM의 View(UI) 역할.
 * ViewModel을 Hilt로 주입받아 LiveData를 관찰, UI를 업데이트.
 */
@AndroidEntryPoint
class WebViewFragment : Fragment() {

    private var _binding: FragmentWebviewBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var isLoggedIn = false
    private var savedUsername = ""
    private var savedPassword = ""
    private var isFabExpanded = false

    // SharedPreferences 키
    companion object {
        private const val PREF_NAME = "WaveParkLogin"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_SESSION_COOKIE = "session_cookie"
    }

    // 파일 업로드 콜백
    private val fileChooserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val results = if (data == null || data.data == null) null else arrayOf(data.data!!)
            filePathCallback?.onReceiveValue(results)
        } else {
            filePathCallback?.onReceiveValue(null)
        }
        filePathCallback = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val webViewViewModel =
            ViewModelProvider(this).get(WebViewViewModel::class.java)

        _binding = FragmentWebviewBinding.inflate(inflater, container, false)
        val root: View = binding.root

        loadLoginInfo()
        setupWebView()
        setupFloatingActionButtons()
        binding.webView.loadUrl("https://wavepark.co.kr/")
        return root
    }

    private fun setupFloatingActionButtons() {
        // 메인 FAB 클릭 이벤트
        binding.fabMain.setOnClickListener {
            toggleFabMenu()
        }

        // 예약 버튼 클릭 이벤트
        binding.fabReservation.setOnClickListener {
            handleReservationClick()
        }

        // 이벤트 버튼 클릭 이벤트
        binding.fabEvent.setOnClickListener {
            handleEventClick()
        }
    }

    private fun toggleFabMenu() {
        if (isFabExpanded) {
            // 축소 애니메이션
            collapseFabMenu()
        } else {
            // 확장 애니메이션
            expandFabMenu()
        }
    }

    private fun expandFabMenu() {
        isFabExpanded = true
        
        // 메인 FAB 회전 애니메이션
        binding.fabMain.animate()
            .rotation(45f)
            .setDuration(300)
            .start()

        // 확장 버튼들 표시 및 애니메이션
        binding.fabContainer.visibility = View.VISIBLE
        binding.fabContainer.alpha = 0f
        binding.fabContainer.animate()
            .alpha(1f)
            .setDuration(300)
            .start()

        // 각 버튼 개별 애니메이션
        binding.fabEvent.animate()
            .translationY(-80f)
            .setDuration(300)
            .start()

        binding.fabReservation.animate()
            .translationY(-160f)
            .setDuration(300)
            .start()
    }

    private fun collapseFabMenu() {
        isFabExpanded = false
        
        // 메인 FAB 회전 애니메이션
        binding.fabMain.animate()
            .rotation(0f)
            .setDuration(300)
            .start()

        // 축소 애니메이션
        binding.fabContainer.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                binding.fabContainer.visibility = View.GONE
            }
            .start()

        // 각 버튼 원위치로
        binding.fabEvent.animate()
            .translationY(0f)
            .setDuration(300)
            .start()

        binding.fabReservation.animate()
            .translationY(0f)
            .setDuration(300)
            .start()
    }

    private fun handleReservationClick() {
        // 예약 기능 구현
        Toast.makeText(requireContext(), "예약 기능", Toast.LENGTH_SHORT).show()
        
        // WebView에서 예약 페이지로 이동
        binding.webView.loadUrl("https://wavepark.co.kr/reservation")
        
        // 메뉴 축소
        collapseFabMenu()
    }

    private fun handleEventClick() {
        // 이벤트 기능 구현
        Toast.makeText(requireContext(), "이벤트 기능", Toast.LENGTH_SHORT).show()
        
        // WebView에서 이벤트 페이지로 이동
        binding.webView.loadUrl("https://wavepark.co.kr/events")
        
        // 메뉴 축소
        collapseFabMenu()
    }

    private fun loadLoginInfo() {
        val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        savedUsername = prefs.getString(KEY_USERNAME, "") ?: ""
        savedPassword = prefs.getString(KEY_PASSWORD, "") ?: ""
        isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    private fun saveLoginInfo(username: String, password: String) {
        val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_USERNAME, username)
            putString(KEY_PASSWORD, password)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
        savedUsername = username
        savedPassword = password
        isLoggedIn = true
    }

    private fun clearLoginInfo() {
        val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        savedUsername = ""
        savedPassword = ""
        isLoggedIn = false
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val webView = binding.webView
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.setSupportMultipleWindows(true)
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = false
        settings.displayZoomControls = false
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.userAgentString = settings.userAgentString + " WaveParkApp"

        webView.webChromeClient = object : WebChromeClient() {
            // 파일 업로드 (PG 결제 포함)
            override fun onShowFileChooser(
                view: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@WebViewFragment.filePathCallback?.onReceiveValue(null)
                this@WebViewFragment.filePathCallback = filePathCallback
                val intent = fileChooserParams?.createIntent()
                try {
                    fileChooserLauncher.launch(intent)
                } catch (e: Exception) {
                    this@WebViewFragment.filePathCallback = null
                    Toast.makeText(requireContext(), "파일 선택 오류", Toast.LENGTH_SHORT).show()
                    return false
                }
                return true
            }

            // 팝업/새창 지원
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                val newWebView = WebView(requireContext())
                newWebView.settings.javaScriptEnabled = true
                newWebView.settings.domStorageEnabled = true
                newWebView.webChromeClient = this
                newWebView.webViewClient = webView.webViewClient
                
                val dialog = AlertDialog.Builder(requireContext())
                    .setView(newWebView)
                    .create()
                dialog.show()
                
                newWebView.webChromeClient = this
                (resultMsg?.obj as? WebView.WebViewTransport)?.webView = newWebView
                resultMsg?.sendToTarget()
                return true
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: ""
                // 결제 등 외부 앱 호출 처리
                if (url.startsWith("intent:") || url.startsWith("market:")) {
                    try {
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(requireContext(), "앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                    return true
                }
                if (url.startsWith("tel:")) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse(url))
                    startActivity(intent)
                    return true
                }
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                url?.let { currentUrl ->
                    // 로그인 페이지 감지
                    if (currentUrl.contains("login") || currentUrl.contains("signin")) {
                        if (isLoggedIn && savedUsername.isNotEmpty() && savedPassword.isNotEmpty()) {
                            // 자동 로그인 실행
                            performAutoLogin(view)
                        }
                    }
                    // 로그인 성공 페이지 감지
                    else if (currentUrl.contains("dashboard") || currentUrl.contains("mypage") || currentUrl.contains("profile")) {
                        if (!isLoggedIn) {
                            // 로그인 성공으로 간주하고 정보 저장
                            checkAndSaveLoginInfo(view)
                        }
                    }
                }
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
                // 로그인 성공 감지를 위한 JavaScript 실행
                if (url?.contains("wavepark.co.kr") == true && !isLoggedIn) {
                    checkLoginStatus(view)
                }
            }
        }
    }

    private fun performAutoLogin(webView: WebView?) {
        webView?.let { view ->
            val loginScript = """
                (function() {
                    // 로그인 폼 찾기
                    var usernameField = document.querySelector('input[name="username"], input[name="email"], input[name="id"], input[type="email"]');
                    var passwordField = document.querySelector('input[name="password"], input[type="password"]');
                    var loginButton = document.querySelector('button[type="submit"], input[type="submit"], .login-btn, .btn-login');
                    
                    if (usernameField && passwordField && loginButton) {
                        usernameField.value = '$savedUsername';
                        passwordField.value = '$savedPassword';
                        loginButton.click();
                        return true;
                    }
                    return false;
                })();
            """.trimIndent()
            
            view.evaluateJavascript(loginScript) { result ->
                if (result == "true") {
                    Toast.makeText(requireContext(), "자동 로그인 시도 중...", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkAndSaveLoginInfo(webView: WebView?) {
        webView?.let { view ->
            val checkScript = """
                (function() {
                    // 로그인 상태 확인 (예: 사용자 이름 표시, 로그아웃 버튼 등)
                    var userInfo = document.querySelector('.user-name, .user-info, .profile-name, .welcome-message');
                    var logoutBtn = document.querySelector('.logout, .btn-logout, a[href*="logout"]');
                    
                    if (userInfo || logoutBtn) {
                        return {
                            isLoggedIn: true,
                            username: userInfo ? userInfo.textContent.trim() : ''
                        };
                    }
                    return { isLoggedIn: false, username: '' };
                })();
            """.trimIndent()
            
            view.evaluateJavascript(checkScript) { result ->
                if (result.contains("true")) {
                    // 로그인 성공으로 간주
                    isLoggedIn = true
                    val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply()
                    Toast.makeText(requireContext(), "로그인 정보가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkLoginStatus(webView: WebView?) {
        webView?.let { view ->
            val statusScript = """
                (function() {
                    // 현재 페이지에서 로그인 상태 확인
                    var isLoggedIn = document.querySelector('.user-menu, .profile-menu, .logout-btn') !== null;
                    return isLoggedIn;
                })();
            """.trimIndent()
            
            view.evaluateJavascript(statusScript) { result ->
                if (result == "true" && !isLoggedIn) {
                    isLoggedIn = true
                    val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply()
                }
            }
        }
    }

    // 로그아웃 함수 (필요시 사용)
    fun logout() {
        clearLoginInfo()
        binding.webView.loadUrl("https://wavepark.co.kr/")
        Toast.makeText(requireContext(), "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}