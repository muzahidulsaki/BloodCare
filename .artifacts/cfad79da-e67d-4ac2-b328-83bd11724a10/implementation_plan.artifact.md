# বাংলা কমেন্ট ইংরেজিতে রূপান্তর করার পরিকল্পনা

এই পরিকল্পনায় আমরা প্রজেক্টের বিভিন্ন ফাইলে থাকা বাংলা কমেন্টগুলোকে ইংরেজিতে অনুবাদ করবো। এটি কোডবেসকে আরও প্রফেশনাল এবং সবার জন্য বোধগম্য করতে সাহায্য করবে।

## Proposed Changes

নিম্নোক্ত ফাইলগুলোতে থাকা বাংলা কমেন্টগুলো ইংরেজিতে রূপান্তর করা হবে:

#### [MODIFY] [BloodRequestActivity.kt](file:///E:/AIUB/Language/Kotlin/Project/app/src/main/java/com/example/bloodcare/BloodRequestActivity.kt)
- লাইন ৪৮: `// এখানে আমরা .limitToLast() দিচ্ছি না, কারণ সব পোস্ট দরকার` -> `// We are not using .limitToLast() here because all posts are needed`
- লাইন ৫৫: `// ডেট চেকিং লজিক (আজকের আগের ডেট বাদ দেওয়া)` -> `// Date checking logic (exclude dates before today)`

#### [MODIFY] [DonorAdapter.kt](file:///E:/AIUB/Language/Kotlin/Project/app/src/main/java/com/example/bloodcare/DonorAdapter.kt)
- ইম্পোর্ট লাইনে থাকা বাংলা কমেন্ট সরানো হবে।

#### [MODIFY] [Login.kt](file:///E:/AIUB/Language/Kotlin/Project/app/src/main/java/com/example/bloodcare/Login.kt)
- ভিউবাইন্ডিং এবং লগইন লজিক সংক্রান্ত কমেন্টগুলো অনুবাদ করা হবে।

#### [MODIFY] [Signup.kt](file:///E:/AIUB/Language/Kotlin/Project/app/src/main/java/com/example/bloodcare/Signup.kt)
- ইম্পোর্ট লাইনে থাকা বাংলা কমেন্ট সরানো হবে।

#### [MODIFY] [SuccessBottomSheet.kt](file:///E:/AIUB/Language/Kotlin/Project/app/src/main/java/com/example/bloodcare/SuccessBottomSheet.kt)
- ইম্পোর্ট লাইনে থাকা বাংলা কমেন্ট সরানো হবে।

#### [MODIFY] [UploadImageFragment.kt](file:///E:/AIUB/Language/Kotlin/Project/app/src/main/java/com/example/bloodcare/fragment/UploadImageFragment.kt)
- Cloudinary সেটআপ, আপলোড এবং ডাটাবেস সেভ সংক্রান্ত বাংলা কমেন্টগুলো অনুবাদ করা হবে।

#### [MODIFY] [welcome.kt](file:///E:/AIUB/Language/Kotlin/Project/app/src/main/java/com/example/bloodcare/welcome.kt)
- ইম্পোর্ট লাইনে থাকা বাংলা কমেন্ট সরানো হবে।

## Verification Plan

### Manual Verification
- প্রতিটি ফাইল চেক করে নিশ্চিত করা হবে যে সব বাংলা কমেন্ট ইংরেজিতে রূপান্তরিত হয়েছে এবং কোডের কোনো লজিক পরিবর্তন হয়নি।
- প্রজেক্টটি বিল্ড করে দেখা হবে কোনো সিনট্যাক্স এরর আছে কিনা।
