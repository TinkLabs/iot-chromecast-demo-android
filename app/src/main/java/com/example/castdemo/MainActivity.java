package com.example.castdemo;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.castdemo.display.DisplayItem;
import com.example.castdemo.display.RemoteDisplayFactory;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.SessionManagerListener;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.mediarouter.app.MediaRouteButton;
import androidx.mediarouter.app.MediaRouteChooserDialogFragment;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "VideoBrowserActivity";
    private CastContext mCastContext;
    private final SessionManagerListener<CastSession> mSessionManagerListener = new MySessionManagerListener();
    private CastSession mCastSession;
    private CastStateListener mCastStateListener;

    private MediaRouteButton mMediaRouteButton;
    private View mSwipeView;
    private RelativeLayout mTouchPanel;
    private TextView mCastStatus;

    private GestureDetectorCompat mGestureDetector;

    private boolean mFirstAutoConnect = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RemoteDisplayFactory.build(getResources());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mCastStatus = findViewById(R.id.tv_cast_status);
        mMediaRouteButton = findViewById(R.id.bt_cast_button);
        mSwipeView = findViewById(R.id.v_swipe);
        mTouchPanel = findViewById(R.id.rl_touch_panel);
        findViewById(R.id.bt_ok).setOnClickListener(this);
        findViewById(R.id.bt_ok).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        findViewById(R.id.bt_back).setOnClickListener(this);

        mSwipeView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });

        mSwipeView.setOnClickListener(this);

        mCastStateListener = new CastStateListener() {
            @Override
            public void onCastStateChanged(int newState) {
                Log.e("Timmy", " " + newState);
                switch (newState) {
                    case CastState.NO_DEVICES_AVAILABLE:
                        mCastStatus.setText(R.string.disconnect);
                        enableDisableViewGroup(mTouchPanel, false);
                        break;
                    case CastState.NOT_CONNECTED:
                        if (mFirstAutoConnect) {
                            mMediaRouteButton.performClick();
                        }
                        mCastStatus.setText(R.string.disconnect);
                        enableDisableViewGroup(mTouchPanel, false);
                        break;
                    case CastState.CONNECTING:
                        mCastStatus.setText(R.string.connecting);
                        enableDisableViewGroup(mTouchPanel, false);
                        break;
                    case CastState.CONNECTED:
                        mCastStatus.setText(R.string.connected);
                        mFirstAutoConnect = false;
                        enableDisableViewGroup(mTouchPanel, true);
                        break;
                }
            }
        };
        mCastContext = CastContext.getSharedInstance(this);
        CastButtonFactory.setUpMediaRouteButton(this, mMediaRouteButton);

        mGestureDetector = new GestureDetectorCompat(this, new GestureListenerImpl());
        enableDisableViewGroup(mTouchPanel, false);
    }

    @Override
    public void onClick(View v) {
        if (mCastSession != null) {
            if (v.getId() == R.id.bt_ok) {
                RemoteDisplayFactory.enter(this, mCastSession);
                enableDisableViewGroup(mTouchPanel, false);
            }
            if (v.getId() == R.id.bt_back) {
                RemoteDisplayFactory.back(mCastSession);
                enableDisableViewGroup(mTouchPanel, false);
            }
        }
    }

    @Override
    public void onBackPressed() {
        mCastContext.removeCastStateListener(mCastStateListener);
        mCastContext.getSessionManager().removeSessionManagerListener(mSessionManagerListener, CastSession.class);
        mCastContext.getSessionManager().endCurrentSession(true);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCastContext.getSessionManager().endCurrentSession(true);
    }

    @Override
    protected void onResume() {
        mCastContext.addCastStateListener(mCastStateListener);
        mCastContext.getSessionManager().addSessionManagerListener(
                mSessionManagerListener, CastSession.class);

        if (mCastSession == null) {
            mCastSession = CastContext.getSharedInstance(this).getSessionManager()
                    .getCurrentCastSession();
        }
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus && mCastSession == null && mFirstAutoConnect) {
            List<Fragment> list = getSupportFragmentManager().getFragments();
            if (list.size() > 0) {
                Fragment fragment = list.get(0);
                if (fragment instanceof MediaRouteChooserDialogFragment) {
                    ListView listView = ((DialogFragment) fragment).getDialog().findViewById(R.id.mr_chooser_list);
                    if (listView != null && listView.getAdapter().getCount() > 0) {
                        listView.performItemClick(
                                listView.getAdapter().getView(0, null, null),
                                0,
                                listView.getAdapter().getItemId(0));
                    }
                }
            }
        }
    }

    @Override
    protected void onPause() {
        mCastContext.removeCastStateListener(mCastStateListener);
        mCastContext.getSessionManager().removeSessionManagerListener(
                mSessionManagerListener, CastSession.class);
        super.onPause();
    }

    private class GestureListenerImpl extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mCastSession != null) {
                if (Math.abs(velocityX) > Math.abs(velocityY)) {
                    if (velocityX > 0) {
                        RemoteDisplayFactory.next(mCastSession);
                    } else {
                        RemoteDisplayFactory.last(mCastSession);
                    }
                } else {
                    if (velocityY > 0) {
                        RemoteDisplayFactory.last(mCastSession);
                    } else {
                        RemoteDisplayFactory.next(mCastSession);
                    }
                }
                enableDisableViewGroup(mTouchPanel, false);
                return true;
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    private void updateSession(CastSession session) {
        mCastSession = session;
        try {
            mCastSession.setMessageReceivedCallbacks(DisplayItem.sS1, new Cast.MessageReceivedCallback() {
                @Override
                public void onMessageReceived(CastDevice castDevice, String s, String s1) {
                    if ("ok".contentEquals(s1)) {
                        enableDisableViewGroup(mTouchPanel, true);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        RemoteDisplayFactory.initial(MainActivity.this, mCastSession);
        enableDisableViewGroup(mTouchPanel, false);
    }

    private class MySessionManagerListener implements SessionManagerListener<CastSession> {

        @Override
        public void onSessionEnded(CastSession session, int error) {
            if (session == mCastSession) {
                mCastSession = null;
            }
        }

        @Override
        public void onSessionResumed(CastSession session, boolean wasSuspended) {
            updateSession(session);
        }

        @Override
        public void onSessionStarted(CastSession session, String sessionId) {
            updateSession(session);
        }

        @Override
        public void onSessionStarting(CastSession session) {
        }

        @Override
        public void onSessionStartFailed(CastSession session, int error) {
        }

        @Override
        public void onSessionEnding(CastSession session) {
        }

        @Override
        public void onSessionResuming(CastSession session, String sessionId) {
        }

        @Override
        public void onSessionResumeFailed(CastSession session, int error) {
        }

        @Override
        public void onSessionSuspended(CastSession session, int reason) {
        }
    }

    public static void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = viewGroup.getChildAt(i);
            view.setEnabled(enabled);
            if (view instanceof ViewGroup) {
                enableDisableViewGroup((ViewGroup) view, enabled);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
