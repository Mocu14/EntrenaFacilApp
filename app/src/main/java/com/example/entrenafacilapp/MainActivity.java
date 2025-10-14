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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Declaramos las variables necesarias para el menú lateral
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Llamamos al método padre
        setContentView(R.layout.activity_main_drawer); // Cargamos el layout principal con el menú

        // Inicializamos y asignamos el Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Inicializamos el DrawerLayout (menú lateral) y el NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Establecemos que esta actividad gestionará los clics en el menú
        navigationView.setNavigationItemSelectedListener(this);

        // Configuramos el botón de hamburguesa del menú lateral
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, // Texto al abrir
                R.string.navigation_drawer_close  // Texto al cerrar
        );
        drawerLayout.addDrawerListener(toggle); // Asignamos el listener
        toggle.syncState(); // Sincronizamos el icono del botón

        // Si es la primera vez que se abre la actividad
        if (savedInstanceState == null) {
            // Mostramos por defecto el fragmento HomeFragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedor_fragment, new HomeFragment())
                    .commit();

            // Marcamos el ítem de "Inicio" como seleccionado
            navigationView.setCheckedItem(R.id.nav_inicio);
        }
    }

    // Método que se ejecuta cuando se selecciona una opción del menú
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragmentSeleccionado = null;
        int id = item.getItemId(); // Obtenemos el ID del ítem clicado

        // Evaluamos qué ítem ha sido seleccionado
        if (id == R.id.nav_inicio) {
            fragmentSeleccionado = new HomeFragment();
        } else if (id == R.id.nav_historial) {
            fragmentSeleccionado = new HistorialFragment();
        } else if (id == R.id.nav_perfil) {
            fragmentSeleccionado = new PerfilFragment();
        } else if (id == R.id.nav_logout) {
            // Si se pulsa cerrar sesión, eliminamos la sesión guardada
            SharedPreferences.Editor editor = getSharedPreferences("sesion", MODE_PRIVATE).edit();
            editor.clear(); // Borramos los datos
            editor.apply();

            // Volvemos a la pantalla de login
            startActivity(new Intent(this, LoginActivity.class));
            finish(); // Cerramos la actividad actual
            return true;
        }

        // Si se ha seleccionado un fragmento, lo mostramos
        if (fragmentSeleccionado != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedor_fragment, fragmentSeleccionado)
                    .commit();
        }

        // Cerramos el menú lateral
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
