package com.example.reversecamera


import android.Manifest
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.media.MediaRecorder
import android.os.*
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.viewfinder.CameraViewfinder
import androidx.camera.viewfinder.CameraViewfinderExt.requestSurface
import androidx.camera.viewfinder.ViewfinderSurfaceRequest
import androidx.core.app.ActivityCompat
import com.example.reversecamera.GraphicOverlay.varib.degree
import com.example.reversecamera.GraphicOverlay.varib.degree1
import com.example.reversecamera.GraphicOverlay.varib.degree2
import com.example.reversecamera.GraphicOverlay.varib.degree3
import com.example.reversecamera.GraphicOverlay.varib.degree4
import com.example.reversecamera.utils.Constants.lengthOfCar
import com.example.reversecamera.utils.Constants.widthOfCar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity(){

    var button: ImageButton? = null
    private var shutter: ImageView? = null
   lateinit var textureView: CameraViewfinder
    private val permissionsArrayList = ArrayList<String>()
    private var cameraId = "0"
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSessions: CameraCaptureSession? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null
    lateinit var imageDimension: Array<Size>
    var mBackgroundHundler: Handler? = null
    var mBackgroundThread: HandlerThread? = null
    var mImageBackgroundThread: HandlerThread? = null
    lateinit var imageTransformMatrix: Matrix
   var  mImageBackgroundHandler: Handler? = null
    private var recorder: MediaRecorder? = null
    private var isRecording = false
    private var isPaused = false
    var timer: Timer? = null
    var file: String? = null
    var seekbar: SeekBar? = null
    lateinit var fps : TextView
    lateinit var manager :CameraManager
    lateinit var mSurface:Surface

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        fps = findViewById(R.id.fps2)
        val view = GraphicOverlay(this)
        textureView = findViewById(R.id.cameraviewfinder)
        button = findViewById(R.id.button)
        shutter = findViewById(R.id.shutter)
        seekbar = findViewById(R.id.seekBar)
        seekbar?.setMax(756)    // 100
        seekbar?.setMin(-756)   // -100
        seekbar?.setProgress(0) // 0
        var seekbar1 = findViewById<SeekBar>(R.id.seekBar1)
        var seekbar2 = findViewById<SeekBar>(R.id.seekBar2)
        var seekbar3 = findViewById<SeekBar>(R.id.seekBar3)
        var seekbar4 = findViewById<SeekBar>(R.id.seekBar4)
        seekbar4.setMax(500)
        seekbar4.setMin(0)
        seekbar3.setMax(500)
        seekbar3.setMin(0)
        seekbar1.setMax(500)
        seekbar1.setMin(0)
        seekbar2.setMax(500)
        seekbar2.setMin(0)
        seekbar1?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                Log.d("LogTag", progress.toString())
                degree1 =  progress
                Log.v("vishalProgress","${progress}")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        seekbar2?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                Log.d("LogTag", progress.toString())
                degree2 =  progress
                Log.v("vishalProgress","${progress}")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        seekbar3?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                Log.d("LogTag", progress.toString())
                degree3 =  progress
                widthOfCar = 250 + degree3
                Log.v("vishalProgress","${progress}")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })


        seekbar4?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                Log.d("LogTag", progress.toString())
                degree4 =  progress
//                view.setupDegree(degree)
                lengthOfCar = (500 + degree4).toDouble()
//                Toast.makeText(this@MainActivity,"${progress}",Toast.LENGTH_LONG).show()
                Log.v("vishalProgress","${progress}")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        seekbar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                Log.d("LogTag", progress.toString())
                degree =  progress
//                view.setupDegree(degree)

