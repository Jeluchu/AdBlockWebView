package com.jeluchu.adblocker

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.adblockplus.libadblockplus.android.settings.AdblockHelper
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private var javaScript: String? = null
    private var userAgentString: String? = null
    private var desktopUserAgent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        javaScript = readJavaScript()

        webView.setProvider(AdblockHelper.get().provider)
        webView.apply {
            settings.javaScriptEnabled = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.loadsImagesAutomatically = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.allowContentAccess = true
            settings.allowFileAccess = true
            settings.domStorageEnabled = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                settings.offscreenPreRaster = true
                setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, false)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }

            userAgentString = settings.userAgentString
            desktopUserAgent = buildDesktopUserAgentString(userAgentString!!)

            mediaFunctions()
        }

        initWebChromeClient()
        initWebViewClient()

        swipeRefresh.setOnRefreshListener { webView.reload() }

        initData()

    }

    private fun initWebChromeClient() {
        val webChromeClient: WebChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress == 100) mediaFunctions()
            }

            override fun onReceivedTitle(view: WebView, title: String) {
                super.onReceivedTitle(view, title)
                mediaFunctions()
            }

            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
            }

            override fun onHideCustomView() {

            }

            override fun onPermissionRequest(request: PermissionRequest) {
                val resources = request.resources
                for (resource in resources) {
                    if (PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID == resource) {
                        request.grant(arrayOf(PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID))
                        return
                    }
                }
                super.onPermissionRequest(request)
            }

        }
        webView.webChromeClient = webChromeClient
    }

    private fun initWebViewClient() {
        val webViewClient: WebViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                swipeRefresh.isRefreshing = true
                mediaFunctions()
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                swipeRefresh!!.isRefreshing = false
                mediaFunctions()
            }
        }
        webView.webViewClient = webViewClient
    }

    private fun mediaFunctions() {
        mediaResetEventListener()
        loadJavaScript(javaScript)
        mediaSetEventListener()
    }

    private fun buildDesktopUserAgentString(agent: String): String {
        return agent
                .replace("Android", "X11;")
                .replace("Mobile", "")
                .replace("wv", "")
                .replace("Version/4.0", "")
    }

    private fun mediaResetEventListener() = loadJavaScript("mediaResetEventListener();")
    private fun mediaSetEventListener() = loadJavaScript("mediaSetEventListener();")
    private fun readJavaScript(): String = readRawResource()

    private fun readRawResource(): String {
        val builder = StringBuilder()
        val inputStream = resources.openRawResource(R.raw.media_functions)
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)
        try {
            while (true) {
                val line = bufferedReader.readLine() ?: break
                builder.append(line)
                builder.append("\n")
            }
        } catch (e: Exception) { }
        return builder.toString()
    }

    private fun loadJavaScript(javaScript: String?) {
        if (javaScript != null) webView.loadUrl("javascript:$javaScript")
    }

    private fun initData() {
        webView.webChromeClient = object : WebChromeClient() {}
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                swipeRefresh.isRefreshing = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                swipeRefresh.isRefreshing = false
            }

        }
        webView?.loadUrl("https://tmofans.com/")
    }

}

