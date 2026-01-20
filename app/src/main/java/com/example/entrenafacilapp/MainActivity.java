package com.example.entrenafacilapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

/**
 * Activity principal de la aplicación.
 *
 * Gestiona la navegación mediante un menú lateral (DrawerLayout) y
 * los fragmentos asociados a cada sección: Inicio, Historial y Perfil.
 * También gestiona el cierre de sesión del usuario.
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Elementos de la interfaz para el menú lateral
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Llamada al método padre
        setContentView(R.layout.activity_main_drawer); // Cargamos el layout principal con DrawerLayout

        // Inicializamos y asignamos el Toolbar como ActionBar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Inicializamos DrawerLayout y NavigationView para el menú lateral
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Establecemos que esta actividad gestionará los clics en el menú
        navigationView.setNavigationItemSelectedListener(this);

        // Configuración del botón "hamburguesa" del menú lateral
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, // Texto accesible al abrir
                R.string.navigation_drawer_close  // Texto accesible al cerrar
        );
        drawerLayout.addDrawerListener(toggle); // Asignamos el listener al Drawer
        toggle.syncState(); // Sincronizamos el icono del botón con el estado del Drawer

        // Si es la primera vez que se abre la actividad
        if (savedInstanceState == null) {
            // Mostramos el fragmento HomeFragment por defecto
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedor_fragment, new HomeFragment())
                    .commit();

            // Marcamos el ítem "Inicio" como seleccionado en el menú
            navigationView.setCheckedItem(R.id.nav_inicio);
        }
    }

    /**
     * Método que se ejecuta cuando se selecciona un ítem del menú lateral.
     *
     * Dependiendo del ítem elegido, se mostrará el fragmento correspondiente
     * o se cerrará la sesión del usuario si se selecciona "Logout".
     *
     * @param item Ítem del menú seleccionado
     * @return true si la acción fue manejada correctamente
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragmentSeleccionado = null; // Fragmento a mostrar según selección
        int id = item.getItemId(); // Obtenemos el ID del ítem seleccionado

        // Evaluamos qué ítem ha sido seleccionado
        if (id == R.id.nav_inicio) {
            fragmentSeleccionado = new HomeFragment(); // Fragmento de inicio
        } else if (id == R.id.nav_historial) {
            fragmentSeleccionado = new HistorialFragment(); // Fragmento de historial
        } else if (id == R.id.nav_perfil) {
            fragmentSeleccionado = new PerfilFragment(); // Fragmento de perfil
        } else if (id == R.id.nav_logout) {
            // Si se pulsa cerrar sesión, eliminamos la sesión guardada en SharedPreferences
            SharedPreferences.Editor editor = getSharedPreferences("sesion", MODE_PRIVATE).edit();
            editor.clear(); // Borramos los datos de la sesión
            editor.apply();

            // Volvemos a la pantalla de Login
            startActivity(new Intent(this, LoginActivity.class));
            finish(); // Cerramos la actividad actual para evitar retorno
            return true;
        }

        // Si se ha seleccionado un fragmento, lo mostramos en el contenedor
        if (fragmentSeleccionado != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedor_fragment, fragmentSeleccionado)
                    .commit();
        }

        // Cerramos el menú lateral después de la selección
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
