package com.example.ghibliapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.ghibliapp.databinding.ActivityMainBinding
import com.example.ghibliapp.ui.fragment.FilmFavoritesFragment
import com.example.ghibliapp.ui.fragment.FilmListFragment

/**
 * Activity contenedora. Solo se encarga de la NAVEGACIÓN:
 * aloja los dos Fragments en el FragmentContainerView y los alterna con el
 * BottomNavigationView. No tiene lógica de negocio.
 *
 * Los Fragments se agregan una sola vez y luego se muestran/ocultan (show/hide),
 * así se conserva la posición del scroll (y el texto del buscador) al cambiar de pestaña.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val TAG_LIST = "tag_film_list"
        private const val TAG_FAVORITES = "tag_film_favorites"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()
        setupBottomNavigation()

        // Primera apertura: mostrar la pantalla de la API.
        // (Al rotar, el FragmentManager restaura solo los Fragments y su visibilidad.)
        if (savedInstanceState == null) {
            showFragment(TAG_LIST)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_films -> showFragment(TAG_LIST)
                R.id.nav_favorites -> showFragment(TAG_FAVORITES)
            }
            true
        }
        // Volver a tocar la pestaña actual no hace nada.
        binding.bottomNavigation.setOnItemReselectedListener { }
    }

    private fun showFragment(tag: String) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()

        fragmentManager.fragments.forEach { transaction.hide(it) }

        val existing = fragmentManager.findFragmentByTag(tag)
        if (existing != null) {
            transaction.show(existing)
        } else {
            transaction.add(R.id.fragmentContainer, createFragment(tag), tag)
        }
        transaction.commit()
    }

    private fun createFragment(tag: String): Fragment = when (tag) {
        TAG_FAVORITES -> FilmFavoritesFragment()
        else -> FilmListFragment()
    }

    /**
     * Desde Android 15 la app se dibuja de borde a borde: se agrega padding arriba y a los
     * costados para no quedar debajo de la barra de estado. El BottomNavigationView ya
     * maneja por sí solo el espacio de la barra de navegación inferior.
     */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, 0)
            insets
        }
    }
}
