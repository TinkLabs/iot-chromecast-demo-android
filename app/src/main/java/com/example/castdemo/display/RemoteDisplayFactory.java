package com.example.castdemo.display;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.castdemo.server.ApiClient;
import com.google.android.gms.cast.framework.CastSession;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class RemoteDisplayFactory {

    private static DisplayItem sRoot;
    private static DisplayItem sCurrentItem;

    static public void build(Resources resources) {

        ApiClient.getDisplayInfosRx()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new SingleObserver<Response<DisplayItem>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Response<DisplayItem> displayItemResponse) {
                        sRoot = displayItemResponse.body();
                        sRoot.setParent(null);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
//        Gson gson = new Gson();
//        JsonReader reader = new JsonReader(new InputStreamReader(resources.openRawResource(R.raw.demo)));
//        try {
//            sRoot = gson.fromJson(reader, DisplayItem.class); // contains the whole reviews list
//            sRoot.setParent(null);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    static public void initial(Context context, CastSession castSession) {
//        sCurrentItem =  sDisplayItem[0];
//        sCurrentItem.display(castSession.getRemoteMediaClient());
        if (sRoot != null) {
            sCurrentItem = sRoot;
            sCurrentItem.display(castSession);
//            sCurrentItem.display(castSession.getRemoteMediaClient());
        } else {
            Toast.makeText(context, "init not ready", Toast.LENGTH_SHORT).show();
        }
    }

    static public void next(CastSession castSession) {
        if (sCurrentItem!= null) {
            sCurrentItem.next();
            sCurrentItem.display(castSession);
//            sCurrentItem.display(castSession.getRemoteMediaClient());
        }
    }

    static public void last(CastSession castSession) {
        if (sCurrentItem != null) {
            sCurrentItem.last();
            sCurrentItem.display(castSession);
//            sCurrentItem.display(castSession.getRemoteMediaClient());
        }
    }

    static public void enter(Context context, CastSession castSession) {
        DisplayItem enter = sCurrentItem.getTargetItem();
        if (enter != null ) {
            if (TextUtils.isEmpty(enter.getApi())) {
                sCurrentItem = enter;
                sCurrentItem.display(castSession);
//                sCurrentItem.display(castSession.getRemoteMediaClient());
            } else {
                enter.showApi(castSession);
                Toast.makeText(context, "Call API " + enter.getApi(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    static public void back(CastSession castSession) {
        DisplayItem back = sCurrentItem.getParent();
        if (back != null) {
            sCurrentItem = back;
            sCurrentItem.display(castSession);
//            sCurrentItem.display(castSession.getRemoteMediaClient());
        }
    }
}
