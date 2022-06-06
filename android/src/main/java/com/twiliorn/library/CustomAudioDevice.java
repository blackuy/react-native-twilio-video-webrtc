package com.twiliorn.library;

import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import android.util.Log;

import com.facebook.react.uimanager.ThemedReactContext;
import com.twilio.video.AudioDevice;
import com.twilio.video.AudioDeviceContext;
import com.twilio.video.AudioFormat;

import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import tvi.webrtc.ThreadUtils;

class CustomAudioDevice implements AudioDevice {
    public static final String TAG = CustomAudioDevice.class.getClass().getSimpleName();

    // TIMEOUT for rendererThread and capturerThread to wait for successful call to join()
    private static final long THREAD_JOIN_TIMEOUT_MS = 2000;

    // We want to get as close to 10 msec buffers as possible because this is what the media engine prefers.
    private static final short CALLBACK_BUFFER_SIZE_MS = 10;

    // Default audio data format is PCM 16 bit per sample. Guaranteed to be supported by all devices.
    private static final short BITS_PER_SAMPLE = 16;

    // Ask for a buffer size of BUFFER_SIZE_FACTOR * (minimum required buffer size). The extra space
    // is allocated to guard against glitches under high load.
    private static final short BUFFER_SIZE_FACTOR = 2;
    private static final short WAV_FILE_HEADER_SIZE = 44;

    private static final int  BUFFERS_PER_SECOND = 1000 / CALLBACK_BUFFER_SIZE_MS;

    private final CustomPathUtils utils;

    // Average number of callbacks per second.
    private InputStream inputStream;
    private DataInputStream dataInputStream;

    // buffers
    private int writeBufferSize = 0;

    private ByteBuffer fileWriteByteBuffer;
    private ByteBuffer micWriteBuffer;
    private ByteBuffer readByteBuffer;

    // Handlers and Threads
    private Handler capturerHandler;
    private HandlerThread capturerThread;
    private Handler rendererHandler;
    private HandlerThread rendererThread;

    //Contexts
    private AudioDeviceContext renderingAudioDeviceContext;
    private AudioDeviceContext capturingAudioDeviceContext;

    private AudioTrack audioTrack;

    private AudioRecord audioRecord;

    private boolean keepAliveRendererRunnable;
    private boolean isFilePlaying;

    CustomAudioDevice(ThemedReactContext context) {
        utils = new CustomPathUtils(context);
    }

    public void switchInputToFile() {
        isFilePlaying = true;
        initializeStreams();
        capturerHandler.removeCallbacks(microphoneCapturerRunnable);
        stopRecording();
        capturerHandler.post(fileCapturerRunnable);
    }

    public void switchInputToMic() {
        capturerHandler.removeCallbacks(fileCapturerRunnable);
        capturerHandler.post(microphoneCapturerRunnable);
    }


    @Override
    public AudioFormat getCapturerFormat() {
        return new AudioFormat(AudioFormat.AUDIO_SAMPLE_RATE_44100,
                AudioFormat.AUDIO_SAMPLE_MONO);
    }

