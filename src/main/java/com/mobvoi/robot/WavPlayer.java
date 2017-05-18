package com.mobvoi.robot;

import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WavPlayer {

    private final static String TAG = "WavPlayer";

    private static WavPlayer sInstance;
    private final byte[] mBuffer = new byte[640];
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private DataInputStream mStream;
    private volatile boolean mIsStopped = true;
    private Listener mListener;

    public synchronized static WavPlayer getInstance() {
        if (sInstance == null) {
            sInstance = new WavPlayer();
        }
        return sInstance;
    }

    public synchronized static WavPlayer newInstance() {
        return new WavPlayer();
    }

    public synchronized void play(AssetFileDescriptor fd, Listener listener) throws IOException {
        play(fd.createInputStream(), listener);
    }

    public synchronized void play(String path, Listener listener) throws FileNotFoundException {
        play(new FileInputStream(new File(path)), listener);
    }

    private void play(FileInputStream fileInputStream, Listener listener) {
        if (fileInputStream == null || listener == null) {
            throw new IllegalArgumentException();
        }
        if (mIsStopped) {
            mIsStopped = false;
            mListener = listener;
            try {
                mStream = new DataInputStream(fileInputStream);
                byte[] buf = new byte[4];
                if (mStream.read(buf) < buf.length || !"RIFF".equals(new String(buf))) {
                    Log.w(TAG, "Error reading header \"RIFF\", file ignored");
                    closeInjectedStream();
                    mIsStopped = true;
                    mListener.onError();
                    return;
                }
                mStream.readInt();
                if (mStream.read(buf) < buf.length || !"WAVE".equals(new String(buf))) {
                    Log.w(TAG, "Error reading header \"WAVE\", file ignored");
                    closeInjectedStream();
                    mIsStopped = true;
                    mListener.onError();
                } else if (mStream.read(buf) < buf.length || !"fmt ".equals(new String(buf))) {
                    Log.w(TAG, "Error reading header \"fmt \", file ignored");
                    closeInjectedStream();
                    mIsStopped = true;
                    mListener.onError();
                } else {
                    mStream.readInt();
                    mStream.readShort();
                    int channelNumber = Short.reverseBytes(mStream.readShort());
                    int sampleRate = Integer.reverseBytes(mStream.readInt());
                    mStream.readInt();
                    mStream.readShort();
                    int bitPerSample = Short.reverseBytes(mStream.readShort());
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, "sampleRate= " + sampleRate + " channelNumber= "
                                + channelNumber + " bitPerSample= " + bitPerSample);
                    }
                    if (mStream.read(buf) < buf.length) {
                        Log.w(TAG, "Error reading header \"data\", file ignored");
                        closeInjectedStream();
                        mIsStopped = true;
                        mListener.onError();
                    } else {
                        mStream.readInt();
                        start(sampleRate, bitPerSample, channelNumber);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException", e);
                mIsStopped = true;
                mListener.onError();
            }
        }
    }

    public void stop() {
        if (!mIsStopped) {
            mIsStopped = true;
            try {
                closeInjectedStream();
            } catch (IOException e) {
                Log.e(TAG, "IOException", e);
            }
        }
    }

    private void closeInjectedStream() throws IOException {
        if (mStream != null) {
            mStream.close();
            mStream = null;
        }
    }

    private int getBitDepthFormat(int bitPerSample) {
        switch (bitPerSample) {
            case 8:
                return AudioFormat.ENCODING_PCM_8BIT;
            case 16:
                return AudioFormat.ENCODING_PCM_16BIT;
            case 32:
                return AudioFormat.ENCODING_PCM_FLOAT;
            default:
                throw new RuntimeException("Unsupported bit depth " + bitPerSample);
        }
    }

    private int getChannelNumberFormat(int channelNumber) {
        switch (channelNumber) {
            case 1:
                return AudioFormat.CHANNEL_OUT_MONO;
            case 2:
                return AudioFormat.CHANNEL_OUT_STEREO;
            default:
                throw new RuntimeException("Unsupported channel number " + channelNumber);
        }
    }


    public void start(final int sampleRate, final int bitPerSample, final int channelNumber) {
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                mListener.onStart();
                int minBufferSize = AudioTrack.getMinBufferSize(sampleRate
                        , getChannelNumberFormat(channelNumber), getBitDepthFormat(bitPerSample));
                final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate
                        , getChannelNumberFormat(channelNumber), getBitDepthFormat(bitPerSample)
                        , minBufferSize, AudioTrack.MODE_STREAM);
                audioTrack.play();
                audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
                    public void onMarkerReached(AudioTrack track) {
                        audioTrack.stop();
                        mIsStopped = true;
                        audioTrack.release();
                        mListener.onDone();
                    }

                    public void onPeriodicNotification(AudioTrack track) {
                    }
                });
                int length = 0;
                try {
                    while (!mIsStopped) {
                        if (mStream != null) {
                            long i;
                            if ((i = mStream.read(mBuffer, 0, mBuffer.length)) == -1) {
                                audioTrack.flush();
                                // setNotificationMarkerPosition() count the audio samples
                                // (not the bytes), so if your audio is 16-bit sample
                                // the position will be half of data size
                                audioTrack.setNotificationMarkerPosition(length / 2);
                                break;
                            } else {
                                length += i;
                                audioTrack.write(mBuffer, 0, (int) i);
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "IOException ", e);
                    mListener.onError();
                } finally {
                    try {
                        closeInjectedStream();
                    } catch (IOException e) {
                        Log.e(TAG, "IOException", e);
                    }
                    audioTrack.stop();
                    audioTrack.release();
                    mIsStopped = true;
                    mListener.onDone();
                }
            }
        });
    }

    public interface Listener {
        void onStart();

        void onDone();

        void onError();
    }

}
