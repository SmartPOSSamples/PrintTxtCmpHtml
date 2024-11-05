package com.cloudpos.demo.print;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudpos.demo.print.util.HtmlFileUtils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private WebView webView;
    private TextView message;
    private String htmlContent;
    private Handler handler;
    private Handler mHandler;
    private HandleCallBack callBack;
    private PrinterModel printer;
    private static long lines;
    private ExecutorService threadPool;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        setContentView(R.layout.activity_main);
        Button textBtn1 = findViewById(R.id.printText1);
        Button textBtn2 = findViewById(R.id.printText2);
        Button htmlBtn1 = findViewById(R.id.printHtml1);
        Button htmlBtn2 = findViewById(R.id.printHtml2);
        message = findViewById(R.id.message);
        textBtn1.setOnClickListener(this);
        textBtn2.setOnClickListener(this);
        htmlBtn1.setOnClickListener(this);
        htmlBtn2.setOnClickListener(this);
        webView = (WebView) findViewById(R.id.wv_webview);
        webView.enableSlowWholeDocumentDraw();
        threadPool = Executors.newSingleThreadExecutor();
        handler = new Handler(handleCallBack);
        callBack = new HandleCallbackImpl(this, handler);
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String message = (String) msg.obj;
                message = lines + " lines of receipt， " + message;
                callBack.sendResponse(HandleCallbackImpl.SUCCESS_CODE, message);
            }
        };
        ;
        printer = PrinterModel.getInstance(this);
        printer.setHandler(mHandler);
        showTicket("default.html");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.printText1:
                showTicket("receipt_60.txt");
                lines = 60;
                threadPool.execute(() -> printer.printTextSync(htmlContent));
                break;
            case R.id.printText2:
                showTicket("receipt_120.txt");
                lines = 120;
                threadPool.execute(() -> printer.printTextSync(htmlContent));
                break;
            case R.id.printHtml1:
                showTicket("receipt_60.html");
                lines = 60;
                printer.printHtmlAsync(htmlContent);
                break;
            case R.id.printHtml2:
                showTicket("receipt_120.html");
                lines = 120;
                printer.printHtmlAsync(htmlContent);
                break;
            default:
                break;
        }
    }

    public void showTicket(String filePath) {
        try {
            htmlContent = HtmlFileUtils.readHtmlFile(getAssets(), filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("ACTIVITY", "ACTIVITY DESTROYED! ");
    }

    private Handler.Callback handleCallBack = msg -> {
        switch (msg.what) {
            case HandleCallbackImpl.SUCCESS_CODE:
                setTextcolor(msg.obj.toString(), Color.BLUE);
                break;
            case HandleCallbackImpl.ERROR_CODE:
                setTextcolor(msg.obj.toString(), Color.RED);
                break;
            default:
                setTextcolor(msg.obj.toString(), Color.BLACK);
                break;
        }
        return false;
    };

    private void setTextcolor(String msg, int color) {
        Spannable span = Spannable.Factory.getInstance().newSpannable(msg);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
        span.setSpan(colorSpan, 0, span.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        message.append(span);
    }

}