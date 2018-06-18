package com.tuncaykarar.webview_ssl_sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class MainActivity extends AppCompatActivity {

    WebView myWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myWebView = (WebView) findViewById(R.id.webview);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setAppCacheEnabled(true);
        myWebView.loadUrl("https://www.marketplace.unicreditgroup.eu");
//        myWebView.loadUrl("https://developer.android.com/studio/");


//        InterceptingWebViewClient
        myWebView.setWebViewClient(new InterceptingWebViewClient(this,myWebView));
        myWebView.setWebChromeClient(new WebChromeClient());

        myWebView.setWebContentsDebuggingEnabled(true);


//        myWebView.setWebViewClient(new WebViewClient() {
//            @Override
//            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
//                final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
//                String message = "SSL Certificate error.";
//                switch (error.getPrimaryError()) {
//                    case SslError.SSL_UNTRUSTED:
//                        message = "The certificate authority is not trusted.";
//                        break;
//                    case SslError.SSL_EXPIRED:
//                        message = "The certificate has expired.";
//                        break;
//                    case SslError.SSL_IDMISMATCH:
//                        message = "The certificate Hostname mismatch.";
//                        break;
//                    case SslError.SSL_NOTYETVALID:
//                        message = "The certificate is not yet valid.";
//                        break;
//                }
//                message += " Do you want to continue anyway?";
//
//                builder.setTitle("SSL Certificate Error");
//                builder.setMessage(message);
//                builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        handler.proceed();
//                    }
//                });
//                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        handler.cancel();
//                    }
//                });
//                final AlertDialog dialog = builder.create();
//                dialog.show();
//            }
//
//        });

    }
}
