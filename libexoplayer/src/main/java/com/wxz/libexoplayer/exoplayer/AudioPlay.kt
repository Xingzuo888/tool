package com.wxz.libexoplayer.exoplayer

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.google.android.exoplayer2.Player
import com.wxz.libexoplayer.R
import com.wxz.libexoplayer.exoplayer.IPlayTarget
import com.wxz.libexoplayer.exoplayer.PageListPlayManager

/**
 *    Author : wxz
 *    Time   : 2020/12/16
 *    Desc   :
 */
class AudioPlay : FrameLayout, IPlayTarget, Player.EventListener {

    protected lateinit var playBtn: ImageView
    protected var mCategory: String? = null
    protected var mAudioUrl: String? = null
    protected var isPlay = false
    protected var mWidthPx = 0
    protected var mHeightPx = 0

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        LayoutInflater.from(context).inflate(R.layout.layout_exo_player_audio, this, true)

        playBtn = findViewById(R.id.play_btn)

        playBtn.setOnClickListener { v ->
            if (isPlaying()) {
                inActive()
            } else {
                onActive()
            }
        }

        this.transitionName = "audioplay"
    }

    fun bindData(
        category: String?,
        widthPx: Int,
        heightPx: Int,
        audioUrl: String?
    ) {
        mCategory = category
        mWidthPx = widthPx
        mHeightPx = heightPx
        mAudioUrl = audioUrl
    }

    override fun getOwner(): ViewGroup {
        return this
    }

    override fun onActive() {
        //音频播放或者回复播放
        val pageListPlay = PageListPlayManager.get(mCategory)
        val playerView = pageListPlay.playerView
        val controlView = pageListPlay.controlView
        val exoPlayer = pageListPlay.exoPlayer
        if (playerView == null) return
        if (TextUtils.equals(pageListPlay.playUrl, mAudioUrl)) {
            onPlayerStateChanged(true, Player.STATE_READY)
        } else {
            val mediaSource = PageListPlayManager.createMediaSource(mAudioUrl)
            exoPlayer.prepare(mediaSource)
            exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
            pageListPlay.playUrl = mAudioUrl
        }
        exoPlayer.addListener(this)
        exoPlayer.playWhenReady = true
    }

    override fun inActive() {
        //暂停时
    }

    override fun isPlaying(): Boolean {
        return isPlay
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        //监听音频播放的状态
        val pageListPlay = PageListPlayManager.get(mCategory)
        val exoPlayer = pageListPlay.exoPlayer
        if (playbackState == Player.STATE_READY && exoPlayer.bufferedPosition != 0L && playWhenReady) {

        } else if (playbackState == Player.STATE_BUFFERING) {

        }
        isPlay =
            playbackState == Player.STATE_READY && exoPlayer.bufferedPosition != 0L && playWhenReady
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isPlay = false
    }
}