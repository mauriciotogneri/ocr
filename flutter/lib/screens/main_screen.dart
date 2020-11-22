import 'package:camera/camera.dart';
import 'package:firebase_ml_vision/firebase_ml_vision.dart';
import 'package:flutter/material.dart';

class MainScreen extends StatefulWidget {
  const MainScreen();

  @override
  _MainScreenState createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  CameraController _camera;
  bool _cameraInitialized = false;

  @override
  void initState() {
    super.initState();
    _initializeCamera();
  }

  Future _initializeCamera() async {
    final List<CameraDescription> cameras = await availableCameras();
    _camera = CameraController(cameras[0], ResolutionPreset.veryHigh);
    await _camera.initialize();
    await _camera.startImageStream(_processCameraImage);
    setState(() {
      _cameraInitialized = true;
    });
  }

  Future _processCameraImage(CameraImage image) async {
    final FirebaseVisionImage visionImage =
        FirebaseVisionImage.fromBytes(image.planes[0].bytes, null);
    final TextRecognizer recognizeText =
        FirebaseVision.instance.textRecognizer();
    final VisionText readText = await recognizeText.processImage(visionImage);
    String result = '';
    for (final TextBlock block in readText.blocks) {
      for (final TextLine line in block.lines) {
        result += '$result ${line.text}\n';
      }
    }

    print(result);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: Center(
        child: _cameraInitialized
            ? AspectRatio(
                aspectRatio: _camera.value.aspectRatio,
                child: CameraPreview(_camera),
              )
            : const CircularProgressIndicator(),
      ),
    );
  }
}
