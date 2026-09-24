# Ganesh Hosiery — Customer Auto Reply

An Android app for **Ganesh Hosiery** that automatically replies to customers who call the
shop — by SMS, and optionally by an official WhatsApp Business message — unless the caller
is on an excluded list (family, staff, suppliers). Everything runs locally on the phone;
no cloud server, no third party except (optionally) Meta's own WhatsApp Business API.

This document is written for someone who has never used Android Studio or GitHub before.
Please read **"A. Get the app onto a phone"** first — it's the fastest path and needs no
technical setup at all.

---

## Important limitation, stated plainly

This project was prepared in an environment with **no internet access and no Android
build tools installed**, so it was not possible to compile the `.apk` file directly here.
What you have is the **complete, correct Android Studio project source code** — every file
an app needs — plus two ways to turn it into an installable APK, both explained below
step by step. Part A (GitHub Actions) needs no software installed on your computer at all.

---

## A. Get the app onto a phone (recommended — no software to install)

This uses a free Microsoft-owned website called **GitHub** to build the APK for you in
the cloud. You only need a web browser.

### A1. Create a free GitHub account
1. Go to **github.com** and click **Sign up**. Follow the steps (email, password, username).

### A2. Create a new repository (an online folder for the project)
1. Once logged in, click the **+** icon (top right) → **New repository**.
2. Name it e.g. `ganesh-hosiery-app`. Leave it **Public** or **Private**, either is fine.
   Do **not** tick "Add a README file". Click **Create repository**.

### A3. Upload the project files
1. On the new (empty) repository page, click **uploading an existing file**.
2. Unzip the `GaneshHosieryAutoReply.zip` you were given, on your computer.
3. Drag the **entire contents** of the unzipped `GaneshHosieryAutoReply` folder
   (the `app` folder, `build.gradle.kts`, `settings.gradle.kts`, the `.github` folder, etc.)
   into the browser upload area. GitHub needs the `.github` folder specifically — if your
   file manager hides folders starting with a dot, turn on "Show hidden files" first, or
   drag the whole unzipped folder in one go rather than picking files one by one.
4. Scroll down, click **Commit changes**.

### A4. Let GitHub build the APK
1. Click the **Actions** tab at the top of your repository.
2. You should see a workflow called **Build APK**. Click it, then click **Run workflow**
   (a small dropdown button) → **Run workflow** again to confirm.
3. Wait 3–6 minutes. Refresh the page — a green tick ✅ means it succeeded.
4. Click into the finished run, scroll down to **Artifacts**, and download
   **GaneshHosieryAutoReply-APK**. This downloads a `.zip` containing `app-debug.apk`.

### A5. Install the APK on the shop's phone
1. Copy `app-debug.apk` onto the phone (via USB cable, WhatsApp-to-self, Google Drive, email — any way you like).
2. On the phone, tap the file to install it. Android will warn about "installing from
   unknown sources" — this is normal for any app not from the Play Store; tap **Settings**
   in that warning and allow installs from that source (Files app / Chrome / whichever
   app you used), then go back and tap **Install**.
3. **Android 13 and newer — one extra unlock step ("Restricted settings"):**
   Because the app was not installed from the Play Store, Android will initially **block**
   it from being granted the SMS, Call Log, and similar permissions, even if you try to
   grant them from inside the app. If permissions seem to "not stick":
   - Open the phone's **Settings → Apps → Ganesh Hosiery Customer Auto Reply**.
   - Tap the **3-dot menu** (top right of that screen).
   - Tap **"Allow restricted settings"**.
   - Now go back into the app and grant permissions normally — they will work.

That's it — no computer, no coding, needed for this path.

---

## B. Build it yourself in Android Studio (alternative / for developers)

Use this only if you'd rather build on your own computer instead of GitHub.

1. Install **Android Studio** (free, from developer.android.com/studio) — this includes
   the Android SDK and everything else needed.
2. Unzip `GaneshHosieryAutoReply.zip` anywhere on your computer.
3. Open Android Studio → **Open** → select the unzipped `GaneshHosieryAutoReply` folder.
4. **About the Gradle wrapper:** this project's build settings (`gradle-wrapper.properties`)
   say which Gradle version to use (8.10.2), but the small `gradlew` / `gradlew.bat`
   launcher files and wrapper `.jar` could not be generated in the offline environment
   this project was prepared in. When you open the project, do one of:
   - Let Android Studio auto-repair it: if it shows a Gradle-wrapper-related message on
     first sync, choose the option to create/download the wrapper, then **Sync Project
     with Gradle Files** again; **or**
   - Install Gradle 8.10.2 yourself (gradle.org/releases) and, in Android Studio, go to
     **File → Settings → Build, Execution, Deployment → Gradle**, set "Gradle Distribution"
     to that local installation, then Sync.
5. Once sync succeeds: **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
6. When it finishes, click the **locate** link in the notification to find
   `app-debug.apk`, and install it on the phone as described in step A5 above.

---

## What this app does — in plain terms

- When the phone **rings**, the app looks at the caller's number.
- If that number is on your **Excluded Contacts** list (family, staff, suppliers you add
  yourself), the app does nothing at all.
