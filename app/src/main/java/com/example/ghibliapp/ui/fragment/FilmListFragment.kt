package com.example.ghibliapp.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ghibliapp.databinding.FragmentFilmListBinding
import com.example.ghibliapp.ui.adapter.FilmAdapter
import com.example.ghibliapp.ui.detail.DetailActivity
import com.example.ghibliapp.ui.viewmodel.MainViewModel

/**
 * Pantalla 1: películas de la API + buscador en tiempo real.
 *
 * Responsabilidades (solo de VISTA):
 *  1. Inflar el layout con ViewBinding.
 *  2. Configurar el RecyclerView y el buscador.
 *  3. Observar los LiveData del ViewModel y reflejarlos en pantalla.
 *  4. Avisar al ViewModel los eventos del usuario (texto, estrella, reintentar).
 *  5. Abrir la pantalla de detalle (Compose) al tocar una película.
 */
class FilmListFragment : Fragment() {

    // ViewBinding en Fragments: se anula en onDestroyView para no filtrar memoria.
    private var _binding: FragmentFilmListBinding? = null
    private val binding get() = _binding!!

    // activityViewModels(): misma instancia que usa FilmFavoritesFragment.
    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var adapter: FilmAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilmListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupListeners()
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
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    /**
     * Buscador en tiempo real: cada cambio de texto se le avisa al ViewModel,
     * que filtra la lista por título.
     */
    private fun setupSearch() {
        // doAfterTextChanged: se ejecuta cada vez que cambia el texto (cada tecla).
        binding.etSearch.doAfterTextChanged { text ->
            viewModel.onSearchQueryChanged(text?.toString() ?: "")
        }
    }

    private fun setupListeners() {
        binding.tvEmptyOrError.setOnClickListener { viewModel.retry() }
    }

    private fun observeViewModel() {
        viewModel.films.observe(viewLifecycleOwner) { films ->
            adapter.submitList(films)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        viewModel.listMessage.observe(viewLifecycleOwner) { message ->
            binding.tvEmptyOrError.isVisible = message != null
            binding.tvEmptyOrError.text = message
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            // Solo el Fragment visible muestra el Toast (evita mostrarlo dos veces).
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
