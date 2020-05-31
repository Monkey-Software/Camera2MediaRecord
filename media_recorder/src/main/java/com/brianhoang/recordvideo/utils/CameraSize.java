package com.brianhoang.recordvideo.utils;

import android.graphics.Point;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Log;
import android.util.Size;
import android.view.Display;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraSize {

    public static final String TAG = "CameraSize";

    /**
     * Standard High Definition size for pictures and video
     */
    SmartSize SIZE_1080P = new SmartSize(1920, 1080);

    public static class SmartSize {
        Size size;
        int longS;
        int shortS;

        public SmartSize(int width, int height) {
            size = new Size(width, height);
            longS = Math.max(width, height);
            shortS = Math.min(width, height);
        }
    }

    public SmartSize getDisplaySmartSize(Display display) {
        Point point = new Point();
        display.getRealSize(point);
        return new SmartSize(point.x, point.y);
    }

    public Size getPreviewOutputSize(Display display, CameraCharacteristics characteristics, Class<? extends Object> targetClass, Integer format) {
        SmartSize screenSize = getDisplaySmartSize(display);
        boolean hdScreen = screenSize.longS >= SIZE_1080P.longS || screenSize.shortS >= SIZE_1080P.shortS;
        SmartSize maxSize = hdScreen ? SIZE_1080P : screenSize;

        // If image format is provided, use it to determine supported sizes; else use target class
        StreamConfigurationMap config = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (format == null) {
            assert (StreamConfigurationMap.isOutputSupportedFor(targetClass));
        } else {
            assert (config.isOutputSupportedFor(format));
        }
        Size[] allSizes = (format == null) ? config.getOutputSizes(targetClass) : config.getOutputSizes(format);
        List<Size> sizes = Arrays.asList(allSizes);
        Collections.sort(sizes, (o2, o1) -> o1.getHeight() * o1.getHeight() - o2.getHeight() * o2.getWidth());

        Size selected = sizes.get(sizes.size() - 1);

        for (Size size : sizes) {
            SmartSize smartSize = new SmartSize(size.getWidth(), size.getHeight());
            if (smartSize.longS <= maxSize.longS && smartSize.shortS <= maxSize.shortS) {
                return smartSize.size;
            }
        }
        return selected;
    }


    /**
     * In this sample, we choose a video size with 3x4 for  aspect ratio. for more perfectness 720 as well Also, we don't use sizes
     * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
     *
     * @param choices The list of available sizes
     * @return The video size 1080p,720px
     */
    public static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (1920 == size.getWidth() && 1080 == size.getHeight()) {
                return size;
            }
        }
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        Log.e(TAG, "Couldn't find any suitable video size");
        return choices[choices.length - 1];
    }


    /**
     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the respective requested values, and whose aspect
     * ratio matches with the specified value.
     *
     * @param choices     The list of sizes that the camera supports for the intended output class
     * @param width       The minimum desired width
     * @param height      The minimum desired height
     * @param aspectRatio The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    public static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }


    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }
}
