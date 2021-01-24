/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.posenet

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.media.Image
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.os.*
import android.speech.tts.TextToSpeech
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import org.tensorflow.lite.examples.posenet.lib.BodyPart
import org.tensorflow.lite.examples.posenet.lib.Person
import org.tensorflow.lite.examples.posenet.lib.Posenet
import org.tensorflow.lite.examples.posenet.lib.Position
import java.io.Console
import java.lang.Exception
import java.sql.DriverManager.println
import java.util.*

class PosenetActivity :
  Fragment(),
  ActivityCompat.OnRequestPermissionsResultCallback {

  /** List of body joints that should be connected.    */
  private val bodyJoints = listOf(
    Pair(BodyPart.LEFT_WRIST, BodyPart.LEFT_ELBOW),
    Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_SHOULDER),
    Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
    Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
    Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
    Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
    Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),
    Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_SHOULDER),
    Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
    Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
    Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
    Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
  )

  /** Threshold for confidence score. */
  private val minConfidence = 0.5

  /** Radius of circle used to draw keypoints.  */
  private val circleRadius = 8.0f

  /** Paint class holds the style and color information to draw geometries,text and bitmaps. */
  private var paint = Paint()

  /** A shape for extracting frame data.   */
  private val PREVIEW_WIDTH = 640
  private val PREVIEW_HEIGHT = 480

  /** An object for the Posenet library.    */
  private lateinit var posenet: Posenet

  /** ID of the current [CameraDevice].   */
  private var cameraId: String? = null

  /** A [SurfaceView] for camera preview.   */
  private var surfaceView: SurfaceView? = null

  /** A [CameraCaptureSession] for camera preview.   */
  private var captureSession: CameraCaptureSession? = null

  /** A reference to the opened [CameraDevice].    */
  private var cameraDevice: CameraDevice? = null

  /** The [android.util.Size] of camera preview.  */
  private var previewSize: Size? = null

  /** The [android.util.Size.getWidth] of camera preview. */
  private var previewWidth = 0

  /** The [android.util.Size.getHeight] of camera preview.  */
  private var previewHeight = 0

  /** A counter to keep count of total frames.  */
  private var frameCounter = 0

  /** An IntArray to save image data in ARGB8888 format  */
  private lateinit var rgbBytes: IntArray

  /** A ByteArray to save image data in YUV format  */
  private var yuvBytes = arrayOfNulls<ByteArray>(3)

  /** An additional thread for running tasks that shouldn't block the UI.   */
  private var backgroundThread: HandlerThread? = null

  /** A [Handler] for running tasks in the background.    */
  private var backgroundHandler: Handler? = null

  /** An [ImageReader] that handles preview frame capture.   */
  private var imageReader: ImageReader? = null

  /** [CaptureRequest.Builder] for the camera preview   */
  private var previewRequestBuilder: CaptureRequest.Builder? = null

  /** [CaptureRequest] generated by [.previewRequestBuilder   */
  private var previewRequest: CaptureRequest? = null

  /** A [Semaphore] to prevent the app from exiting before closing the camera.    */
  private val cameraOpenCloseLock = Semaphore(1)

  /** Whether the current camera device supports Flash or not.    */
  private var flashSupported = false

  /** Orientation of the camera sensor.   */
  private var sensorOrientation: Int? = null

  /** Abstract interface to someone holding a display surface.    */
  private var surfaceHolder: SurfaceHolder? = null

  private var currentPoseHeader: TextView? = null

  private var timeLeftText: TextView? = null

  private var currentPoseIndex: Int = 0



  private var locale: Locale? = Locale("en", "IND")
  private var lang: String? = "Hindi"

  private var hindiPoses = arrayOf("ताड़ासन", "वृक्षासन", "मालासन")

  private var englishPoses = arrayOf("Mountain Pose", "Tree Pose", "Squat")

  private var poses = arrayOf("Mountain Pose", "Tree Pose", "Squat")


  private var mTTS: TextToSpeech? = null

  private var prevFeedback: String? = ""

  private var firstTime: Boolean = true

  private var firstSquat: Boolean = true



  /** [CameraDevice.StateCallback] is called when [CameraDevice] changes its state.   */
  private val stateCallback = object : CameraDevice.StateCallback() {

    override fun onOpened(cameraDevice: CameraDevice) {
      cameraOpenCloseLock.release()
      this@PosenetActivity.cameraDevice = cameraDevice
      createCameraPreviewSession()
    }

    override fun onDisconnected(cameraDevice: CameraDevice) {
      cameraOpenCloseLock.release()
      cameraDevice.close()
      this@PosenetActivity.cameraDevice = null
    }

    override fun onError(cameraDevice: CameraDevice, error: Int) {
      onDisconnected(cameraDevice)
      this@PosenetActivity.activity?.finish()
    }
  }



    /**
   * A [CameraCaptureSession.CaptureCallback] that handles events related to JPEG capture.
   */
  private val captureCallback = object : CameraCaptureSession.CaptureCallback() {
    override fun onCaptureProgressed(
      session: CameraCaptureSession,
      request: CaptureRequest,
      partialResult: CaptureResult
    ) {
    }

    override fun onCaptureCompleted(
      session: CameraCaptureSession,
      request: CaptureRequest,
      result: TotalCaptureResult
    ) {
    }
  }

  /**
   * Shows a [Toast] on the UI thread.
   *
   * @param text The message to show
   */
  private fun showToast(text: String) {
    val activity = activity
    activity?.runOnUiThread { Toast.makeText(activity, text, Toast.LENGTH_SHORT).show() }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View?{
    lang = this.arguments!!.getString("LangToUse")
    return inflater.inflate(R.layout.tfe_pn_activity_posenet, container, false)
  }


  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    surfaceView = view.findViewById(R.id.surfaceView)
    currentPoseHeader = view.findViewById(R.id.PoseHeader)
    timeLeftText = view.findViewById(R.id.Clock)
    surfaceHolder = surfaceView!!.holder

    if (lang == "Hindi"){
      poses = hindiPoses
      locale = Locale("hi", "IND")
    } else {
      poses = englishPoses
      locale = Locale("en", "IND")
    }

    mTTS = TextToSpeech(this.context, TextToSpeech.OnInitListener { status ->
      if (status == TextToSpeech.SUCCESS){
//        val locale = Locale("en", "IND")
        mTTS!!.language = locale
        mTTS!!.setSpeechRate(0.85f)

      }
    })
  }

  override fun onResume() {
    super.onResume()
    startBackgroundThread()
  }

  override fun onStart() {
    super.onStart()
    openCamera()
    posenet = Posenet(this.context!!)
  }

  override fun onPause() {
    closeCamera()
    stopBackgroundThread()
    super.onPause()
  }

  override fun onDestroy() {
    super.onDestroy()
    posenet.close()
  }

  private fun requestCameraPermission() {
    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
      ConfirmationDialog().show(childFragmentManager, FRAGMENT_DIALOG)
    } else {
      requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    if (requestCode == REQUEST_CAMERA_PERMISSION) {
      if (allPermissionsGranted(grantResults)) {
        ErrorDialog.newInstance(getString(R.string.tfe_pn_request_permission))
          .show(childFragmentManager, FRAGMENT_DIALOG)
      }
    } else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
  }

  private fun allPermissionsGranted(grantResults: IntArray) = grantResults.all {
    it == PackageManager.PERMISSION_GRANTED
  }

  /**
   * Sets up member variables related to camera.
   */
  private fun setUpCameraOutputs() {
    val activity = activity
    val manager = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    try {
      for (cameraId in manager.cameraIdList) {
        val characteristics = manager.getCameraCharacteristics(cameraId)

        val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)
        if (cameraDirection != null &&
          cameraDirection == CameraCharacteristics.LENS_FACING_FRONT
        ) {
          continue
        }

        previewSize = Size(PREVIEW_WIDTH, PREVIEW_HEIGHT)

        imageReader = ImageReader.newInstance(
          PREVIEW_WIDTH, PREVIEW_HEIGHT,
          ImageFormat.YUV_420_888, /*maxImages*/ 2
        )

        sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!

        previewHeight = previewSize!!.height
        previewWidth = previewSize!!.width

        // Initialize the storage bitmaps once when the resolution is known.
        rgbBytes = IntArray(previewWidth * previewHeight)

        // Check if the flash is supported.
        flashSupported =
          characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

        this.cameraId = cameraId

        // We've found a viable camera and finished setting up member variables,
        // so we don't need to iterate through other available cameras.
        return
      }
    } catch (e: CameraAccessException) {
      Log.e(TAG, e.toString())
    } catch (e: NullPointerException) {
      // Currently an NPE is thrown when the Camera2API is used but not supported on the
      // device this code runs.
      ErrorDialog.newInstance(getString(R.string.tfe_pn_camera_error))
        .show(childFragmentManager, FRAGMENT_DIALOG)
    }
  }

  /**
   * Opens the camera specified by [PosenetActivity.cameraId].
   */
  private fun openCamera() {
    val permissionCamera = getContext()!!.checkPermission(
      Manifest.permission.CAMERA, Process.myPid(), Process.myUid()
    )
    if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
      requestCameraPermission()
    }
    setUpCameraOutputs()
    val manager = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    try {
      // Wait for camera to open - 2.5 seconds is sufficient
      if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
        throw RuntimeException("Time out waiting to lock camera opening.")
      }
      manager.openCamera(cameraId!!, stateCallback, backgroundHandler)
    } catch (e: CameraAccessException) {
      Log.e(TAG, e.toString())
    } catch (e: InterruptedException) {
      throw RuntimeException("Interrupted while trying to lock camera opening.", e)
    }
  }

  /**
   * Closes the current [CameraDevice].
   */
  private fun closeCamera() {
    if (captureSession == null) {
      return
    }

    try {
      cameraOpenCloseLock.acquire()
      captureSession!!.close()
      captureSession = null
      cameraDevice!!.close()
      cameraDevice = null
      imageReader!!.close()
      imageReader = null
    } catch (e: InterruptedException) {
      throw RuntimeException("Interrupted while trying to lock camera closing.", e)
    } finally {
      cameraOpenCloseLock.release()
    }
  }

  /**
   * Starts a background thread and its [Handler].
   */
  private fun startBackgroundThread() {
    backgroundThread = HandlerThread("imageAvailableListener").also { it.start() }
    backgroundHandler = Handler(backgroundThread!!.looper)
  }

  /**
   * Stops the background thread and its [Handler].
   */
  private fun stopBackgroundThread() {
    backgroundThread?.quitSafely()
    try {
      backgroundThread?.join()
      backgroundThread = null
      backgroundHandler = null
    } catch (e: InterruptedException) {
      Log.e(TAG, e.toString())
    }
  }

  /** Fill the yuvBytes with data from image planes.   */
  private fun fillBytes(planes: Array<Image.Plane>, yuvBytes: Array<ByteArray?>) {
    // Row stride is the total number of bytes occupied in memory by a row of an image.
    // Because of the variable row stride it's not possible to know in
    // advance the actual necessary dimensions of the yuv planes.
    for (i in planes.indices) {
      val buffer = planes[i].buffer
      if (yuvBytes[i] == null) {
        yuvBytes[i] = ByteArray(buffer.capacity())
      }
      buffer.get(yuvBytes[i]!!)
    }
  }

  /** A [OnImageAvailableListener] to receive frames as they are available.  */
  private var imageAvailableListener = object : OnImageAvailableListener {
    override fun onImageAvailable(imageReader: ImageReader) {
      // We need wait until we have some size from onPreviewSizeChosen
      if (previewWidth == 0 || previewHeight == 0) {
        return
      }

      val image = imageReader.acquireLatestImage() ?: return
      fillBytes(image.planes, yuvBytes)

      ImageUtils.convertYUV420ToARGB8888(
        yuvBytes[0]!!,
        yuvBytes[1]!!,
        yuvBytes[2]!!,
        previewWidth,
        previewHeight,
        /*yRowStride=*/ image.planes[0].rowStride,
        /*uvRowStride=*/ image.planes[1].rowStride,
        /*uvPixelStride=*/ image.planes[1].pixelStride,
        rgbBytes
      )

      // Create bitmap from int array
      val imageBitmap = Bitmap.createBitmap(
        rgbBytes, previewWidth, previewHeight,
        Bitmap.Config.ARGB_8888
      )

      // Create rotated version for portrait display
      val rotateMatrix = Matrix()
      rotateMatrix.postRotate(90.0f)

      val rotatedBitmap = Bitmap.createBitmap(
        imageBitmap, 0, 0, previewWidth, previewHeight,
        rotateMatrix, true
      )
      image.close()

      processImage(rotatedBitmap)
    }
  }

  /** Crop Bitmap to maintain aspect ratio of model input.   */
  private fun cropBitmap(bitmap: Bitmap): Bitmap {
    val bitmapRatio = bitmap.height.toFloat() / bitmap.width
    val modelInputRatio = MODEL_HEIGHT.toFloat() / MODEL_WIDTH
    var croppedBitmap = bitmap

    // Acceptable difference between the modelInputRatio and bitmapRatio to skip cropping.
    val maxDifference = 1e-5

    // Checks if the bitmap has similar aspect ratio as the required model input.
    when {
      abs(modelInputRatio - bitmapRatio) < maxDifference -> return croppedBitmap
      modelInputRatio < bitmapRatio -> {
        // New image is taller so we are height constrained.
        val cropHeight = bitmap.height - (bitmap.width.toFloat() / modelInputRatio)
        croppedBitmap = Bitmap.createBitmap(
          bitmap,
          0,
          (cropHeight / 2).toInt(),
          bitmap.width,
          (bitmap.height - cropHeight).toInt()
        )
      }
      else -> {
        val cropWidth = bitmap.width - (bitmap.height.toFloat() * modelInputRatio)
        croppedBitmap = Bitmap.createBitmap(
          bitmap,
          (cropWidth / 2).toInt(),
          0,
          (bitmap.width - cropWidth).toInt(),
          bitmap.height
        )
      }
    }
    return croppedBitmap
  }

  /** Set the paint color and size.    */
  private fun setPaint() {
    paint.color = Color.RED
    paint.textSize = 80.0f
    paint.strokeWidth = 8.0f
  }

  /** Draw bitmap on Canvas.   */
  private fun draw(canvas: Canvas, person: Person, bitmap: Bitmap) {

    if (firstTime){
      firstTime = false
      if (lang == "Hindi"){
        mTTS!!.speak("आइये " + poses[currentPoseIndex] + " के साथ शुरुआत करते हैं", TextToSpeech.QUEUE_FLUSH, null) //
      } else{
        mTTS!!.speak("Let's start off with the " + poses[currentPoseIndex], TextToSpeech.QUEUE_FLUSH, null)
      }

      Thread.sleep(2800)
    }

    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    // Draw `bitmap` and `person` in square canvas.
    val screenWidth: Int
    val screenHeight: Int
    val left: Int
    val right: Int
    val top: Int
    val bottom: Int
    if (canvas.height > canvas.width) {
      screenWidth = canvas.width
      screenHeight = canvas.width
      left = 0
      top = (canvas.height - canvas.width) / 2
    } else {
      screenWidth = canvas.height
      screenHeight = canvas.height
      left = (canvas.width - canvas.height) / 2
      top = 0
    }
    right = left + screenWidth
    bottom = top + screenHeight

    setPaint()
    canvas.drawBitmap(
      bitmap,
      Rect(0, 0, bitmap.width, bitmap.height),
      Rect(left, top, right, bottom),
      paint
    )

    val widthRatio = screenWidth.toFloat() / MODEL_WIDTH
    val heightRatio = screenHeight.toFloat() / MODEL_HEIGHT

    // Draw key points over the image.
    for (keyPoint in person.keyPoints) {
      if (keyPoint.score > minConfidence) {
        val position = keyPoint.position
        val adjustedX: Float = position.x.toFloat() * widthRatio + left
        val adjustedY: Float = position.y.toFloat() * heightRatio + top
        canvas.drawCircle(adjustedX, adjustedY, circleRadius, paint)
      }
    }

    for (line in bodyJoints) {
      if (
        (person.keyPoints[line.first.ordinal].score > minConfidence) and
        (person.keyPoints[line.second.ordinal].score > minConfidence)
      ) {
        canvas.drawLine(
          person.keyPoints[line.first.ordinal].position.x.toFloat() * widthRatio + left,
          person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio + top,
          person.keyPoints[line.second.ordinal].position.x.toFloat() * widthRatio + left,
          person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio + top,
          paint
        )
      }
    }

    var (true_or_not, feedback, c) = Triple(false, "", "1.0")
    if (currentPoseIndex == 0 && !firstSquat){
      var (temp_true_or_not, temp_feedback, temp_c) = mountain_pose(person)
      true_or_not = temp_true_or_not
      feedback = temp_feedback
      c = temp_c
      if (true_or_not){
        firstSquat = true
      }

    } else if (currentPoseIndex == 1 && !firstSquat){
      var (temp_true_or_not, temp_feedback, temp_c) = tree_pose(person)   //mountain_pose(person)  //tree_pose(person)
      true_or_not = temp_true_or_not
      feedback = temp_feedback
      c = temp_c
      if (true_or_not){
        firstSquat = true
      }
    } else if (currentPoseIndex == 2 && !firstSquat){

      var (temp_true_or_not, temp_feedback, temp_c) = squat(person)   //Triple(true, "Good Job!", "1.0")
      true_or_not = temp_true_or_not
      feedback = temp_feedback
      c = temp_c
      if (true_or_not){
        firstSquat = true
      }

    } else if (currentPoseIndex == 2){
      firstSquat = false
    }
    else if (currentPoseIndex == 1){
//      var (temp_true_or_not, temp_feedback, temp_c) = squat(person)   //Triple(true, "Good Job!", "1.0")
//      true_or_not = temp_true_or_not
//      feedback = temp_feedback
//      c = temp_c
      firstSquat = false
    }
    else if (currentPoseIndex == 0){
//      var (temp_true_or_not, temp_feedback, temp_c) = squat(person)   //Triple(true, "Good Job!", "1.0")
//      true_or_not = temp_true_or_not
//      feedback = temp_feedback
//      c = temp_c
      firstSquat = false
    }



    try{
      if (lang == "Hindi"){
        activity?.runOnUiThread(java.lang.Runnable { this.currentPoseHeader!!.text = "अब " + poses[currentPoseIndex] + " करते हैं"})
      } else {
        activity?.runOnUiThread(java.lang.Runnable { this.currentPoseHeader!!.text = "Let's do " + poses[currentPoseIndex]})
      }

    }
   catch (e: Exception){
     e.printStackTrace()
   }

    // This is the code for writing text on the screen.
    // Just need to create a method here for calculations.

    paint.color = Color.WHITE
    paint.textSize = 90.0f
    paint.strokeWidth = 8.0f

    canvas.drawText(
      feedback,
      (40.0f * widthRatio),
      (30.0f * heightRatio + bottom),
      paint
    )

//    canvas.drawText(
//      true_or_not.toString() + "   " + c,
//      (15.0f * widthRatio),
//      (50.0f * heightRatio + bottom),
//      paint
//    )
//    canvas.drawText(
//      "Time: %.2f ms".format(posenet.lastInferenceTimeNanos * 1.0f / 1_000_000),
//      (15.0f * widthRatio),
//      (70.0f * heightRatio + bottom),
//      paint
//    )

    // Draw!
    surfaceHolder!!.unlockCanvasAndPost(canvas)

    if (feedback != prevFeedback){
      mTTS!!.speak(feedback, TextToSpeech.QUEUE_FLUSH, null)
      Thread.sleep(1000)
      prevFeedback = feedback
    }

    if (true_or_not){
      currentPoseIndex++

      for (i in 1..5) {
        activity?.runOnUiThread(java.lang.Runnable { this.timeLeftText!!.text = (5 - i).toString() + " seconds"})
//        speak((5 - i).toString())
        Thread.sleep(1000)
      }
      if (currentPoseIndex == 3){
        currentPoseIndex = 0
      }

      var firstString = "Let's move on to the "

      if (currentPoseIndex == 0 && lang == "Hindi") {
        firstString = "आइये अब दोबारा शुरू करते हैं"
        mTTS!!.speak(firstString, TextToSpeech.QUEUE_FLUSH, null)
      }
      else if (currentPoseIndex == 0) {
        firstString = "Let's repeat the set with the "
        mTTS!!.speak(firstString + poses[currentPoseIndex], TextToSpeech.QUEUE_FLUSH, null)

      } else if (lang == "Hindi"){
        firstString = "अब "
        mTTS!!.speak(firstString + poses[currentPoseIndex] + " करते हैं", TextToSpeech.QUEUE_FLUSH, null)

      } else{
        mTTS!!.speak(firstString + poses[currentPoseIndex], TextToSpeech.QUEUE_FLUSH, null)
      }

//      mTTS!!.speak(firstString + poses[currentPoseIndex], TextToSpeech.QUEUE_FLUSH, null)
      Thread.sleep(3500)

      if (currentPoseIndex == 1){
        Thread.sleep(1500)
      }
    }
  }


  private fun square(value: Int): Double{
    return Math.pow(value.toDouble(), 2.0)
  }
  private fun square(value: Double): Double{
    return Math.pow(value, 2.0)
  }


  private fun euclidian(p1: Position, p2: Position): Double {
    var distance = square((p1.x - p2.x)) + square((p1.y - p2.y))
    distance = Math.sqrt(distance)
    return distance
  }

  private fun angle_calc(p0: Position, p1: Position, p2: Position): Double {
//    p1 is center point
    try{
      var a = euclidian(p0, p1)
//      square((p1.x - p0.x)) + square((p1.y - p0.y))
      var b = euclidian(p1, p2)
//        square((p1.x - p2.y)) + square((p1.y - p2.y))
      var c = euclidian(p2, p0)
//        square((p2.x - p0.x)) + square((p2.y - p0.y))
      var angle = Math.acos((square(a) + square(b) - square(c)) / (2 * a * b))

      return angle
    }
    catch (e: Exception) {
      return 0.0
    }

  }

  private fun check_range(value: Double, min: Double, max: Double): Boolean{
    if (value < max){
      if (value > min) {
        return true
      }
    }
    return false
  }

  private fun mountain_pose( person: Person): Triple<Boolean, String, String>{
  /*
  b and c are angle between two shoulders  and wrist
  */

    var b = angle_calc(person.keyPoints[6].position, person.keyPoints[5].position, person.keyPoints[7].position)
    var c = angle_calc(person.keyPoints[5].position, person.keyPoints[6].position, person.keyPoints[8].position)

    var feedbacks = arrayOf("Perfect Now hold for 5 seconds", "Raise your hands higher", "Raise your left hand higher", "Raise your right hand higher", "Try it Again.")

    if (lang == "Hindi"){
      feedbacks = arrayOf("५ सेकंड रुकें", "अपने हाथ ऊपर करें", "अपना उल्टा हाथ ऊपर करें", "अपना सीधा हाथ ऊपर करें", "दुबारा करें")
    }

    // If everything is correct
    if (check_range(b, 1.9, 2.5) && check_range(c, 1.9, 2.5) && (person.keyPoints[9].position.y < 80)) // && check_range(d, 100.0, 145.0) && check_range(e, 100.0, 145.0)
//      check_range(a, 20.0, 160.0) &&
    {
      return Triple(true, feedbacks[0], b.toString() + "    " + c.toString())
    }
    else if (check_range(b, 2.5, 5.0) && check_range(c, 2.5, 5.0)){
      return Triple(false, feedbacks[1], b.toString() + "    " + c.toString())
    }
    else if (check_range(b, 2.5, 5.0)){
      return Triple(false, feedbacks[2], b.toString() + "    " + c.toString())
    }
    else if (check_range(c, 2.5, 5.0)){
      return Triple(false, feedbacks[3], b.toString() + "    " + c.toString())
    }

    return Triple(false, feedbacks[4], b.toString() + "    " + c.toString())
  }

  private fun tree_pose(person: Person): Triple<Boolean, String, String>{
    /*
        a is the angle between the right hip, knee and ankle.
        b is the distance between wrists.

     */
    var a = angle_calc(person.keyPoints[12].position, person.keyPoints[14].position, person.keyPoints[16].position)
    var b = euclidian(person.keyPoints[9].position, person.keyPoints[10].position)

    var feedbacks = arrayOf("Good job Now hold for 5 seconds", "Raise your leg higher", "Bring your hands closer")


    if (lang == "Hindi"){
      feedbacks = arrayOf("बहुत बढ़िए। अब ५ सेकंड रुकें", "अपना पैर ऊपर करें", "हाथ करीब लाएं")
    }

    if (check_range(a, 0.0, 2.6) && check_range(b, 0.0, 30.0)){
      return Triple(true, feedbacks[0], a.toString() + "    " + b.toString())
    } else if (check_range(a, 2.6, 5.0)){
      return Triple(false, feedbacks[1], a.toString() + "    " + b.toString())
    } else if (check_range(b, 30.0, 1000.0)){
      return Triple(false, feedbacks[2], a.toString() + "    " + b.toString())
    }

    return Triple(false, feedbacks[1], b.toString())

  }

  private fun squat(person: Person): Triple<Boolean, String, String> {
    /*
    a is angle between right shoulder, right hip and right knee
    b is angle between left shoulder, left hip and left knee
    c is horizontal distance between right shoulder and right hip
    d is horizontal distance between left shoulder and left knee.
     */

    var a = angle_calc(
      person.keyPoints[6].position,
      person.keyPoints[12].position,
      person.keyPoints[14].position
    )
    var b = angle_calc(
      person.keyPoints[5].position,
      person.keyPoints[11].position,
      person.keyPoints[13].position
    )

    var c = (person.keyPoints[6].position.x - person.keyPoints[12].position.x).toDouble()
    var d = (person.keyPoints[5].position.x - person.keyPoints[11].position.x).toDouble()

    c = Math.abs(c)
    d = Math.abs(d)

    var feedbacks = arrayOf("Perfect Keep it up for 5 seconds", "Bend your knees more", "Straighten your back")

    if (lang == "Hindi"){
      feedbacks = arrayOf("बहुत बढ़िए। अब ५ सेकंड रुकें", "अपना घुटना और मोड़ें", "पीठ सीधी करें")
    }

    if ((check_range(a, 0.0, 2.1) || check_range(b, 0.0, 2.1)) && (check_range(c, 0.0, 25.0) || check_range(d, 0.0, 25.0))
    ) {
      return Triple(true, feedbacks[0], c.toString() + "  " + a.toString())
    } else if (check_range(a, 2.1, 5.0) || check_range(b, 2.1, 5.0)) {
      return Triple(false, feedbacks[1], c.toString() + " " + a.toString())
    } else if (check_range(c, 25.0, 100.0) || check_range(d, 25.0, 100.0)) {
      return Triple(false, feedbacks[2], c.toString() + "  " + a.toString())
    }

    return Triple(false, feedbacks[1], (c.toString() + "   " + d.toString()))
  }

  /** Process image using Posenet library.   */
  private fun processImage(bitmap: Bitmap) {
    // Crop bitmap.
    val croppedBitmap = cropBitmap(bitmap)

    // Created scaled version of bitmap for model input.
    val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, MODEL_WIDTH, MODEL_HEIGHT, true)

    // Perform inference.
    val person = posenet.estimateSinglePose(scaledBitmap)
    val canvas: Canvas = surfaceHolder!!.lockCanvas()
    draw(canvas, person, scaledBitmap)
  }

  /**
   * Creates a new [CameraCaptureSession] for camera preview.
   */
  private fun createCameraPreviewSession() {
    try {
      // We capture images from preview in YUV format.
      imageReader = ImageReader.newInstance(
        previewSize!!.width, previewSize!!.height, ImageFormat.YUV_420_888, 2
      )
      imageReader!!.setOnImageAvailableListener(imageAvailableListener, backgroundHandler)

      // This is the surface we need to record images for processing.
      val recordingSurface = imageReader!!.surface

      // We set up a CaptureRequest.Builder with the output Surface.
      previewRequestBuilder = cameraDevice!!.createCaptureRequest(
        CameraDevice.TEMPLATE_PREVIEW
      )
      previewRequestBuilder!!.addTarget(recordingSurface)

      // Here, we create a CameraCaptureSession for camera preview.
      cameraDevice!!.createCaptureSession(
        listOf(recordingSurface),
        object : CameraCaptureSession.StateCallback() {
          override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
            // The camera is already closed
            if (cameraDevice == null) return

            // When the session is ready, we start displaying the preview.
            captureSession = cameraCaptureSession
            try {
              // Auto focus should be continuous for camera preview.
              previewRequestBuilder!!.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
              )
              // Flash is automatically enabled when necessary.
              setAutoFlash(previewRequestBuilder!!)

              // Finally, we start displaying the camera preview.
              previewRequest = previewRequestBuilder!!.build()
              captureSession!!.setRepeatingRequest(
                previewRequest!!,
                captureCallback, backgroundHandler
              )
            } catch (e: CameraAccessException) {
              Log.e(TAG, e.toString())
            }
          }

          override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
            showToast("Failed")
          }
        },
        null
      )
    } catch (e: CameraAccessException) {
      Log.e(TAG, e.toString())
    }
  }

  private fun setAutoFlash(requestBuilder: CaptureRequest.Builder) {
    if (flashSupported) {
      requestBuilder.set(
        CaptureRequest.CONTROL_AE_MODE,
        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
      )
    }
  }

  /**
   * Shows an error message dialog.
   */
  class ErrorDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
      AlertDialog.Builder(activity)
        .setMessage(arguments!!.getString(ARG_MESSAGE))
        .setPositiveButton(android.R.string.ok) { _, _ -> activity!!.finish() }
        .create()

    companion object {

      @JvmStatic
      private val ARG_MESSAGE = "message"

      @JvmStatic
      fun newInstance(message: String): ErrorDialog = ErrorDialog().apply {
        arguments = Bundle().apply { putString(ARG_MESSAGE, message) }
      }
    }
  }

  companion object {
    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    private val ORIENTATIONS = SparseIntArray()
    private val FRAGMENT_DIALOG = "dialog"

    init {
      ORIENTATIONS.append(Surface.ROTATION_0, 90)
      ORIENTATIONS.append(Surface.ROTATION_90, 0)
      ORIENTATIONS.append(Surface.ROTATION_180, 270)
      ORIENTATIONS.append(Surface.ROTATION_270, 180)
    }

    /**
     * Tag for the [Log].
     */
    private const val TAG = "PosenetActivity"
  }
}