    @Override
    public boolean onInitCapturer() {
        int bytesPerFrame = 2 * (BITS_PER_SAMPLE / 8);
        AudioFormat capturerFormat = getCapturerFormat();
        int  framesPerBuffer = capturerFormat.getSampleRate() / BUFFERS_PER_SECOND;
        // Calculate the minimum buffer size required for the successful creation of
        // an AudioRecord object, in byte units.
        int channelConfig = channelCountToConfiguration(capturerFormat.getChannelCount());
        int minBufferSize = AudioRecord.getMinBufferSize(capturerFormat.getSampleRate(),
                channelConfig, android.media.AudioFormat.ENCODING_PCM_16BIT);
        micWriteBuffer = ByteBuffer.allocateDirect(bytesPerFrame * framesPerBuffer);

        ByteBuffer tempMicWriteBuffer = micWriteBuffer;
        int bufferSizeInBytes = Math.max(BUFFER_SIZE_FACTOR * minBufferSize, tempMicWriteBuffer.capacity());
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, capturerFormat.getSampleRate(),
                android.media.AudioFormat.CHANNEL_OUT_STEREO, android.media.AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);
        fileWriteByteBuffer = ByteBuffer.allocateDirect(bytesPerFrame * framesPerBuffer);
        ByteBuffer testFileWriteByteBuffer = fileWriteByteBuffer;
        writeBufferSize = testFileWriteByteBuffer.capacity();
        // Initialize the streams.
        initializeStreams();
        return true;
    }

    @Override
    public boolean onStartCapturing(AudioDeviceContext audioDeviceContext) {
        // Initialize the AudioDeviceContext
        capturingAudioDeviceContext = audioDeviceContext;
        // Create the capturer thread and start
        capturerThread = new HandlerThread("CapturerThread");
        capturerThread.start();
        // Create the capturer handler that processes the capturer Runnables.
        capturerHandler = new Handler(capturerThread.getLooper());
        isFilePlaying = true;
        capturerHandler.post(fileCapturerRunnable);
        return true;
    }

    @Override
    public boolean onStopCapturing() {
        if (isFilePlaying) {
            isFilePlaying = false;
            closeStreams();
        } else {
            stopRecording();
        }
        /*
         * When onStopCapturing is called, the AudioDevice API expects that at the completion
         * of the callback the capturer has completely stopped. As a result, quit the capturer
         * thread and explicitly wait for the thread to complete.
         */
        capturerThread.quit();
        if (!ThreadUtils.joinUninterruptibly(capturerThread, THREAD_JOIN_TIMEOUT_MS)) {
            Log.e(TAG, "Join of capturerThread timed out");
            return false;
        }
        return true;
    }

    @Override
    public AudioFormat getRendererFormat() {
        return new AudioFormat(AudioFormat.AUDIO_SAMPLE_RATE_44100,
                AudioFormat.AUDIO_SAMPLE_MONO);
    }

    @Override
    public boolean onInitRenderer() {
        AudioFormat rendererFormat = getRendererFormat();
        int bytesPerFrame = rendererFormat.getChannelCount() * (BITS_PER_SAMPLE / 8);
        readByteBuffer = ByteBuffer.allocateDirect(bytesPerFrame * (rendererFormat.getSampleRate() / BUFFERS_PER_SECOND));
        int channelConfig = channelCountToConfiguration(rendererFormat.getChannelCount());
        int minBufferSize = AudioRecord.getMinBufferSize(rendererFormat.getSampleRate(), channelConfig, android.media.AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, rendererFormat.getSampleRate(), channelConfig,
                android.media.AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);
        keepAliveRendererRunnable = true;
        return true;
    }

    @Override
    public boolean onStartRendering(AudioDeviceContext audioDeviceContext) {
        renderingAudioDeviceContext = audioDeviceContext;
        // Create the renderer thread and start
        rendererThread = new HandlerThread("RendererThread");
        rendererThread.start();
        // Create the capturer handler that processes the renderer Runnables.
        rendererHandler = new Handler(rendererThread.getLooper());
        rendererHandler.post(speakerRendererRunnable);
        return true;
    }

    @Override
    public boolean onStopRendering() {
        stopAudioTrack();
        // Quit the rendererThread's looper to stop processing any further messages.
        rendererThread.quit();
        /*
         * When onStopRendering is called, the AudioDevice API expects that at the completion
         * of the callback the renderer has completely stopped. As a result, quit the renderer
         * thread and explicitly wait for the thread to complete.
         */
        if (!ThreadUtils.joinUninterruptibly(rendererThread, THREAD_JOIN_TIMEOUT_MS)) {
            Log.e(TAG, "Join of rendererThread timed out");
            return false;
        }
        return true;
    }


    private void processRemaining(@NotNull() ByteBuffer bb, int chunkSize) {
        bb.position(bb.limit()); // move at the end
        bb.limit(chunkSize); // get ready to pad with longs
        while (bb.position() < chunkSize) {
            bb.putLong(0);
        }
        bb.limit(chunkSize);
        bb.flip();
    }

    private int write(AudioTrack audioTrack, ByteBuffer byteBuffer, int sizeInBytes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return audioTrack.write(byteBuffer, sizeInBytes, AudioTrack.WRITE_BLOCKING);
        }
        return 0;
    }


    private void initializeStreams() {
        String path = utils.getStethoscopePipePath();
        File cfile = new File(path);
        try {
            inputStream = new FileInputStream(cfile);
            dataInputStream = new DataInputStream(inputStream);
            int bytes = dataInputStream.skipBytes(WAV_FILE_HEADER_SIZE);

            Log.d(TAG, "Number of bytes skipped: " + bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeStreams() {
        Log.d(TAG, "Remove any pending posts of fileCapturerRunnable that are in the message queue");
        capturerHandler.removeCallbacks(fileCapturerRunnable);
        try {
            dataInputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void stopRecording() {
        Log.d(TAG, "Remove any pending posts of microphoneCapturerRunnable that are in the message queue ");
        capturerHandler.removeCallbacks(microphoneCapturerRunnable);
        try {
            audioRecord.stop();
        } catch (IllegalStateException e) {
            Log.e(TAG, "AudioRecord.stop failed: " + e.getMessage());
        }
    }


    private int channelCountToConfiguration(int channels) {
        if (channels == 1)
            return android.media.AudioFormat.CHANNEL_IN_MONO;
        else
            return android.media.AudioFormat.CHANNEL_IN_STEREO;
    }

    private void releaseAudioResources() {
        audioTrack.flush();
        audioTrack.release();
    }

    private void stopAudioTrack() {
        keepAliveRendererRunnable = false;
        Log.d(TAG, "Remove any pending posts of speakerRendererRunnable that are in the message queue ");
        rendererHandler.removeCallbacks(speakerRendererRunnable);
        try {
            audioTrack.stop();
        } catch (IllegalStateException e) {
            Log.e(TAG, "AudioTrack.stop failed: " + e.getMessage());
        }
        releaseAudioResources();
    }

    /*
     * This Runnable reads a music file and provides the audio frames to the AudioDevice API via
     * AudioDevice.audioDeviceWriteCaptureData(..) until there is no more data to be read, the
     * capturer input switches to the microphone, or the call ends.
     */

    private Runnable fileCapturerRunnable = new Runnable() {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
            int bytesRead = 0;
            try {
                bytesRead = dataInputStream.read(fileWriteByteBuffer.array(), 0, writeBufferSize);
                if ( bytesRead > -1) {
                    if (bytesRead == fileWriteByteBuffer.capacity()) {
                        AudioDevice.audioDeviceWriteCaptureData(capturingAudioDeviceContext,
                                fileWriteByteBuffer
                        );
                    } else {
                        processRemaining(fileWriteByteBuffer, fileWriteByteBuffer.capacity());
                        AudioDevice.audioDeviceWriteCaptureData(capturingAudioDeviceContext,
                                fileWriteByteBuffer
                        );
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            capturerHandler.postDelayed(this, CALLBACK_BUFFER_SIZE_MS);
        }
    };


    /*
     * This Runnable reads data from the microphone and provides the audio frames to the AudioDevice
     * API via AudioDevice.audioDeviceWriteCaptureData(..) until the capturer input switches to
     * microphone or the call ends.
     */
    private Runnable microphoneCapturerRunnable = new Runnable() {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
            if (audioRecord.getState() != AudioRecord.STATE_UNINITIALIZED) {
                audioRecord.startRecording();
                while (true) {
                    int bytesRead = audioRecord.read(micWriteBuffer, micWriteBuffer.capacity());
                    if (bytesRead == micWriteBuffer.capacity()) {
                        AudioDevice.audioDeviceWriteCaptureData(capturingAudioDeviceContext,
                                micWriteBuffer
                        );
                    } else {
                        String errorMessage = "AudioRecord.read failed " + bytesRead;
                        Log.e(TAG, errorMessage);
                        if (bytesRead == AudioRecord.ERROR_INVALID_OPERATION) {
                            stopRecording();
                            Log.e(TAG, errorMessage);
                        }
                        break;
                    }
                }
            }
        }
    };


    /*
     * This Runnable reads audio data from the callee perspective via AudioDevice.audioDeviceReadRenderData(...)
     * and plays out the audio data using AudioTrack.write().
     */
    private Runnable speakerRendererRunnable = new Runnable() {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
            try {
                audioTrack.play();
            } catch (IllegalStateException e) {
                Log.e(TAG, "AudioTrack.play failed: " + e.getMessage());
                releaseAudioResources();
            }
            while (keepAliveRendererRunnable) {
                // Get 10ms of PCM data from the SDK. Audio data is written into the ByteBuffer provided.
                AudioDevice.audioDeviceReadRenderData(renderingAudioDeviceContext, readByteBuffer);
                int bytesWritten = write(audioTrack, readByteBuffer, readByteBuffer.capacity());
                if (bytesWritten != readByteBuffer.capacity()) {
                    Log.e(TAG, "AudioTrack.write failed: " + bytesWritten);
                    if (bytesWritten == AudioTrack.ERROR_INVALID_OPERATION) {
                        keepAliveRendererRunnable = false;
                        break;
                    }
                }
                // The byte buffer must be rewinded since byteBuffer.position() is increased at each
                // call to AudioTrack.write(). If we don't do this, will fail the next  AudioTrack.write().
                readByteBuffer.rewind();
            }
        }
    };


}