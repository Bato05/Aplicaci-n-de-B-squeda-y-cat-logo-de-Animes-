package com.example.ghibliapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ghibliapp.R
import com.example.ghibliapp.data.model.FilmModel
import com.example.ghibliapp.data.repository.FilmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * ViewModel de la pantalla de detalle (Jetpack Compose).
 *
 * A diferencia del MainViewModel (LiveData, pensado para vistas XML), acá se usa
 * StateFlow, que es lo que la pantalla Compose observa con collectAsState().
 *
 * Igual que en el MainViewModel, cada dato que ve la UI tiene su par:
 *  - _xxx: MutableStateFlow privado, solo lo modifica el ViewModel.
 *  - xxx:  StateFlow público de solo lectura, lo consume la pantalla.
 */
class DetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FilmRepository(application)

    // ---------------- Estado interno (privado, mutable) ----------------

    /** Película a mostrar. null = todavía no llegó (o hubo un error). */
    private val _film = MutableStateFlow<FilmModel?>(null)

    private val _isLoading = MutableStateFlow(false)

    /** Mensaje de error. null = no hay error. */
    private val _errorMessage = MutableStateFlow<String?>(null)

    /** Id de la película pedida: se guarda para poder reintentar. */
    private var filmId = ""

    // ---------------- Estado público (solo lectura para la UI) ----------------

    val film: StateFlow<FilmModel?> = _film.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ---------------- Eventos que avisa la UI ----------------

    /** La Activity avisa qué película hay que mostrar (el id llega en el Intent). */
    fun loadFilm(id: String) {
        filmId = id
        fetchFilm()
    }

    /** La UI avisa que se tocó "Reintentar". */
    fun retry() {
        fetchFilm()
    }

    // ---------------- Lógica privada ----------------

    private fun fetchFilm() {
        _isLoading.update { true }
        _errorMessage.update { null }
        viewModelScope.launch {
            try {
                val response = repository.getFilmDetail(filmId)
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    _film.update { body }
                } else {
                    _errorMessage.update { text(R.string.error_server, response.code()) }
                }
            } catch (e: IOException) {
                _errorMessage.update { text(R.string.error_connection) }
            } catch (e: Exception) {
                _errorMessage.update { text(R.string.error_unexpected) }
            } finally {
                _isLoading.update { false }
            }
        }
    }

    private fun text(resId: Int, vararg args: Any): String =
        getApplication<Application>().getString(resId, *args)
}
