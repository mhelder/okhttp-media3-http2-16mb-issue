package com.example.okhttpexosample

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cronet.CronetDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.okhttpexosample.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import okhttp3.Protocol
import org.chromium.net.CronetEngine
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding.playerControlView) {
            // show controller and keep showing it
            showController()
            controllerShowTimeoutMs = -1
            controllerHideOnTouch = false
        }

        // default http stack
        binding.defaultHttpButton.setOnClickListener {
            releasePlayer()

            val httpDataSource = DefaultHttpDataSource.Factory()
            binding.playerControlView.player = buildPlayer(httpDataSource)
                .also(Player::prepareAndPlayItem)
        }

        // default okhttp (http2 enabled)
        binding.okhttpHttp2Button.setOnClickListener {
            releasePlayer()

            val okHttpClient = OkHttpClient.Builder()
                .build()
            val httpDataSource = OkHttpDataSource.Factory(okHttpClient)
            binding.playerControlView.player = buildPlayer(httpDataSource)
                .also(Player::prepareAndPlayItem)
        }

        // okhttp forced to http1.1
        binding.okhttpHttp1Button.setOnClickListener {
            releasePlayer()

            val okHttpClient = OkHttpClient.Builder()
                .protocols(listOf(Protocol.HTTP_1_1))
                .build()
            val httpDataSource = OkHttpDataSource.Factory(okHttpClient)
            binding.playerControlView.player = buildPlayer(httpDataSource)
                .also(Player::prepareAndPlayItem)
        }

        // default cronet (http2; requires play services)
        binding.cronetHttp2Button.setOnClickListener {
            releasePlayer()

            val cronetEngine = CronetEngine.Builder(applicationContext)
                .build()
            val httpDataSource =
                CronetDataSource.Factory(cronetEngine, Executors.newSingleThreadExecutor())
            binding.playerControlView.player = buildPlayer(httpDataSource)
                .also(Player::prepareAndPlayItem)
        }
    }

    private fun releasePlayer() {
        with(binding.playerControlView) {
            player?.release()
            player = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }
}

private fun Context.buildPlayer(
    httpDataSource: DataSource.Factory,
): Player {
    val mediaSourceFactory = DefaultMediaSourceFactory(this)
        .setDataSourceFactory(httpDataSource)

    return ExoPlayer.Builder(applicationContext)
        .setMediaSourceFactory(mediaSourceFactory)
        .build()
        .apply {
            // speed up playback by an arbitrary high value
            playbackParameters = PlaybackParameters(128f, 1f)
        }
}

private fun Player.prepareAndPlayItem() {
    setMediaItem(MediaItem.fromUri(mediaUrl))
    prepare()
    play()
}

private const val mediaUrl =
    "https://25473.mc.tritondigital.com/OMNY_DSVANDAAG_PODCAST_P/media-session/16bd9128-ab1d-4d8b-8261-ac7da5144703/d/clips/fdd7ab40-270d-4a1e-a257-acd200da1324/71125754-bd8a-43e1-9a89-ad86013d02f9/29a6f2f8-0dc9-4f78-b73a-b00b00bea6b2/audio/direct/t1684886453/Moet_de_beer_dood_verhit_de_Italiaanse_gemoederen.mp3?t=1684886453&in_playlist=cb1d5e53-dc3a-4853-84f0-ad86013d0307&utm_source=Podcast"
