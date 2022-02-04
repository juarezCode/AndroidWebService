package com.juarez.upaxdemo.data.repositories

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.storage.FirebaseStorage
import com.juarez.upaxdemo.data.models.Location
import com.juarez.upaxdemo.data.models.Photo
import com.juarez.upaxdemo.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
) {
    private val locationsCollectionRef = firestore.collection("locations")

    suspend fun saveLocation(location: Location) {
        locationsCollectionRef.add(location).await()
    }

    fun getLocations(): Flow<Resource<List<Location>>> = flow {
        emit(Resource.Loading(true))
        try {
            val snapshot = locationsCollectionRef.get().await()
            val locations = snapshot.toObjects<Location>()
            emit(Resource.Success(locations))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unexpected Error"))
        }
        emit(Resource.Loading(false))
    }

    fun uploadImage(uri: Uri, fileName: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading(true))
        storage.reference.child("images/${fileName}").putFile(uri).await()
        emit(Resource.Success(true))
        emit(Resource.Loading(false))
    }

    fun getImages(): Flow<FirebaseResult<MutableList<Photo>>> = flow {
        emit(FirebaseResult.Loading(true))
        val images = storage.reference.child("images/").listAll().await()
        val imageUrls = mutableListOf<Photo>()
        images.items.forEach { img ->
            val url = img.downloadUrl.await()
            val metadata = img.metadata.await()
            val size = metadata.sizeBytes
            imageUrls.add(Photo(url.toString(), img.name, size.toString()))
        }
        emit(FirebaseResult.Success(imageUrls))
        emit(FirebaseResult.Loading(false))
    }

    fun deletePhoto(filename: String): Flow<FirebaseResult<Boolean>> = flow {
        emit(FirebaseResult.Loading(true))
        storage.reference.child(("images/${filename}")).delete().await()
        emit(FirebaseResult.Success(true))
        emit(FirebaseResult.Loading(false))
    }
}

sealed class FirebaseResult<out T> {
    data class Loading(val isLoading: Boolean) : FirebaseResult<Nothing>()
    data class Success<out T>(val data: T) : FirebaseResult<T>()
}