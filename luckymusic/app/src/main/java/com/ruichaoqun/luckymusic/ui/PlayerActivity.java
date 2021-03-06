package com.ruichaoqun.luckymusic.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.core.graphics.drawable.DrawableCompat;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.ruichaoqun.luckymusic.R;
import com.ruichaoqun.luckymusic.base.activity.BaseMVPActivity;
import com.ruichaoqun.luckymusic.common.GlideApp;
import com.ruichaoqun.luckymusic.ui.equalizer.EqualizerActivity;
import com.ruichaoqun.luckymusic.utils.ColorUtil;
import com.ruichaoqun.luckymusic.utils.CommonUtils;
import com.ruichaoqun.luckymusic.utils.RenderScriptTransformation;
import com.ruichaoqun.luckymusic.utils.StylusAnimation;
import com.ruichaoqun.luckymusic.utils.TimeUtils;
import com.ruichaoqun.luckymusic.utils.UiUtils;
import com.ruichaoqun.luckymusic.widget.BottomSheetDialog.DynamicEffectSheet;
import com.ruichaoqun.luckymusic.widget.LyricView;
import com.ruichaoqun.luckymusic.widget.PlayerDiscViewFlipper;
import com.ruichaoqun.luckymusic.widget.RotationRelativeLayout;
import com.ruichaoqun.luckymusic.utils.ViewSwitcherTarget;
import com.ruichaoqun.luckymusic.widget.effect.BounceMelodyEffectView;
import com.ruichaoqun.luckymusic.widget.effect.DynamicEffectLayout;
import com.ruichaoqun.luckymusic.widget.effect.DynamicEffectView;
import com.ruichaoqun.luckymusic.widget.effect.LonelyPlanetEffectView;
import com.ruichaoqun.luckymusic.widget.effect.CosmicDustEffectView;
import com.ruichaoqun.luckymusic.widget.effect.PsychedelicRippleEffectView;
import com.ruichaoqun.luckymusic.widget.effect.DynamicScaleEffectView;
import com.ruichaoqun.luckymusic.widget.effect.ExplosiveParticleEffectView;
import com.ruichaoqun.luckymusic.widget.effect.MusicRadialEffectView;
import com.ruichaoqun.luckymusic.widget.effect.CrystalSoundWaveEffectView;

import java.util.List;

import butterknife.BindView;

/**
 * @author Rui Chaoqun
 * @date :2020-1-13 9:37:48
 * description:PlayerActivity
 */
public class PlayerActivity extends BaseMVPActivity<PlayerContact.Presenter> {

    @BindView(R.id.view_flipper)
    PlayerDiscViewFlipper mViewFlipper;
    @BindView(R.id.iv_stylus)
    ImageView mStylus;

    @BindView(R.id.iv_like)
    ImageView mLike;
    @BindView(R.id.iv_dynamic_effect)
    ImageView mDynamicEffect;
    @BindView(R.id.iv_audio_effect)
    ImageView mAudioEffect;
    @BindView(R.id.iv_play_mode)
    ImageView mPlayMode;
    @BindView(R.id.iv_play_previous)
    ImageView mPlayPrevious;
    @BindView(R.id.iv_play_pause)
    ImageView mPlayPause;
    @BindView(R.id.iv_play_next)
    ImageView mPlayNext;
    @BindView(R.id.iv_play_list)
    ImageView mPlayList;
    @BindView(R.id.tv_current_time)
    TextView mCurrentPosition;
    @BindView(R.id.tv_total_time)
    TextView mTotalPosition;
    @BindView(R.id.player_seek_bar)
    SeekBar mPlayerSeekBar;
    @BindView(R.id.vs_bacground)
    ViewSwitcher mVsBacground;
    @BindView(R.id.lv_lyric)
    LyricView mLyricView;
    @BindView(R.id.layout_lyric)
    RelativeLayout mLayoutLyric;
    @BindView(R.id.layout_sound_controller)
    LinearLayout mLayoutSoundController;
    @BindView(R.id.rl_display_container)
    RelativeLayout mRlDisplayContainer;
    @BindView(R.id.cur_lyric_container)
    LinearLayout mCurLyricContainer;
    @BindView(R.id.tv_lyric_container_time)
    TextView mTvLyricContainerTime;
    @BindView(R.id.artist_image_container)
    RelativeLayout mArtistImageContainer;

