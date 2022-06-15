package com.twiliorn.library;

import android.content.Context;
import android.util.Log;

import com.facebook.react.bridge.Promise;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import tvi.webrtc.ThreadUtils;

public class StethoscopeDevice {
    public static final String TAG = CustomAudioDevice.class.getClass().getSimpleName();

    private static final long THREAD_JOIN_TIMEOUT_MS = 2000;
    private static final String PROGRAM_COMMAND = "su -c ./rec_steth";

    private final CustomPathUtils utils;
    private Context context;
    private SafePromise<String> stethoscopeDeviceStartPromise;

    private Thread stethoscopeDeviceThread;

    public StethoscopeDevice(Context context) {
        this.context = context;
        utils = new CustomPathUtils(context);
    }


    public void start(SafePromise<String> promise) {
        try {
            stethoscopeDeviceStartPromise = promise;
            this.stethoscopeDeviceThread = new Thread(stethoscopeDeviceRunnable);
            this.stethoscopeDeviceThread.start();
        } catch (Exception e) {
            Log.e(TAG, "Stethoscope start failed: " + e.getMessage());

            e.printStackTrace();

            promise.reject(e);
        }
    }

    public void stop(SafePromise promise) {
        try {
            stethoscopeDeviceStartPromise = null;
            if(stethoscopeDeviceThread == null)
            {
                promise.resolve(null);
                return;
            }
            this.stethoscopeDeviceThread.interrupt();
            if (!ThreadUtils.joinUninterruptibly(this.stethoscopeDeviceThread, THREAD_JOIN_TIMEOUT_MS)) {
                Log.e(TAG, "Join of stethoscopeDeviceThread timed out");
                promise.reject("-1", "Join of stethoscopeDeviceThread timed out");
            }
            promise.resolve(null);
        } catch (Exception e) {
            Log.e(TAG, "Stethoscope stop failed: " + e.getMessage());
            e.printStackTrace();
            promise.reject(e);
        }
    }

    public void recordToFile(String path, int timeout, SafePromise<String> promise) {
        Thread thread = new Thread(() ->{
            int retV       = -1;
            String command = buildWriteToFileCommand(path, timeout);

            try {
                Process process = Runtime.getRuntime().exec(command);
                process.waitFor();
                retV = process.exitValue();

                if(retV != 0) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    promise.reject(String.valueOf(retV), in.readLine());
                }
            } catch (IOException e) {
                e.printStackTrace();
                promise.reject(String.valueOf(-1), e.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
                promise.reject(String.valueOf(-1), e.getMessage());
            }


            try {
                Process process = execMakeFileReadable(path);
                process.waitFor();

                retV = process.exitValue();

                if(retV != 0) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    promise.reject(String.valueOf(retV), in.readLine());
                }
            } catch (IOException e) {
                e.printStackTrace();
                promise.reject(String.valueOf(-1), e.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
                promise.reject(String.valueOf(-1), e.getMessage());
            }

            if(retV == 0) {
                promise.resolve(path);
            } else {
                promise.reject("-1", "Something went wrong");
            }
        });
        thread.start();
    }

    public void recordToFile(String path, SafePromise<String> promise) {
        recordToFile(path, 20, promise);
    }

    private String getPath() {
        return utils.getStethoscopePipePath();
    }

    private Runnable stethoscopeDeviceRunnable = new Runnable() {
        @Override
        public void run() {
            int retV       = -1;
            String path    = getPath();
            String command = buildWriteToPipeCommand(path);

            Process recordingProcess = null;
            Process chmodProcess = null;

            try {
                recordingProcess = Runtime.getRuntime().exec(command);
                if(retV != 0) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(recordingProcess.getErrorStream()));
                    stethoscopeDeviceStartPromise.reject(String.valueOf(retV), in.readLine());
                }

                chmodProcess = execMakeFileReadable(path);
                chmodProcess.waitFor();

                retV = chmodProcess.exitValue();
                if(retV != 0) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(chmodProcess.getErrorStream()));
                    stethoscopeDeviceStartPromise.reject(String.valueOf(retV), in.readLine());
                }

                if(retV == 0) {
                    stethoscopeDeviceStartPromise.resolve(path);
                }

                recordingProcess.waitFor();
            } catch (IOException e) {
                e.printStackTrace();
                stethoscopeDeviceStartPromise.reject(String.valueOf(-1), e.getMessage());

                if(recordingProcess != null) recordingProcess.destroy();
                if(chmodProcess != null) chmodProcess.destroy();
            } catch (InterruptedException e) {
                e.printStackTrace();
                stethoscopeDeviceStartPromise.reject(String.valueOf(-1), e.getMessage());

                if(recordingProcess != null) recordingProcess.destroy();
                if(chmodProcess != null) chmodProcess.destroy();
            }
        }

    };

    private Process execMakeFileReadable(String path) throws IOException {
        return Runtime.getRuntime().exec("su -c chmod 666 " + path);
    }


    private String buildWriteToFileCommand(String path, int timeout) {
        return PROGRAM_COMMAND + " -t " + timeout + " -o " + path;
    }

    private String buildWriteToPipeCommand(String path) {
        return PROGRAM_COMMAND + "-s " + path;
    }
}
