# MediaPipe Pose Detection Model

## Download the Model

Download the MediaPipe Pose Landmarker model and place it here:

1. **Download from:** https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/float16/latest/pose_landmarker_lite.task

2. **File name:** `pose_landmarker_lite.task`

3. **Place in:** `app/src/main/assets/models/`

## Alternative Models

- **Lite (Recommended for mobile):** `pose_landmarker_lite.task` (~3MB)
- **Full:** `pose_landmarker_full.task` (~13MB)
- **Heavy:** `pose_landmarker_heavy.task` (~28MB)

For best performance on mobile, use the **lite** version.

## Model Info

The pose landmarker detects 33 body landmarks:
- Face (nose, eyes, ears, mouth)
- Upper body (shoulders, elbows, wrists)
- Torso (hips)
- Lower body (knees, ankles, feet)

This provides much more accurate tracking than the 6 points we're currently using!