    private DynamicEffectLayout mEffectLayout;

    private ViewSwitcherTarget mViewSwitcherTarget;

    private RotationRelativeLayout mCurrentDiscLayout;

    private Handler clientHandler = new Handler();

    //????????????????????????
    public StylusAnimation mStylusRemoveAnimation;
    //????????????????????????
    public StylusAnimation mStylusReturnAnimation;

    public int mStylusAnimationType = STYLUS_ON;

    public static final int STYLUS_ON = 2;
    public static final int STYLUS_OFF = 4;
    public static final int STYLUS_ON_TO_OFF = 3;
    public static final int STYLUS_OFF_TO_ON = 1;

    //??????????????????????????????????????????index???????????????QueueItem???queueId???
    // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
    private int currentDataPosition = -1;
    private MediaSessionCompat.QueueItem nextQueueItem;
    private boolean updatePosition = true;

    //seekbar???????????????????????????????????????seekbar???????????????????????????????????????????????????seekbar???
    private boolean mSeekbarInTouch = false;

    //seekbar???????????????????????????????????????seekbar????????????????????????state??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    //??????????????????????????????seekbar????????????????????????seekbar????????????state????????????????????????
    private boolean mAwaitPlaySeekChanged = false;
    //????????????????????????????????????
    private boolean isBacgroundAutoNext = false;
    long currentPosition;

    private int effectType = -1;


    private Runnable mStylusRemoveRunnable = new Runnable() {
        @Override
        public void run() {
            if (PlayerActivity.this.mStylusAnimationType == STYLUS_OFF_TO_ON || PlayerActivity.this.mStylusAnimationType == STYLUS_ON_TO_OFF) {
                //??????????????????????????????????????????
                PlayerActivity.this.clientHandler.postDelayed(this, 50);
            } else if (PlayerActivity.this.mStylusAnimationType == STYLUS_ON) {
                //????????????ON?????????????????????
                PlayerActivity.this.mStylus.clearAnimation();
                PlayerActivity.this.mStylus.startAnimation(PlayerActivity.this.mStylusRemoveAnimation);
            }
        }
    };

    private Runnable mStylusReturnRunnable = new Runnable() {
        @Override
        public void run() {
            if (PlayerActivity.this.mStylusAnimationType == STYLUS_OFF_TO_ON || PlayerActivity.this.mStylusAnimationType == STYLUS_ON_TO_OFF) {
                //??????????????????????????????????????????
                PlayerActivity.this.clientHandler.postDelayed(this, 50);
            } else if (PlayerActivity.this.mStylusAnimationType == STYLUS_OFF) {
                //????????????OFF?????????????????????
                PlayerActivity.this.mStylus.clearAnimation();
                PlayerActivity.this.mStylus.startAnimation(PlayerActivity.this.mStylusReturnAnimation);
            }
        }
    };

