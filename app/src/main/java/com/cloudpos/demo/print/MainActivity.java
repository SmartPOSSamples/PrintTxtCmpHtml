package com.cloudpos.demo.print;

import android.annotation.SuppressLint;
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

import androidx.appcompat.app.AppCompatActivity;

import com.cloudpos.demo.print.util.HtmlFileUtils;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private WebView webView;
    private TextView message;
    private String htmlContent;
    private Handler handler;
    private Handler mHandler;
    private HandleCallBack callBack;
    private static long lines;
    private PrinterModel printer;
    Button textBtn1, textBtn2, htmlBtn1, htmlBtn2;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        setContentView(R.layout.activity_main);
        textBtn1 = findViewById(R.id.printText1);
        textBtn2 = findViewById(R.id.printText2);
        htmlBtn1 = findViewById(R.id.printHtml1);
        htmlBtn2 = findViewById(R.id.printHtml2);
        message = findViewById(R.id.message);
        textBtn1.setOnClickListener(this);
        textBtn2.setOnClickListener(this);
        htmlBtn1.setOnClickListener(this);
        htmlBtn2.setOnClickListener(this);
        webView = (WebView) findViewById(R.id.wv_webview);
        webView.enableSlowWholeDocumentDraw();
        handler = new Handler(handleCallBack);
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String message = (String) msg.obj;
                callBack.sendResponse(HandleCallBack.SUCCESS_CODE, message);
                disableButtons(true);
            }
        };
        callBack = new HandleCallbackImpl(this, handler);
        printer = PrinterModel.getInstance(this);
        printer.setHandler(mHandler);
        showTicket("default.html");
    }

    @Override
    public void onClick(View v) {
        disableButtons(false);
        switch (v.getId()) {
            case R.id.printText1:
                showTicket("receipt_60.txt");
                printer.addTextTask(printer. new PrintTextTask(htmlContent));
                break;
            case R.id.printText2:
                showTicket("receipt_120.txt");
                printer.addTextTask(printer. new PrintTextTask(htmlContent));
                break;
            case R.id.printHtml1:
                showTicket("receipt_60.html");
                printer.printHtmlAsync(htmlContent);
                break;
            case R.id.printHtml2:
                showTicket("receipt_120.html");
                printer.printHtmlAsync(htmlContent);
                break;
            default:
                break;
        }
    }

    private void disableButtons(boolean enable) {
        textBtn1.setEnabled(enable);
        textBtn2.setEnabled(enable);
        htmlBtn1.setEnabled(enable);
        htmlBtn2.setEnabled(enable);
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
        if (printer!=null){
            printer.shutdown();
        }
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