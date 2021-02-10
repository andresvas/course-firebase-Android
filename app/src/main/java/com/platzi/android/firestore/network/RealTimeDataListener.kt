package com.platzi.android.firestore.network

import java.lang.Exception

interface RealTimeDataListener<T> {

    fun onDataChange(updatedData: T)

    fun onError(exception: Exception)
}