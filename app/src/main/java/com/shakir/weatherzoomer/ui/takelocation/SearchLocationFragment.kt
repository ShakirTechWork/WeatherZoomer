package com.shakir.weatherzoomer.ui.takelocation

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.shakir.weatherzoomer.Application
import com.shakir.weatherzoomer.R
import com.shakir.weatherzoomer.adapter.LocationSearchResultsAdapter
import com.shakir.weatherzoomer.api.ApiResponse
import com.shakir.weatherzoomer.databinding.FragmentSearchLocationBinding
import com.shakir.weatherzoomer.model.searchLocation.SearchLocationResultModelItem
import com.shakir.weatherzoomer.ui.dashboard.DashboardViewModel
import com.shakir.weatherzoomer.ui.dashboard.DashboardViewModelFactory
import com.shakir.weatherzoomer.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchLocationFragment() : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentSearchLocationBinding

    private lateinit var searchLocationViewModel: SearchLocationViewModel

    private lateinit var onLocationSelectedListener: OnLocationSelectedListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSearchLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = (requireActivity().application as Application).appRepository
        searchLocationViewModel = ViewModelProvider(
            this,
            SearchLocationViewModelFactory(repository)
        )[SearchLocationViewModel::class.java]

        binding.textInputEditTextSearch.requestFocus()

        searchLocationViewModel.searchResultLiveData.observe(viewLifecycleOwner) {
            binding.progressBarSearchLocation.visibility = View.GONE
            when (it) {
                is ApiResponse.Success -> {
                    val location = it.data
                    if (!location.isNullOrEmpty()) {
                        val adapter = LocationSearchResultsAdapter(it.data, object : LocationSearchResultsAdapter.OnLocationSelectedListener {
                            override fun onLocationSelected(location: String) {
                                onLocationSelectedListener.onLocationSelected(location)
                                dismiss()
                            }

                        })
                        binding.rvLocations.adapter = adapter
                        binding.rvLocations.visibility = View.VISIBLE
                    } else {
                        Utils.showLongToast(requireContext(), "No location found!")
                    }
                }
                is ApiResponse.Failure -> {
                    Utils.showLongToast(requireContext(), "No location found!")
                }
                is ApiResponse.Loading -> {
                    Utils.printDebugLog("loading")
                }
                else -> {}
            }
        }

        binding.textInputEditTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.textInputLayoutSearch.error != null) {
                    binding.textInputLayoutSearch.error = null
                }
            }
            override fun afterTextChanged(p0: Editable?) {
                Log.d(TAG, "afterTextChanged: ${p0.toString()}")
                binding.rvLocations.visibility = View.GONE
                binding.progressBarSearchLocation.visibility = View.VISIBLE
                lifecycleScope.launch(Dispatchers.IO) {
                    searchLocationViewModel.getSearchResults(binding.textInputEditTextSearch.text.toString())
                }
            }
        })

    }

    fun setLocationSelectionListener(onLocationSelectedListener: OnLocationSelectedListener) {
        this.onLocationSelectedListener = onLocationSelectedListener
    }

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialog
    }

    companion object {
        const val TAG = "SearchLocationFragment"
        fun newInstance(): SearchLocationFragment {
            return SearchLocationFragment()
        }
    }

    interface OnLocationSelectedListener {
        fun onLocationSelected(location: String)
    }

}