package com.platzi.android.firestore.network

import com.google.firebase.firestore.FirebaseFirestore
import com.platzi.android.firestore.model.Crypto
import com.platzi.android.firestore.model.User

const val CRYPTO_COLLECTION_NAME = "cryptos";
const val USER_COLLECTION_NAME = "users";

class FirestroeService(val firebaseFirestore: FirebaseFirestore) {

    fun setDocument (data: Any, collectionName: String, id: String, callback: Callback<Void>) {
        firebaseFirestore.collection(collectionName).document(id).set(data).addOnSuccessListener {
            callback.onSuccess(null)
        }.addOnFailureListener {
            callback.onFailed(it)
        }
    }

    fun updateUser(user:User, callback: Callback<User>) {
        firebaseFirestore.collection(USER_COLLECTION_NAME).document(user.username).update("cryptosList", user.cryptoList).addOnSuccessListener {
            if (callback != null) {
                callback.onSuccess(user);
            }
        }.addOnFailureListener { it ->
            callback.onFailed(it)
        }
    }

    fun updateCrypto(crypto: Crypto){
        firebaseFirestore.collection(CRYPTO_COLLECTION_NAME).document(crypto.getDocumentId())
            .update("available", crypto.available)
    }
}