package com.cloudpos.demo.print;

import android.app.Service;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

public class PrinterService extends Service {

    private PrinterModel printer;
    private static final String TAG = "PrinterService";
    public static final String PRINT_TEXT_ASYNC = "PRINT_TEXT_ASYNC";
    public static final String PRINT_HTML_ASYNC = "PRINT_HTML_ASYNC";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        printer = PrinterModel.getInstance(this);
        HandlerThread handlerThread = new HandlerThread("PrintHandlerThread");
        handlerThread.start();
        Log.d("PrinterService", "Service Created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        String textContent = intent.getStringExtra("printText");
        if (action != null) {
            switch (action) {
                case PRINT_TEXT_ASYNC:
                    printer.printTextSync(textContent);
                    break;
                case PRINT_HTML_ASYNC:
                    printer.printHtmlAsync(textContent);
                    break;
                default:
                    Log.d(TAG, "Unknown action: " + action);
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("PrinterService", "Service Destroyed");
    }
}

