package com.example.ghibliapp.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ghibliapp.databinding.FragmentFilmFavoritesBinding
import com.example.ghibliapp.ui.adapter.FilmAdapter
import com.example.ghibliapp.ui.detail.DetailActivity
import com.example.ghibliapp.ui.viewmodel.MainViewModel

/**
 * Pantalla 2: películas guardadas en la base de datos local (SQLite). Funciona sin internet.
 *
 * Al tocar la estrella de un favorito, se avisa al ViewModel (que lo elimina de la base).
 * Como la Pantalla 1 observa el mismo ViewModel, su estrella también se apaga al instante.
 * Al tocar la fila se abre el detalle (Compose), que pide los datos completos a la API.
 */
class FilmFavoritesFragment : Fragment() {

    private var _binding: FragmentFilmFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var adapter: FilmAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilmFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = FilmAdapter(
            onItemClick = { film ->
                // Intent: abre DetailActivity y le pasa solo el id de la película.
                val intent = Intent(requireContext(), DetailActivity::class.java)
                intent.putExtra(DetailActivity.EXTRA_FILM_ID, film.id)
                startActivity(intent)
            },
            onFavoriteClick = { film -> viewModel.toggleFavorite(film) }
        )
        binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFavorites.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.favorites.observe(viewLifecycleOwner) { favorites ->
            adapter.submitList(favorites)
        }

        viewModel.isFavoritesEmpty.observe(viewLifecycleOwner) { isEmpty ->
            binding.tvEmptyFavorites.isVisible = isEmpty
            binding.recyclerViewFavorites.isVisible = !isEmpty
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            if (message != null && !isHidden) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.onToastShown()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
