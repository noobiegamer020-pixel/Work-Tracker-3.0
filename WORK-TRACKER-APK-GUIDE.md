# Work Tracker APK: step-by-step guide (Realme Neo 7)

You need a computer, your GitHub account, and the file `work-tracker-apk-project.zip`. The repository you already made, **Work-Tracker**, is the right one. The first build takes about 30 minutes, and later updates about 5.

## Part 1. Upload the project to GitHub (computer)
1. Unzip `work-tracker-apk-project.zip`. Open the folder `field-planner-apk` inside it. You should see `www`, `tools`, `signing`, `package.json`, `package-lock.json`, `capacitor.config.json` and more.
2. Open your **Work-Tracker** repository on github.com.
3. Press **Add file**, then **Upload files**.
4. Select **everything inside** the `field-planner-apk` folder and drag it onto the page. Wait until all files are listed, including the folders `www`, `tools` and `signing`.
5. Press **Commit changes**.
6. The hidden `.github` folder often doesn't upload, so create it by hand:
   - Press **Add file**, then **Create new file**.
   - In the name box type exactly: `.github/workflows/build.yml` (typing the slashes creates the folders).
   - Open `GITHUB-WORKFLOW-build.yml` from the zip with Notepad, copy everything, and paste it into the big box.
   - Press **Commit changes**.

## Part 2. Build the APK
1. Open the **Actions** tab. If a **Build APK** run is already going, wait for it. Otherwise press **Build APK**, then **Run workflow**.
2. Wait 5 to 8 minutes for a green tick.
3. Open the finished run and scroll down to **Artifacts**. Download **FieldPlanner-APK** and unzip it. It contains `app-release.apk`.
4. If the run turns red, open it, click the red step, copy the last 30 lines of the error and send them to me. I will fix it.

## Part 3. Install on your phone
1. Send `app-release.apk` to your phone (Google Drive or USB).
2. Open it from the Files app. Allow **Install unknown apps** when Android asks, then tap **Install**.
3. Open **Work Tracker**. Allow **Notifications** when asked.
4. Move your data: in the Claude version go to **Settings, Backup, Copy**. In Work Tracker go to **Settings, Backup**, paste, then **Restore**.

## Part 4. Make reminders reliable on Realme
Realme closes apps in the background unless you allow them. Menu names can differ slightly.
1. **Settings, Apps, App management, Work Tracker**:
   - **Notifications**: allow all, including the lock screen.
   - **Battery usage**: choose **Allow background activity** or **Unrestricted**.
   - **Auto launch**: turn on.
2. In the app, open **Settings, Reminders and alarms** and tap **Allow notifications**, **Exact alarms** and **Allow background**.
3. In Recent apps, lock the Work Tracker card (pull it down or tap the lock icon).
4. Tap **Test in 10 seconds** and close the app. The test notification should arrive.
5. Alarms are made in your Clock app (daily Sat to Thu, and one-time). Edit or delete them there.

## Part 5. Google sign-in and Drive sync (once)
Google needs a one-time setup, because every app that reads your Drive must be registered with Google. After this, the app only shows a **Sign in with Google** button: tap it, pick your account, and everything saves to your Drive automatically.
1. Go to https://console.cloud.google.com, create a project, and enable **Google Drive API**.
2. **OAuth consent screen**, then **Audience**: add your Gmail under **Test users** (or publish after filling the **Branding** page). Under **Data access** add `.../auth/drive.file`, `email` and `profile`.
3. **Clients, Create client**:
   - **Web application**: copy its **Client ID**.
   - **Android**: package name `com.mizou.fieldplanner` and SHA-1 `C8:99:DF:92:EF:0C:0C:0B:DC:38:15:3A:73:A2:A8:19:9F:67:07:F6`.