    private Runnable mCheckPlaybackPositionRunnable = new Runnable() {
        @Override
        public void run() {

            if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
                long timeDelta = SystemClock.elapsedRealtime() - mPlaybackState.getLastPositionUpdateTime();
                currentPosition = (long) (mPlaybackState.getPosition() + (timeDelta * mPlaybackState.getPlaybackSpeed()));
            } else {
                currentPosition = mPlaybackState.getPosition();
            }
            if (updatePosition) {
                if (!mSeekbarInTouch && !mAwaitPlaySeekChanged) {
                    mCurrentPosition.setText(TimeUtils.getCurrentPosition(currentPosition));
                    mPlayerSeekBar.setProgress(TimeUtils.formateToSeconds(currentPosition));
                }
                checkPlaybackPosition();
                if(mLayoutLyric.getVisibility() == View.VISIBLE){
                    mLyricView.setPosition(currentPosition,mPlaybackState.getState());
                }
            }
        }
    };

    public void startStylusRemove() {
        //??????????????????????????????
        this.clientHandler.removeCallbacks(this.mStylusRemoveRunnable);
        this.clientHandler.removeCallbacks(this.mStylusReturnRunnable);
        //??????OFF??????
        this.clientHandler.post(this.mStylusRemoveRunnable);
    }

    public void startStylusReturn() {
        //??????????????????????????????
        this.clientHandler.removeCallbacks(this.mStylusRemoveRunnable);
        this.clientHandler.removeCallbacks(this.mStylusReturnRunnable);
        //??????ON??????
        this.clientHandler.post(this.mStylusReturnRunnable);
    }


    public static void launchFrom(Activity activity) {
        activity.startActivity(new Intent(activity, PlayerActivity.class));
    }


    @Override
    protected int getLayoutResId() {
        return R.layout.player_activity;
    }

    @Override
    protected void initParams() {

    }

    @Override
    protected void initView() {
        this.mCurrentDiscLayout = (RotationRelativeLayout) this.mViewFlipper.getCurrentView();
        this.mCurrentDiscLayout.prepareAnimation();
        this.mCurrentDiscLayout.start();
        mPlayerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mCurrentPosition.setText(TimeUtils.getCurrentPositionFromSeekbar(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mSeekbarInTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSeekbarInTouch = false;
                startSeekPlayer(seekBar);
            }
        });
        mVsBacground.setInAnimation(this, android.R.anim.fade_in);
        mVsBacground.setOutAnimation(this, android.R.anim.fade_out);
        mViewSwitcherTarget = new ViewSwitcherTarget(mVsBacground);
    }

    private void startSeekPlayer(SeekBar seekBar) {
        mAwaitPlaySeekChanged = true;
        int duration = seekBar.getProgress();
        mCurrentPosition.setText(TimeUtils.getCurrentPositionFromSeekbar(duration));
        this.mControllerCompat.getTransportControls().seekTo((long) (duration * 1E3));
    }

    @Override
    protected void initData() {
        //??????????????????X?????????
        int stylusPivotX = UiUtils.dp2px(24.888f);
        //??????????????????Y?????????
        int stylusPivotY = UiUtils.dp2px(42.222f);
        this.mStylusRemoveAnimation = new StylusAnimation(0.0f, -25.0f, stylusPivotX, stylusPivotY);
        this.mStylusRemoveAnimation.setDuration(300);
        this.mStylusRemoveAnimation.setRepeatCount(0);
        this.mStylusRemoveAnimation.setFillAfter(true);
        this.mStylusRemoveAnimation.setFillEnabled(true);
        this.mStylusRemoveAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                PlayerActivity.this.mStylusAnimationType = STYLUS_ON_TO_OFF;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (PlayerActivity.this.mStylusRemoveAnimation.getInterpolatedTime() >= 1.0f) {
                    PlayerActivity.this.mStylusAnimationType = STYLUS_OFF;
                    PlayerActivity.this.mCurrentDiscLayout.pause();
                    PlayerActivity.this.mStylusReturnAnimation.setDegrees(Integer.MIN_VALUE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        this.mStylusReturnAnimation = new StylusAnimation(-25.0f, 0.0f, stylusPivotX, stylusPivotY);
        this.mStylusReturnAnimation.setDuration(300);
        this.mStylusReturnAnimation.setRepeatCount(0);
        this.mStylusReturnAnimation.setFillAfter(true);
        this.mStylusReturnAnimation.setFillEnabled(true);
        this.mStylusReturnAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                PlayerActivity.this.mStylusAnimationType = STYLUS_OFF_TO_ON;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                PlayerActivity.this.mStylusAnimationType = STYLUS_ON;
                PlayerActivity.this.mStylusRemoveAnimation.setDegrees(Integer.MIN_VALUE);
                PlayerActivity.this.mCurrentDiscLayout.start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        this.mViewFlipper.setOnPlayerDiscListener(new PlayerDiscViewFlipper.OnPlayerDiscListener() {
            @Override
            public void onScrolled(boolean z) {
                //?????????????????????????????????????????????????????????????????????????????????
                Log.w(TAG, "onScrolled-->");
                PlayerActivity.this.startStylusRemove();
                PlayerActivity.this.mCurrentDiscLayout.pause();
            }


            /**
             * ????????????flipperView???nextView??????
             * @param bool false??????????????????true???????????????
             */
            @Override
            public void onDiscDirectionChange(Boolean bool) {
                if (PlayerActivity.this.mPlaybackState != null && PlayerActivity.this.queueItems != null) {
                    if (bool) {
                        PlayerActivity.this.nextQueueItem = PlayerActivity.this.queueItems.get(currentDataPosition == 0 ? PlayerActivity.this.queueItems.size() - 1 :  (currentDataPosition - 1));
                    } else {
                        PlayerActivity.this.nextQueueItem = PlayerActivity.this.queueItems.get(currentDataPosition == PlayerActivity.this.queueItems.size() - 1 ? 0 : (currentDataPosition + 1));
                    }
                    GlideApp.with(PlayerActivity.this)
                            .load(nextQueueItem.getDescription().getIconUri())
                            .transform(new CircleCrop())
                            .centerCrop()
                            .error(R.drawable.ic_disc_playhoder)
                            .into((ImageView) ((ViewGroup) PlayerActivity.this.mViewFlipper.getNextView()).getChildAt(0));
                }
            }


            /**
             *
             * @param z ???????????????disc
             * @param justSwitchDisc ????????????????????????disc
             * @param isScrollingLeft  ?????????????????????
             */
            @Override
            public void onDiscSwitchComplete(boolean z, boolean justSwitchDisc, boolean isScrollingLeft) {
                Log.w(TAG, "onDiscSwitchComplete-->" + z + "    " + justSwitchDisc + "    " + isScrollingLeft);
                //???????????????????????????????????????????????????????????????title???subtitle??????????????????????????????

                //??????????????????
                if (!z && !isBacgroundAutoNext) {
                    //????????????????????????????????????????????????
                    if (isScrollingLeft) {
                        //???????????????
                        PlayerActivity.this.mControllerCompat.getTransportControls().skipToNext();
                    } else {
                        //???????????????
                        PlayerActivity.this.mControllerCompat.getTransportControls().skipToPrevious();
                    }
                    switchBacground(nextQueueItem.getDescription().getIconUri());

                } else {
                    //?????????????????????????????????
                    if(mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING){
                        PlayerActivity.this.startStylusReturn();
                    }
                }
                isBacgroundAutoNext = false;
                PlayerActivity.this.mCurrentDiscLayout = (RotationRelativeLayout) PlayerActivity.this.mViewFlipper.getCurrentView();
                for (int i = 0; i < queueItems.size(); i++) {
                    if (nextQueueItem != null && nextQueueItem.getQueueId() == queueItems.get(i).getQueueId()) {
                        PlayerActivity.this.currentDataPosition = i;
                        break;
                    }
                }
                ((RotationRelativeLayout) PlayerActivity.this.mViewFlipper.getNextView()).stopAndRest();
                PlayerActivity.this.mCurrentDiscLayout.prepareAnimation();
            }

            @Override
            public void onDiscSwitchHalf(Boolean bool) {
                Log.w(TAG, "onDiscSwitchHalf-->" + bool);
                if (bool == null) {
                    PlayerActivity.this.setTitle(PlayerActivity.this.mCurrentMetadata.getDescription().getTitle());
                    PlayerActivity.this.setSubTitle(PlayerActivity.this.mCurrentMetadata.getDescription().getSubtitle());
                    //TODO ??????????????????
                } else {
                    PlayerActivity.this.setTitle(nextQueueItem.getDescription().getTitle());
                    PlayerActivity.this.setSubTitle(nextQueueItem.getDescription().getSubtitle());
                    //TODO ??????????????????
                }
            }
        });

        this.mPlayMode.setOnClickListener(v -> switchPlayMode());

        this.mPlayPrevious.setOnClickListener(v -> {
//                PlayerActivity.this.mControllerCompat.getTransportControls().skipToPrevious();
            //?????????????????????????????????????????????????????????????????????????????????
            PlayerActivity.this.mViewFlipper.switchDisc(false);
        });

        this.mPlayPause.setOnClickListener(v -> {
            if (PlayerActivity.this.mCurrentMetadata != null && !TextUtils.isEmpty(PlayerActivity.this.mCurrentMetadata.getDescription().getMediaId())) {
                if (PlayerActivity.this.mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING ||
                        PlayerActivity.this.mPlaybackState.getState() == PlaybackStateCompat.STATE_BUFFERING ||
                        PlayerActivity.this.mPlaybackState.getState() == PlaybackStateCompat.STATE_CONNECTING) {
                    PlayerActivity.this.mControllerCompat.getTransportControls().pause();
                } else {
                    PlayerActivity.this.mControllerCompat.getTransportControls().play();
                }
            }
        });

        this.mPlayNext.setOnClickListener(v -> PlayerActivity.this.mViewFlipper.switchDisc(true));

        this.mPlayList.setOnClickListener(v -> showPlayListDialog());

        this.mRlDisplayContainer.setOnClickListener(v -> {
            mRlDisplayContainer.setVisibility(View.INVISIBLE);
            mLayoutLyric.setVisibility(View.VISIBLE);
            mLayoutSoundController.setVisibility(View.VISIBLE);
        });

        this.mCurLyricContainer.setOnClickListener(v -> {
            long times = mLyricView.getCurrentTims();
            if(times == -1){
                if(mPlaybackState.getState() == PlaybackStateCompat.STATE_PAUSED){
                    PlayerActivity.this.mControllerCompat.getTransportControls().play();
                }
            }else{
                PlayerActivity.this.mControllerCompat.getTransportControls().seekTo(times);
                if(mPlaybackState.getState() == PlaybackStateCompat.STATE_PAUSED){
                    PlayerActivity.this.mControllerCompat.getTransportControls().play();
                }
            }
        });

        this.mLyricView.setOnClickListener(v -> {
            mRlDisplayContainer.setVisibility(View.VISIBLE);
            mLayoutLyric.setVisibility(View.GONE);
            mLayoutSoundController.setVisibility(View.GONE);
        });

        setEffectView(mPresenter.getDynamicEffectType());

        mDynamicEffect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDynamicEffectDialog();
            }
        });

        mLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mAudioEffect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EqualizerActivity.launchFrom(PlayerActivity.this);
            }
        });
    }

    public void setEffectView(int type) {
        if(this.effectType != type){
            this.effectType = type;
            mPresenter.setDynamicEffectType(type);
            if(effectType != 0){
                addEffectView();
                mRlDisplayContainer.setVisibility(View.GONE);
                this.mDynamicEffect.setImageResource(R.drawable.selector_dynamic_effect_on);
            }else{
                removeEffectView();
                mRlDisplayContainer.setVisibility(View.VISIBLE);
                this.mDynamicEffect.setImageResource(R.drawable.selector_dynamic_effect_off);
            }
        }
    }

    private void addEffectView() {
        if(this.mEffectLayout == null){
            this.mEffectLayout = new DynamicEffectLayout(this);
            this.mEffectLayout.setOnColorGetListener(color -> {
                LayerDrawable drawable = (LayerDrawable) mPlayerSeekBar.getProgressDrawable();
                int c =  ColorUtil.getEffectColor(color, new float[3]);
                DrawableCompat.setTintList(drawable.findDrawableByLayerId(android.R.id.progress), ColorStateList.valueOf(c));
                mPlayerSeekBar.setProgressDrawable(drawable);
            });
            ImageView imageView = findViewById(R.id.iv_disc_bg_1);
            int height = imageView.getLayoutParams().height;
            if (height <= 0) {
                Drawable drawable = imageView.getDrawable();
                if (drawable == null) {
                    drawable = getResources().getDrawable(R.drawable.bg_disc);
                }
                height = drawable.getIntrinsicWidth();
            }
            this.mArtistImageContainer.addView(this.mEffectLayout, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height + (((RelativeLayout.LayoutParams) this.mViewFlipper.getLayoutParams()).topMargin * 2)));
            if(this.mCurrentMetadata != null){
                this.mEffectLayout.setArtViewResource(this.mCurrentMetadata.getDescription().getIconUri());
            }
        }
        this.mEffectLayout.setVisualizer(mPresenter.getSessionId());
        this.mEffectLayout.addDynamicEffectView(getEffectView(effectType));
        if(mPlaybackState != null && mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING){
            this.mEffectLayout.prepare();
        }
    }

    private void removeEffectView() {
        if(this.mEffectLayout != null){
            this.mArtistImageContainer.removeView(this.mEffectLayout);
            mEffectLayout = null;
        }
    }

    /**
     * ??????????????????dialog
     */
    private void showDynamicEffectDialog() {
        DynamicEffectSheet.showDynamicEffectSheet(this, effectType);
    }

    private DynamicEffectView getEffectView(long type){
        if(type == 1){
            return new CosmicDustEffectView(this);
        }

        if(type == 2){
            return new LonelyPlanetEffectView(this);
        }

        if(type == 3){
            return new BounceMelodyEffectView(this);
        }

        if(type == 4){
            return new PsychedelicRippleEffectView(this);
        }

        if(type == 5){
            return new ExplosiveParticleEffectView(this);
        }

        if(type == 6){
            return new DynamicScaleEffectView(this);
        }

        if(type == 7){
            return new CrystalSoundWaveEffectView(this,false);
        }

        if(type == 8){
            return new MusicRadialEffectView(this);
        }

        throw new RuntimeException("??????????????????");
    }

    private void switchBacground(Uri iconUri) {
        GlideApp.with(PlayerActivity.this)
                .load(iconUri)
                .transform(new RenderScriptTransformation())
                .error(R.drawable.bg_playing)
                .into(this.mViewSwitcherTarget);
    }

    @Override
    public void onMediaServiceConnected() {
        super.onMediaServiceConnected();
        //???????????????????????????
        if (this.mCurrentMetadata != null && !TextUtils.isEmpty(this.mCurrentMetadata.getDescription().getMediaId())) {
            this.setTitle(this.mCurrentMetadata.getDescription().getTitle());
            this.setSubTitle(this.mCurrentMetadata.getDescription().getSubtitle());
            currentDataPosition = getCurrentMusicPosition();
            this.mTotalPosition.setText(TimeUtils.getCurrentPosition(mCurrentMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)));
            this.mPlayerSeekBar.setMax(TimeUtils.formateToSeconds(mCurrentMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)));
            this.mPlayerSeekBar.setProgress(TimeUtils.formateToSeconds(mPlaybackState.getPosition()));
            GlideApp.with(this).load(mCurrentMetadata.getDescription().getIconUri()).transform(new CircleCrop()).centerCrop().transition(DrawableTransitionOptions.withCrossFade()).placeholder(R.drawable.ic_disc_playhoder).into((ImageView) this.mCurrentDiscLayout.getChildAt(0));
            //TODO ??????????????????
            switchBacground(mCurrentMetadata.getDescription().getIconUri());
            if(this.mEffectLayout != null){
                this.mEffectLayout.setArtViewResource(mCurrentMetadata.getDescription().getIconUri());
            }
        }
        switch (this.mPlaybackState.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                if (!this.mCurrentDiscLayout.isRunning()) {
                    this.mCurrentDiscLayout.start();
                }
                if (this.mStylusAnimationType != STYLUS_ON) {
                    this.mStylusReturnAnimation.setDegrees(0);
                    this.startStylusReturn();
                }
                this.mPlayPause.setImageResource(R.drawable.selector_player_pause);
                if(this.mEffectLayout != null) {
                    this.mEffectLayout.prepare();
                }
                break;
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
            case PlaybackStateCompat.STATE_PAUSED:
                this.mStylusRemoveAnimation.setDegrees(-25);
                this.startStylusRemove();
                this.mPlayPause.setImageResource(R.drawable.selector_player_play);
                break;
            default:
        }
        checkPlaybackPosition();
        onRepeatModeChanged(playMode);
    }

    private int getCurrentMusicPosition() {
        //???????????????????????????????????????index?????????item???queueId????????????
        for (int i = 0; i < queueItems.size(); i++) {
            if (TextUtils.equals(mCurrentMetadata.getDescription().getMediaId(),queueItems.get(i).getDescription().getMediaId())) {
                return i;
            }
        }
        return -1;
    }

    private void checkPlaybackPosition() {
        this.clientHandler.postDelayed(mCheckPlaybackPositionRunnable, 100L);
    }

    /**
     * ??????????????????
     *
     * @param metadata
     */
    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        super.onMetadataChanged(metadata);
        PlayerActivity.this.setTitle(metadata.getDescription().getTitle());
        PlayerActivity.this.setSubTitle(metadata.getDescription().getSubtitle());
        this.mTotalPosition.setText(TimeUtils.getCurrentPosition(metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)));
        this.mPlayerSeekBar.setMax(TimeUtils.formateToSeconds(metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)));
        //????????????????????????????????????
        if (currentDataPosition == -1) {
            GlideApp.with(this).load(mCurrentMetadata.getDescription().getIconUri()).transform(new CircleCrop()).centerCrop().transition(DrawableTransitionOptions.withCrossFade()).placeholder(R.drawable.ic_disc_playhoder).into((ImageView) this.mCurrentDiscLayout.getChildAt(0));
            //TODO ??????????????????
            switchBacground(mCurrentMetadata.getDescription().getIconUri());
            currentDataPosition = getCurrentMusicPosition();
             return;
        }

        int position = getCurrentMusicPosition();
        if(((currentDataPosition == queueItems.size()-1 && position == 0)||position == currentDataPosition+1)&& !isBacgroundAutoNext ){
            if (!TextUtils.equals(queueItems.get(currentDataPosition).getDescription().getMediaId(), metadata.getDescription().getMediaId()) && !isBacgroundAutoNext) {
                isBacgroundAutoNext = true;
                PlayerActivity.this.mViewFlipper.switchDisc(true);
            }
        }else{
            GlideApp.with(this).load(mCurrentMetadata.getDescription().getIconUri()).transform(new CircleCrop()).centerCrop().transition(DrawableTransitionOptions.withCrossFade()).placeholder(R.drawable.ic_disc_playhoder).into((ImageView) this.mCurrentDiscLayout.getChildAt(0));
            //TODO ??????????????????
            switchBacground(mCurrentMetadata.getDescription().getIconUri());
            currentDataPosition = position;
        }
        if(this.mEffectLayout != null) {
            this.mEffectLayout.setArtViewResource(mCurrentMetadata.getDescription().getIconUri());
        }
    }

    /**
     * ????????????????????????
     *
     * @param state
     */
    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat state) {
        super.onPlaybackStateChanged(state);
        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
            case PlaybackStateCompat.STATE_BUFFERING:
            case PlaybackStateCompat.STATE_CONNECTING:
                this.mAwaitPlaySeekChanged = false;
                this.startStylusReturn();
                if (!this.mCurrentDiscLayout.isRunning()) {
                    this.mCurrentDiscLayout.start();
                }
                this.mPlayPause.setImageResource(R.drawable.selector_player_pause);
                if(this.mEffectLayout != null) {
                    this.mEffectLayout.prepare();
                }
                break;
            case PlaybackStateCompat.STATE_STOPPED:
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_PAUSED:
            case PlaybackStateCompat.STATE_ERROR:
                if(this.mEffectLayout != null) {
                    this.mEffectLayout.pause();
                }
                this.startStylusRemove();
                this.mPlayPause.setImageResource(R.drawable.selector_player_play);
                break;
            case PlaybackStateCompat.STATE_FAST_FORWARDING:
                break;
            case PlaybackStateCompat.STATE_REWINDING:
                break;
            case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT:
                break;
            case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS:
                break;
            case PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM:
                break;
            default:
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        if (repeatMode == 3) {
            this.mPlayMode.setImageResource(R.drawable.selector_player_mode_shuffer);
        } else if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) {
            this.mPlayMode.setImageResource(R.drawable.selector_player_mode_single);
        } else if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL) {
            this.mPlayMode.setImageResource(R.drawable.selector_player_mode_list_circulation);
        }
    }

    @Override
    public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
        super.onQueueChanged(queue);
        currentDataPosition = getCurrentMusicPosition();
        if(queue.size() == 0){
            finish();
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initToolBar();
        transparentStatusBar(true);
    }

    @Override
    public boolean needToolBar() {
        return false;
    }

    private int getToolbarHeight() {
        return (CommonUtils.versionAbove19() ? UiUtils.getStatusBarHeight(this) : 0) + UiUtils.getToolbarHeight();
    }

    @Override
    public void initToolBar() {
        super.initToolBar();
        this.toolbar.setPadding(0, UiUtils.getStatusBarHeight(this), 0, 0);
        ((RelativeLayout.LayoutParams) this.toolbar.getLayoutParams()).height = getToolbarHeight();
        this.toolbar.setBackgroundColor(Color.TRANSPARENT);
        applyToolbarCurrentThemeWithViewColor(this.toolbar);
        setTitle(R.string.is_playing);
    }

    @Override
    public boolean isToolbarOnImage() {
        return true;
    }

    @Override
    public boolean isNeedMiniPlayerBar() {
        return false;
    }

    @Override
    protected void onDestroy() {
        updatePosition = false;
        mCurrentDiscLayout.stop();
        super.onDestroy();
    }

    public View getPlayCurLyricContainer() {
        return this.mCurLyricContainer;
    }
}
