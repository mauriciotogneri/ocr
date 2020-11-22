import 'dart:typed_data';
import 'package:camera/camera.dart';
import 'package:firebase_ml_vision/firebase_ml_vision.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

class MainScreen extends StatefulWidget {
  const MainScreen();

  @override
  _MainScreenState createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  CameraController _camera;
  bool _cameraInitialized = false;
  bool _isDetecting = false;
  String detectedText = 'YOLO';

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
    if (_isDetecting) {
      return;
    }

    _isDetecting = true;

    final ImageRotation rotation = rotationIntToImageRotation(
      _camera.description.sensorOrientation,
    );

    final FirebaseVisionImage visionImage = FirebaseVisionImage.fromBytes(
      concatenatePlanes(image.planes),
      buildMetaData(image, rotation),
    );
    final TextRecognizer textRecognizer =
        FirebaseVision.instance.textRecognizer();
    final VisionText readText = await textRecognizer.processImage(visionImage);

    String result = '';
    for (final TextBlock block in readText.blocks) {
      for (final TextLine line in block.lines) {
        result += '$result ${line.text}\n';
      }
    }

    setState(() {
      detectedText = result;
    });

    _isDetecting = false;
  }

  Uint8List concatenatePlanes(List<Plane> planes) {
    final WriteBuffer allBytes = WriteBuffer();
    planes.forEach((plane) => allBytes.putUint8List(plane.bytes));

    return allBytes.done().buffer.asUint8List();
  }

  FirebaseVisionImageMetadata buildMetaData(
    CameraImage image,
    ImageRotation rotation,
  ) {
    return FirebaseVisionImageMetadata(
      rawFormat: image.format.raw,
      size: Size(image.width.toDouble(), image.height.toDouble()),
      rotation: rotation,
      planeData: image.planes.map(
        (plane) {
          return FirebaseVisionImagePlaneMetadata(
            bytesPerRow: plane.bytesPerRow,
            height: plane.height,
            width: plane.width,
          );
        },
      ).toList(),
    );
  }

  ImageRotation rotationIntToImageRotation(int rotation) {
    switch (rotation) {
      case 0:
        return ImageRotation.rotation0;
      case 90:
        return ImageRotation.rotation90;
      case 180:
        return ImageRotation.rotation180;
      default:
        assert(rotation == 270);
        return ImageRotation.rotation270;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: Stack(
        children: [
          Center(
            child: _cameraInitialized
                ? AspectRatio(
                    aspectRatio: _camera.value.aspectRatio,
                    child: CameraPreview(_camera),
                  )
                : const CircularProgressIndicator(),
          ),
          Center(
            child: Text(
              detectedText,
              style: const TextStyle(color: Colors.white, fontSize: 20),
            ),
          ),
        ],
      ),
    );
  }
}
