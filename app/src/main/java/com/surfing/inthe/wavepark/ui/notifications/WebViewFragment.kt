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
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import org.jsoup.Jsoup
import org.jsoup.Connection
import org.jsoup.nodes.Document
import kotlinx.coroutines.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.surfing.inthe.wavepark.databinding.FragmentWebviewBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.fragment.app.viewModels
import com.surfing.inthe.wavepark.data.model.Reservation
import com.surfing.inthe.wavepark.ui.notifications.ReservationListActivity
import com.surfing.inthe.wavepark.ui.notifications.ReservationViewModel

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
    private var webViewUserAgent: String? = null

    // SharedPreferences 키
    companion object {
        private const val PREF_NAME = "INFO"
        private const val KEY_USERNAME = "appId"
        private const val KEY_PASSWORD = "appPw"
        private const val KEY_IS_LOGGED_IN = "isOnAuto"
        private const val KEY_SESSION_COOKIE = "session_cookie"
        private const val TAG = "WebViewFragment"
    }

    // ViewModel Hilt 주입
    private val reservationViewModel: ReservationViewModel by viewModels()

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
        binding.fabMain.setOnClickListener {
            // 리스트 화면으로 이동
            val intent = Intent(requireContext(), ReservationListActivity::class.java)
            startActivity(intent)
        }
    }
    

    

    
    private fun showParsedDataDialog(title: String, data: Map<String, Any>) {
        val message = buildString {
            appendLine("파싱된 데이터:")
            data.forEach { (key, value) ->
                when (value) {
                    is String -> appendLine("$key: $value")
                    is List<*> -> appendLine("$key: ${value.size}개 항목")
                    else -> appendLine("$key: $value")
                }
            }
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("확인", null)
            .show()
    }
    
    private fun showDialog(title: String, message: String) {
        activity?.runOnUiThread {
            AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("확인", null)
            .show()
        }

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

        // userAgentString을 미리 저장
        webViewUserAgent = settings.userAgentString

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
            var isFirstInit = true
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
                if (url == "https://wavepark.co.kr/login" && isLoggedIn){
                    performBackgroundLogin()
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
                // WebView 로딩 완료 후 1초 뒤에 예약 크롤링 시작
                if (isFirstInit && isLoggedIn){
                    isFirstInit = false
                    Handler(Looper.getMainLooper()).postDelayed({
                        crawlMyPageReservationsAsync(3)
                    }, 1000)
                }



                
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
                        onLoginSuccess()
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

    private fun checkLoginStatus(webView: WebView?) {
        webView?.let { view ->
            val statusScript = """
                (function() {
                    // 현재 페이지에서 로그인 상태 확인
                    var isLoggedIn = document.querySelector('.login-layer-pop') !== null;
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
    
    /**
     * 웹뷰의 쿠키를 추출하여 문자열로 반환
     */
    private fun getWebViewCookies(): String {
        val cookieManager = CookieManager.getInstance()
        val cookies = cookieManager.getCookie("https://wavepark.co.kr")
        return cookies ?: ""
    }
    
    /**
     * 웹뷰의 User-Agent를 가져오기
     */
    private fun getWebViewUserAgent(): String {
        return webViewUserAgent ?: "WaveParkApp"
    }
    
    /**
     * 로그인된 세션으로 특정 URL에 접근하여 Jsoup으로 파싱
     * @param url 파싱할 URL
     * @param callback 파싱 결과를 받을 콜백
     */
    fun parseUrlWithSession(url: String, callback: (Document?, String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 웹뷰의 쿠키와 User-Agent 가져오기
                val cookies = getWebViewCookies()
                val userAgent = getWebViewUserAgent()
                
                if (cookies.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        callback(null, "쿠키가 없습니다. 로그인이 필요합니다.")
                    }
                    return@launch
                }
                
                // Jsoup으로 요청 보내기 (더 많은 브라우저 헤더 추가)
                val connection: Connection = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .header("Cookie", cookies)
                    .header("Referer", "https://wavepark.co.kr/")
                    .header("Host", "www.wavepark.co.kr")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("Connection", "keep-alive")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("Cache-Control", "max-age=0")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-User", "?1")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-Dest", "document")
                    .timeout(10000)
                    .followRedirects(true)
                
                val document: Document = connection.get()
                println("[예약파싱]  ${document.toString()}")
                withContext(Dispatchers.Main) {
                    callback(document, null)
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(null, "파싱 오류: ${e.message}")
                }
            }
        }
    }
    
    /**
     * 특정 URL의 데이터를 파싱하여 콜백으로 반환
     * @param url 파싱할 URL
     * @param callback 파싱된 데이터를 받을 콜백
     */
    fun parseSpecificData(url: String, callback: (Map<String, Any>?, String?) -> Unit) {
        parseUrlWithSession(url) { document, error ->
            if (error != null) {
                callback(null, error)
                return@parseUrlWithSession
            }
            
            document?.let { doc ->
                try {
                    val data = mutableMapOf<String, Any>()
                    
                    // 예시: 특정 요소들 파싱
                    // 제목
                    val title = doc.select("h1, h2, .title").firstOrNull()?.text()
                    title?.let { data["title"] = it }
                    
                    // 내용
                    val content = doc.select(".content, .body, .text").firstOrNull()?.text()
                    content?.let { data["content"] = it }
                    
                    // 이미지
                    val images = doc.select("img").map { it.attr("src") }
                    if (images.isNotEmpty()) {
                        data["images"] = images
                    }
                    
                    // 링크
                    val links = doc.select("a").map { 
                        mapOf(
                            "text" to it.text(),
                            "href" to it.attr("href")
                        )
                    }
                    if (links.isNotEmpty()) {
                        data["links"] = links
                    }
                    
                    // 테이블 데이터
                    val tables = doc.select("table").map { table ->
                        table.select("tr").map { row ->
                            row.select("td, th").map { it.text() }
                        }
                    }
                    if (tables.isNotEmpty()) {
                        data["tables"] = tables
                    }
                    
                    callback(data, null)
                    
                } catch (e: Exception) {
                    callback(null, "데이터 파싱 오류: ${e.message}")
                }
            }
        }
    }
    
    // 로그인 성공 후 예약내역 자동 크롤링
    private fun onLoginSuccess() {
        showFabLoading(true)
//        crawlMyPageReservationsAsync(3)
    }
    // FAB ProgressBar 표시/숨김
    private fun completeFabLoading() {
        activity?.runOnUiThread {
            binding.fabProgress.visibility = View.GONE
            binding.fabMain.visibility = View.VISIBLE
        }
    }
    // FAB ProgressBar 표시/숨김
    private fun showFabLoading(show: Boolean) {
        activity?.runOnUiThread {
            binding.fabProgress.visibility = if (show) View.VISIBLE else View.GONE
            binding.fabMain.visibility = if (!show) View.VISIBLE else View.GONE
        }
    }

    // 3페이지를 async로 동시에 요청, 예약일자 오늘 이후만 collect, 각 페이지마다 emit
    fun crawlMyPageReservationsAsync(maxPage: Int = 3) {
        reservationViewModel.clearReservations()
        reservationViewModel.setLoading(true)
        showFabLoading(true)
        Log.d(TAG, "[예약크롤] 크롤링 시작")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val today = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
                val deferreds = (1..maxPage).map { page ->
                    async {
                        try {
                            Log.d(TAG, "[예약크롤] page=$page 크롤링 시작")
                            val url = if (page == 1) "https://www.wavepark.co.kr/mypage" else "https://www.wavepark.co.kr/mypage?page=$page"
                            val cookies = getWebViewCookies()
                            val userAgent = getWebViewUserAgent()
                            val connection: org.jsoup.Connection = org.jsoup.Jsoup.connect(url)
                                .userAgent(userAgent)
                                .header("Cookie", cookies)
                                .header("Host", "www.wavepark.co.kr")
                                .header("Referer", "https://wavepark.co.kr/")
                                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                                .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                                .header("Connection", "keep-alive")
                                .header("Upgrade-Insecure-Requests", "1")
                                .header("Cache-Control", "max-age=0")
                                .header("Sec-Fetch-Mode", "navigate")
                                .header("Sec-Fetch-User", "?1")
                                .header("Sec-Fetch-Site", "same-origin")
                                .header("Sec-Fetch-Dest", "document")
                                .timeout(30000)
                                .followRedirects(true)
                            val document = connection.get()
                            val table = document.select("table.basic").firstOrNull()
                            val reservations = mutableListOf<com.surfing.inthe.wavepark.data.model.Reservation>()
                            if (table != null) {
                                val rows = table.select("tbody tr.list-item-boxer")
                                for (row in rows) {
                                    val cells = row.select("td")
                                    if (cells.size >= 6) {
                                        try {
                                            val dateStr = cells[1].text().trim()
                                            val date = LocalDate.parse(dateStr, formatter)
                                            if (date.isAfter(today) || date.isEqual(today)) {
                                                val applyDateStr = cells[2].text().trim()
                                                val applyDate = LocalDate.parse(applyDateStr, formatter)
                                                
                                                // LocalDate를 Date로 변환
                                                val sessionDate = java.util.Date.from(date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
                                                val createdAt = java.util.Date.from(applyDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
                                                
                                                val reservation = Reservation(
                                                    reservationNumber = cells[0].select("a").firstOrNull()?.attr("href")?.split("/")?.lastOrNull() ?: cells[0].text().trim(),
                                                    sessionDate = sessionDate,
                                                    sessionTime = "09:00", // 기본값, 실제로는 파싱 필요
                                                    sessionType = cells[3].text().trim(),
                                                    remainingSeats = 10, // 기본값, 실제로는 파싱 필요
                                                    totalSeats = 20, // 기본값, 실제로는 파싱 필요
                                                    price = 50000, // 기본값, 실제로는 파싱 필요
                                                    status = if (cells[5].text().trim() == "결제완료") "confirmed" else "pending"
                                                )
                                                reservations.add(reservation)
                                            }
                                        } catch (_: Exception) {}
                                    }
                                }
                            }
                            Log.d(TAG, "[예약크롤] page=$page 크롤링 완료, 수집: ${reservations.size}")
                            // 각 페이지마다 emit
                            if (reservations.isNotEmpty()) {
                                Log.d(TAG, "[예약크롤] page=$page addReservations 호출")
                                reservationViewModel.addReservations(reservations)
                                reservationViewModel.setLoading(false)
                                completeFabLoading()
                            }
                            reservations
                        } catch (e: Exception) {
                            Log.e(TAG, "[예약크롤] page=$page 크롤링 오류: ${e.message}", e)
                            emptyList<Reservation>()
                        }
                    }
                }
                deferreds.awaitAll()
                Log.d(TAG, "[예약크롤] 모든 페이지 완료, 최종: ${reservationViewModel.reservations.value.size}")
                // 모든 페이지 완료 후 무조건 로딩 해제
                reservationViewModel.setLoading(false)
                showFabLoading(false)
            } catch (e: Exception) {
                Log.e(TAG, "[예약크롤] 크롤링 오류: ${e.message}", e)
                reservationViewModel.setLoading(false)
                showFabLoading(false)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}