//                Toast.makeText(this@MainActivity,"${progress}",Toast.LENGTH_LONG).show()
                Log.v("vishalProgress","${progress}")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        button?.setOnClickListener(View.OnClickListener { v: View? ->
            try {
                flipCamera()
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        })
        shutter?.setOnClickListener(View.OnClickListener { v: View? ->
            setupmediaRecorder()
            startRecording()
            Toast.makeText(this, "Recording Started", Toast.LENGTH_SHORT).show()
        })
    }



    private fun setupmediaRecorder(){
        requestPerms()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            recorder = MediaRecorder()
            recorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            recorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
             recorder!!.setVideoFrameRate(30)
            recorder!!.setOrientationHint(90)
            file = recordingFilePath
            Toast.makeText(this, file, Toast.LENGTH_SHORT).show()
            recorder!!.setOutputFile(file)
            try {
                recorder!!.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        isRecording = true
        isPaused = false
    }

    private fun startRecording() {
        Log.v("vishal_recording","started")
        val previewSurface: Surface =mSurface
        val recordingSurface = recorder?.surface
        captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
        captureRequestBuilder?.addTarget(previewSurface)
        if (recordingSurface != null) {
            Log.v("vishal_recording","surface is null")
            captureRequestBuilder?.addTarget(recordingSurface)
            cameraDevice?.createCaptureSession(listOf(previewSurface, recordingSurface), captureStateVideoCallback, mBackgroundHundler)
        }


    }

    private val captureStateVideoCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(session: CameraCaptureSession) {

        }
        override fun onConfigured(session: CameraCaptureSession) {
            cameraCaptureSessions = session
            captureRequestBuilder?.set(CaptureRequest.CONTROL_AF_MODE, CaptureResult.CONTROL_AF_MODE_CONTINUOUS_VIDEO)
            try {
                cameraCaptureSessions!!.setRepeatingRequest(
                    captureRequestBuilder!!.build(), null,
                    mBackgroundHundler
                )
                recorder?.start()
            } catch (e: CameraAccessException) {
                e.printStackTrace()
                //Log.e(TAG, "Failed to start camera preview because it couldn't access the camera")
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

        }
    }

    private fun stopRecorder() {
        if(recorder != null) {
            recorder!!.stop()
        }
    }


    fun pauseRecorder() {
        if(recorder != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                recorder!!.pause()
            }
        }
        isPaused = true
    }

    fun resumeRecorder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder!!.resume()
        }
        isPaused = false
    }

    //        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.DD_hh.mm.ss");
    private val recordingFilePath: String
        private get() {
            val contextWrapper = ContextWrapper(applicationContext)
            val musicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            //        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.DD_hh.mm.ss");
            val file = File(musicDirectory, "MyCamera" + "date" + ".mp4")
            Log.v("vishal_recording","${file.path}")
            return file.path
        }

    @Throws(CameraAccessException::class)
    private fun flipCamera() {
        if (cameraDevice != null && cameraId == "0") {
            closeCamera()
            cameraId = "1"
           setupCameraViewfinder()
        } else if (cameraDevice != null && cameraId == "1") {
            closeCamera()
            cameraId = "0"
            setupCameraViewfinder()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    applicationContext,
                    "Sorry, camera permission is necessary",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } else if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    applicationContext,
                    "Sorry, camera permission is necessary",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    fun requestPerms() {
        val permission = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this@MainActivity, permission, 101)
        }
    }


    private val stateCallBack: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            try {
                createCameraPreview()
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice!!.close()
        }

        override fun onError(camera: CameraDevice, i: Int) {
            cameraDevice!!.close()
            cameraDevice = null
        }
    }

    @Throws(CameraAccessException::class)
    private fun createCameraPreview() {

        try {
            captureRequestBuilder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        captureRequestBuilder!!.addTarget(mSurface)
      //  captureRequestBuilder!!.addTarget(mImageReader.surface)
        cameraDevice!!.createCaptureSession(
            listOf(mSurface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    if (cameraDevice == null) {
                        return
                    }
                    cameraCaptureSessions = session
                    try {
                        updatePreview()
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }

                override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                    Toast.makeText(applicationContext, "Configuration change", Toast.LENGTH_LONG)
                        .show()
                }
            },
            null
        )
    }

    @Throws(CameraAccessException::class)
    private fun updatePreview() {
        if (cameraDevice == null) {
            return
        }
        captureRequestBuilder!!.set(
            CaptureRequest.CONTROL_AE_MODE,
            CameraMetadata.CONTROL_MODE_AUTO
        )
        cameraCaptureSessions!!.setRepeatingRequest(
            captureRequestBuilder!!.build(),
            null,
            mBackgroundHundler
        )
    }

    private fun openCamera() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPerms()
            return
        }
         manager = getSystemService(CAMERA_SERVICE) as CameraManager
        manager.openCamera(cameraId, stateCallBack, null)

    }

    fun closeCamera() {
        if (cameraDevice != null) {
            cameraDevice!!.close()
            cameraDevice = null
        }
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        setupCameraViewfinder()

    }


    fun setupCameraViewfinder(){
        val manager = this.getSystemService(CAMERA_SERVICE) as CameraManager
        for (mcameraId: String in manager.getCameraIdList()) {var characteristics: CameraCharacteristics = manager.getCameraCharacteristics(cameraId);var facing = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (facing != null && cameraId != mcameraId) {
                continue;
            }
            var map = characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
            );

            var resolution = getResolutionForCameraViewFinder(characteristics)
            val sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            val builder = ViewfinderSurfaceRequest.Builder(resolution)
            builder.setImplementationMode(CameraViewfinder.ImplementationMode.PERFORMANCE)
            if (sensorOrientation != null) {
                builder.setSensorOrientation(sensorOrientation)
            }
            val viewfinderSurfaceRequest = builder.build()
            CoroutineScope(Dispatchers.Main).launch {
                mSurface = textureView.requestSurface(viewfinderSurfaceRequest)
                openCamera()
            }
        }
    }

    private fun getResolutionForCameraViewFinder(characteristics: CameraCharacteristics):Size{
        var map = characteristics.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        );
        val displaySize = Point()
        this.getWindowManager().getDefaultDisplay().getSize(displaySize)
        var surfaceWidth =0
        var surfaceHeight =0
        for(option in map!!.getOutputSizes<SurfaceTexture>(SurfaceTexture::class.java)){
            if(option.width<= displaySize.x ){
                surfaceWidth = Math.max(option.width,surfaceWidth)
                if(surfaceWidth == option.width){
                    surfaceHeight = option.height
                }
            }
        }
        return Size(surfaceWidth,surfaceHeight)
    }


    override fun onPause() {
        try {
            stopBackgroundThread()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        pauseRecorder()
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        stopRecorder()
    }

    @Throws(InterruptedException::class)
    protected fun stopBackgroundThread() {
        mBackgroundThread!!.quitSafely()
        mBackgroundThread!!.join()
        mBackgroundThread = null
        mBackgroundHundler = null
    }

    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("Camera background")
        mBackgroundThread!!.start()
        mBackgroundHundler = Handler(mBackgroundThread!!.looper)
        mImageBackgroundThread = HandlerThread("Image background")
        mImageBackgroundThread!!.start()
        mImageBackgroundHandler = Handler(mImageBackgroundThread!!.looper)
    }

}