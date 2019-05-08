package usage.ywb.personal.mycamera.camera1;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.TextureView;

import java.io.IOException;
import java.util.List;

import usage.ywb.personal.mycamera.interfaces.CameraLauncher;

/**
 * @author yuwenbo
 * @version [ V.1.0.0  2018/8/7 ]
 */
public class Camera1Launcher extends CameraLauncher implements SensorController.OnFocusableListener {

    private static final String TAG = Camera1Launcher.class.getSimpleName();

    private TextureView textureView;
    private SurfaceTexture surfaceTexture;

    private Camera camera;
    private Camera.Parameters parameters;
    private CameraAutoFocus cameraAutoFocus;
    private PreviewCallback previewCallback;
    private Camera.Size previewSize;
    private Camera.Size pictureSize;

    private SensorController sensorController;

    private boolean isFocusing = false;

    private static final String CAMERA_THREAD = "CameraBackground";

    public Camera1Launcher(Activity activity, TextureView textureView) {
        this.textureView = textureView;
        textureView.setSurfaceTextureListener(surfaceTextureListener);
        sensorController = new SensorController(activity);
        sensorController.setOnFocusableListener(this);
    }

    /**
     * 相机预览视图创建
     */
    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, final int width, final int height) {
            surfaceTexture = surface;
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.i(TAG, "surfaceTextureListener：" + "    onSurfaceTextureDestroyed");
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    /**
     * 为相机设置参数
     */
    private void openCamera() {
        try {
            camera = Camera.open();
            cameraAutoFocus = new CameraAutoFocus();
            previewCallback = new PreviewCallback();
            parameters = camera.getParameters();
            if (previewSize == null) {
                previewSize = getBestPreviewResolution(textureView.getWidth(), textureView.getHeight());
                Log.i(TAG, "best preview sizes:----width = " + previewSize.width + " ,height = " + previewSize.height);
            }
            parameters.setPreviewSize(previewSize.width, previewSize.height);// 获得摄像预览的大小
            if (pictureSize == null) {
                pictureSize = getBestPictureResolution(previewSize);
                Log.i(TAG, "best picture sizes:----width = " + pictureSize.width + " ,height = " + pictureSize.height);
            }
            parameters.setPictureSize(pictureSize.width, pictureSize.height);// 设置拍出来的屏幕大小
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);// 自动对焦模式
//            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);// 开启闪光灯
            camera.setDisplayOrientation(90);
            camera.setParameters(parameters);
            camera.setPreviewTexture(surfaceTexture);
            Log.i(TAG, "相机准备就绪，开启预览...");
            preview();
        } catch (IOException e) {
            Log.e(TAG, "相机打开失败");
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        if (surfaceTexture != null && textureView != null && textureView.isAvailable()) {
            openCamera();
        }
        sensorController.onStart();
    }

    @Override
    public void onPause() {
        sensorController.onStop();
        if (camera != null) {
            camera.cancelAutoFocus();
            camera.stopPreview();
            camera.unlock();
            camera.release();
            camera = null;
        }
    }

    /**
     * 释放相机资源
     */
    @Override
    public void release() {
        Log.i(TAG, "----release----");
        this.onPause();
        if (surfaceTexture != null) {
            surfaceTexture.release();
            surfaceTexture = null;
        }
    }

    @Override
    public void preview() {
        if (camera != null && cameraAutoFocus != null) {
            camera.stopPreview();
            camera.startPreview();
            requestFocus();
        }
    }

    @Override
    public void capture() {
        if (camera != null && previewCallback != null) {
            camera.setOneShotPreviewCallback(previewCallback);
        }
    }

    @Override
    public void onFocusable() {
        if (camera != null && cameraAutoFocus != null && !isFocusing) {
            requestFocus();
        }
    }

    /**
     * 请求对焦
     */
    private void requestFocus() {
        if (surfaceTexture == null) {
            return;
        }
        isFocusing = true;
        camera.cancelAutoFocus();
        camera.autoFocus(cameraAutoFocus);
    }

    /**
     * 自动对焦回调
     */
    private class CameraAutoFocus implements Camera.AutoFocusCallback {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            Log.i(TAG, "----onAutoFocus----");
            isFocusing = false;
            if (!success) {
                Log.i(TAG, "对焦失败，请求继续对焦");
                // 对焦失败，发送请求继续对焦
                requestFocus();
            }
        }
    }

    /**
     * 获取预览帧画面内容
     */
    private class PreviewCallback implements Camera.PreviewCallback {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Log.i(TAG, "获取相机成功对焦后生成的预览图...");
            if (onCaptureListener != null) {
                onCaptureListener.onCaptureResult(data, pictureSize.width, pictureSize.height);
            }
        }
    }

    /**
     * 获取与屏幕预览框尺寸宽高比最接近的最高分辨率
     *
     * @param surfaceWidth  相机容器的宽度
     * @param surfaceHeight 相机容器的高度
     * @return 预览分辨率
     */
    private Camera.Size getBestPreviewResolution(int surfaceWidth, int surfaceHeight) {
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        int minOffset = Integer.MIN_VALUE;
        float minRated = Float.MAX_VALUE;
        Camera.Size bestSize = null;
        for (Camera.Size size : previewSizes) {
            Log.i(TAG, "support preview sizes:----width = " + size.width + " ,height = " + size.height);
            // 因为相机是默认横屏，所以相机宽对应组件的高（获取相机真实尺寸和容器组件的差值）
            int offset = size.width - surfaceHeight;
            // 相机所支持尺寸与当前相机容器的比例差
            float rated = Math.abs((float) size.width / (float) size.height - (float) surfaceHeight / (float) surfaceWidth);
            // 当相机支持尺寸与当前容器比例差小于0.03且相机尺寸小于容器尺寸的时候取相机尺寸和容器组件尺寸差值最小的作为最佳分表率
            if (offset > minOffset && rated < 0.03 && rated < minRated) {
                minOffset = offset;
                minRated = rated;
                bestSize = size;
            }
        }
        if (bestSize != null) {
            return bestSize;
        } else {
            return previewSizes.get(0);
        }
    }


    /**
     * 获取与最佳预览分辨率最接近的图片尺寸
     *
     * @param previewSize 预览尺寸
     * @return 图片尺寸
     */
    private Camera.Size getBestPictureResolution(Camera.Size previewSize) {
        List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
        Camera.Size bestSize = null;
        float rated = (float) previewSize.width / (float) previewSize.height;
        int minOffset = Integer.MAX_VALUE;
        for (Camera.Size size : pictureSizes) {
            Log.i(TAG, "support picture sizes:----width = " + size.width + " ,height = " + size.height);
            int offset = Math.abs(previewSize.width - size.width);
            if (offset < minOffset && (float) size.width / (float) size.height == rated) {
                bestSize = size;
                minOffset = offset;
            }
        }
        if (bestSize != null) {
            return bestSize;
        } else {
            return pictureSizes.get(0);
        }
    }


}
