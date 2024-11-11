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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PrinterModel {

    private static volatile PrinterModel instance;
    private PrinterDevice device;
    private Handler handler;
    private long startTime;
    private BlockingQueue<Runnable> taskQueue;
    private ThreadPoolExecutor executorService;
    private ExecutorService threadPool;

    private final PrinterHtmlListener printerHtmlListener = new PrinterHtmlListener() {
        @Override
        public void onGet(Bitmap bitmap, int i) {

        }

        @Override
        public void onFinishPrinting(int i) {
            threadPool.execute(()->{
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
            });
        }
    };

    private PrinterModel(Context mContext) {
        if (device == null) {
            taskQueue = new ArrayBlockingQueue<>(1);
            executorService = new ThreadPoolExecutor(
                    1, 1, 0L, TimeUnit.MILLISECONDS, taskQueue,
                    new ThreadPoolExecutor.DiscardOldestPolicy()
            );
            threadPool = Executors.newSingleThreadExecutor();
            device = (PrinterDevice) POSTerminal.getInstance(mContext).getDevice("cloudpos.device.printer");
            try {
                device.open();
            } catch (DeviceException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void addTextTask(PrintTextTask task) {
        executorService.execute(task);
    }
    public void addHtmlTask(PrintHtmlTask task) {
        executorService.execute(task);
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
            byte[] fontSize = new byte[] {0x1B, 0x21, 0x11};
            byte[] typeface = new byte[] {0x1B, 0x74, 0x00};
            device.sendESCCommand(fontSize);
            device.sendESCCommand(typeface);
            device.printHTML(htmlContent, printerHtmlListener);
        } catch (DeviceException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void printTextSync(String text) {
        try {
            startTime = System.currentTimeMillis();
            byte[] fontSize = new byte[] {0x1B, 0x21, 0x11};
            byte[] typeface = new byte[] {0x1B, 0x74, 0x00};
            device.sendESCCommand(fontSize);
            device.sendESCCommand(typeface);
            device.printlnText(text);
            threadPool.execute(()->{
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
            });
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

    public void shutdown() {
        if (device!=null){
            try {
                device.close();
            } catch (DeviceException e) {
                throw new RuntimeException(e);
            }
        }
        executorService.shutdown();
    }

    class PrintTextTask implements Runnable{
        private String content;

        public PrintTextTask(String content) {
            this.content = content;
        }

        @Override
        public void run() {
            printTextSync(content);
        }
    }

    class PrintHtmlTask implements Runnable{
        private String content;

        public PrintHtmlTask(String content) {
            this.content = content;
        }

        @Override
        public void run() {
            printHtmlAsync(content);
        }
    }
}


