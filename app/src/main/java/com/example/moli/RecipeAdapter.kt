package com.example.moli

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.chip.Chip

class RecipeAdapter(
    private var recipes: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivRecipe: ImageView = view.findViewById(R.id.ivRecipe)
        val tvTitle: TextView = view.findViewById(R.id.tvRecipeTitle)
        val tvIngredients: TextView = view.findViewById(R.id.tvRecipeIngredients)
        val tvTime: TextView = view.findViewById(R.id.tvRecipeTime)
        val chipCategory: Chip = view.findViewById(R.id.chipCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.tvTitle.text = recipe.title
        holder.tvIngredients.text = recipe.ingredients.take(3).joinToString(", ") { it.name } + if (recipe.ingredients.size > 3) "..." else ""
        holder.tvTime.text = "${recipe.totalTimeMinutes} min"
        holder.chipCategory.text = recipe.category

        // Cargar imagen con Glide
        Glide.with(holder.itemView.context)
            .load(recipe.imageUrl)
            .transform(CenterCrop(), RoundedCorners(24))
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_report_image)
            .into(holder.ivRecipe)

        holder.itemView.setOnClickListener {
            onItemClick(recipe)
        }
    }

    override fun getItemCount() = recipes.size

    fun updateList(newList: List<Recipe>) {
        recipes = newList
        notifyDataSetChanged()
    }
}
