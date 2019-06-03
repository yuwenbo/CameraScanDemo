Android Camera1和Camera2的使用
===========

模拟扫码界面，带方形取景框

需要权限项
```xml
    <uses-permission android:name="android.permission.CAMERA" />

    <uses-feature android:name="android.hardware.camera" />
    <uses-feature android:name="android.hardware.camera.autofocus" />
```

相机预览容器
-------------

* #### 布局
``` xml
    <TextureView
        android:id="@+id/capture_preview"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```
* #### 自定义取景框
``` xml
    <usage.ywb.personal.mycamera.view.ViewfinderView
        android:id="@+id/view_finder"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```
Camera1
----------
* #### 相机 Camera
```java
    //默认打开后置摄像头
    mCamera = Camera.open();
    
    mCamera.setDisplayOrientation(90);
    mCamera.setParameters(mParameters);
    mCamera.setPreviewTexture(mSurfaceTexture);
    //开启预览
    mCamera.startPreview();
```    
```java
    //点击拍照时调用，获取一次当前预览，实现拍照 
    mCamera.setOneShotPreviewCallback(mPreviewCallback);
```
```java
    //拍照结束或需要停止预览时调用
    mCamera.stopPreview();
```
* #### 相机参数 Camera.Parameters 
```java
    mParameters = mCamera.getParameters();

    mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);// 自动对焦模式
    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);// 开启闪光灯
    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);// 开启闪光灯
    ...
```
* #### 预览SurfaceTexture
>创建预览
``` java
    /**
     * 相机预览视图创建
     */
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, final int width, final int height) {
            mSurfaceTexture = surface;
            ...
        }
        ...
    }

    mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);

```
>预览尺寸
```java
mParameters.getSupportedPreviewSizes();
```
>图片尺寸
```java
mParameters.getSupportedPictureSizes();
```
>设置尺寸参数
```java
    mParameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);// 获得摄像预览的大小
    mParameters.setPictureSize(mPictureSize.width, mPictureSize.height);// 设置拍出来的屏幕大小
```
* #### 自动对焦 Camera.AutoFocusCallback 
```java
    /**
     * 自动对焦回调
     */
    private class CameraAutoFocus implements Camera.AutoFocusCallback {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {

        }
    }
    
    //每次请求对焦之前需要先取消上次对焦，否则请求不会生效
    mCamera.cancelAutoFocus();
    mCamera.autoFocus(mCameraAutoFocus);
```

* #### 预览数据回调 Camera.PreviewCallback
```java
    /**
     * 获取预览帧画面内容
     */
    private class PreviewCallback implements Camera.PreviewCallback {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {

        }
    }
```
* #### 传感器Sensor
>使用传感器实现连续自动对焦，检测手机是否发生移动
```java
    mSensorManager = (SensorManager) activity.getSystemService(Activity.SENSOR_SERVICE);
    mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    
    mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    mSensorManager.unregisterListener(this, mSensor);
```

Camera2
-----------
* #### CameraDevice
>获取相机对象，可以获取到相机ID列表
```java 
    mCameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
    //设备支持的相机
    mCameraManager.getCameraIdList();
```
>尝试打开相机时的监听，成功开启会返回 <b>CameraDevice</b> 对象
```java
    /**
     * 相机打开状态的回调
     */
    private CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.i(TAG, "cameraStateCallback：" + "   onOpened：");
            try {
                if (mTextureView != null && mTextureView.isAvailable()) {
                    mCameraDevice = camera;
                    Log.i(TAG, "相机已打开，创建获取会话！");
                    camera.createCaptureSession(Arrays.asList(mSurface, mImageReader.getSurface()), captureStateCallback, mCameraHandler);
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        ...
    };
    
    mCameraManager.openCamera(mCameraId, cameraStateCallback, mCameraHandler);
    
```

