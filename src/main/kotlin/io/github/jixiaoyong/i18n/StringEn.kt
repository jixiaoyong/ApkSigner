package io.github.jixiaoyong.i18n

import cafe.adriel.lyricist.LyricistStrings

/**
 * @author : jixiaoyong
 * @description ：英文翻译
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 30/3/2024
 */
@LyricistStrings(languageTag = Locales.EN, default = true)
internal val EnStrings = Strings(
    loading = "Loading...",
    alreadyRunning = "Apk Signer has been started, please do not start it again.",
    exit = "Quit",
    retry = "Retry",
    signConfig = "Signature information",
    signApp = "Signature APP",
    settingsConfig = "Settings Config",
    confirm = "Confirm",
    change = "Change",
    noContent = "No content",
    currentSignInfo = "Current signature",
    changeSignInfo = "Re-select signature",
    deleteSignInfoTips = "Delete stored signature information, \nit won't delete your signature files",
    nickName = "Signature nickname",
    nickNameDescription = "Nickname, used to distinguish different signatures",
    filePath = "File path",
    plzSelectSignFile = "Please select an Android signature file",
    chooseFile = "Choose file",
    absolutePathOfSignFile = "The absolute path of the signature file",
    saveNewSignInfo = "Save new signature information",
    saveNewSignInfoTips = "🎉Save successfully!\nPlease click the \"Re-select signature\" button to view. Whether to clear the filled in content?",
    cleanUp = "Clean up",
    resetSuccess = "Reset successful",
    confirmReset = "Confirm reset",
    confirmResetTips = "Resetting will clear the following content, please operate with caution",
    signToolsConfigResetTips = "Signing tool configuration (files will not be deleted)",
    signType = "Signature scheme",
    signedApkOutputDir = "Signature file output directory",
    chooseBuildTools = "Please select the build-tools directory",
    chooseBuildToolsTips = "Please drag and drop the subfolder of Android SDK's build-tools here to modify the apkSigner and zipAlign directories at once",
    apksignerDirectory = "apksigner file",
    plzChooseApksigner = "Please select apksigner file",
    chooseApksignerTips = "Please select the apksigner file in the build-tools directory in the Android SDK",
    notInit = "Not initialized yet",
    changeSuccess = "Successfully modified",
    zipDirectory = "zipalign file",
    chooseZipTips = "Please select the zipalign file in the build-tools directory in the Android SDK",
    plzChooseZip = "Please select zipAlign file",
    autoMatchSignature = "Automatically match signatures",
    autoMatchSignatureTips = "When there is only one apk file, it will automatically try to match the last used signature information.",
    aaptDirectory = "aapt file",
    chooseAaptDirectory = "Please select aapt file",
    aaptDirectoryTips = "To automatically match signatures, please correctly configure the aapt file in the build-tools directory in the Android SDK",
    reset = "Reset",
    appIntro = { version, webSite ->
        "This is a tool for local visualization of signed APKs. \nIn order to avoid leaking information such as keys, this tool will not be connected to the Internet.\n" +
                "Current version:$version，To get the latest version, please click here to visit:$webSite"
    },
    signInfoTitle = "Signature information (scroll your mouse up and down to view more)",
    checkSignFailed = { msg -> "Failed to query signature:$msg" },
    processing = "Processing...",
    plzChooseApkFile = "Please select the apk file to be signed",
    chooseApkFileTips = "Please drag and drop the apk file here\n(supports multiple selections, you can also click here to select the apk file)",
    currentSelectedFile = "Selected apk file",
    plzSelectApkFileFirst = "Please select the apk file first",
    checkSignInfo = "View signature",
    plzChooseSignedApkOutDir = "Please select the signature file output directory first",
    signType1Desc = "V 1 signature cannot be used when Android minimum API is 24+",
    isApkAlign = "Whether to enable alignment (zipAlign)",
    chooseRightSignatureTips = "Please configure the correct signature file first",
    setupApksignerAndZipAlignTips = "Please configure apksigner and zipalign paths first",
    chooseSignTypeFirst = "Please select at least one signature method",
    chooseOpenSignedApkFile = "The signature is successful. Do you want to open the signed file?",
    open = "Open",
    signedFailed = "Signing failed: ",
    copyErrorMsg = "Copy error message",
    startSignApk = "Start signing apk",
    changeLanguage = "Modify language",
    currentLanguageTitle = "Languages",
    cancel = "Cancel"
)

//internal val EnStrings = Strings(
//    simple = "Hello Compose!",
//
//    annotated = buildAnnotatedString {
//        withStyle(SpanStyle(color = Color.Red)) { append("Hello ") }
//        withStyle(SpanStyle(fontWeight = FontWeight.Light)) { append("Compose!") }
//    },
//
//    parameter = { locale ->
//        "Current locale: $locale"
//    },
//
//    plural = { count ->
//        val value = when (count) {
//            0 -> "no"
//            1, 2 -> "a few"
//            in 3..10 -> "a bunch of"
//            else -> "a lot of"
//        }
//        "I have $value apples"
//    },
//
//    list = listOf("Avocado", "Pineapple", "Plum")
//)