package com.twiliorn.library.niceday;

import android.support.annotation.Nullable;
import android.util.Log;

import com.twilio.video.TrackPriority;
import com.twilio.video.VideoDimensions;

/**
This class contains helpers that can be used anywhere.
 */
public class NDHelper {
    private static final String TAG = "NDHelper";
    public static VideoDimensions parseDimensionsString(@Nullable String dimensions) {
        if (dimensions != null && !dimensions.trim().isEmpty()) {
            String[] dimensions_array = dimensions.split("x");

            // There can only be 2 items for a correct <width>x<height> string
            if (dimensions_array.length != 2) {
                return null;
            }

            int w = Integer.parseInt(dimensions_array[0]);
            int h = Integer.parseInt(dimensions_array[1]);

            return new VideoDimensions(w,h);
        }

        return null;
    }
    // Functions to parse the bandwidth profile map
    public static TrackPriority parsePriorityString(@Nullable String priority) {
        if (priority != null && !priority.trim().isEmpty()) {
            if (priority.equalsIgnoreCase("LOW")) {
                return TrackPriority.LOW;
            } else if (priority.equalsIgnoreCase("STANDARD")) {
                return TrackPriority.STANDARD;
            } else if (priority.equalsIgnoreCase("HIGH")) {
                return TrackPriority.HIGH;
            } else if (priority.equalsIgnoreCase("NULL")) {
                return null;
            } else {
                Log.w(TAG, "Unknown priority string" + priority);
                return null;
            }
        }

        return null;
    }
}
