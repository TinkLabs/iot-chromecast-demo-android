package com.example.castdemo.display;

import android.net.Uri;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Locale;

public class DisplayItem {
    private static final MediaLoadOptions sMediaLoadOpt = new MediaLoadOptions.Builder()
            .setAutoplay(true)
            .build();


    public static final String sS1 = "urn:x-cast:com.url.cast";
    private String mTitle;

    @SerializedName("screen")
    private String mUrl;

    @SerializedName("api")
    private String mApi;

    @SerializedName("options")
    private List<DisplayItem> mOption;

    private DisplayItem mParent;

    private int mDisplayOptIndex = 0;

    public void setParent(DisplayItem parent) {
        mParent = parent;
        if (mOption != null) {
            for (DisplayItem item : mOption) {
                item.setParent(this);
            }
        }
    }

    public String getApi() {
        return mApi;
    }

    public DisplayItem getTargetItem() {
        if (mOption != null && mOption.size() > 0) {
            return mOption.get(mDisplayOptIndex);
        }
        return null;
    }

    public DisplayItem getParent() {
        return mParent;
    }

    public DisplayItem(String title, String url) {
        mTitle = title;
        mUrl = url;
    }

    private MediaMetadata getMediaMetaData() {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_PHOTO);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, mTitle);
        mediaMetadata.addImage(new WebImage(Uri.parse(mUrl)));
        return mediaMetadata;
    }

    private MediaInfo createMediaInfo() {
        return new MediaInfo.Builder(mUrl)
                .setContentType("image/jpeg")
                .setMetadata(getMediaMetaData())
                .setStreamType(MediaInfo.STREAM_TYPE_NONE)
                .build();
    }

    private String createImageJson() {
        return String.format(Locale.getDefault(), "{\"type\":\"image\",\"url\":\"%s\"}", mUrl);
    }

    private String createApiJson() {
        return String.format(Locale.getDefault(), "{\"type\":\"api\",\"url\":\"%s\"}", mApi);
    }

    public void last() {
        normalizeIndex(mDisplayOptIndex - 1);
    }
    public void next() {
        normalizeIndex(mDisplayOptIndex + 1);
    }

    private void normalizeIndex(int targetIndex) {
        if (mOption != null) {
            if (targetIndex >= mOption.size()) {
                mDisplayOptIndex = 0;
            } else if (targetIndex < 0) {
                mDisplayOptIndex = mOption.size() - 1;
            } else {
                mDisplayOptIndex = targetIndex;
            }
        } else {
            mDisplayOptIndex = -1;
        }
    }

    public void display(final CastSession session) {
        String s2;
        if (mOption != null && mOption.size() > 0) {
//            remoteMediaClient.load(mOption.get(mDisplayOptIndex).createMediaInfo(), sMediaLoadOpt);
            s2 = mOption.get(mDisplayOptIndex).createImageJson();
        } else {
//            remoteMediaClient.load(createMediaInfo(), sMediaLoadOpt);
            s2 = createImageJson();
        }
        session.sendMessage(sS1, s2);
    }

    public void display(final RemoteMediaClient remoteMediaClient) {
        if (mOption != null && mOption.size() > 0) {
            remoteMediaClient.load(mOption.get(mDisplayOptIndex).createMediaInfo(), sMediaLoadOpt);
        } else {
            remoteMediaClient.load(createMediaInfo(), sMediaLoadOpt);
        }
    }

    public void showApi(final CastSession session) {
        String s2 = createApiJson();
        session.sendMessage(sS1, s2);
    }
}
