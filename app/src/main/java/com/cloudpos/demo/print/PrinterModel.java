package com.cloudpos.demo.print;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cloudpos.DeviceException;
import com.cloudpos.POSTerminal;
import com.cloudpos.printer.PrinterDevice;
import com.cloudpos.printer.PrinterHtmlListener;

import java.util.concurrent.ThreadPoolExecutor;

public class PrinterModel {

    private static volatile PrinterModel instance;
    private PrinterDevice device;
    private Context context;
    private Handler handler;
    private long startTime;
    private final PrinterHtmlListener printerHtmlListener = new PrinterHtmlListener() {
        @Override
        public void onGet(Bitmap bitmap, int i) {

        }

        @Override
        public void onFinishPrinting(int i) {
            try {
                device.isPrintingDone(1000 * 60);
            } catch (DeviceException e) {
                throw new RuntimeException(e);
            }
            long endTime = System.currentTimeMillis();
            long interval = endTime - startTime;
            String msg = "Printer Html: finish tag: " + i + " execution time: " + interval + "ms";
            Log.e("PrinterModel", msg);
            sendMessage(msg);
        }
    };

    private PrinterModel(Context mContext) {
        if (device == null) {
            context = mContext;
            device = (PrinterDevice) POSTerminal.getInstance(mContext).getDevice("cloudpos.device.printer");
            try {
                device.open();
            } catch (DeviceException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static PrinterModel getInstance(Context mContext) {
        if (instance == null) {
            synchronized (PrinterModel.class) {
                if (instance == null) {
                    instance = new PrinterModel(mContext);
                }
            }
        }
        return instance;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void printHtmlAsync(String htmlContent) {
        try {
            startTime = System.currentTimeMillis();
            device.printHTML(htmlContent, printerHtmlListener);
        } catch (DeviceException e) {
            throw new RuntimeException(e);
        }
    }

    public void printTextSync(String text) {
        try {
            startTime = System.currentTimeMillis();
            device.printText(text);
            boolean isDone = false;
            try {
                isDone = device.isPrintingDone(1000 * 60);
            } catch (DeviceException e) {
                throw new RuntimeException(e);
            }
            long endTime = System.currentTimeMillis();
            long interval = endTime - startTime;
            String msg = "Printer text: finish success: execution time: " + interval + "ms";
            Log.e("PrinterModel", msg);
            msg = msg + " is Done: " + (isDone ? "true" : "false");
            sendMessage(msg);
        } catch (DeviceException e) {
            throw new RuntimeException(e);
        }

    }

    public void sendMessage(String msg) {
        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Message message = handler.obtainMessage();
                    message.obj = msg;
                    handler.sendMessage(message);
                }
            });
        }
    }

}


