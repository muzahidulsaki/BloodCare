# Blood Care 🩸
**Saving Lives, One Drop at a Time.**

Blood Care is a modern Android application designed to bridge the gap between blood donors and those in need. Built with a focus on ease of use and reliability, it allows users to quickly find donors, post blood requests, and manage their donation history.

---

## 🚀 Key Features

- **🔐 Secure Authentication:** Seamless signup and login powered by Firebase Authentication.
- **👤 Detailed Profiles:** Professional profile setup including blood group, location, and age verification.
- **🖼️ Profile Image Hosting:** Secure and optimized profile image uploads via **Cloudinary**.
- **💉 Blood Requests:** 
    - Create, Edit, and Delete blood requests.
    - Real-time list of all active requests with date filtering (shows only today's and future requests).
    - Detailed view for each request with one-tap phone copying and sharing.
- **🔍 Find Donors:** A dedicated list to find available donors nearby with status tracking.
- **🏆 Donation Tracking & Badges:** 
    - Record your donations and earn badges like "Red Guardian" or "Life Saver".
    - 90-day cooldown logic to ensure safe donation intervals.
- **📞 Direct Communication:** One-tap call feature to connect donors and recipients instantly.
- **🛡️ Security:** Implementation of **Secrets Gradle Plugin** to protect sensitive API keys.

---

## 🛠️ Technology Stack

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Toolkit:** XML Layouts with Material Components.
- **Backend:** [Firebase](https://firebase.google.com/) (Realtime Database & Auth).
- **Image Management:** [Cloudinary Android SDK](https://cloudinary.com/documentation/android_integration).
- **Networking/Utilities:**
    - [Glide](https://github.com/bumptech/glide) for image loading.
    - [Secrets Gradle Plugin](https://github.com/google/secrets-gradle-plugin) for security.
    - ViewBinding for cleaner code.
- **Architecture:** MVVM (using Shared ViewModels for multi-step signup).

---

## 📸 Screenshots

| Welcome Screen | Dashboard | Find Donors |
| :---: | :---: | :---: |
| ![Welcome](https://via.placeholder.com/200x400?text=Welcome+Screen) | ![Dashboard](https://via.placeholder.com/200x400?text=Dashboard) | ![Donors](https://via.placeholder.com/200x400?text=Find+Donors) |

| Create Request | Profile Details | Success Modal |
| :---: | :---: | :---: |
| ![Create](https://via.placeholder.com/200x400?text=Create+Request) | ![Profile](https://via.placeholder.com/200x400?text=Profile) | ![Success](https://via.placeholder.com/200x400?text=Success) |

---

## ⚙️ Setup & Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/Blood-Care.git
   ```

2. **Firebase Setup:**
   - Create a project on [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app and download `google-services.json`.
   - Place `google-services.json` in the `app/` directory.

3. **Cloudinary Setup:**
   - Add your Cloudinary credentials to your `local.properties` file (these are protected by the Secrets Plugin):
     ```properties
     CLOUDINARY_CLOUD_NAME=your_cloud_name
     CLOUDINARY_API_KEY=your_api_key
     CLOUDINARY_API_SECRET=your_api_secret
     CLOUDINARY_UPLOAD_PRESET=your_unsigned_preset
     ```

4. **Build & Run:**
   - Sync Gradle and run the app on an emulator or physical device.

---

## 📜 Contribution
Feel free to open issues or submit pull requests to help make **Blood Care** even better!

---

## 📄 License
This project is licensed under the MIT License.
