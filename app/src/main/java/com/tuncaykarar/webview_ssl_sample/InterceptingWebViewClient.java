package com.tuncaykarar.webview_ssl_sample;

import android.content.Context;
import android.net.Uri;
import android.net.http.SslError;
import android.support.annotation.NonNull;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.squareup.mimecraft.FormEncoding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;



/**
 * Created by Summ on 6/16/2018.
 */

public class InterceptingWebViewClient extends WebViewClient {
    public static final String TAG = "InterceptingWebViewClient";

    private Context mContext = null;
    private WebView mWebView = null;
    private PostInterceptJavascriptInterface mJSSubmitIntercept = null;
    private OkHttpClient client = new OkHttpClient();


    public InterceptingWebViewClient(Context context, WebView webView) {
        mContext = context;
        mWebView = webView;

        mJSSubmitIntercept = new PostInterceptJavascriptInterface(this);
        mWebView.addJavascriptInterface(mJSSubmitIntercept, "interception");

    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed();
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        mNextAjaxRequestContents = null;
        mNextFormRequestContents = null;

        view.loadUrl(url);
        return true;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(@NonNull WebView view, @NonNull WebResourceRequest request) {
//        return super.shouldInterceptRequest(view, request);
        return handleRequestViaOkHttp(request.getUrl().toString());
    }



    private SSLContext getSslContext(Context context) throws Exception {

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        InputStream stream = context.getResources().openRawResource(R.raw.marketplace_cert);
        keyStore.load(stream, CERT_PASSWORD.toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
        kmf.init(keyStore, CERT_PASSWORD.toCharArray());
        KeyManager[] keyManagers = kmf.getKeyManagers();
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(keyManagers, null, null);

        return sslContext;

    }

    private final String CERT_PASSWORD = "123363";

    private SSLSocketFactory newSslSocketFactory() {

        try {

            // Get an instance of the Bouncy Castle KeyStore format

            KeyStore trusted = KeyStore.getInstance("BKS");

            // Get the raw resource, which contains the keystore with

            // your trusted certificates (root and any intermediate certs)

            InputStream stream = null;

            try {

                // Initialize the keystore with the provided trusted certificates
                // Provide the password of the keystore
                stream = mContext.getResources().openRawResource(R.raw.marketplace);
                trusted.load(stream, CERT_PASSWORD.toCharArray());

            } finally {
                if (stream != null) {
                    stream.close();
                }
            }


            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);

            tmf.init(trusted);


            KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
            kmf.init(trusted, CERT_PASSWORD.toCharArray());
            KeyManager[] keyManagers = kmf.getKeyManagers();


            SSLContext context = SSLContext.getInstance("TLS");

            context.init(keyManagers, tmf.getTrustManagers(), null);


            return context.getSocketFactory();

        } catch (Exception e) {

            throw new AssertionError(e);

        }

    }

    @NonNull
    private WebResourceResponse handleRequestViaOkHttp(@NonNull String url) {
        try {

            OkHttpClient client = new OkHttpClient.Builder()
                    .sslSocketFactory(newSslSocketFactory())
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Call call = client.newCall(request);

            final okhttp3.Response response = call.execute();

            return new WebResourceResponse(
                    response.header("content-type", "text/plain"), // You can set something other as default content-type
                    response.header("content-encoding", "utf-8"),  // Again, you can set another encoding as default
                    response.body().byteStream()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null; // return response for bad request
        }
    }

    private OkHttpClient getSecureOkHttpClient() {
        CertificateFactory cf = null;
        InputStream cert = null;
        Certificate ca = null;
        SSLContext sslContext = null;
        try {
            cf = CertificateFactory.getInstance("X.509");
            cert = mContext.getResources().openRawResource(R.raw.marketplace_cert); // Place your 'my_cert.crt' file in `res/raw`

            ca = cf.generateCertificate(cert);
            cert.close();

            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, tmf.getTrustManagers(), null);

            client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory())
                    .build();

        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | KeyManagementException e) {
            e.printStackTrace();
        }

        return client;
    }




//    @SuppressLint("LongLogTag")
//    @Override
//    public WebResourceResponse shouldInterceptRequest(final WebView view, final String url) {
//        try {
//            // Our implementation just parses the response and visualizes it. It does not properly handle
//            // redirects or HTTP errors at the moment. It only serves as a demo for intercepting POST requests
//            // as a starting point for supporting multiple types of HTTP requests in a full fletched browser
//
//            // Construct request
//            HttpURLConnection conn = client.open(new URL(url));
//            conn.setRequestMethod(isPOST() ? "POST" : "GET");
//
//            // Write body
//            if (isPOST()) {
//                OutputStream os = conn.getOutputStream();
//                if (mNextAjaxRequestContents != null) {
//                    writeBody(os);
//                } else {
//                    writeForm(os);
//                }
//                os.close();
//            }
//
//            // Read input
//            String charset = conn.getContentEncoding() != null ? conn.getContentEncoding() : Charset.defaultCharset().displayName();
//            String mime = conn.getContentType();
//            byte[] pageContents = IOUtils.readFully(conn.getInputStream());
//
//            // Perform JS injection
//            if (mime.equals("text/html")) {
//                pageContents = PostInterceptJavascriptInterface
//                        .enableIntercept(mContext, pageContents)
//                        .getBytes(charset);
//            }
//
//            // Convert the contents and return
//            InputStream isContents = new ByteArrayInputStream(pageContents);
//
//            return new WebResourceResponse(mime, charset,
//                    isContents);
//        } catch (FileNotFoundException e) {
//            Log.w(TAG, "Error 404: " + e.getMessage());
//            e.printStackTrace();
//
//            return null;        // Let Android try handling things itself
//        } catch (Exception e) {
//            e.printStackTrace();
//
//            return null;        // Let Android try handling things itself
//        }
//    }

    private boolean isPOST() {
        return (mNextFormRequestContents != null || mNextAjaxRequestContents != null);
    }

    private void writeBody(OutputStream out) {
        try {
            out.write(mNextAjaxRequestContents.body.getBytes("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void writeForm(OutputStream out) {
        try {
            JSONArray jsonPars = new JSONArray(mNextFormRequestContents.json);

            // We assume to be dealing with a very simple form here, so no file uploads or anything
            // are possible for reasons of clarity
            FormEncoding.Builder m = new FormEncoding.Builder();
            for (int i = 0; i < jsonPars.length(); i++) {
                JSONObject jsonPar = jsonPars.getJSONObject(i);

                m.add(jsonPar.getString("name"), jsonPar.getString("value"));
                // jsonPar.getString("type");
                // TODO TYPE?
            }
            m.build().writeBodyTo(out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getType(Uri uri) {
        String contentResolverUri = mContext.getContentResolver().getType(uri);
        if (contentResolverUri == null) {
            contentResolverUri = "*/*";
        }
        return contentResolverUri;
    }

    private PostInterceptJavascriptInterface.FormRequestContents mNextFormRequestContents = null;

    public void nextMessageIsFormRequest(PostInterceptJavascriptInterface.FormRequestContents formRequestContents) {
        mNextFormRequestContents = formRequestContents;
    }

    private PostInterceptJavascriptInterface.AjaxRequestContents mNextAjaxRequestContents = null;

    public void nextMessageIsAjaxRequest(PostInterceptJavascriptInterface.AjaxRequestContents ajaxRequestContents) {
        mNextAjaxRequestContents = ajaxRequestContents;
    }

    public File getCacheDir() {
        return null;
    }
}
