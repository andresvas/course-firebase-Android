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

    fun updateUser(user:User, callback: Callback<User>?) {
        firebaseFirestore.collection(USER_COLLECTION_NAME).document(user.username).update("cryptosList", user.cryptosList).addOnSuccessListener {
            if (callback != null)
                callback.onSuccess(user)
        }.addOnFailureListener { it ->
            callback?.onFailed(it)
        }
    }

    fun updateCrypto(crypto: Crypto){
        firebaseFirestore.collection(CRYPTO_COLLECTION_NAME).document(crypto.getDocumentId())
            .update("available", crypto.available)
    }

    fun getCryptos(callback: Callback<List<Crypto>>?) {
        firebaseFirestore.collection(CRYPTO_COLLECTION_NAME)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val cryptoList = result.toObjects(Crypto::class.java)
                    callback!!.onSuccess(cryptoList)
                    break
                }
            }.addOnFailureListener { exception ->
                callback!!.onFailed(exception)
            }
    }


    fun findUserById(id: String, callback: Callback<User>?) {
        firebaseFirestore.collection(USER_COLLECTION_NAME).document(id)
            .get()
            .addOnSuccessListener { result ->
                if (result.data != null) {
                    callback!!.onSuccess(result.toObject(User::class.java))
                } else {
                    callback!!.onSuccess(null)
                }
            }
            .addOnFailureListener { exception ->
                callback!!.onFailed(exception)
            }
    }


    fun listenForUpdates(cryptos: List<Crypto>,  listeber: RealTimeDataListener<Crypto>) {
        val cryptoReference = firebaseFirestore.collection(CRYPTO_COLLECTION_NAME)
        for (crypto in cryptos) {
            cryptoReference.document(crypto.getDocumentId()).addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    listeber.onError(exception)
                }
                if (snapshot != null && snapshot.exists()) {
                    listeber.onDataChange(snapshot.toObject(Crypto::class.java)!!)
                }

            }
        }
    }

    fun listenForUpdate(user: User, listeber: RealTimeDataListener<User>) {
        val  userReference = firebaseFirestore.collection(USER_COLLECTION_NAME)
        userReference.document(user.username).addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                listeber.onError(exception)
            }

            if (snapshot != null && snapshot.exists()) {
                listeber.onDataChange(snapshot.toObject(User::class.java)!!)
            }
        }
    }
}