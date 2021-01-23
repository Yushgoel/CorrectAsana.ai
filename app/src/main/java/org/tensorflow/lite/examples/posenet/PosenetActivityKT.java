package org.tensorflow.lite.examples.posenet;
//
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.DialogInterface.OnClickListener;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Matrix;
//import android.graphics.Paint;
//import android.graphics.Rect;
//import android.graphics.Bitmap.Config;
//import android.graphics.PorterDuff.Mode;
//import android.hardware.camera2.CameraAccessException;
//import android.hardware.camera2.CameraCaptureSession;
//import android.hardware.camera2.CameraCharacteristics;
//import android.hardware.camera2.CameraDevice;
//import android.hardware.camera2.CameraManager;
//import android.hardware.camera2.CaptureRequest;
//import android.hardware.camera2.CaptureResult;
//import android.hardware.camera2.TotalCaptureResult;
//import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
//import android.hardware.camera2.CameraDevice.StateCallback;
//import android.hardware.camera2.CaptureRequest.Builder;
//import android.media.Image;
//import android.media.ImageReader;
//import android.media.Image.Plane;
//import android.media.ImageReader.OnImageAvailableListener;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.os.Process;
//import android.util.Log;
//import android.util.Size;
//import android.util.SparseIntArray;
//import android.view.LayoutInflater;
//import android.view.Surface;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
//import androidx.fragment.app.DialogFragment;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentActivity;
//import java.nio.ByteBuffer;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.concurrent.Semaphore;
//import java.util.concurrent.TimeUnit;
//import kotlin.Metadata;
//import kotlin.Pair;
//import kotlin.TypeCastException;
//import kotlin.collections.CollectionsKt;
//import kotlin.jvm.JvmStatic;
//import kotlin.jvm.internal.DefaultConstructorMarker;
//import kotlin.jvm.internal.Intrinsics;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//import org.tensorflow.lite.examples.posenet.lib.BodyPart;
//import org.tensorflow.lite.examples.posenet.lib.Device;
//import org.tensorflow.lite.examples.posenet.lib.KeyPoint;
//import org.tensorflow.lite.examples.posenet.lib.Person;
//import org.tensorflow.lite.examples.posenet.lib.Posenet;
//import org.tensorflow.lite.examples.posenet.lib.Position;
//
//@Metadata(
//        mv = {1, 1, 16},
//        bv = {1, 0, 3},
//        k = 1,
//        d1 = {"\u0000÷\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0006\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0015\n\u0002\b\u0003\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0011\n\u0002\u0010\u0012\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0018*\u0003\u0016 7\u0018\u0000 o2\u00020\u00012\u00020\u0002:\u0002opB\u0005¢\u0006\u0002\u0010\u0003J\u0010\u0010A\u001a\u00020\u001d2\u0006\u0010B\u001a\u000203H\u0002J\b\u0010C\u001a\u00020DH\u0002J\b\u0010E\u001a\u00020DH\u0002J\u0010\u0010F\u001a\u00020G2\u0006\u0010H\u001a\u00020GH\u0002J \u0010I\u001a\u00020D2\u0006\u0010J\u001a\u00020K2\u0006\u0010L\u001a\u00020M2\u0006\u0010H\u001a\u00020GH\u0002J+\u0010N\u001a\u00020D2\f\u0010O\u001a\b\u0012\u0004\u0012\u00020P0>2\u000e\u0010=\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010?0>H\u0002¢\u0006\u0002\u0010QJ&\u0010R\u001a\u0004\u0018\u00010S2\u0006\u0010T\u001a\u00020U2\b\u0010V\u001a\u0004\u0018\u00010W2\b\u0010X\u001a\u0004\u0018\u00010YH\u0016J\b\u0010Z\u001a\u00020DH\u0016J\b\u0010[\u001a\u00020DH\u0016J+\u0010\\\u001a\u00020D2\u0006\u0010]\u001a\u00020\u00052\f\u0010^\u001a\b\u0012\u0004\u0012\u00020\u00120>2\u0006\u0010B\u001a\u000203H\u0016¢\u0006\u0002\u0010_J\b\u0010`\u001a\u00020DH\u0016J\b\u0010a\u001a\u00020DH\u0016J\u001a\u0010b\u001a\u00020D2\u0006\u0010c\u001a\u00020S2\b\u0010X\u001a\u0004\u0018\u00010YH\u0016J\b\u0010d\u001a\u00020DH\u0002J\u0010\u0010e\u001a\u00020D2\u0006\u0010H\u001a\u00020GH\u0002J\b\u0010f\u001a\u00020DH\u0002J\u0010\u0010g\u001a\u00020D2\u0006\u0010h\u001a\u00020.H\u0002J\b\u0010i\u001a\u00020DH\u0002J\b\u0010j\u001a\u00020DH\u0002J\u0010\u0010k\u001a\u00020D2\u0006\u0010l\u001a\u00020\u0012H\u0002J\b\u0010m\u001a\u00020DH\u0002J\b\u0010n\u001a\u00020DH\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082D¢\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e¢\u0006\u0002\n\u0000R \u0010\u000b\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u000e0\r0\fX\u0082\u0004¢\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0012X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u0004¢\u0006\u0002\n\u0000R\u0010\u0010\u0015\u001a\u00020\u0016X\u0082\u0004¢\u0006\u0004\n\u0002\u0010\u0017R\u0010\u0010\u0018\u001a\u0004\u0018\u00010\u0019X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u001bX\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\u001dX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u001e\u001a\u00020\u0005X\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010\u001f\u001a\u00020 X\u0082\u000e¢\u0006\u0004\n\u0002\u0010!R\u0010\u0010\"\u001a\u0004\u0018\u00010#X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010$\u001a\u00020%X\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010&\u001a\u00020'X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010(\u001a\u00020)X\u0082.¢\u0006\u0002\n\u0000R\u000e\u0010*\u001a\u00020\u0005X\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010+\u001a\u0004\u0018\u00010,X\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010-\u001a\u0004\u0018\u00010.X\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010/\u001a\u0004\u0018\u000100X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u00101\u001a\u00020\u0005X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u00102\u001a\u000203X\u0082.¢\u0006\u0002\n\u0000R\u0012\u00104\u001a\u0004\u0018\u00010\u0005X\u0082\u000e¢\u0006\u0004\n\u0002\u00105R\u0010\u00106\u001a\u000207X\u0082\u0004¢\u0006\u0004\n\u0002\u00108R\u0010\u00109\u001a\u0004\u0018\u00010:X\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010;\u001a\u0004\u0018\u00010<X\u0082\u000e¢\u0006\u0002\n\u0000R\u0018\u0010=\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010?0>X\u0082\u000e¢\u0006\u0004\n\u0002\u0010@¨\u0006q"},
//        d2 = {"Lorg/tensorflow/lite/examples/posenet/PosenetActivity;", "Landroidx/fragment/app/Fragment;", "Landroidx/core/app/ActivityCompat$OnRequestPermissionsResultCallback;", "()V", "PREVIEW_HEIGHT", "", "PREVIEW_WIDTH", "backgroundHandler", "Landroid/os/Handler;", "backgroundThread", "Landroid/os/HandlerThread;", "bodyJoints", "", "Lkotlin/Pair;", "Lorg/tensorflow/lite/examples/posenet/lib/BodyPart;", "cameraDevice", "Landroid/hardware/camera2/CameraDevice;", "cameraId", "", "cameraOpenCloseLock", "Ljava/util/concurrent/Semaphore;", "captureCallback", "org/tensorflow/lite/examples/posenet/PosenetActivity$captureCallback$1", "Lorg/tensorflow/lite/examples/posenet/PosenetActivity$captureCallback$1;", "captureSession", "Landroid/hardware/camera2/CameraCaptureSession;", "circleRadius", "", "flashSupported", "", "frameCounter", "imageAvailableListener", "org/tensorflow/lite/examples/posenet/PosenetActivity$imageAvailableListener$1", "Lorg/tensorflow/lite/examples/posenet/PosenetActivity$imageAvailableListener$1;", "imageReader", "Landroid/media/ImageReader;", "minConfidence", "", "paint", "Landroid/graphics/Paint;", "posenet", "Lorg/tensorflow/lite/examples/posenet/lib/Posenet;", "previewHeight", "previewRequest", "Landroid/hardware/camera2/CaptureRequest;", "previewRequestBuilder", "Landroid/hardware/camera2/CaptureRequest$Builder;", "previewSize", "Landroid/util/Size;", "previewWidth", "rgbBytes", "", "sensorOrientation", "Ljava/lang/Integer;", "stateCallback", "org/tensorflow/lite/examples/posenet/PosenetActivity$stateCallback$1", "Lorg/tensorflow/lite/examples/posenet/PosenetActivity$stateCallback$1;", "surfaceHolder", "Landroid/view/SurfaceHolder;", "surfaceView", "Landroid/view/SurfaceView;", "yuvBytes", "", "", "[[B", "allPermissionsGranted", "grantResults", "closeCamera", "", "createCameraPreviewSession", "cropBitmap", "Landroid/graphics/Bitmap;", "bitmap", "draw", "canvas", "Landroid/graphics/Canvas;", "person", "Lorg/tensorflow/lite/examples/posenet/lib/Person;", "fillBytes", "planes", "Landroid/media/Image$Plane;", "([Landroid/media/Image$Plane;[[B)V", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onPause", "onRequestPermissionsResult", "requestCode", "permissions", "(I[Ljava/lang/String;[I)V", "onResume", "onStart", "onViewCreated", "view", "openCamera", "processImage", "requestCameraPermission", "setAutoFlash", "requestBuilder", "setPaint", "setUpCameraOutputs", "showToast", "text", "startBackgroundThread", "stopBackgroundThread", "Companion", "ErrorDialog", "app"}
//)
//public final class PosenetActivityKT extends Fragment implements OnRequestPermissionsResultCallback {
//    private final List bodyJoints;
//    private final double minConfidence;
//    private final float circleRadius;
//    private Paint paint;
//    private final int PREVIEW_WIDTH;
//    private final int PREVIEW_HEIGHT;
//    private Posenet posenet;
//    private String cameraId;
//    private SurfaceView surfaceView;
//    private CameraCaptureSession captureSession;
//    private CameraDevice cameraDevice;
//    private Size previewSize;
//    private int previewWidth;
//    private int previewHeight;
//    private int frameCounter;
//    private int[] rgbBytes;
//    private byte[][] yuvBytes;
//    private HandlerThread backgroundThread;
//    private Handler backgroundHandler;
//    private ImageReader imageReader;
//    private Builder previewRequestBuilder;
//    private CaptureRequest previewRequest;
//    private final Semaphore cameraOpenCloseLock;
//    private boolean flashSupported;
//    private Integer sensorOrientation;
//    private SurfaceHolder surfaceHolder;
//    private final <undefinedtype> stateCallback;
//
//    /** [CameraDevice.StateCallback] is called when [CameraDevice] changes its state.   */
//    private StateCallback stateCallback = CameraDevice.StateCallback() {
//
//        override fun onOpened(cameraDevice: CameraDevice) {
//            cameraOpenCloseLock.release()
//            this@PosenetActivity.cameraDevice = cameraDevice
//            createCameraPreviewSession()
//        }
//
//        override fun onDisconnected(cameraDevice: CameraDevice) {
//            cameraOpenCloseLock.release()
//            cameraDevice.close()
//            this@PosenetActivity.cameraDevice = null
//        }
//
//        override fun onError(cameraDevice: CameraDevice, error: Int) {
//            onDisconnected(cameraDevice)
//            this@PosenetActivity.activity?.finish()
//        }
//
//    private final <undefinedtype> captureCallback;
//    private <undefinedtype> imageAvailableListener;
//    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
//    private static final String FRAGMENT_DIALOG = "dialog";
//    private static final String TAG = "PosenetActivity";
//    public static final PosenetActivityKT.Companion Companion = new PosenetActivityKT.Companion((DefaultConstructorMarker)null);
//    private HashMap _$_findViewCache;
//
//    private final void showToast(final String text) {
//        final FragmentActivity activity = this.getActivity();
//        if (activity != null) {
//            activity.runOnUiThread((Runnable)(new Runnable() {
//                public final void run() {
//                    Toast.makeText((Context)activity, (CharSequence)text, 0).show();
//                }
//            }));
//        }
//
//    }
//
//    @Nullable
//    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        Intrinsics.checkParameterIsNotNull(inflater, "inflater");
//        return inflater.inflate(1300018, container, false);
//    }
//
//    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
//        Intrinsics.checkParameterIsNotNull(view, "view");
//        this.surfaceView = (SurfaceView)view.findViewById(1000083);
//        SurfaceView var10001 = this.surfaceView;
//        if (var10001 == null) {
//            Intrinsics.throwNpe();
//        }
//
//        this.surfaceHolder = var10001.getHolder();
//    }
//
//    public void onResume() {
//        super.onResume();
//        this.startBackgroundThread();
//    }
//
//    public void onStart() {
//        super.onStart();
//        this.openCamera();
//        Posenet var10001 = new Posenet;
//        Context var10003 = this.getContext();
//        if (var10003 == null) {
//            Intrinsics.throwNpe();
//        }
//
//        Intrinsics.checkExpressionValueIsNotNull(var10003, "this.context!!");
//     //   var10001.<init>(var10003, (String)null, (Device)null, 6, (DefaultConstructorMarker)null);
//        this.posenet = var10001;
//    }
//
//    public void onPause() {
//        this.closeCamera();
//        this.stopBackgroundThread();
//        super.onPause();
//    }
//
//    public void onDestroy() {
//        super.onDestroy();
//        Posenet var10000 = this.posenet;
//        if (var10000 == null) {
//            Intrinsics.throwUninitializedPropertyAccessException("posenet");
//        }
//
//        var10000.close();
//    }
//
//    private final void requestCameraPermission() {
//        if (this.shouldShowRequestPermissionRationale("android.permission.CAMERA")) {
//            (new ConfirmationDialog()).show(this.getChildFragmentManager(), FRAGMENT_DIALOG);
//        } else {
//            this.requestPermissions(new String[]{"android.permission.CAMERA"}, 1);
//        }
//
//    }
//
//    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
//        Intrinsics.checkParameterIsNotNull(permissions, "permissions");
//        Intrinsics.checkParameterIsNotNull(grantResults, "grantResults");
//        if (requestCode == 1) {
//            if (this.allPermissionsGranted(grantResults)) {
//                PosenetActivityKT.ErrorDialog.Companion var10000 = PosenetActivityKT.ErrorDialog.Companion;
//                String var10001 = this.getString(1900030);
//                Intrinsics.checkExpressionValueIsNotNull(var10001, "getString(R.string.tfe_pn_request_permission)");
//                var10000.newInstance(var10001).show(this.getChildFragmentManager(), FRAGMENT_DIALOG);
//            }
//        } else {
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
//
//    }
//
//    private final boolean allPermissionsGranted(int[] grantResults) {
//        int $i$f$all = false;
//        int[] var4 = grantResults;
//        int var5 = grantResults.length;
//        int var6 = 0;
//
//        boolean var10000;
//        while(true) {
//            if (var6 >= var5) {
//                var10000 = true;
//                break;
//            }
//
//            int element$iv = var4[var6];
//            int var9 = false;
//            if (element$iv != 0) {
//                var10000 = false;
//                break;
//            }
//
//            ++var6;
//        }
//
//        return var10000;
//    }
//
//    private final void setUpCameraOutputs() {
//        FragmentActivity activity = this.getActivity();
//        if (activity == null) {
//            Intrinsics.throwNpe();
//        }
//
//        Object var10000 = activity.getSystemService("camera");
//        if (var10000 == null) {
//            throw new TypeCastException("null cannot be cast to non-null type android.hardware.camera2.CameraManager");
//        } else {
//            CameraManager manager = (CameraManager)var10000;
//
//            try {
//                String[] var5 = manager.getCameraIdList();
//                int var6 = var5.length;
//                int var4 = 0;
//
//                String cameraId;
//                CameraCharacteristics characteristics;
//                while(true) {
//                    if (var4 >= var6) {
//                        return;
//                    }
//
//                    cameraId = var5[var4];
//                    CameraCharacteristics var13 = manager.getCameraCharacteristics(cameraId);
//                    Intrinsics.checkExpressionValueIsNotNull(var13, "manager.getCameraCharacteristics(cameraId)");
//                    characteristics = var13;
//                    Integer cameraDirection = (Integer)characteristics.get(CameraCharacteristics.LENS_FACING);
//                    if (cameraDirection == null) {
//                        break;
//                    }
//
//                    boolean var9 = false;
//                    if (cameraDirection != 0) {
//                        break;
//                    }
//
//                    ++var4;
//                }
//
//                this.previewSize = new Size(this.PREVIEW_WIDTH, this.PREVIEW_HEIGHT);
//                this.imageReader = ImageReader.newInstance(this.PREVIEW_WIDTH, this.PREVIEW_HEIGHT, 35, 2);
//                Object var14 = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
//                if (var14 == null) {
//                    Intrinsics.throwNpe();
//                }
//
//                this.sensorOrientation = (Integer)var14;
//                Size var15 = this.previewSize;
//                if (var15 == null) {
//                    Intrinsics.throwNpe();
//                }
//
//                this.previewHeight = var15.getHeight();
//                var15 = this.previewSize;
//                if (var15 == null) {
//                    Intrinsics.throwNpe();
//                }
//
//                this.previewWidth = var15.getWidth();
//                this.rgbBytes = new int[this.previewWidth * this.previewHeight];
//                this.flashSupported = Intrinsics.areEqual((Boolean)characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE), true);
//                this.cameraId = cameraId;
//                return;
//            } catch (CameraAccessException var10) {
//                Log.e("PosenetActivity", var10.toString());
//            } catch (NullPointerException var11) {
//                PosenetActivityKT.ErrorDialog.Companion var12 = PosenetActivityKT.ErrorDialog.Companion;
//                String var10001 = this.getString(1900023);
//                Intrinsics.checkExpressionValueIsNotNull(var10001, "getString(R.string.tfe_pn_camera_error)");
//                var12.newInstance(var10001).show(this.getChildFragmentManager(), FRAGMENT_DIALOG);
//            }
//
//        }
//    }
//
//    private final void openCamera() {
//        Context var10000 = this.getContext();
//        if (var10000 == null) {
//            Intrinsics.throwNpe();
//        }
//
//        int permissionCamera = var10000.checkPermission("android.permission.CAMERA", Process.myPid(), Process.myUid());
//        if (permissionCamera != 0) {
//            this.requestCameraPermission();
//        }
//
//        this.setUpCameraOutputs();
//        FragmentActivity var6 = this.getActivity();
//        if (var6 == null) {
//            Intrinsics.throwNpe();
//        }
//
//        Object var7 = var6.getSystemService("camera");
//        if (var7 == null) {
//            throw new TypeCastException("null cannot be cast to non-null type android.hardware.camera2.CameraManager");
//        } else {
//            CameraManager manager = (CameraManager)var7;
//
//            try {
//                if (!this.cameraOpenCloseLock.tryAcquire(2500L, TimeUnit.MILLISECONDS)) {
//                    throw (Throwable)(new RuntimeException("Time out waiting to lock camera opening."));
//                }
//
//                String var10001 = this.cameraId;
//                if (var10001 == null) {
//                    Intrinsics.throwNpe();
//                }
//
//                manager.openCamera(var10001, (StateCallback)this.stateCallback, this.backgroundHandler);
//            } catch (CameraAccessException var4) {
//                Log.e("PosenetActivity", var4.toString());
//            } catch (InterruptedException var5) {
//                throw (Throwable)(new RuntimeException("Interrupted while trying to lock camera opening.", (Throwable)var5));
//            }
//
//        }
//    }
//
//    private final void closeCamera() {
//        if (this.captureSession != null) {
//            try {
//                this.cameraOpenCloseLock.acquire();
//                CameraCaptureSession var10000 = this.captureSession;
//                if (var10000 == null) {
//                    Intrinsics.throwNpe();
//                }
//
//                var10000.close();
//                this.captureSession = (CameraCaptureSession)null;
//                CameraDevice var6 = this.cameraDevice;
//                if (var6 == null) {
//                    Intrinsics.throwNpe();
//                }
//
//                var6.close();
//                this.cameraDevice = (CameraDevice)null;
//                ImageReader var7 = this.imageReader;
//                if (var7 == null) {
//                    Intrinsics.throwNpe();
//                }
//
//                var7.close();
//                this.imageReader = (ImageReader)null;
//            } catch (InterruptedException var4) {
//                throw (Throwable)(new RuntimeException("Interrupted while trying to lock camera closing.", (Throwable)var4));
//            } finally {
//                this.cameraOpenCloseLock.release();
//            }
//
//        }
//    }
//
//    private final void startBackgroundThread() {
//        HandlerThread var1 = new HandlerThread("imageAvailableListener");
//        boolean var2 = false;
//        boolean var3 = false;
//        int var5 = false;
//        var1.start();
//        this.backgroundThread = var1;
//        Handler var10001 = new Handler;
//        HandlerThread var10003 = this.backgroundThread;
//        if (var10003 == null) {
//            Intrinsics.throwNpe();
//        }
//
//        var10001.<init>(var10003.getLooper());
//        this.backgroundHandler = var10001;
//    }
//
//    private final void stopBackgroundThread() {
//        HandlerThread var10000 = this.backgroundThread;
//        if (var10000 != null) {
//            var10000.quitSafely();
//        }
//
//        try {
//            var10000 = this.backgroundThread;
//            if (var10000 != null) {
//                var10000.join();
//            }
//
//            this.backgroundThread = (HandlerThread)null;
//            this.backgroundHandler = (Handler)null;
//        } catch (InterruptedException var2) {
//            Log.e("PosenetActivity", var2.toString());
//        }
//
//    }
//
//    private final void fillBytes(Plane[] planes, byte[][] yuvBytes) {
//        int i = 0;
//
//        for(int var4 = planes.length; i < var4; ++i) {
//            ByteBuffer buffer = planes[i].getBuffer();
//            if (yuvBytes[i] == null) {
//                yuvBytes[i] = new byte[buffer.capacity()];
//            }
//
//            byte[] var10001 = yuvBytes[i];
//            if (yuvBytes[i] == null) {
//                Intrinsics.throwNpe();
//            }
//
//            buffer.get(var10001);
//        }
//
//    }
//
//    private final Bitmap cropBitmap(Bitmap bitmap) {
//        float bitmapRatio = (float)bitmap.getHeight() / (float)bitmap.getWidth();
//        float modelInputRatio = 1.0F;
//        double maxDifference = 1.0E-5D;
//        float cropHeight = modelInputRatio - bitmapRatio;
//        boolean var8 = false;
//        if ((double)Math.abs(cropHeight) < maxDifference) {
//            return bitmap;
//        } else {
//            Bitmap var10000;
//            Bitmap croppedBitmap;
//            if (modelInputRatio < bitmapRatio) {
//                cropHeight = (float)bitmap.getHeight() - (float)bitmap.getWidth() / modelInputRatio;
//                var10000 = Bitmap.createBitmap(bitmap, 0, (int)(cropHeight / (float)2), bitmap.getWidth(), (int)((float)bitmap.getHeight() - cropHeight));
//                Intrinsics.checkExpressionValueIsNotNull(var10000, "Bitmap.createBitmap(\n   …Height).toInt()\n        )");
//                croppedBitmap = var10000;
//            } else {
//                cropHeight = (float)bitmap.getWidth() - (float)bitmap.getHeight() * modelInputRatio;
//                var10000 = Bitmap.createBitmap(bitmap, (int)(cropHeight / (float)2), 0, (int)((float)bitmap.getWidth() - cropHeight), bitmap.getHeight());
//                Intrinsics.checkExpressionValueIsNotNull(var10000, "Bitmap.createBitmap(\n   …  bitmap.height\n        )");
//                croppedBitmap = var10000;
//            }
//
//            return croppedBitmap;
//        }
//    }
//
//    private final void setPaint() {
//        this.paint.setColor(-65536);
//        this.paint.setTextSize(80.0F);
//        this.paint.setStrokeWidth(8.0F);
//    }
//
//    private final void draw(Canvas canvas, Person person, Bitmap bitmap) {
//        canvas.drawColor(0, Mode.CLEAR);
//        int screenWidth = false;
//        int screenHeight = false;
//        int left = false;
//        int right = false;
//        int top = false;
//        int bottom = false;
//        int screenWidth;
//        int screenHeight;
//        int left;
//        int top;
//        if (canvas.getHeight() > canvas.getWidth()) {
//            screenWidth = canvas.getWidth();
//            screenHeight = canvas.getWidth();
//            left = 0;
//            top = (canvas.getHeight() - canvas.getWidth()) / 2;
//        } else {
//            screenWidth = canvas.getHeight();
//            screenHeight = canvas.getHeight();
//            left = (canvas.getWidth() - canvas.getHeight()) / 2;
//            top = 0;
//        }
//
//        int right = left + screenWidth;
//        int bottom = top + screenHeight;
//        this.setPaint();
//        canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), new Rect(left, top, right, bottom), this.paint);
//        float widthRatio = (float)screenWidth / (float)257;
//        float heightRatio = (float)screenHeight / (float)257;
//        Iterator var13 = person.getKeyPoints().iterator();
//
//        while(var13.hasNext()) {
//            KeyPoint keyPoint = (KeyPoint)var13.next();
//            if ((double)keyPoint.getScore() > this.minConfidence) {
//                Position position = keyPoint.getPosition();
//                float adjustedX = (float)position.getX() * widthRatio + (float)left;
//                float adjustedY = (float)position.getY() * heightRatio + (float)top;
//                canvas.drawCircle(adjustedX, adjustedY, this.circleRadius, this.paint);
//            }
//        }
//
//        var13 = this.bodyJoints.iterator();
//
//        while(var13.hasNext()) {
//            Pair line = (Pair)var13.next();
//            if ((double)((KeyPoint)person.getKeyPoints().get(((BodyPart)line.getFirst()).ordinal())).getScore() > this.minConfidence & (double)((KeyPoint)person.getKeyPoints().get(((BodyPart)line.getSecond()).ordinal())).getScore() > this.minConfidence) {
//                canvas.drawLine((float)((KeyPoint)person.getKeyPoints().get(((BodyPart)line.getFirst()).ordinal())).getPosition().getX() * widthRatio + (float)left, (float)((KeyPoint)person.getKeyPoints().get(((BodyPart)line.getFirst()).ordinal())).getPosition().getY() * heightRatio + (float)top, (float)((KeyPoint)person.getKeyPoints().get(((BodyPart)line.getSecond()).ordinal())).getPosition().getX() * widthRatio + (float)left, (float)((KeyPoint)person.getKeyPoints().get(((BodyPart)line.getSecond()).ordinal())).getPosition().getY() * heightRatio + (float)top, this.paint);
//            }
//        }
//
//        String var26 = "Score: %.2f";
//        Object[] var27 = new Object[]{person.getScore()};
//        boolean var28 = false;
//        String var10000 = String.format(var26, Arrays.copyOf(var27, var27.length));
//        Intrinsics.checkExpressionValueIsNotNull(var10000, "java.lang.String.format(this, *args)");
//        String var18 = var10000;
//        canvas.drawText(var18, 15.0F * widthRatio, 30.0F * heightRatio + (float)bottom, this.paint);
//        var26 = "Device: %s";
//        Object[] var10001 = new Object[1];
//        Posenet var10004 = this.posenet;
//        if (var10004 == null) {
//            Intrinsics.throwUninitializedPropertyAccessException("posenet");
//        }
//
//        var10001[0] = var10004.getDevice();
//        var27 = var10001;
//        var28 = false;
//        var10000 = String.format(var26, Arrays.copyOf(var27, var27.length));
//        Intrinsics.checkExpressionValueIsNotNull(var10000, "java.lang.String.format(this, *args)");
//        var18 = var10000;
//        canvas.drawText(var18, 15.0F * widthRatio, 50.0F * heightRatio + (float)bottom, this.paint);
//        var26 = "Time: %.2f ms";
//        var10001 = new Object[1];
//        var10004 = this.posenet;
//        if (var10004 == null) {
//            Intrinsics.throwUninitializedPropertyAccessException("posenet");
//        }
//
//        var10001[0] = (float)var10004.getLastInferenceTimeNanos() * 1.0F / (float)1000000;
//        var27 = var10001;
//        var28 = false;
//        var10000 = String.format(var26, Arrays.copyOf(var27, var27.length));
//        Intrinsics.checkExpressionValueIsNotNull(var10000, "java.lang.String.format(this, *args)");
//        var18 = var10000;
//        canvas.drawText(var18, 15.0F * widthRatio, 70.0F * heightRatio + (float)bottom, this.paint);
//        SurfaceHolder var29 = this.surfaceHolder;
//        if (var29 == null) {
//            Intrinsics.throwNpe();
//        }
//
//        var29.unlockCanvasAndPost(canvas);
//    }
//
//    private final void processImage(Bitmap bitmap) {
//        Bitmap croppedBitmap = this.cropBitmap(bitmap);
//        Bitmap scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, 257, 257, true);
//        Posenet var10000 = this.posenet;
//        if (var10000 == null) {
//            Intrinsics.throwUninitializedPropertyAccessException("posenet");
//        }
//
//        Intrinsics.checkExpressionValueIsNotNull(scaledBitmap, "scaledBitmap");
//        Person person = var10000.estimateSinglePose(scaledBitmap);
//        SurfaceHolder var6 = this.surfaceHolder;
//        if (var6 == null) {
//            Intrinsics.throwNpe();
//        }
//
//        Canvas var7 = var6.lockCanvas();
//        Intrinsics.checkExpressionValueIsNotNull(var7, "surfaceHolder!!.lockCanvas()");
//        Canvas canvas = var7;
//        this.draw(canvas, person, scaledBitmap);
//    }
//
//    private final void createCameraPreviewSession() {
//        try {
//            Size var10001 = this.previewSize;
//            if (var10001 == null) {
//                Intrinsics.throwNpe();
//            }
//
//            int var5 = var10001.getWidth();
//            Size var10002 = this.previewSize;
//            if (var10002 == null) {
//                Intrinsics.throwNpe();
//            }
//
//            this.imageReader = ImageReader.newInstance(var5, var10002.getHeight(), 35, 2);
//            ImageReader var10000 = this.imageReader;
//            if (var10000 == null) {
//                Intrinsics.throwNpe();
//            }
//
//            var10000.setOnImageAvailableListener((OnImageAvailableListener)this.imageAvailableListener, this.backgroundHandler);
//            var10000 = this.imageReader;
//            if (var10000 == null) {
//                Intrinsics.throwNpe();
//            }
//
//            Surface recordingSurface = var10000.getSurface();
//            CameraDevice var6 = this.cameraDevice;
//            if (var6 == null) {
//                Intrinsics.throwNpe();
//            }
//
//            this.previewRequestBuilder = var6.createCaptureRequest(1);
//            Builder var3 = this.previewRequestBuilder;
//            if (var3 == null) {
//                Intrinsics.throwNpe();
//            }
//
//            var3.addTarget(recordingSurface);
//            CameraDevice var4 = this.cameraDevice;
//            if (var4 == null) {
//                Intrinsics.throwNpe();
//            }
//
//            var4.createCaptureSession(CollectionsKt.listOf(recordingSurface), (android.hardware.camera2.CameraCaptureSession.StateCallback)(new android.hardware.camera2.CameraCaptureSession.StateCallback() {
//                public void onConfigured(@NotNull CameraCaptureSession cameraCaptureSession) {
//                    Intrinsics.checkParameterIsNotNull(cameraCaptureSession, "cameraCaptureSession");
//                    if (PosenetActivityKT.this.cameraDevice != null) {
//                        PosenetActivityKT.this.captureSession = cameraCaptureSession;
//
//                        try {
//                            Builder var10000 = PosenetActivityKT.this.previewRequestBuilder;
//                            if (var10000 == null) {
//                                Intrinsics.throwNpe();
//                            }
//
//                            var10000.set(CaptureRequest.CONTROL_AF_MODE, 4);
//                            PosenetActivityKT var4 = PosenetActivityKT.this;
//                            Builder var10001 = PosenetActivityKT.this.previewRequestBuilder;
//                            if (var10001 == null) {
//                                Intrinsics.throwNpe();
//                            }
//
//                            var4.setAutoFlash(var10001);
//                            var4 = PosenetActivityKT.this;
//                            var10001 = PosenetActivityKT.this.previewRequestBuilder;
//                            if (var10001 == null) {
//                                Intrinsics.throwNpe();
//                            }
//
//                            var4.previewRequest = var10001.build();
//                            CameraCaptureSession var5 = PosenetActivityKT.this.captureSession;
//                            if (var5 == null) {
//                                Intrinsics.throwNpe();
//                            }
//
//                            CaptureRequest var6 = PosenetActivityKT.this.previewRequest;
//                            if (var6 == null) {
//                                Intrinsics.throwNpe();
//                            }
//
//                            var5.setRepeatingRequest(var6, (CaptureCallback) PosenetActivityKT.this.captureCallback, PosenetActivityKT.this.backgroundHandler);
//                        } catch (CameraAccessException var3) {
//                            Log.e("PosenetActivity", var3.toString());
//                        }
//
//                    }
//                }
//
//                public void onConfigureFailed(@NotNull CameraCaptureSession cameraCaptureSession) {
//                    Intrinsics.checkParameterIsNotNull(cameraCaptureSession, "cameraCaptureSession");
//                    PosenetActivityKT.this.showToast("Failed");
//                }
//            }), (Handler)null);
//        } catch (CameraAccessException var2) {
//            Log.e("PosenetActivity", var2.toString());
//        }
//
//    }
//
//    private final void setAutoFlash(Builder requestBuilder) {
//        if (this.flashSupported) {
//            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, 2);
//        }
//
//    }
//
//    public PosenetActivityKT() {
//        this.bodyJoints = CollectionsKt.listOf(new Pair[]{new Pair(BodyPart.LEFT_WRIST, BodyPart.LEFT_ELBOW), new Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_SHOULDER), new Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER), new Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW), new Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST), new Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP), new Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP), new Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_SHOULDER), new Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE), new Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE), new Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE), new Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)});
//        this.minConfidence = 0.5D;
//        this.circleRadius = 8.0F;
//        this.paint = new Paint();
//        this.PREVIEW_WIDTH = 640;
//        this.PREVIEW_HEIGHT = 480;
//        this.yuvBytes = new byte[3][];
//        this.cameraOpenCloseLock = new Semaphore(1);
//        this.stateCallback = new StateCallback() {
//            public void onOpened(@NotNull CameraDevice cameraDevice) {
//                Intrinsics.checkParameterIsNotNull(cameraDevice, "cameraDevice");
//                PosenetActivityKT.this.cameraOpenCloseLock.release();
//                PosenetActivityKT.this.cameraDevice = cameraDevice;
//                PosenetActivityKT.this.createCameraPreviewSession();
//            }
//
//            public void onDisconnected(@NotNull CameraDevice cameraDevice) {
//                Intrinsics.checkParameterIsNotNull(cameraDevice, "cameraDevice");
//                PosenetActivityKT.this.cameraOpenCloseLock.release();
//                cameraDevice.close();
//                PosenetActivityKT.this.cameraDevice = (CameraDevice)null;
//            }
//
//            public void onError(@NotNull CameraDevice cameraDevice, int error) {
//                Intrinsics.checkParameterIsNotNull(cameraDevice, "cameraDevice");
//                this.onDisconnected(cameraDevice);
//                FragmentActivity var10000 = PosenetActivityKT.this.getActivity();
//                if (var10000 != null) {
//                    var10000.finish();
//                }
//
//            }
//        };
//        this.captureCallback = new CaptureCallback() {
//            public void onCaptureProgressed(@NotNull CameraCaptureSession session, @NotNull CaptureRequest request, @NotNull CaptureResult partialResult) {
//                Intrinsics.checkParameterIsNotNull(session, "session");
//                Intrinsics.checkParameterIsNotNull(request, "request");
//                Intrinsics.checkParameterIsNotNull(partialResult, "partialResult");
//            }
//
//            public void onCaptureCompleted(@NotNull CameraCaptureSession session, @NotNull CaptureRequest request, @NotNull TotalCaptureResult result) {
//                Intrinsics.checkParameterIsNotNull(session, "session");
//                Intrinsics.checkParameterIsNotNull(request, "request");
//                Intrinsics.checkParameterIsNotNull(result, "result");
//            }
//        };
//        this.imageAvailableListener = new OnImageAvailableListener() {
//            public void onImageAvailable(@NotNull ImageReader imageReader) {
//                Intrinsics.checkParameterIsNotNull(imageReader, "imageReader");
//                if (PosenetActivityKT.this.previewWidth != 0 && PosenetActivityKT.this.previewHeight != 0) {
//                    Image var10000 = imageReader.acquireLatestImage();
//                    if (var10000 != null) {
//                        Image image = var10000;
//                        PosenetActivityKT var8 = PosenetActivityKT.this;
//                        Plane[] var10001 = image.getPlanes();
//                        Intrinsics.checkExpressionValueIsNotNull(var10001, "image.planes");
//                        var8.fillBytes(var10001, PosenetActivityKT.this.yuvBytes);
//                        ImageUtils var9 = ImageUtils.INSTANCE;
//                        byte[] var10 = PosenetActivityKT.this.yuvBytes[0];
//                        if (var10 == null) {
//                            Intrinsics.throwNpe();
//                        }
//
//                        byte[] var10002 = PosenetActivityKT.this.yuvBytes[1];
//                        if (var10002 == null) {
//                            Intrinsics.throwNpe();
//                        }
//
//                        byte[] var10003 = PosenetActivityKT.this.yuvBytes[2];
//                        if (var10003 == null) {
//                            Intrinsics.throwNpe();
//                        }
//
//                        int var10004 = PosenetActivityKT.this.previewWidth;
//                        int var10005 = PosenetActivityKT.this.previewHeight;
//                        Plane var10006 = image.getPlanes()[0];
//                        Intrinsics.checkExpressionValueIsNotNull(var10006, "image.planes[0]");
//                        int var6 = var10006.getRowStride();
//                        Plane var10007 = image.getPlanes()[1];
//                        Intrinsics.checkExpressionValueIsNotNull(var10007, "image.planes[1]");
//                        int var7 = var10007.getRowStride();
//                        Plane var10008 = image.getPlanes()[1];
//                        Intrinsics.checkExpressionValueIsNotNull(var10008, "image.planes[1]");
//                        var9.convertYUV420ToARGB8888(var10, var10002, var10003, var10004, var10005, var6, var7, var10008.getPixelStride(), PosenetActivityKT.access$getRgbBytes$p(PosenetActivityKT.this));
//                        Bitmap imageBitmap = Bitmap.createBitmap(PosenetActivityKT.access$getRgbBytes$p(PosenetActivityKT.this), PosenetActivityKT.this.previewWidth, PosenetActivityKT.this.previewHeight, Config.ARGB_8888);
//                        Matrix rotateMatrix = new Matrix();
//                        rotateMatrix.postRotate(90.0F);
//                        Bitmap rotatedBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, PosenetActivityKT.this.previewWidth, PosenetActivityKT.this.previewHeight, rotateMatrix, true);
//                        image.close();
//                        var8 = PosenetActivityKT.this;
//                        Intrinsics.checkExpressionValueIsNotNull(rotatedBitmap, "rotatedBitmap");
//                        var8.processImage(rotatedBitmap);
//                    }
//                }
//            }
//        };
//    }
//
//    static {
//        ORIENTATIONS.append(0, 90);
//        ORIENTATIONS.append(1, 0);
//        ORIENTATIONS.append(2, 270);
//        ORIENTATIONS.append(3, 180);
//    }
//
//    // $FF: synthetic method
//    public static final void access$setPreviewRequestBuilder$p(PosenetActivityKT $this, Builder var1) {
//        $this.previewRequestBuilder = var1;
//    }
//
//    // $FF: synthetic method
//    public static final void access$setBackgroundHandler$p(PosenetActivityKT $this, Handler var1) {
//        $this.backgroundHandler = var1;
//    }
//
//    // $FF: synthetic method
//    public static final void access$setPreviewWidth$p(PosenetActivityKT $this, int var1) {
//        $this.previewWidth = var1;
//    }
//
//    // $FF: synthetic method
//    public static final void access$setPreviewHeight$p(PosenetActivityKT $this, int var1) {
//        $this.previewHeight = var1;
//    }
//
//    // $FF: synthetic method
//    public static final void access$setYuvBytes$p(PosenetActivityKT $this, byte[][] var1) {
//        $this.yuvBytes = var1;
//    }
//
//    // $FF: synthetic method
//    public static final int[] access$getRgbBytes$p(PosenetActivityKT $this) {
//        int[] var10000 = $this.rgbBytes;
//        if (var10000 == null) {
//            Intrinsics.throwUninitializedPropertyAccessException("rgbBytes");
//        }
//
//        return var10000;
//    }
//
//    // $FF: synthetic method
//    public static final void access$setRgbBytes$p(PosenetActivityKT $this, int[] var1) {
//        $this.rgbBytes = var1;
//    }
//
//    public View _$_findCachedViewById(int var1) {
//        if (this._$_findViewCache == null) {
//            this._$_findViewCache = new HashMap();
//        }
//
//        View var2 = (View)this._$_findViewCache.get(var1);
//        if (var2 == null) {
//            View var10000 = this.getView();
//            if (var10000 == null) {
//                return null;
//            }
//
//            var2 = var10000.findViewById(var1);
//            this._$_findViewCache.put(var1, var2);
//        }
//
//        return var2;
//    }
//
//    public void _$_clearFindViewByIdCache() {
//        if (this._$_findViewCache != null) {
//            this._$_findViewCache.clear();
//        }
//
//    }
//
//    // $FF: synthetic method
//    public void onDestroyView() {
//        super.onDestroyView();
//        this._$_clearFindViewByIdCache();
//    }
//
//    @Metadata(
//            mv = {1, 1, 16},
//            bv = {1, 0, 3},
//            k = 1,
//            d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u0000 \u00072\u00020\u0001:\u0001\u0007B\u0005¢\u0006\u0002\u0010\u0002J\u0012\u0010\u0003\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006H\u0016¨\u0006\b"},
//            d2 = {"Lorg/tensorflow/lite/examples/posenet/PosenetActivity$ErrorDialog;", "Landroidx/fragment/app/DialogFragment;", "()V", "onCreateDialog", "Landroid/app/Dialog;", "savedInstanceState", "Landroid/os/Bundle;", "Companion", "app"}
//    )
//    public static final class ErrorDialog extends DialogFragment {
//        private static final String ARG_MESSAGE = "message";
//        public static final PosenetActivityKT.ErrorDialog.Companion Companion = new PosenetActivityKT.ErrorDialog.Companion((DefaultConstructorMarker)null);
//        private HashMap _$_findViewCache;
//
//        @NotNull
//        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//            android.app.AlertDialog.Builder var10000 = new android.app.AlertDialog.Builder((Context)this.getActivity());
//            Bundle var10001 = this.getArguments();
//            if (var10001 == null) {
//                Intrinsics.throwNpe();
//            }
//
//            AlertDialog var2 = var10000.setMessage((CharSequence)var10001.getString(ARG_MESSAGE)).setPositiveButton(17039370, (OnClickListener)(new OnClickListener() {
//                public final void onClick(DialogInterface $noName_0, int $noName_1) {
//                    FragmentActivity var10000 = ErrorDialog.this.getActivity();
//                    if (var10000 == null) {
//                        Intrinsics.throwNpe();
//                    }
//
//                    var10000.finish();
//                }
//            })).create();
//            Intrinsics.checkExpressionValueIsNotNull(var2, "AlertDialog.Builder(acti…ish() }\n        .create()");
//            return (Dialog)var2;
//        }
//
//        public View _$_findCachedViewById(int var1) {
//            if (this._$_findViewCache == null) {
//                this._$_findViewCache = new HashMap();
//            }
//
//            View var2 = (View)this._$_findViewCache.get(var1);
//            if (var2 == null) {
//                View var10000 = this.getView();
//                if (var10000 == null) {
//                    return null;
//                }
//
//                var2 = var10000.findViewById(var1);
//                this._$_findViewCache.put(var1, var2);
//            }
//
//            return var2;
//        }
//
//        public void _$_clearFindViewByIdCache() {
//            if (this._$_findViewCache != null) {
//                this._$_findViewCache.clear();
//            }
//
//        }
//
//        // $FF: synthetic method
//        public void onDestroyView() {
//            super.onDestroyView();
//            this._$_clearFindViewByIdCache();
//        }
//
//        @JvmStatic
//        @NotNull
//        public static final PosenetActivityKT.ErrorDialog newInstance(@NotNull String message) {
//            return Companion.newInstance(message);
//        }
//
//        @Metadata(
//                mv = {1, 1, 16},
//                bv = {1, 0, 3},
//                k = 1,
//                d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0010\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0004H\u0007R\u0016\u0010\u0003\u001a\u00020\u00048\u0002X\u0083D¢\u0006\b\n\u0000\u0012\u0004\b\u0005\u0010\u0002¨\u0006\t"},
//                d2 = {"Lorg/tensorflow/lite/examples/posenet/PosenetActivity$ErrorDialog$Companion;", "", "()V", "ARG_MESSAGE", "", "ARG_MESSAGE$annotations", "newInstance", "Lorg/tensorflow/lite/examples/posenet/PosenetActivity$ErrorDialog;", "message", "app"}
//        )
//        public static final class Companion {
//            /** @deprecated */
//            // $FF: synthetic method
//            @JvmStatic
//            private static void ARG_MESSAGE$annotations() {
//            }
//
//            @JvmStatic
//            @NotNull
//            public final PosenetActivityKT.ErrorDialog newInstance(@NotNull String message) {
//                Intrinsics.checkParameterIsNotNull(message, "message");
//                PosenetActivityKT.ErrorDialog var2 = new PosenetActivityKT.ErrorDialog();
//                boolean var3 = false;
//                boolean var4 = false;
//                int var6 = false;
//                Bundle var7 = new Bundle();
//                boolean var9 = false;
//                boolean var10 = false;
//                int var12 = false;
//                var7.putString(PosenetActivityKT.ErrorDialog.ARG_MESSAGE, message);
//                var2.setArguments(var7);
//                return var2;
//            }
//
//            private Companion() {
//            }
//
//            // $FF: synthetic method
//            public Companion(DefaultConstructorMarker $constructor_marker) {
//                this();
//            }
//        }
//    }
//
//    @Metadata(
//            mv = {1, 1, 16},
//            bv = {1, 0, 3},
//            k = 1,
//            d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T¢\u0006\u0002\n\u0000¨\u0006\b"},
//            d2 = {"Lorg/tensorflow/lite/examples/posenet/PosenetActivity$Companion;", "", "()V", "FRAGMENT_DIALOG", "", "ORIENTATIONS", "Landroid/util/SparseIntArray;", "TAG", "app"}
//    )
//    public static final class Companion {
//        private Companion() {
//        }
//
//        // $FF: synthetic method
//        public Companion(DefaultConstructorMarker $constructor_marker) {
//            this();
//        }
//    }
//}
