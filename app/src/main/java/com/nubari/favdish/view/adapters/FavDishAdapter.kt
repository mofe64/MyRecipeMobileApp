package com.nubari.favdish.view.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nubari.favdish.R
import com.nubari.favdish.databinding.ItemDishLayoutBinding
import com.nubari.favdish.model.entities.FavDIsh
import com.nubari.favdish.utils.Constants
import com.nubari.favdish.view.activities.AddUpdateDishActivity
import com.nubari.favdish.view.fragments.AllDishesFragment
import com.nubari.favdish.view.fragments.FavoriteDishesFragment

class FavDishAdapter(private val fragment: Fragment) :
    RecyclerView.Adapter<FavDishAdapter.ViewHolder>() {
    private var dishes: List<FavDIsh> = listOf()

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class ViewHolder(view: ItemDishLayoutBinding) : RecyclerView.ViewHolder(view.root) {
        // Holds the TextView that will add each item to
        val ivDishImage = view.ivDishImage
        val tvTitle = view.tvDishTitle
        val ibMore = view.ibMore
    }

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemDishLayoutBinding = ItemDishLayoutBinding.inflate(
            LayoutInflater.from(fragment.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dish = dishes[position]
        // Load the dish image in the ImageView.
        Glide.with(fragment)
            .load(dish.image)
            .into(holder.ivDishImage)
        holder.tvTitle.text = dish.title

        // item view refers to each item in held by the view holder
        holder.itemView.setOnClickListener {
            // on click if the fragment calling this method is the all dishes frag
            // we call the all dishes frags go to dish details method to navigate to the
            // dish details fragment
            if (fragment is AllDishesFragment) {
                fragment.goToDishDetails(dish)
            }
            if (fragment is FavoriteDishesFragment) {
                fragment.goToDetails(dish)
            }
        }
        holder.ibMore.setOnClickListener {
            val popup = PopupMenu(
                fragment.context, //context
                holder.ibMore //view to anchor the pop up to
            )
            popup.menuInflater.inflate(R.menu.menu_adapter, popup.menu)
            popup.setOnMenuItemClickListener {
                if (it.itemId == R.id.action_edit_dish) {
                    val intent = Intent(
                        fragment.requireActivity(),
                        AddUpdateDishActivity::class.java
                    )
                    intent.putExtra(Constants.EXTRA_DISH_DETAILS, dish)
                    fragment.requireActivity().startActivity(intent)

                } else if (it.itemId == R.id.action_delete_dish) {
                    if (fragment is AllDishesFragment) {
                        fragment.deleteDish(dish)
                    }
                }
                true
            }
            popup.show()
        }
        // we only want the more menu icon to be visible in the all dishes fragment
        // as users should not be able to delete from fav dishes
        if (fragment is AllDishesFragment) {
            holder.ibMore.visibility = View.VISIBLE
        } else if (fragment is FavoriteDishesFragment) {
            holder.ibMore.visibility = View.GONE

        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return dishes.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun dishesList(list: List<FavDIsh>) {
        dishes = list
        //notify any registered observers that data set has changed
        notifyDataSetChanged()
    }
}