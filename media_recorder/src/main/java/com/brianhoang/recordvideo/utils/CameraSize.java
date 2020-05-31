package com.brianhoang.recordvideo.utils;

import android.graphics.Point;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Size;
import android.view.Display;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CameraSize {

    /**
     * Standard High Definition size for pictures and video
     */
    SmartSize SIZE_1080P = new SmartSize(1920, 1080);

    public class SmartSize {
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

    public Size getPreviewOutputSize(Display display, CameraCharacteristics characteristics, Class targetClass, Integer format) {
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
}
