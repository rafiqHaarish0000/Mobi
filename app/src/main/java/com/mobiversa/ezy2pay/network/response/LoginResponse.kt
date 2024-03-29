package com.mobiversa.ezy2pay.network.response

data class LoginResponse(
    val responseCode: String,
    val responseData: LoginResponseData,
    val responseDescription: String,
    val responseMessage: String
)

data class LoginResponseData(
    val appStatus: String,
    val appVersion: String,
    val auth3DS: String,
    val connectType: String,
    val deviceExpiry: String,
    val deviceId: String,
    val deviceRenewal: String,
    val deviceStatus: String,
    val enableBoost: String,
    val enableEzypass: String,
    val enableSplit: String,
    val enableEzyrec: String,
    val enableEzywire: String,
    val enableGrabPay: String,
    val enableMoto: String,
    val ezypassMid: String,
    val ezypassTid: String,
    val ezyrecDeviceExpiry: String,
    val ezyrecDeviceRenewal: String,
    val ezyrecDeviceStatus: String,
    val ezyrecMid: String,
    val ezyrecTid: String,
    val ezysplitMid: String,
    val ezysplitTid: String,
    val gpayMid: String,
    val gpayTid: String,
    val hostType: String,
    val merchantId: String,
    val merchantName: String,
    val merchantType: String,
    val mid: String,
    val mobileUserId: String,
    val motoDeviceExpiry: String,
    val motoDeviceRenewal: String,
    val motoDeviceStatus: String,
    val motoMid: String,
    val motoTid: String,
    val preAuth: String,
    val productsEnable: List<String>,
    val sessionId: String,
    val tc: String,
    val type: String,
    val liteMid: String,
    var liteUpdate: String,
    val tid: String,
    val gpayOnlineTid: String,
    val enableGrabPayOnline: String
)