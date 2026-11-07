package com.example.ghibliapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.ghibliapp.R
import com.example.ghibliapp.data.model.FilmModel
import com.example.ghibliapp.data.repository.FilmRepository
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * ViewModel COMPARTIDO por los dos Fragments (se obtiene con activityViewModels()).
 *
 * Acá vive TODA la lógica de negocio de la pantalla principal:
 *  - Descargar la lista de películas de la API (una sola vez: la API no tiene páginas).
 *  - Buscador en tiempo real: filtrar por título la lista ya descargada
 *    (la API no tiene un endpoint de búsqueda).
 *  - Manejo de errores y de los mensajes que ve el usuario.
 *  - Decidir si un toque en la estrella inserta o elimina en la base de datos.
 *  - Cruzar la lista de la API con los favoritos para pintar la estrella (⋆).
 *
 * Los Fragments solo OBSERVAN estos LiveData y AVISAN eventos (texto, clicks).
 * Como ambos Fragments observan el mismo ViewModel, la estrella se actualiza en
 * tiempo real en las dos pantallas.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FilmRepository(application)

    // ---------------- Estado interno (privado, mutable) ----------------

    /** Todas las películas que devolvió la API. */
    private val _apiFilms = MutableLiveData<List<FilmModel>>(emptyList())

    /** Películas guardadas en SQLite. */
    private val _favorites = MutableLiveData<List<FilmModel>>(emptyList())

    /** Texto escrito en el buscador. Vacío = se muestran todas las películas. */
    private val _searchQuery = MutableLiveData("")

    private val _isLoading = MutableLiveData(false)
    private val _listMessage = MutableLiveData<String?>(null)
    private val _toastMessage = MutableLiveData<String?>(null)

    /** true cuando la lista ya se descargó bien al menos una vez. */
    private var isListLoaded = false

    // ---------------- Estado público (solo lectura para la UI) ----------------

    /**
     * Lista de la Pantalla 1. Se recalcula sola cada vez que cambia la lista de la API,
     * el texto del buscador O la lista de favoritos.
     */
    val films: LiveData<List<FilmModel>> = MediatorLiveData<List<FilmModel>>().apply {
        addSource(_apiFilms) { value = buildVisibleList() }
        addSource(_searchQuery) { value = buildVisibleList() }
        addSource(_favorites) { value = buildVisibleList() }
    }

    /** Lista de la Pantalla 2 (solo lo que hay en la base local). */
    val favorites: LiveData<List<FilmModel>> = _favorites

    /** true si no hay favoritos -> la Pantalla 2 muestra el mensaje de lista vacía. */
    val isFavoritesEmpty: LiveData<Boolean> = _favorites.map { it.isEmpty() }

    val isLoading: LiveData<Boolean> = _isLoading

    /** Mensaje central de la Pantalla 1 (error o lista vacía). null = no mostrar nada. */
    val listMessage: LiveData<String?> = _listMessage

    /** Mensaje corto (Toast). null = no hay nada pendiente de mostrar. */
    val toastMessage: LiveData<String?> = _toastMessage

    init {
        loadFilms()
        loadFavorites()
    }

    // ---------------- Eventos que avisa la UI ----------------

    /**
     * La UI avisa que cambió el texto del buscador (en cada tecla).
     * No se consulta la API: solo se guarda el texto y la lista se vuelve a filtrar.
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query.trim()
        if (isListLoaded) {
            updateListMessage()
        }
    }

    /** La UI avisa que el usuario tocó el mensaje de error: reintentar la carga. */
    fun retry() {
        loadFilms()
    }

    /**
     * La UI avisa que se tocó la estrella de una película.
     * Regla de negocio: si ya está en la base -> se elimina; si no está -> se inserta.
     * Se consulta la base (fuente de verdad) en lugar de confiar en el estado de la vista.
     */
    fun toggleFavorite(film: FilmModel) {
        viewModelScope.launch {
            try {
                val ok = if (repository.isFavorite(film.id)) {
                    repository.removeFavorite(film.id)
                } else {
                    repository.addFavorite(film)
                }
                if (!ok) {
                    _toastMessage.value = text(R.string.error_favorite)
                }
                loadFavoritesSuspend()
            } catch (e: Exception) {
                _toastMessage.value = text(R.string.error_favorite)
            }
        }
    }

    /** La UI avisa que ya mostró el Toast, para no repetirlo al rotar la pantalla. */
    fun onToastShown() {
        _toastMessage.value = null
    }

    // ---------------- Lógica privada ----------------

    private fun loadFilms() {
        // Evita pedidos duplicados mientras ya se está cargando.
        if (_isLoading.value == true) return

        _isLoading.value = true
        _listMessage.value = null
        viewModelScope.launch {
            try {
                val response = repository.getFilms()
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    _apiFilms.value = body
                    isListLoaded = true
                    updateListMessage()
                } else {
                    showLoadError(text(R.string.error_server, response.code()))
                }
            } catch (e: IOException) {
                showLoadError(text(R.string.error_connection))
            } catch (e: Exception) {
                showLoadError(text(R.string.error_unexpected))
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** El error se muestra en el centro de la pantalla, con la opción de tocarlo para reintentar. */
    private fun showLoadError(error: String) {
        _listMessage.value = text(R.string.error_with_retry, error)
    }

    /** Si el filtro no deja ninguna película, se avisa con un mensaje en el centro. */
    private fun updateListMessage() {
        val query = _searchQuery.value.orEmpty()
        _listMessage.value = when {
            filterByTitle().isNotEmpty() -> null
            query.isEmpty() -> text(R.string.empty_films)
            else -> text(R.string.empty_search, query)
        }
    }

    /**
     * Buscador en tiempo real: deja solo las películas cuyo título contiene el texto buscado.
     * ignoreCase = true -> no distingue mayúsculas de minúsculas ("totoro" encuentra "Totoro").
     */
    private fun filterByTitle(): List<FilmModel> {
        val query = _searchQuery.value.orEmpty()
        return _apiFilms.value.orEmpty().filter { it.title.contains(query, ignoreCase = true) }
    }

    /** Lista filtrada + estrella marcada en las películas que están en SQLite. */
    private fun buildVisibleList(): List<FilmModel> {
        val favoriteIds = _favorites.value.orEmpty().map { it.id }.toSet()
        return filterByTitle().map { it.copy(isFavorite = it.id in favoriteIds) }
    }

    private fun loadFavorites() {
        viewModelScope.launch { loadFavoritesSuspend() }
    }

    private suspend fun loadFavoritesSuspend() {
        _favorites.value = repository.getFavorites()
    }

    private fun text(resId: Int, vararg args: Any): String =
        getApplication<Application>().getString(resId, *args)
}
