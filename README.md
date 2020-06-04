# Camera 2 Media Recorder
Camera2MediaRecord is an opensource built on top of Android camera2 api.

| ![Camera screen](https://github.com/namhvcntt/Camera2MediaRecord/blob/master/photos/camera.png?raw=true) | ![Recording](https://github.com/namhvcntt/Camera2MediaRecord/blob/master/photos/recording.png?raw=true)  |
|---|---|
| ![Video viewer](https://github.com/namhvcntt/Camera2MediaRecord/blob/master/photos/videoviewer.png?raw=true) | ![Photo viewer](https://github.com/namhvcntt/Camera2MediaRecord/blob/master/photos/photoviewer.png?raw=true) |


# Notice
In develop mode

# Features!
  - Capture photo
  - Record video
  - View video, photo

# You can also:
  - Setting capture parameter
  - Customize UI

# How to use
  - Import code as a module and add 
  - Start activity
```
public static final int REQUEST_CAMERA = 1001;
```
```
startActivityForResult(new Intent(MainActivity.this, CameraActivity.class), REQUEST_CAMERA);
```
  - Handle result
```
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CAMERA) {
        if (resultCode == Activity.RESULT_OK) {
            int type = data.getIntExtra(CameraActivity.INTENT_DATA_TYPE, -1);
            if (CameraActivity.RESULT_TYPE_VIDEO == type) {
                Uri fileCapture = data.getParcelableExtra(CameraActivity.INTENT_PATH);
                onReceiveVideo(fileCapture);
            } else if (CameraActivity.RESULT_TYPE_PHOTO == type) {
                Uri fileCapture = data.getParcelableExtra(CameraActivity.INTENT_PATH);
                onReceiveImage(fileCapture);
            }
        }
    }
}
```
# Customize
Extend `CameraLogicActivity` if you need to apply new UI and Camera settings of camera.
If you want to change UI of VideoViewer or PhotoViewer, do it your self.
Maybe can open to extend in the future.

# License
```
MIT License

Copyright (c) 2020 Brian Hoang

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```


