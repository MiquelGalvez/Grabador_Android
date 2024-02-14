package campalans.dam.grabador_android

import android.Manifest.permission
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import campalans.dam.grabador_android.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null
    var mFileName: File? = null
    // Nueva variable para controlar la reproducción
    private var isPlaying = false
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)



        binding.btnRecord.setOnClickListener {
            startRecording()
        }

        binding.btnStop.setOnClickListener {
            pauseRecording()
        }

        binding.btnPlay.setOnClickListener {
            if (mFileName != null) { // Verifica si hay un archivo para reproducir
                if (isPlaying) {
                    pauseAudio() // Pausa la reproducción si está en curso
                } else {
                    playAudio() // Reproduce el audio si no está en curso
                }
            } else {
                Toast.makeText(applicationContext, "No hi ha grabació que reproduir", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnStopPlay.setOnClickListener {
            pausePlaying()
        }
    }

    private fun startRecording() {

        // Check permissions
        if (CheckPermissions()) {

            // Save file
            mFileName = File(getExternalFilesDir("")?.absolutePath,"Record.3gp")

            // If file exists then increment counter
            var n = 0
            while (mFileName!!.exists()) {
                n++
                mFileName = File(getExternalFilesDir("")?.absolutePath,"Record$n.3gp")
            }

            // Initialize the class MediaRecorder
            mRecorder = MediaRecorder()

            // Set source to get audio
            mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)

            // Set the format of the file
            mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)

            // Set the audio encoder
            mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            // Set the save path
            mRecorder!!.setOutputFile(mFileName)
            try {
                // Preparation of the audio file
                mRecorder!!.prepare()
            } catch (e: IOException) {
                Log.e("TAG", "prepare() failed")
            }
            // Start the audio recording
            mRecorder!!.start()
            binding.idTVstatus.text = "Recording in progress"
        } else {
            // Request permissions
            RequestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // If permissions accepted ->
        when (requestCode) {
            REQUEST_AUDIO_PERMISSION_CODE -> if (grantResults.size > 0) {
                val permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (permissionToRecord && permissionToStore) {

                    // Message
                    Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_LONG).show()

                } else {

                    // Message
                    Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun CheckPermissions(): Boolean {

        // Check permissions
        val result =
            ContextCompat.checkSelfPermission(applicationContext, permission.WRITE_EXTERNAL_STORAGE)
        val result1 = ContextCompat.checkSelfPermission(applicationContext, permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun RequestPermissions() {

        // Request permissions
        ActivityCompat.requestPermissions(this,
            arrayOf(permission.RECORD_AUDIO, permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_AUDIO_PERMISSION_CODE)
    }

    fun playAudio() {
        if (!isPlaying) { // Nomes inicia la reproducció si no esta ja en curs
            binding.btnPlay.setBackgroundResource(R.drawable.btn_rec_play)
            mPlayer = MediaPlayer()
            try {
                mPlayer!!.setDataSource(mFileName.toString())
                mPlayer!!.prepare()
                mPlayer!!.start()
                binding.idTVstatus.text = "Listening recording"
                isPlaying = true // Actualiza l'estat de reproducció
            } catch (e: IOException) {
                Log.e("TAG", "prepare() failed")
            }
        }
    }

    fun pauseRecording() {

        // Stop recording
        if (mFileName == null) {

            // Message
            Toast.makeText(getApplicationContext(), "Registration not started", Toast.LENGTH_LONG).show()

        } else {
            mRecorder!!.stop()

            // Message to confirm save file
            val savedUri = Uri.fromFile(mFileName)
            val msg = "File saved: " + savedUri!!.lastPathSegment
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show()

            // Release the class mRecorder
            mRecorder!!.release()
            mRecorder = null
            binding.idTVstatus.text = "Recording interrupted"
        }
    }

    fun pausePlaying() {

        // Stop playing the audio file
        binding.idTVstatus.text = "Recording stopped"
        // Tornar a posar el boto com ha iniciar reproducció per que no es quedi mai en pausa
        binding.btnPlay.setBackgroundResource(R.drawable.btn_rec_play)
    }

    fun pauseAudio() {
        if (mPlayer != null && mPlayer!!.isPlaying) {
            mPlayer!!.pause() // Pausa la reprodcucció si esta en curs
            binding.btnPlay.setBackgroundResource(R.drawable.button_pause)
            binding.idTVstatus.text = "Recording paused"
            isPlaying = false // Actualitza l'estat a false per si es torna a clicar al boto de play
        }
    }

    companion object {
        const val REQUEST_AUDIO_PERMISSION_CODE = 1
    }
}