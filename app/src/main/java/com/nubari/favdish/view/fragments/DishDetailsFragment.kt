package com.nubari.favdish.view.fragments

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.nubari.favdish.R
import com.nubari.favdish.application.FavDishApplication
import com.nubari.favdish.databinding.FragmentDishDetailsBinding
import com.nubari.favdish.model.entities.FavDIsh
import com.nubari.favdish.utils.Constants
import com.nubari.favdish.viewmodel.FavDishViewModel
import com.nubari.favdish.viewmodel.FavDishViewModelFactory
import java.io.IOException
import java.util.*


class DishDetailsFragment : Fragment() {

    private lateinit var binding: FragmentDishDetailsBinding
    private var details: FavDIsh? = null
    private val favDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory(
            (requireActivity().application as FavDishApplication)
                .repository
        )

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // indicates that this frag will have an options menu
        setHasOptionsMenu(true)
    }

    // we inflate the custom menu we created here
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_share, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    // we specify what should happen when a menu item is clicked here
    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        when (item.itemId) {
            R.id.action_share_dish -> {
                val type = "text/plain"
                val subject = "Check this out"
                var extraText = ""
                val shareWith = "Share with"
                details?.let {
                    var image = ""
                    if (it.imageSource == Constants.DISH_IMAGE_SOURCE_ONLINE) {
                        image = it.image
                    }
                    var cookingInstructions = ""
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        cookingInstructions = Html.fromHtml(
                            it.directionToCook,
                            Html.FROM_HTML_MODE_COMPACT
                        ).toString()
                    } else {
                        @Suppress("DEPRECATION")
                        cookingInstructions = Html.fromHtml(it.directionToCook).toString()
                    }
                    extraText = "some text  \n$cookingInstructions"
                }

                val intent = Intent(Intent.ACTION_SEND)
                intent.type = type
                intent.putExtra(Intent.EXTRA_SUBJECT, subject)
                intent.putExtra(Intent.EXTRA_TEXT, extraText)
                startActivity(Intent.createChooser(intent, shareWith))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDishDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    // ONCE VIEW IS CREATED
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: DishDetailsFragmentArgs by navArgs()
        details = args.dishDetails
        args.let { it ->
            try {
                Glide.with(requireActivity())
                    .load(it.dishDetails.image)
                    .centerCrop()
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.e("Glide load fail", e?.localizedMessage ?: "error")
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            resource.let {
                                Palette.from(resource!!.toBitmap()).generate() { palette ->
                                    val intColor = palette?.vibrantSwatch?.rgb ?: 0
                                    binding.rlDishDetailMain.setBackgroundColor(intColor)
                                }
                            }
                            return false
                        }

                    })
                    .into(binding.ivDishImage)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            binding.tvTitle.text = it.dishDetails.title
            binding.tvType.text = it.dishDetails.type.replaceFirstChar { firstChar ->
                if (firstChar.isLowerCase()) firstChar.titlecase(
                    Locale.getDefault()
                ) else firstChar.toString()
            }
            binding.tvCategory.text = it.dishDetails.category
            binding.tvIngredients.text = it.dishDetails.ingredients
            binding.tvCookingDirection.text = it.dishDetails.directionToCook
            binding.tvCookingTime.text =
                resources.getString(R.string.lbl_estimate_cooking_time, it.dishDetails.cookingTime)

            if (args.dishDetails.favoriteDish) {
                binding.ivFavoriteDish.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_favorite_selected
                    )
                )
            } else {
                binding.ivFavoriteDish.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_favorite_unselected
                    )
                )
            }
        }

        binding.ivFavoriteDish.setOnClickListener {
            args.dishDetails.favoriteDish = !args.dishDetails.favoriteDish
            favDishViewModel.update(args.dishDetails)
            if (args.dishDetails.favoriteDish) {
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
            } else {
                binding.ivFavoriteDish.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_favorite_unselected
                    )
                )
                Toast.makeText(
                    requireActivity(),
                    resources.getString(R.string.msg_removed_to_favorites),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


}