- Otherwise, it sends your customer an **SMS** with your shop's details (whatever you've
  filled in under Shop Details — name, address, phone, Google Maps link). Anything you
  leave blank is simply left out of the message, never invented.
- If you have set up an official **WhatsApp Business API** connection (entirely optional,
  off by default), it also sends an approved WhatsApp template message the same way.
- Every call and message is recorded in **Call History** on the phone only — nothing is
  sent anywhere else except, for WhatsApp messages, to Meta's WhatsApp Business API.

## Permissions the app asks for, and why

| Permission | Why |
|---|---|
| Phone state | To know the phone is ringing. |
| Call log | Android 9+ only shares the caller's number with apps that also hold this permission — the app never reads or displays your past call log. |
| SMS | To send the automatic reply. Sending SMS this way does **not** require the app to be your default SMS app. |
| Contacts | Optional. Lets the app show a caller's saved name and makes picking excluded contacts easier. |
| Internet / network state | Used only for WhatsApp Business messages; the app has no other use for the internet. |
| Ignore battery optimizations | Lets you ask Android not to freeze the app in the background. |

## WhatsApp Business API setup (optional)

The app **only** talks to Meta's official WhatsApp Business Cloud API — it never automates
the ordinary WhatsApp app, and no unofficial library or "WhatsApp bot" tool is used
anywhere in this code, by design.

1. Create a **Meta Business Account** and a **WhatsApp Business App** at
   developers.facebook.com (Meta's own free developer portal).
2. From the WhatsApp product setup, note down your **Phone Number ID**.
3. Generate a **permanent access token** for that app (a temporary 24-hour token also
   works for testing, but you'll need to replace it with a permanent one for real use).
4. Create and submit a **message template** for approval (Meta requires this for any
   message you send outside of a 24-hour customer-initiated conversation window — which
   is exactly this app's situation, since it is replying automatically). Approval usually
   takes minutes to a day.
5. In the app, open **WhatsApp Business Setup**, enter the Phone Number ID, the Access
   Token, the approved template's name and language code, tap **Test Connection**, then
   **Save**. Turn WhatsApp on from **Message Settings**.

The access token is encrypted on the phone (Android Keystore) and is never written to
logs or stored as plain text anywhere.

## Keeping the app reliable in the background

Several popular Indian phone brands (Xiaomi/Redmi, Oppo, Vivo, OnePlus, Realme...)
aggressively stop background apps to save battery, which would stop this app from
noticing calls. Inside the app, open **"Keep Working in Background"** on the main screen
— it has two one-tap steps (battery optimization + auto-start) plus manual instructions
for each phone brand's own settings menu.

## Google Play Store note

This app is **not** meant for the Play Store and should only be installed directly
("sideloaded") as described above — Play policy restricts apps from using SMS/Call Log
permissions the way this app needs to. That's expected and fine for a shop's own phone.

## Testing before relying on it

Use the in-app **Test Mode** screen (also reachable from the Dashboard) for:
- Sending a test SMS to any number you can check.
- Sending a test WhatsApp message (after WhatsApp setup is complete).
- Simulating a full incoming call from any number, which runs the exact same logic as a
  real call (checks the excluded list, sends SMS/WhatsApp) and is clearly marked "test"
  in Call History, without affecting daily limits or real statistics.

Suggested checks:
1. Add your own second number to **Excluded Contacts**, then simulate a call from it —
   confirm no message is sent.
2. Simulate a call from an unknown number — confirm an SMS is sent with your shop's real
   details filled in.
3. Turn off SMS permission in the phone's Settings, then simulate a call — confirm the
   app records "Failed — SMS permission is not granted" instead of crashing.
4. Turn off Wi-Fi/mobile data, then send a test WhatsApp message — confirm it is retried
   automatically once the connection is back (WhatsApp Setup must be completed first).
5. Simulate two calls from the same number within the same few seconds — confirm only
   one message is sent (duplicate-call protection).

---

## WHAT YOU NEED TO DO

This is the only part that requires you personally:

1. **Get the APK onto the shop's phone** — follow Part A above (GitHub) unless you'd
   rather build it yourself in Android Studio (Part B).
2. **Install it and complete the 7-step Setup Wizard** the first time you open it —
   grant the permissions it asks for, and fill in your shop's name, address, phone
   number and Google Maps link (leave anything blank you don't want shown).
3. **Add family, staff and supplier numbers to Excluded Contacts** so they never get an
   automatic message.
4. **Review the ready-made SMS message** under Message Settings, and edit the wording if
   you'd like — a live preview shows exactly what a customer will see.
5. **On Android 13+ phones**, if permissions don't seem to take effect, go to
   Settings → Apps → this app → 3-dot menu → **"Allow restricted settings"** (see Part A5).
6. **Open "Keep Working in Background"** in the app and tap through its two steps, so the
   phone's battery saver doesn't stop the app.
7. *(Optional)* If you'd like WhatsApp replies too: get a Meta Business/WhatsApp Business
   API account and enter its details under **WhatsApp Business Setup** in the app.
8. **Try the Test Mode screen** before relying on the app for real customers, using the
   checks listed above.

That's all — no coding or technical knowledge is required for any of these steps.
