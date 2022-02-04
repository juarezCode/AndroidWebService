package com.juarez.upaxdemo.ui.photo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.juarez.upaxdemo.data.adapters.PhotosAdapter
import com.juarez.upaxdemo.databinding.FragmentPhotosBinding
import com.juarez.upaxdemo.utils.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhotosFragment : Fragment() {

    private val viewModel: PhotoViewModel by activityViewModels()
    private var _binding: FragmentPhotosBinding? = null
    private val binding get() = _binding!!
    private val photosAdapter = PhotosAdapter { deletePhoto(it) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPhotosBinding.inflate(inflater, container, false)
        binding.recyclerFirebasePhotos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = photosAdapter
            setHasFixedSize(true)
        }
        viewModel.getImages()
        lifecycleScope.launchWhenStarted {
            viewModel.photosState.collect {
                when (it) {
                    is GetPhotosState.Loading -> binding.progressFirePhotos.isVisible = it.isLoading
                    is GetPhotosState.Success -> photosAdapter.submitList(it.data)
                    is GetPhotosState.Error -> {
                        Snackbar.make(binding.root, it.message, Snackbar.LENGTH_INDEFINITE)
                            .setAction("Ok") { toast("close") }
                            .show()
                    }
                    else -> Unit
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.deletePhotoState.collect {
                when (it) {
                    is DeletePhotoState.Loading -> {
                        binding.progressFirePhotos.isVisible = it.isLoading
                    }
                    is DeletePhotoState.Success -> toast("Archivo Eliminado exitosamente")
                    else -> Unit
                }
            }
        }
        return binding.root
    }

    private fun deletePhoto(filename: String) {
        viewModel.deletePhoto(filename, photosAdapter.currentList)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}