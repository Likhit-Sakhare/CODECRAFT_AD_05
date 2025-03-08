# **QRCode Scanner**

This task by CODECRAFT INFOTECH is a QRCode scanner, where you can scan any QRCode and it will show you the data embedded in it and you can perform some actions based on that QR code.

---

## **Features**

#### **Fast QR Code Scanning**
- You can Scans QR codes at any rotation and extracts embedded text which uses Google ML model.

#### **Flashlight Support**
- You can toggle flash on/off for scanning in low-light conditions.

#### **Smart Actions Based on QR Code Content**
##### **1. Text Content**
- Select and copy text or copy with a single button tap.
- Search the text in a browser.
- Share the text easily.

##### **2. Link Content**
- Open the link by clicking or using a dedicated button.
- Copy the link to the clipboard.
- Share the link anywhere.

---

## **Demo**

<video src="https://github.com/user-attachments/assets/22951353-cea5-48d0-8518-e4f65d455862" controls="controls" style="max-width: 100%; height: auto;">
    Demo how the app works.
</video>

---

## **Libraries and Methods Used**
1. **Kotlin**: First-class and official programming language for Android development.
2. **Jetpack compose**: A toolkit for building Android apps that uses a declarative API to simplify and speed up UI development
3. **Material Components for Android**: For modular and customizable Material Design UI components.
4. **MVVM**: It is an architectural pattern that separates UI (View) from business logic (ViewModel) and data (Model) to improve maintainability, testability, and scalability.
5. **Kotlin Coroutines**: They are a concurrency framework that simplifies asynchronous programming by allowing tasks to be written sequentially while managing threading and suspensions efficiently.
6. **CameraX**: It is a Jetpack library that simplifies camera integration in Android apps, providing a lifecycle-aware, consistent API for image capture, preview, and analysis.
7. **Dagger Hilt**: It is a dependency injection library for Android that simplifies Dagger setup, provides lifecycle-aware components, and improves code scalability and testability.
8. **Accompanist Permissions**: It is a Jetpack Compose library that simplifies runtime permission handling by providing a Compose-friendly API to request and manage permissions easily.
9. **SplashScreen API**: It is a native API introduced in Android 12 that provides a smooth and customizable startup experience by showing a splash screen with an icon, animation, and transition before launching the main activity.

---

## Lessons Learned

While building this app, I learned about:

1. **Google ML Kit - Barcode Scanning**
   - Learned about Google ML Kit's Barcode Scanning and how it simplifies QR code scanning.
   - Understood PreviewView and implemented it for displaying the camera feed.
   - Built and use the analyzer function to process QR codes, capturing images and converting them into raw data.

2. **CameraX, it's properties and Image Analysis**
   - Understood ImageAnalysis, its purpose and how to build it for the UI.
   - Integrated LifecycleOwner and Observer for managing camera lifecycles.
   - Used DisposableEffect to properly handle resources within the UI.
   - Configured CameraX, including camera permissions, lens facing and CameraProvider for smooth operations.

3. **Handling Actions with Intents**
   - Learned what are Intents and how useful they are for performing particular actions like opening a browser.
   - I used Intents for actions like opening URLs or searching the text in the browser from the scanned QR codes or sharing a text or links.

---

## **Contact**
For any questions or feedback, feel free to contact me at sakhare1181likhit@gmail.com and also connect with me on LinkedIn at www.linkedin.com/in/likhit-sakhare and on Twitter at https://x.com/likhit_sakhare




