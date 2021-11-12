package com.mobiversa.ezy2pay.utils

import java.io.IOException

object AppExceptions {
    class APIFailStateException(val responseDescription: String): Throwable() {
        override val message: String
            get() = responseDescription
    }

    class NoConnectivityException : IOException() {
        override val message: String
            get() = "Connection error, check your network"
    }
}