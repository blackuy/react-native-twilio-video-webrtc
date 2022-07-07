package com.twiliorn.library.events;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.twiliorn.library.utils.ImageFileReference;

public class TakeSnapshotReturnedEvent extends Event<TakeSnapshotReturnedEvent> {

    public static final String EVENT_NAME = "TakeSnapshotReturned";

    private final WritableMap payload;

    public TakeSnapshotReturnedEvent(@IdRes int viewId,
                                     int requestId,
                                     @NonNull ImageFileReference reference) {
        super(viewId);

        payload = Arguments.createMap();
        payload.putInt("requestId", requestId);
        // Put our annotations into the payload.
        WritableMap result = Arguments.createMap();
        result.putString("uri", reference.getUri());
        result.putInt("width", reference.getWidth());
        result.putInt("height", reference.getHeight());
        payload.putMap("result", result);
    }

    @Override
    public String getEventName() {
        return EVENT_NAME;
    }

    @Override
    public void dispatch(RCTEventEmitter rctEventEmitter) {
        rctEventEmitter.receiveEvent(getViewTag(), getEventName(), payload);
    }

    public WritableMap toWritableMap() {
        return this.payload;
    }
}