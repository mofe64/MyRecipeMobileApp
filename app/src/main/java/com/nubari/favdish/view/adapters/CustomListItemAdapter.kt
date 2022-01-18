package com.nubari.favdish.view.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.nubari.favdish.databinding.ItemCustomListBinding
import com.nubari.favdish.view.activities.AddUpdateDishActivity
import com.nubari.favdish.view.fragments.AllDishesFragment

class CustomListItemAdapter(
    private val activity: Activity,
    private val fragment: Fragment? = null,
    private val listItems: List<String>,
    private val selection: String,
) : RecyclerView.Adapter<CustomListItemAdapter.ViewHolder>() {

    // our view holder describes the item view and gives it meta data about its place
    // within a recycler view
    class ViewHolder(view: ItemCustomListBinding) : RecyclerView.ViewHolder(view.root) {
        val tvText = view.tvText
    }

    //called once view holder is created
    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // our item custom list binding is the binding obj for the item custom list
        // layout which specifies how each item in recycler view is supposed to
        // look at.

        //we inflate our binding obj
        val binding: ItemCustomListBinding = ItemCustomListBinding.inflate(
            LayoutInflater.from(activity),
            parent,
            false
        )
        // we return a view holder which has passed in the bind ing obj from the item xml layout
        return ViewHolder(binding)
    }

    // called for every single item in recycler view
    // basically used to bind data to the view holder
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
        val item = listItems[position]
        holder.tvText.text = item
        holder.itemView.setOnClickListener {
            if (activity is AddUpdateDishActivity) {
                activity.selectedListItem(item, selection)
            }
            if (fragment is AllDishesFragment) {
                fragment.filterSection(item)
            }
        }
    }

    override fun getItemCount(): Int {
        return listItems.size
    }
}