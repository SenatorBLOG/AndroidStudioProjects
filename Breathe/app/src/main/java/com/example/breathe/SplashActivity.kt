//import android.content.Intent
//import android.media.MediaPlayer
//import android.net.Uri
//import android.os.Bundle
//import android.widget.VideoView
//import androidx.appcompat.app.AppCompatActivity
//import com.example.breathe.MainActivity
//import com.example.breathe.R
//
//class SplashActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Прозрачный фон и без тулбара
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//        )
//
//        val videoView = VideoView(this)
//        setContentView(videoView)
//
//        val uri = Uri.parse("android.resource://${packageName}/${R.raw.breathe_intro}")
//        videoView.setVideoURI(uri)
//
//        val mediaPlayer = MediaPlayer.create(this, R.raw.breath_sound)
//        mediaPlayer.start()
//
//        videoView.setOnCompletionListener {
//            mediaPlayer.release()
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
//        }
//
//        videoView.start()
//    }
//}