4. In your GitHub repository open the file **`google-client-id.txt`**, tap the pencil, paste the **Web** client ID on a new line (below the two lines starting with #), and tap **Commit changes**. The build runs again.
5. Install the new APK. In **Settings, Google Drive sync** tap **Sign in with Google**, choose your account and allow Drive access. If Google warns the app isn't verified, tap **Advanced**, then **Go to Work Tracker**.
6. If you skip step 4, the app shows a box where you can type the Web client ID instead.

## Part 6. Products, PDF presentation and WhatsApp
1. Open the **🧠 Coach** tab, then **🛍️ My best sellers**. Add up to 40 products with a photo (📷 Camera or 🖼️ Gallery), a name and your best price. Allow camera access when Android asks.
2. Open **My details for the PDF** and fill in your name, phone number and company. They appear on the cover and the last page.
3. Tap **📄 PDF + WhatsApp**. The app draws a presentation (cover, why choose us, product pages, contact page). Choose **English**, **Français** or **العربية**.
4. Choose **💬 WhatsApp** or **💼 WhatsApp Business**, edit the message, and tap **Send** next to a potential client who has a phone number (or type another number). WhatsApp opens with the PDF attached, and you tap send.
5. If WhatsApp doesn't preselect the chat on your phone, it opens its contact list instead. Choose the person there. **Share PDF…** sends the file to any other app.

## Part 7. GPS trip tracking
1. Open **Settings, 📍 GPS trip tracking**. Tap **Set home here** while you are at home, then tap **Start tracking**.
2. Allow **Location** when Android asks. Choose **Allow all the time** (or "While using the app"), and keep **Battery usage** on Unrestricted for Work Tracker, as in Part 4.
3. While tracking is on, a small notification says "Recording your route for today". The app records your route between 5:00 and 22:00 (you can change the hours).
4. Open the **Fees** tab. The **📍 GPS trip** card shows the distance, the regions you were in, a route map and your stops. Tag a stop ⛽ fuel, 🍽 meal, 🅿️ parking or 🏨 hotel. ⛽ and 🅿️ answer those fee questions for you.
5. If you slept more than 30 km from home, or started before 7:00, the card offers "Mark as slept out" or "Add breakfast". In the evening, the Fees reminder and the assistant also show the trip, for example "GPS: 238 km, Mila, Ferdjioua".
6. Your route stays on your phone only. It is not saved to Drive. Turn tracking off with **Stop tracking**.

## Part 8. What is new in this version
- **First start popup:** Work Tracker asks once for notifications, exact alarms, background use and location. Tap **Allow** and accept each question. When all are on, a green check says "Everything works fine". The Settings button disappears once everything is allowed.
- **One reminders card** in Settings, with the alarms inside a dropdown. **Appearance** is the 🎨 button at the top right.
- **Swipe left or right** to move between sections.
- **Add new client** opens as a popup. **Upload a schedule file** and **Fee rates** are dropdowns.
- **Backup:** **Create backup** saves a file in Downloads/WorkTracker. **Restore** picks that file, shows what is inside, and restores it. The app restarts afterwards.
- **GPS:** it starts by itself when you open the app. The map in Fees shows the real map with your route, and tapping it opens Google Maps. Stops near a gas station, hotel, restaurant or parking show a green ✓ with the place name. This uses OpenStreetMap places, so it needs internet and some places may be missing. **🗺 Trip history** keeps each day. Closed fees periods are in **📚 Fees history**.

## Part 9. Security
Open **Settings, 🔐 Security** in the phone app.
- **Fingerprint for gallery** (on by default): your fingerprint or screen lock is asked only when you press **Gallery** to choose a photo. The Camera button and the rest of the products section don't ask. It asks again after 2 minutes or after you leave the app.
- **Lock the whole app:** asks again when you return after a minute.
- **Block screenshots:** hides the app in recent apps and blocks screen capture. Turn it off when you want to send me a screenshot.
- **Encrypt backup files:** the backup file is protected with a password (AES-256). Keep the password safe: without it the file cannot be opened.
- **Built-in protection:** the app's data is stored in its private area (other apps can't read it), Android backups of the app are off, internet traffic is HTTPS only, the app can only talk to Google, OpenStreetMap and its own files, and web debugging is off.
- **Limits:** no app can be made impossible to hack. Keep your phone's screen lock on, install updates, and don't install apps from unknown sources.

## Part 10. Live GPS and your car Bluetooth
- **Live tracking:** while the app is open on **Fees**, the 📍 GPS trip card shows **● Live** and the route and the red dot on the map move as you drive. If your signal drops for a while, the route continues from where you are when it returns. It is one route per day, never separate pieces. If the phone's location stops sending points, the app restarts it by itself within a couple of minutes.
- **Car Bluetooth:** open **Settings, 🚗 Car Bluetooth**, tap **Choose car**, allow Bluetooth, and pick the car tablet (it must already be paired with your phone). From then on, when your phone connects to the tablet in the morning, you get a "Good morning" notification, the time is saved as the start of your day, and it shows in the Fees trip card ("Day started 06:12"). It also feeds the early-start hint for breakfast.
- **Realme:** keep **Auto launch** on and battery on **Unrestricted** for Work Tracker (Part 4), or Android may not wake the app when the car connects.

## Updates later
Replace the changed file(s) in GitHub (for example `www/index.html`). The build runs by itself. Download the new APK and install it over the old one. Your data stays, because the app is signed with the same key each time.

## Notes
- **Printing the fees sheet:** the Print button opens Android's print screen. Choose **Save as PDF**.
- **Keep the repository private.** The signing key is inside it.
- **The first build may need one fix.** Send me the error and I'll fix it.
