package com.nubari.favdish.view.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nubari.favdish.databinding.ItemCustomListBinding

class CustomListItemAdapter(
    private val activity: Activity,
    private val listItems: List<String>,
    private val selection: String,
) : RecyclerView.Adapter<CustomListItemAdapter.ViewHolder>() {

    // our view holder describes the item view and gives it meta data about its place
    // within a recycler view
    class ViewHolder(view: ItemCustomListBinding) : RecyclerView.ViewHolder(view.root) {
        val tvText = view.tvText
    }

    //called once view holder is created
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
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listItems[position]
        holder.tvText.text = item
    }

    override fun getItemCount(): Int {
        return listItems.size
    }
}