Android Camera1和Camera2的使用
===========

模拟扫码界面，带方形取景框

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
            openCamera();
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
* #### Camera
* #### Camera.Parameters 
* #### Camera.AutoFocusCallback 
* #### Camera.PreviewCallback
* #### SensorController

Camera2
-----------
* #### CameraManager 
* #### CameraDevice
* #### CameraCharacteristics 
* #### CameraCaptureSession 
* #### CaptureRequest 
* #### ImageReader 
