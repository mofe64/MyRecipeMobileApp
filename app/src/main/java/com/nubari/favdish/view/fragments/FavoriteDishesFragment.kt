package com.nubari.favdish.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.nubari.favdish.application.FavDishApplication
import com.nubari.favdish.databinding.FragmentFavoriteDishesBinding
import com.nubari.favdish.model.entities.FavDIsh
import com.nubari.favdish.view.activities.MainActivity
import com.nubari.favdish.view.adapters.FavDishAdapter
import com.nubari.favdish.viewmodel.FavDishViewModel
import com.nubari.favdish.viewmodel.FavDishViewModelFactory

class FavoriteDishesFragment : Fragment() {

    private var _binding: FragmentFavoriteDishesBinding? = null
    private val favViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory(
            (requireActivity().application as FavDishApplication)
                .repository
        )
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFavoriteDishesBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        favViewModel.favDishesList.observe(viewLifecycleOwner) { dishes ->
            dishes.let {
                binding.rvFavoriteDishesList.layoutManager = GridLayoutManager(
                    requireActivity(), 2
                )
                val adapter = FavDishAdapter(this)
                binding.rvFavoriteDishesList.adapter = adapter
                if (it.isNotEmpty()) {
                    binding.rvFavoriteDishesList.visibility = View.VISIBLE
                    binding.tvNoFavoriteDishesAvailable.visibility = View.GONE
                    adapter.dishesList(it)
                } else {
                    binding.rvFavoriteDishesList.visibility = View.GONE
                    binding.tvNoFavoriteDishesAvailable.visibility = View.VISIBLE
                }
            }
        }
    }

    fun goToDetails(dish: FavDIsh) {
        findNavController().navigate(
            FavoriteDishesFragmentDirections
                .actionFavoriteDishesToDishDetails(dish)
        )
        if (requireActivity() is MainActivity) {
            (activity as MainActivity).hideBottomNavigationView()

        }
    }

    override fun onResume() {
        super.onResume()
        if (requireActivity() is MainActivity) {
            (activity as MainActivity).showBottomNavigationView()

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}