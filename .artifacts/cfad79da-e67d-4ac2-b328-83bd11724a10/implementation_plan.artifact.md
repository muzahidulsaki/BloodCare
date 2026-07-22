# API Key এবং সিক্রেট হাইড করার পরিকল্পনা

এই পরিকল্পনায় আমরা **Secrets Gradle Plugin for Android** ব্যবহার করে আপনার API Key এবং সিক্রেটগুলো নিরাপদভাবে `local.properties` ফাইলে সরিয়ে নেব, যাতে গিঠহাবে কোড পুশ করলেও এগুলো ফাঁস না হয়।

## Proposed Changes

### Configuration & Dependency

#### [MODIFY] [libs.versions.toml](file:///E:/AIUB/Language/Kotlin/Project/gradle/libs.versions.toml)
Secrets Gradle Plugin-এর ডিপেনডেন্সি যোগ করা হবে।

#### [MODIFY] [build.gradle.kts (Project)](file:///E:/AIUB/Language/Kotlin/Project/build.gradle.kts)
প্রজেক্ট লেভেলে প্লাগইনটি রেজিস্টার করা হবে।

#### [MODIFY] [build.gradle.kts (App)](file:///E:/AIUB/Language/Kotlin/Project/app/build.gradle.kts)
অ্যাপ মডিউলে প্লাগইনটি অ্যাপ্লাই করা হবে এবং `buildConfig` এনাবেল করা হবে।

### Secret Management

#### [MODIFY] [local.properties](file:///E:/AIUB/Language/Kotlin/Project/local.properties)
এখানে আপনার Cloudinary-এর তথ্যগুলো যোগ করা হবে:
- `CLOUDINARY_CLOUD_NAME`
- `CLOUDINARY_API_KEY`
- `CLOUDINARY_API_SECRET`
- `CLOUDINARY_UPLOAD_PRESET`

### Implementation

#### [MODIFY] [UploadImageFragment.kt](file:///E:/AIUB/Language/Kotlin/Project/app/src/main/java/com/example/bloodcare/fragment/UploadImageFragment.kt)
হার্ডকোড করা মানগুলোর বদলে `BuildConfig` থেকে মানগুলো ব্যবহার করা হবে।

## Verification Plan

### Automated Tests
- প্রজেক্টটি বিল্ড করা হবে (`gradlew assembleDebug`) যাতে `BuildConfig` জেনারেট হয়।

### Manual Verification
- `UploadImageFragment.kt` ফাইলে `BuildConfig` থেকে আসা মানগুলো ঠিকমতো কাজ করছে কিনা তা পরীক্ষা করা।
- নিশ্চিত করা যে গিঠহাবে পুশ দেওয়ার সময় `local.properties` ফাইলটি ইনডেক্স হচ্ছে না।
