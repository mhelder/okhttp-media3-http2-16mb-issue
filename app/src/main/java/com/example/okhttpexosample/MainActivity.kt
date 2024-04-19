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
    "https://25523.mc.tritondigital.com/OMNY_DSVANDAAGAPP_PODCAST_P/media-session/048a28d3-b74f-4782-ab7f-d07291f80ce1/d/clips/fdd7ab40-270d-4a1e-a257-acd200da1324/2e850afd-b63e-4791-ae3f-b0380101da98/47eba102-a0f8-4277-b44e-b0d1012b5f6f/audio/direct/t1701998186/Gaza_(2_2)_Ondanks_vredespogingen_dreven_Isra_l_en_Palestina_verder_uit_elkaar.mp3?t=1701998186&in_playlist=a4cdb64d-2881-4330-be5d-b0380101dbc7&utm_source=Podcast"
