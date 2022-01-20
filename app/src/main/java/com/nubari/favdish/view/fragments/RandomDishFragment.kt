package com.nubari.favdish.view.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.nubari.favdish.R
import com.nubari.favdish.application.FavDishApplication
import com.nubari.favdish.databinding.FragmentRandomDishBinding
import com.nubari.favdish.model.entities.FavDIsh
import com.nubari.favdish.model.entities.RandomDish
import com.nubari.favdish.utils.Constants
import com.nubari.favdish.viewmodel.FavDishViewModel
import com.nubari.favdish.viewmodel.FavDishViewModelFactory
import com.nubari.favdish.viewmodel.RandomDishViewModel

class RandomDishFragment : Fragment() {

    private var _binding: FragmentRandomDishBinding? = null
    private lateinit var randomDishViewModel: RandomDishViewModel
    private var progressDialog: Dialog? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRandomDishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        randomDishViewModel = ViewModelProvider(this).get(
            RandomDishViewModel::class.java
        )
        randomDishViewModel.getRandomRecipeFromAPI()
        randomDishViewModelObserver()

        binding.srlRandomDish.setOnRefreshListener {
            // on refresh we trigger this function again
            // which will trigger a new response and
            // since we are observing the state, the
            // ui will be updated
            randomDishViewModel.getRandomRecipeFromAPI()
        }
    }

    private fun showProgressDialog() {
        progressDialog = Dialog(requireActivity())
        progressDialog?.let {
            it.setContentView(R.layout.dialog_custom_progress)
            it.show()
        }
    }

    private fun hideProgressDialog() {
        progressDialog?.dismiss()
    }

    private fun randomDishViewModelObserver() {
        randomDishViewModel.randomDishResponse.observe(
            viewLifecycleOwner,
            { randomDishResponse ->
                randomDishResponse?.let {
                    // on new value we set
                    // the refreshing value to false on our
                    // refresh layout
                    // to stop the refreshing icon from displaying

                    if (binding.srlRandomDish.isRefreshing) {
                        binding.srlRandomDish.isRefreshing = false
                    }
                    setUpUi(it.recipes[0])
                }
            }
        )

        randomDishViewModel.randomDishLoadingError.observe(
            viewLifecycleOwner,
            { error ->
                error?.let {
                    // on error we set
                    // the refreshing value to false on our
                    // refresh layout
                    // to stop the refreshing icon fron displaying
                    Log.e("Error", it.toString())
                }
            }
        )
        randomDishViewModel.loadRandomDish.observe(
            viewLifecycleOwner,
            { loading ->
                loading?.let {
                    if (loading && !binding.srlRandomDish.isRefreshing) {
                        showProgressDialog()
                    } else {
                        hideProgressDialog()
                    }
                }
            }
        )
    }

    @SuppressLint("SetTextI18n")
    private fun setUpUi(recipe: RandomDish.Recipe) {
        Glide.with(requireActivity())
            .load(recipe.image)
            .centerCrop()
            .into(binding.ivDishImage)
        binding.tvTitle.text = recipe.title
        var dishType: String = "other"
        if (recipe.dishTypes.isNotEmpty()) {
            dishType = recipe.dishTypes[0]
            binding.tvType.text = dishType
        }
        binding.tvCategory.text = "Other"
        var ingredients = ""
        for (value in recipe.extendedIngredients) {
            ingredients = if (ingredients.isEmpty()) {
                value.original
            } else {
                ingredients + ", \n" + value.original
            }
        }
        binding.tvIngredients.text = ingredients

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.tvCookingDirection.text = Html.fromHtml(
                recipe.instructions,
                Html.FROM_HTML_MODE_COMPACT
            )
        } else {
            @Suppress("Deprecation")
            binding.tvCookingDirection.text = Html.fromHtml(recipe.instructions)
        }

        binding.tvCookingTime.text =
            resources.getString(
                R.string.lbl_estimate_cooking_time,
                recipe.readyInMinutes.toString()
            )
        binding.ivFavoriteDish.setImageDrawable(
            ContextCompat.getDrawable(
                requireActivity(),
                R.drawable.ic_favorite_unselected
            )
        )
        var added = false

        binding.ivFavoriteDish.setOnClickListener {
            if (added) {
                Toast.makeText(
                    requireActivity(),
                    resources.getString(R.string.msg_already_added_to_fav),
                    Toast.LENGTH_SHORT
                ).show()
            } else {

                val details = FavDIsh(
                    recipe.image,
                    Constants.DISH_IMAGE_SOURCE_ONLINE,
                    recipe.title,
                    dishType,
                    "other",
                    ingredients,
                    recipe.readyInMinutes.toString(),
                    recipe.instructions,
                    true
                )
                val favDishViewModel: FavDishViewModel by viewModels {
                    FavDishViewModelFactory(
                        (requireActivity().application as FavDishApplication)
                            .repository
                    )
                }
                favDishViewModel.insert(details)
                added = true
                binding.ivFavoriteDish.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_favorite_selected
                    )
                )
                Toast.makeText(
                    requireActivity(),
                    resources.getString(R.string.msg_added_to_favorites),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}