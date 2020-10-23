package com.robin729.aqi.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.robin729.aqi.R
import com.robin729.aqi.adapter.FavouritesListAdapter
import com.robin729.aqi.data.model.Resource
import com.robin729.aqi.viewmodel.FavouritesViewModel
import kotlinx.android.synthetic.main.fragment_favourites.*

/**
 * A simple [Fragment] subclass.
 */
class FavouritesFragment : Fragment() {

    private val favouritesViewModel: FavouritesViewModel by lazy {
        ViewModelProvider(this).get(FavouritesViewModel::class.java)
    }

    private val favouritesListAdapter: FavouritesListAdapter by lazy {
        FavouritesListAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favourites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favouritesRv.adapter = favouritesListAdapter
        favouritesRv.layoutManager = LinearLayoutManager(context)


        favouritesViewModel.favouritesData.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    favouritesListAdapter.submitList(it.data)
                    progressBar.visibility = View.GONE
                    errorTxt.visibility = View.GONE
                }

                Resource.Status.LOADING -> {
                    progressBar.visibility = View.VISIBLE
                    errorTxt.visibility = View.GONE
                }

                Resource.Status.ERROR -> {
                    progressBar.visibility = View.GONE
                    errorTxt.visibility = View.VISIBLE
                }
            }
        })
    }

}
