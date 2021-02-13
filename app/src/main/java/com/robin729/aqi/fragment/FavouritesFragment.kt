package com.robin729.aqi.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.robin729.aqi.adapter.FavouritesListAdapter
import com.robin729.aqi.data.model.Resource
import com.robin729.aqi.databinding.FragmentFavouritesBinding
import com.robin729.aqi.utils.gone
import com.robin729.aqi.utils.visible
import com.robin729.aqi.viewmodel.FavouritesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavouritesFragment : Fragment() {

    private var _binding: FragmentFavouritesBinding? = null
    private val binding get() = _binding!!

    private val favouritesViewModel: FavouritesViewModel by viewModels()

    private val favouritesListAdapter: FavouritesListAdapter by lazy {
        FavouritesListAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            favouritesRv.adapter = favouritesListAdapter
            favouritesRv.layoutManager = LinearLayoutManager(context)
        }

        favouritesViewModel.favouritesData.observe(viewLifecycleOwner, {
            binding.apply {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        favouritesListAdapter.submitList(it.data)
                        progressBar.gone()
                        errorTxt.gone()
                    }

                    Resource.Status.LOADING -> {
                        progressBar.visible()
                        errorTxt.gone()
                    }

                    Resource.Status.ERROR -> {
                        progressBar.gone()
                        errorTxt.visible()
                    }
                }
            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
