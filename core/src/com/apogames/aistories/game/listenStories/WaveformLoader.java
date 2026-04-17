package com.apogames.aistories.game.listenStories;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.MP3Decoder;
import javazoom.jl.decoder.OutputBuffer;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class WaveformLoader {

    public interface Callback {
        void onHeights(float[] heights);
    }

    private static final String CACHE_SUFFIX = ".peaks";
    private static final String CACHE_VERSION = "v1";

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(new ThreadFactory() {
        private final AtomicInteger n = new AtomicInteger(0);
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "waveform-loader-" + n.incrementAndGet());
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        }
    });

    private WaveformLoader() {
    }

    public static void shutdown() {
        EXECUTOR.shutdownNow();
    }

    public static void loadAsync(final FileHandle mp3, final int barCount, final Callback callback) {
        if (mp3 == null || callback == null || barCount <= 0) return;

        final FileHandle cache = mp3.sibling(mp3.name() + CACHE_SUFFIX);
        float[] cached = readCache(cache, barCount);
        if (cached != null) {
            callback.onHeights(cached);
            return;
        }

        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    float[] peaks = decodePeaks(mp3, barCount);
                    if (peaks != null) {
                        writeCache(cache, peaks);
                        deliver(peaks, callback);
                    }
                } catch (Throwable t) {
                    Gdx.app.log("WaveformLoader", "decode failed: " + t.getMessage());
                }
            }
        });
    }

    public static void deleteCache(FileHandle mp3) {
        if (mp3 == null) return;
        FileHandle cache = mp3.sibling(mp3.name() + CACHE_SUFFIX);
        try {
            if (cache.exists()) cache.delete();
        } catch (Throwable ignored) {
        }
    }

    private static void deliver(final float[] heights, final Callback callback) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                callback.onHeights(heights);
            }
        });
    }

    private static float[] readCache(FileHandle cache, int barCount) {
        try {
            if (!cache.exists()) return null;
            String text = cache.readString();
            if (text == null) return null;
            String[] tokens = text.trim().split("\\s+");
            if (tokens.length < barCount + 1) return null;
            if (!CACHE_VERSION.equals(tokens[0])) return null;
            float[] out = new float[barCount];
            for (int i = 0; i < barCount; i++) {
                out[i] = Float.parseFloat(tokens[i + 1]);
            }
            return out;
        } catch (Throwable t) {
            return null;
        }
    }

    private static void writeCache(FileHandle cache, float[] peaks) {
        try {
            StringBuilder sb = new StringBuilder(peaks.length * 8);
            sb.append(CACHE_VERSION);
            for (float p : peaks) {
                sb.append(' ').append(p);
            }
            cache.writeString(sb.toString(), false);
        } catch (Throwable t) {
            Gdx.app.log("WaveformLoader", "cache write failed: " + t.getMessage());
        }
    }

    private static float[] decodePeaks(FileHandle mp3, int barCount) throws Exception {
        float[] bucketMax = new float[barCount];
        float[] bucketSum = new float[barCount];
        int[] bucketCount = new int[barCount];

        InputStream in = null;
        Bitstream stream = null;
        try {
            in = new BufferedInputStream(mp3.read(), 1 << 15);
            stream = new Bitstream(in);
            MP3Decoder decoder = new MP3Decoder();
            OutputBuffer output = null;

            int totalSamples = 0;
            int frameCountEstimate = barCount * 32;
            int framesPerBucket = Math.max(1, frameCountEstimate / barCount);
            int frameIdx = 0;
            boolean estimated = false;

            while (true) {
                Header h = stream.readFrame();
                if (h == null) break;
                if (!estimated) {
                    float msPerFrame = h.ms_per_frame();
                    float totalMs = h.total_ms((int) Math.max(1, mp3.length()));
                    if (totalMs <= 0f) totalMs = msPerFrame * 2000f;
                    frameCountEstimate = Math.max(barCount, (int) Math.ceil(totalMs / Math.max(1f, msPerFrame)));
                    framesPerBucket = Math.max(1, frameCountEstimate / barCount);
                    int channels = (h.mode() == Header.SINGLE_CHANNEL) ? 1 : 2;
                    output = new OutputBuffer(channels, false);
                    decoder.setOutputBuffer(output);
                    estimated = true;
                }
                try {
                    decoder.decodeFrame(h, stream);
                } catch (Throwable ignored) {
                }
                stream.closeFrame();
                byte[] buf = output.getBuffer();
                int byteLen = output.reset();
                int bucket = Math.min(barCount - 1, frameIdx / framesPerBucket);
                float localMax = 0f;
                float localSum = 0f;
                int localCount = 0;
                for (int i = 0; i + 1 < byteLen; i += 2) {
                    int lo = buf[i] & 0xFF;
                    int hi = buf[i + 1];
                    short sample = (short) ((hi << 8) | lo);
                    float a = Math.abs((int) sample) / 32768f;
                    if (a > localMax) localMax = a;
                    localSum += a;
                    localCount++;
                }
                if (localCount > 0) {
                    if (localMax > bucketMax[bucket]) bucketMax[bucket] = localMax;
                    bucketSum[bucket] += localSum;
                    bucketCount[bucket] += localCount;
                    totalSamples += localCount;
                }
                frameIdx++;
            }

            if (totalSamples == 0) return null;

            float[] peaks = new float[barCount];
            int lastFilled = -1;
            for (int i = 0; i < barCount; i++) {
                if (bucketCount[i] > 0) {
                    float avg = bucketSum[i] / bucketCount[i];
                    peaks[i] = 0.35f * avg + 0.65f * bucketMax[i];
                    lastFilled = i;
                } else if (lastFilled >= 0) {
                    peaks[i] = peaks[lastFilled];
                }
            }

            float maxP = 0f;
            for (float p : peaks) if (p > maxP) maxP = p;
            if (maxP > 0.01f) {
                float gain = 0.95f / maxP;
                for (int i = 0; i < barCount; i++) peaks[i] *= gain;
            }
            for (int i = 0; i < barCount; i++) {
                if (peaks[i] < 0.08f) peaks[i] = 0.08f;
                if (peaks[i] > 1.0f) peaks[i] = 1.0f;
            }
            return peaks;
        } finally {
            if (stream != null) {
                try { stream.close(); } catch (Throwable ignored) {}
            } else if (in != null) {
                try { in.close(); } catch (Throwable ignored) {}
            }
        }
    }
}
