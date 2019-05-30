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
* #### SurfaceTexture
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
    
    mCamera.startPreview();
   
    mCamera.stopPreview();
    //点击拍照时调用，获取一次当前预览，实现拍照 
    mCamera.setOneShotPreviewCallback(mPreviewCallback);

```
* #### 相机参数 Camera.Parameters 
```java
    mParameters = mCamera.getParameters();
    mParameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);// 获得摄像预览的大小
    mParameters.setPictureSize(mPictureSize.width, mPictureSize.height);// 设置拍出来的屏幕大小
    mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);// 自动对焦模式

    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);// 开启闪光灯
    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);// 开启闪光灯
    ...
```
>>预览尺寸
```java
mParameters.getSupportedPreviewSizes();
```
>>图片尺寸
```java
mParameters.getSupportedPictureSizes();
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
>>使用传感器实现连续自动对焦，检测手机是否发生移动
```java
    mSensorManager = (SensorManager) activity.getSystemService(Activity.SENSOR_SERVICE);
    mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    
    mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    mSensorManager.unregisterListener(this, mSensor);
```

Camera2
-----------
* #### CameraDevice
* #### CameraCharacteristics 
* #### CameraCaptureSession 
* #### CaptureRequest 
* #### ImageReader 
