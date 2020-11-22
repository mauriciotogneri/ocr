import 'package:camera/camera.dart';
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
    print(image);
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
