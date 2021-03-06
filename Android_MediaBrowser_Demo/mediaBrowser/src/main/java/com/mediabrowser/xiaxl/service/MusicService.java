/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.mediabrowser.xiaxl.service;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.mediabrowser.xiaxl.service.notification.MediaNotificationManager;
import com.mediabrowser.xiaxl.service.playback.MusicPlayback;
import com.mediabrowser.xiaxl.service.playback.Playback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a MediaBrowser through a service. It exposes the media library to a browsing
 * client, through the onGetRoot and onLoadChildren methods. It also creates a MediaSession and
 * exposes it through its MediaSession.Token, which allows the client to create a MediaController
 * that connects to and send control commands to the MediaSession remotely. This is useful for
 * user interfaces that need to interact with your media session, like Android Auto. You can
 * (should) also use the same service from your app's UI, which gives a seamless playback
 * experience to the user.
 * <p>
 * To implement a MediaBrowserService, you need to:
 * <p>
 * <ul>
 * <p>
 * <li> Extend {@link android.service.media.MediaBrowserService}, implementing the media browsing
 * related methods {@link android.service.media.MediaBrowserService#onGetRoot} and
 * {@link android.service.media.MediaBrowserService#onLoadChildren};
 * <li> In onCreate, start a new {@link android.media.session.MediaSession} and notify its parent
 * with the session's token {@link android.service.media.MediaBrowserService#setSessionToken};
 * <p>
 * <li> Set a callback on the
 * {@link android.media.session.MediaSession#setCallback(android.media.session.MediaSession.Callback)}.
 * The callback will receive all the user's actions, like play, pause, etc;
 * <p>
 * <li> Handle all the actual music playing using any method your app prefers (for example,
 * {@link android.media.MediaPlayer})
 * <p>
 * <li> Update playbackState, "now playing" metadata and queue, using MediaSession proper methods
 * {@link android.media.session.MediaSession#setPlaybackState(android.media.session.PlaybackState)}
 * {@link android.media.session.MediaSession#setMetadata(android.media.MediaMetadata)} and
 * {@link android.media.session.MediaSession#setQueue(List)})
 * <p>
 * <li> Declare and export the service in AndroidManifest with an intent receiver for the action
 * android.media.browse.MediaBrowserService
 * <p>
 * </ul>
 * <p>
 * To make your app compatible with Android Auto, you also need to:
 * <p>
 * <ul>
 * <p>
 * <li> Declare a meta-data tag in AndroidManifest.xml linking to a xml resource
 * with a &lt;automotiveApp&gt; root element. For a media app, this must include
 * an &lt;uses name="media"/&gt; element as a child.
 * For example, in AndroidManifest.xml:
 * &lt;meta-data android:name="com.google.android.gms.car.application"
 * android:resource="@xml/automotive_app_desc"/&gt;
 * And in res/values/automotive_app_desc.xml:
 * &lt;automotiveApp&gt;
 * &lt;uses name="media"/&gt;
 * &lt;/automotiveApp&gt;
 * <p>
 * </ul>
 *
 * @see <a href="README.md">README.md</a> for more details.
 */
public class MusicService extends MediaBrowserServiceCompat implements
        MusicPlaybackManager.PlaybackServiceCallback {

    private static final String TAG = "MusicService";

    public static final String MEDIA_ID_EMPTY_ROOT = "__EMPTY_ROOT__";
    public static final String MEDIA_ID_ROOT = "__ROOT__";

    /**
     * action
     */
    // The action of the incoming Intent indicating that it contains a command
    // to be executed (see {@link #onStartCommand})
    public static final String ACTION_CMD = "com.netease.awakeing.music.ACTION_CMD";

    /**
     * key
     */
    // The key in the extras of the incoming Intent indicating the command that
    // should be executed (see {@link #onStartCommand})
    public static final String CMD_NAME = "CMD_NAME";
    // A value of a CMD_NAME key in the extras of the incoming Intent that
    // indicates that the music playback should be paused (see {@link #onStartCommand})
    public static final String CMD_PAUSE = "CMD_PAUSE";
    // A value of a CMD_NAME key that indicates that the music playback should switch
    // to local playback from cast playback.
    public static final String CMD_STOP_CASTING = "CMD_STOP_CASTING";

    /**
     * delay ????????????????????????service
     */
    // Delay stopSelf by using a handler.
    private static final int STOP_DELAY = 30000;

    /**
     * obj
     */
    // MediaSessionCompat
    private MediaSessionCompat mMediaSession;
    // 1???MusicPlayback???????????????
    // 2???MediaSession.Callback ????????????
    private MusicPlaybackManager mPlaybackManager;
    // notification
    private MediaNotificationManager mMediaNotificationManager;
    // ?????????????????? ?????????????????? ???stop service
    private final DelayedStopHandler mDelayedStopHandler = new DelayedStopHandler(this);


    /**
     * create ??????
     */
    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * ??????????????? queue
         */
        MusicQueue queueManager = new MusicQueue(getResources(),
                new MusicQueue.MetadataUpdateListener() {
                    // ?????????????????????
                    @Override
                    public void onBeforeMetadataChanged(MediaMetadataCompat metadata) {

                    }

                    // ????????????????????????
                    @Override
                    public void onMetadataChanged(MediaMetadataCompat metadata) {
                        /**
                         *
                         * ????????????????????? Client ??? {@link MediaControllerCallback.onMetadataChanged}
                         */
                        if (mMediaSession != null) {
                            mMediaSession.setMetadata(metadata);
                        }
                    }

                    // ??????????????????
                    @Override
                    public void onMetadataRetrieveError() {
                        // ??????????????????
                        mPlaybackManager.callbackServicePlaybackState("Unable to retrieve metadata.");
                    }

                    @Override
                    public void onQueueUpdated(String title,
                                               List<MediaSessionCompat.QueueItem> newQueue) {
                        /**
                         *
                         * ????????????????????? Client ??? {@link MediaControllerCallback.onQueueChanged}
                         */
                        if (mMediaSession != null) {
                            mMediaSession.setQueue(newQueue);
                            mMediaSession.setQueueTitle(title);
                        }
                    }
                });
        // ????????? MusicPlayback
        Playback playback = new MusicPlayback(this);
        // ????????? MusicPlaybackManager
        mPlaybackManager = new MusicPlaybackManager(getApplicationContext(), this, queueManager, playback);
        // ?????? MediaSessionCompat
        // Start a new MediaSession
        initSession();
        //
        if (mMediaSession == null) {
            return;
        }
        // ???????????????token
        setSessionToken(mMediaSession.getSessionToken());
        // ????????????MediaControllerCompat???UI????????????
        // ?????????MediaSessionCompat.Callback ?????????Service??????
        // ???????????????????????????????????????????????????????????????????????????????????????
        mMediaSession.setCallback(mPlaybackManager.getMediaSessionCallback());
        try {
            mMediaSession.setFlags(
                    // ??????
                    MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                            // ??????
                            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ?????????service ???????????????????????????
        mPlaybackManager.callbackServicePlaybackState(null);
        /**
         *
         */
        try {
            mMediaNotificationManager = new MediaNotificationManager(this);
        } catch (RemoteException e) {
            throw new IllegalStateException("Could not create a MediaNotificationManager", e);
        }

    }

    /**
     * ?????????session
     */
    private void initSession() {
        try {
            // ??????MediaSessionCompat
            mMediaSession = new MediaSessionCompat(this, TAG);
        } catch (Exception e) {
            mMediaSession = null;
        }
        try {
            if (mMediaSession == null) {
                mMediaSession = new MediaSessionCompat(this, TAG, new ComponentName(this, "android.support.v4.media.session.MediaButtonReceiver"), null);
            }
        } catch (Exception e) {
            mMediaSession = null;
        }
    }


    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        // intent ??????null
        if (startIntent != null) {
            // action
            String action = startIntent.getAction();
            String command = startIntent.getStringExtra(CMD_NAME);
            //
            if (ACTION_CMD.equals(action)) {
                // ????????????
                if (CMD_PAUSE.equals(command)) {
                    mPlaybackManager.handlePauseRequest();
                }
            } else {
                // ??????mediaButtonReceiver???????????????
                // Try to handle the intent as a media button event wrapped by MediaButtonReceiver
                if (mMediaSession != null) {
                    MediaButtonReceiver.handleIntent(mMediaSession, startIntent);
                }
            }
        }
        // ?????????????????? ?????????????????? ???stop service
        // Reset the delay handler to enqueue a message to stop the service if
        // nothing is playing.
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        return START_STICKY;
    }

    /**
     * (non-Javadoc)
     *
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {
        // ????????????  ????????????
        // Service is being killed, so make sure we release our resources
        mPlaybackManager.handleStopRequest(null);
        // ???????????????Notification
        if (mMediaNotificationManager != null) {
            mMediaNotificationManager.stopNotification();
        }
        // ?????????????????????
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        // ??????session
        if (mMediaSession != null) {
            mMediaSession.release();
        }

    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid,
                                 Bundle rootHints) {
        // Verify the client is authorized to browse media and return the root that
        // makes the most sense here. In this example we simply verify the package name
        // is the same as ours, but more complicated checks, and responses, are possible
        if (!clientPackageName.equals(getPackageName())) {
            // Allow the client to connect, but not browse, by returning an empty root
            return new MediaBrowserServiceCompat.BrowserRoot(MEDIA_ID_EMPTY_ROOT, null);
        }

        return new BrowserRoot(MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentMediaId,
                               @NonNull final Result<List<MediaItem>> result) {
        if (MEDIA_ID_EMPTY_ROOT.equals(parentMediaId)) {
            result.sendResult(new ArrayList<MediaItem>());
        }
    }


    // ####################################################################################

    /**
     * {@link com.mediabrowser.xiaxl.service.MusicPlaybackManager.PlaybackServiceCallback}
     * ????????????
     * PlaybackCallback method called from MusicPlaybackManager whenever the music is about to play.
     */
    @Override
    public void onPlaybackStart() {
        // ??????????????????????????????
        if (mMediaSession != null) {
            mMediaSession.setActive(true);
        }
        mDelayedStopHandler.removeCallbacksAndMessages(null);

        // The service needs to continue running even after the bound client (usually a
        // MediaController) disconnects, otherwise the music playback will stop.
        // Calling startService(Intent) will keep the service running until it is explicitly killed.
        startService(new Intent(getApplicationContext(), MusicService.class));
    }

    @Override
    public void onPlaybackPause() {
        // Reset the delayed stop handler, so after STOP_DELAY it will be executed again,
        // potentially stopping the service.
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        stopForeground(true);
    }

    /**
     * PlaybackCallback method called from MusicPlaybackManager whenever the music stops playing.
     */
    @Override
    public void onPlaybackStop() {
        if (mMediaSession != null) {
            mMediaSession.setActive(false);
        }
        // Reset the delayed stop handler, so after STOP_DELAY it will be executed again,
        // potentially stopping the service.
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        //
        stopForeground(true);
    }


    @Override
    public void onNotificationRequired() {
        // ??????notification
        mMediaNotificationManager.startNotification();
    }

    @Override
    public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
        if (mMediaSession == null) {
            return;
        }
        /**
         *
         * ????????????????????? Client ??? {@link MediaControllerCallback.onPlaybackStateChanged}
         */
        //
        try {
            mMediaSession.setPlaybackState(newState);
        } catch (NoSuchMethodError e) {
            mMediaSession = null;
        }
    }


    // ###########################################################################################

    /**
     * ?????????????????? ?????????????????? ???stop service
     * A simple handler that stops the service if playback is not active (playing)
     */
    private static class DelayedStopHandler extends Handler {
        private final WeakReference<MusicService> mWeakReference;

        private DelayedStopHandler(MusicService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            // ??????service
            MusicService service = mWeakReference.get();
            // ?????????????????????????????????service
            if (service != null && service.mPlaybackManager.getPlayback() != null) {
                if (service.mPlaybackManager.getPlayback().isPlaying()) {
                    return;
                }
                service.stopSelf();
            }
        }
    }


}
