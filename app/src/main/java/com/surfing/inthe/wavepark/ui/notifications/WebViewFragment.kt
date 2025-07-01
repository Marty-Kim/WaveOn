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
import android.graphics.Bitmap
import android.webkit.*
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import org.json.JSONObject
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
        private const val PREF_NAME = "INFO"
        private const val KEY_USERNAME = "appId"
        private const val KEY_PASSWORD = "appPw"
        private const val KEY_IS_LOGGED_IN = "isOnAuto"
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
        
        // 백그라운드 자동 로그인 후 페이지 로드
        performBackgroundLogin()
        
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
        isLoggedIn = prefs.getInt(KEY_IS_LOGGED_IN, -1) > -1
    }

    private fun saveLoginInfo(username: String, password: String) {
        val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_USERNAME, username)
            putString(KEY_PASSWORD, password)
            putInt(KEY_IS_LOGGED_IN, 0)  // 0: 자동로그인 활성화
            apply()
        }
        savedUsername = username
        savedPassword = password
        isLoggedIn = true
    }

    private fun clearLoginInfo() {
        val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt(KEY_IS_LOGGED_IN, -1)
            putString(KEY_USERNAME, "")
            putString(KEY_PASSWORD, "")
            apply()
        }
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

        // JavaScript 인터페이스 추가
        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun onPostRequest(postData: String) {
                try {
                    val jsonObject = JSONObject(postData)
                    val id = jsonObject.getString("site_id")
                    val pw = jsonObject.getString("site_pw")
                    
                    // 로그인 정보 저장
                    saveLoginInfo(id, pw)
                    
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "로그인 정보가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, "Android")

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
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                // 로딩 시작 시 프로그레스바 표시 (선택사항)
                // binding.progressBar.visibility = View.VISIBLE
            }
            
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: ""
                
                // 로그인 성공 후 자동로그인 상태 업데이트
                if (url == "https://wavepark.co.kr/") {
                    val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    if (prefs.getInt(KEY_IS_LOGGED_IN, -1) == 0) {
                        prefs.edit().putInt(KEY_IS_LOGGED_IN, 1).apply()
                    }
                }
                
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



            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
                // 로그인 성공 감지를 위한 JavaScript 실행
                if (url?.contains("wavepark.co.kr") == true && !isLoggedIn) {
                    checkLoginStatus(view)
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                
                // 로딩 완료 시 프로그레스바 숨김 (선택사항)
                // binding.progressBar.visibility = View.GONE
                
                // 폼 제출 감지 JavaScript 주입
                injectFormDetectionScript(view)
            }
        }
    }

    private fun performHttpLogin(username: String, password: String): Boolean {
        return try {
            val url = URL("https://www.wavepark.co.kr/login/doLogin")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            val postData = "returnUri=&site_id=${URLEncoder.encode(username, "UTF-8")}&site_pw=${URLEncoder.encode(password, "UTF-8")}"
            
            val os = connection.outputStream
            os.write(postData.toByteArray())
            os.flush()
            os.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 쿠키 저장
                val headerFields = connection.headerFields
                val cookiesHeader = headerFields["Set-Cookie"]
                
                if (cookiesHeader != null) {
                    for (cookie in cookiesHeader) {
                        CookieManager.getInstance().setCookie("https://www.wavepark.co.kr", cookie)
                    }
                    CookieManager.getInstance().flush()
                }
                true // 로그인 성공
            } else {
                false // 로그인 실패
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false // 에러 발생
        }
    }

    // 백그라운드 자동 로그인 처리
    private fun performBackgroundLogin() {
        if (isLoggedIn && savedUsername.isNotEmpty() && savedPassword.isNotEmpty()) {
            Thread {
                try {
                    // HTTP POST 로그인
                    val success = performHttpLogin(savedUsername, savedPassword)
                    if (success) {
                        // 로그인 성공 시 메인 페이지 로드
                        requireActivity().runOnUiThread {
                            binding.webView.loadUrl("https://wavepark.co.kr/")
                        }
                    } else {
                        // 로그인 실패 시 로그인 페이지로
                        requireActivity().runOnUiThread {
                            binding.webView.loadUrl("https://wavepark.co.kr/login")
                        }
                    }
                } catch (e: Exception) {
                    // 에러 시 로그인 페이지로
                    requireActivity().runOnUiThread {
                        binding.webView.loadUrl("https://wavepark.co.kr/login")
                    }
                }
            }.start()
        } else {
            // 저장된 로그인 정보 없으면 로그인 페이지로
            binding.webView.loadUrl("https://wavepark.co.kr/login")
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
                    prefs.edit().putInt(KEY_IS_LOGGED_IN, 1).apply()
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
                    prefs.edit().putInt(KEY_IS_LOGGED_IN, 1).apply()
                }
            }
        }
    }

    // 폼 제출 감지 JavaScript 주입
    private fun injectFormDetectionScript(view: WebView?) {
        val formScript = """
            (function() {
                document.addEventListener('submit', function(event) {
                    var data = {};
                    var form = event.target;
                    for (var i = 0; i < form.elements.length; i++) {
                        var element = form.elements[i];
                        if (element.tagName.toLowerCase() !== 'button') {
                            data[element.name] = element.value;
                        }
                    }
                    // Android로 데이터 전송
                    if (window.Android && window.Android.onPostRequest) {
                        window.Android.onPostRequest(JSON.stringify(data));
                    }
                });
            })();
        """.trimIndent()
        
        view?.evaluateJavascript(formScript, null)
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