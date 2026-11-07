package com.example.ghibliapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ghibliapp.R
import com.example.ghibliapp.data.model.FilmModel
import com.example.ghibliapp.databinding.ItemFilmBinding
import com.squareup.picasso.Picasso

/**
 * Adapter del RecyclerView, reutilizado por las DOS pantallas (API y Favoritos).
 *
 * Solo "pinta" datos: póster (Picasso), título, puntaje y estrella según isFavorite.
 * No decide nada: los toques se avisan hacia afuera con dos lambdas
 *  - [onItemClick]: se tocó la fila -> el Fragment abre el detalle.
 *  - [onFavoriteClick]: se tocó la estrella -> el ViewModel decide si insertar o eliminar.
 *
 * Usa ListAdapter + DiffUtil: al llegar una lista nueva, solo se redibujan las filas que
 * cambiaron (por ejemplo, la estrella de una única película).
 */
class FilmAdapter(
    private val onItemClick: (FilmModel) -> Unit,
    private val onFavoriteClick: (FilmModel) -> Unit
) : ListAdapter<FilmModel, FilmAdapter.FilmViewHolder>(DiffCallback) {

    inner class FilmViewHolder(
        private val binding: ItemFilmBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            // Los listeners se asignan UNA vez por ViewHolder (no en cada bind).
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
            binding.btnFavorite.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onFavoriteClick(getItem(position))
                }
            }
        }

        fun bind(film: FilmModel) {
            val context = binding.root.context

            binding.tvTitle.text = film.title
            binding.tvScore.text = if (film.rtScore != null) {
                context.getString(R.string.score_format, film.rtScore)
            } else {
                context.getString(R.string.score_not_available)
            }

            Picasso.get()
                .load(film.image)
                .placeholder(R.drawable.ic_film_placeholder)
                .error(R.drawable.ic_film_placeholder)
                .into(binding.ivPoster)

            binding.btnFavorite.setImageResource(
                if (film.isFavorite) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )
            binding.btnFavorite.contentDescription = context.getString(
                if (film.isFavorite) R.string.cd_remove_favorite else R.string.cd_add_favorite
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmViewHolder {
        val binding = ItemFilmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FilmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object DiffCallback : DiffUtil.ItemCallback<FilmModel>() {
        override fun areItemsTheSame(oldItem: FilmModel, newItem: FilmModel): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: FilmModel, newItem: FilmModel): Boolean =
            oldItem == newItem
    }
}
