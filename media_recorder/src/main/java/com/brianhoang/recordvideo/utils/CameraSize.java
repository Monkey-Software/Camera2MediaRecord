package com.brianhoang.recordvideo.utils;

import android.util.Log;
import android.util.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraSize {

    public static final String TAG = "CameraSize";

    /**
     * Choice video size
     *
     * @param maxSize Max video size accept
     * @param choices Camera support size
     * @return Selected video size
     */
    public static Size chooseVideoSize(Size maxSize, Size[] choices) {
        List<Size> sizes = Arrays.asList(choices);
        Collections.sort(sizes, new CompareSizesByArea());

        for (Size size : sizes) {
            if (maxSize.getWidth() == size.getWidth() && maxSize.getHeight() == size.getHeight()) {
                return size;
            }
        }
        for (Size size : sizes) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        Log.e(TAG, "Couldn't find any suitable video size");
        return sizes.get(sizes.size() - 1);
    }


    /**
     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the respective requested values, and whose aspect
     * ratio matches with the specified value.
     *
     * @param choices     The list of sizes that the camera supports for the intended output class
     * @param aspectRatio The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    public static Size chooseOptimalSize(Size[] choices, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w) {
                bigEnough.add(option);
            }
        }

        // Pick the largest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.max(bigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return aspectRatio;
        }
    }


    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }

}
