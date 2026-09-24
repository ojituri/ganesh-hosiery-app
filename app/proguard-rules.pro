# R8 / ProGuard rules (used only for release builds; the debug APK is not shrunk).

# Room entities and DAOs are accessed by generated code.
-keep class com.ganeshhosiery.autoreply.data.** { *; }

# Broadcast receivers and WorkManager workers are created by Android from the manifest / class name.
-keep class com.ganeshhosiery.autoreply.call.CallStateReceiver { *; }
-keep class com.ganeshhosiery.autoreply.messaging.SmsStatusReceiver { *; }
-keep class com.ganeshhosiery.autoreply.messaging.WhatsAppWorker { *; }

# Keep useful line numbers in internal crash reports.
-keepattributes SourceFile,LineNumberTable
