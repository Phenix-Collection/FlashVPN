package com.polestar.ad;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by guojia on 2017/5/31.
 */

public class AdViewBinder {
    public final static class Builder {
        private final int layoutId;
        private int titleId;
        private int textId;
        private int callToActionId;
        private int mainMediaId;
        private int iconImageId = -1;
        private int privacyInformationId;
        private int starLevelLayoutId = -1;
        @NonNull
        private Map<String, Integer> extras = Collections.emptyMap();

        public Builder(final int layoutId) {
            this.layoutId = layoutId;
            this.extras = new HashMap<String, Integer>();
        }

        @NonNull
        public final Builder titleId(final int titleId) {
            this.titleId = titleId;
            return this;
        }

        @NonNull
        public final Builder textId(final int textId) {
            this.textId = textId;
            return this;
        }

        @NonNull
        public final Builder callToActionId(final int callToActionId) {
            this.callToActionId = callToActionId;
            return this;
        }

        @NonNull
        public final Builder mainMediaId(final int mediaLayoutId) {
            this.mainMediaId = mediaLayoutId;
            return this;
        }

        @NonNull
        public final Builder iconImageId(final int iconImageId) {
            this.iconImageId = iconImageId;
            return this;
        }

        @NonNull
        public final Builder privacyInformationId(final int privacyInformationIconImageId) {
            this.privacyInformationId = privacyInformationIconImageId;
            return this;
        }

        @NonNull
        public final Builder starLevelLayoutId(final int starLevelLayoutId) {
            this.starLevelLayoutId = starLevelLayoutId;
            return this;
        }

        @NonNull
        public final Builder addExtras(final Map<String, Integer> resourceIds) {
            this.extras = new HashMap<String, Integer>(resourceIds);
            return this;
        }

        @NonNull
        public final Builder addExtra(final String key, final int resourceId) {
            this.extras.put(key, resourceId);
            return this;
        }

        @NonNull
        public final AdViewBinder build() {
            return new AdViewBinder(this);
        }
    }

    public final int layoutId;
    public final int titleId;
    public final int textId;
    public final int callToActionId;
    public final int mainMediaId;
    public final int iconImageId;
    public final int privacyInformationId;
    public final int starLevelLayoutId;
    @NonNull public final Map<String, Integer> extras;

    private AdViewBinder(@NonNull final AdViewBinder.Builder builder) {
        this.layoutId = builder.layoutId;
        this.titleId = builder.titleId;
        this.textId = builder.textId;
        this.callToActionId = builder.callToActionId;
        this.mainMediaId = builder.mainMediaId;
        this.iconImageId = builder.iconImageId;
        this.privacyInformationId = builder.privacyInformationId;
        this.starLevelLayoutId = builder.starLevelLayoutId;
        this.extras = builder.extras;
    }
}