* #### CameraCharacteristics 
>获取存储相机配置的 ``CameraCharacteristics`` 对象
```java 
    mCharacteristics = mCameraManager.getCameraCharacteristics(cameraId);

    //获取摄像头的方向，可以在相机列表中找到后置摄像头
    Integer lensFacing = mCharacteristics.get(CameraCharacteristics.LENS_FACING);
    if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
         // 如果是后置摄像头
         return cameraId;
    }
```
>相机可用的配置流
```java    
    StreamConfigurationMap map = mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
    if (map != null) {
        /**
         * {@link StreamConfigurationMap#getOutputSizes(Class)} 要求传递一个 Class 类型，
         * 然后根据这个类型返回对应的尺寸列表，如果给定的类型不支持，则返回 null，
         * 可以通过 StreamConfigurationMap.isOutputSupportedFor() 方法判断某一个类型是否被支持，常见的类型有：
         *
         * ImageReader：常用来拍照或接收 YUV 数据。
         * MediaRecorder：常用来录制视频。
         * MediaCodec：常用来录制视频。
         * SurfaceHolder：常用来显示预览画面。
         * SurfaceTexture：常用来显示预览画面。
         */
         Size[] mPreviewSizes = map.getOutputSizes(SurfaceTexture.class);
    }
```
根据需要可以在 mPreviewSizes 中选择一个最合适的尺寸 mPreviewSize
* #### 预览SurfaceTexture
>为TextureView设置容器监听，有效时会返回 <b>SurfaceTexture</b> 对象
``` java
    /**
     * 相机预览视图创建
     */
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, final int width, final int height) {
            mSurfaceTexture = surface;
            mSurface = new Surface(mSurfaceTexture);
            ...
        }
        ...
    }

    mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
    mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
```
* #### CameraCaptureSession 

>相机图像捕获配置回调，配置完成会返回 <b>CameraCaptureSession</b> 对象
```java
    private CameraCaptureSession.StateCallback captureStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.i(TAG, "captureStateCallback：" + "  onConfigured");
            mCaptureSession = session;
            preview();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.i(TAG, "captureStateCallback：" + "  onConfigureFailed");
            mCaptureSession = null;
        }
    };
```

>预览回调
```java 
    /**
     * 捕获图像的监听
     */
    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            Log.i(TAG, "captureCallback：" + "   onCaptureProgressed");
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Log.i(TAG, "captureCallback：" + "   onCaptureCompleted");
        }
    };

```
* #### CaptureRequest 
>预览
```java 
    //创建一个适用于配置预览的模板
    mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
    
    mCaptureRequestBuilder.addTarget(mSurface);
    //CaptureRequest.Builder#set()方法设置预览界面的特征,例如，闪光灯，zoom调焦等
    mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);//自动对焦
    // 设置自动曝光模式
    mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
    // 获取设备方向
    int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
    // 根据设备方向计算设置照片的方向
    mCaptureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
    mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    mCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mCameraHandler);
```
>拍照
```java
    //创建一个适用于配置拍照的模板
    mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
    mCaptureRequestBuilder.addTarget(mSurface);
    //为模板添加一个可以存储数据的Target
    mCaptureRequestBuilder.addTarget(mImageReader.getSurface());
    int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
    // 根据设备方向计算设置照片的方向
    mCaptureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
    mCaptureSession.capture(mCaptureRequestBuilder.build(), captureCallback, mCameraHandler);
```
>闪光灯
```java
    mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);//开启闪光灯
    mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);//关闭闪光灯
```

* #### ImageReader 
```java 
           
    mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.JPEG, 1);
    mImageReader.setOnImageAvailableListener(imageAvailableListener, mCameraHandler);
    
    /**
     * 图片文件有效读取监听
     */
    private ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireNextImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            image.close();
            reader.close();
        }
    };
    
```
在mCaptureSession完成一次拍照时候会回调onImageAvailable，并返回一个 <b>ImageReader</b> 对象

