package com.example.entrenafacilapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Fragmento principal de la app. Muestra las rutinas según el filtro aplicado.
 */
public class HomeFragment extends Fragment {

    // Base de datos
    DBHelper dbHelper;

    // Elementos de la vista
    ListView listView;
    Spinner spinnerFiltro;
    Button btnAgregarRutina;

    // Identificador del usuario logueado
    int usuarioId;

    /**
     * Clase interna para representar una rutina en la lista
     */
    public static class Rutina {
        public int id;
        public String nombre;
        public String dia;

        // Constructor
        public Rutina(int id, String nombre, String dia) {
            this.id = id;
            this.nombre = nombre;
            this.dia = dia;
        }

        // Cómo se mostrará en el ListView
        @Override
        public String toString() {
            return dia + ": " + nombre;
        }
    }

    /**
     * Método que se ejecuta al crear la vista del fragmento
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Cargamos el layout del fragmento
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Inicializamos la base de datos
        dbHelper = new DBHelper(getContext());

        // Obtenemos el ID del usuario desde las preferencias
        SharedPreferences prefs = requireContext().getSharedPreferences("sesion", getContext().MODE_PRIVATE);
        usuarioId = prefs.getInt("usuario_id", -1);

        // Referenciamos los elementos de la vista
        listView = view.findViewById(R.id.listViewRutinas);
        spinnerFiltro = view.findViewById(R.id.spinnerFiltro);
        btnAgregarRutina = view.findViewById(R.id.btnAgregarRutina);

        // Configuración del Spinner con filtros
        String[] opciones = {"Día actual", "Toda la semana", "Todos los días"};
        ArrayAdapter<String> filtroAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, opciones);
        filtroAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltro.setAdapter(filtroAdapter);

        // Al seleccionar una opción del spinner, se carga la lista de rutinas filtradas
        spinnerFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                cargarRutinasFiltradas(position); // Cargamos las rutinas según filtro
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No se hace nada si no selecciona nada
            }
        });

        // Botón para agregar una nueva rutina
        btnAgregarRutina.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddRutinaActivity.class);
            startActivity(intent); // Abrimos la actividad de añadir rutina
        });

        // Si pulsamos sobre una rutina, vamos al detalle
        listView.setOnItemClickListener((parent, view12, position, id) -> {
            Rutina rutina = (Rutina) parent.getItemAtPosition(position);
            Intent intent = new Intent(getContext(), DetalleRutinaActivity.class);
            intent.putExtra("rutina_id", rutina.id);
            startActivity(intent); // Mostramos los detalles de esa rutina
        });

        return view; // Retornamos la vista completa
    }

    /**
     * Cuando se vuelve al fragmento (ej: tras crear o editar rutina), recarga la lista
     */
    @Override
    public void onResume() {
        super.onResume();
        int filtroSeleccionado = spinnerFiltro.getSelectedItemPosition();
        cargarRutinasFiltradas(filtroSeleccionado);
    }

    /**
     * Carga la lista de rutinas según el filtro seleccionado
     * @param opcion posición del filtro: 0 = Día actual, 1 = Toda la semana, 2 = Todos los días
     */
    private void cargarRutinasFiltradas(int opcion) {
        // Lista donde se guardarán las rutinas
        ArrayList<Rutina> lista = new ArrayList<>();

        // Abrimos la base de datos
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Variables para la consulta
        String query;
        String[] args;

        // Elegimos la consulta dependiendo del filtro
        switch (opcion) {
            case 0: // Día actual
                String diaActual = new SimpleDateFormat("EEEE", new Locale("es", "ES")).format(new Date());
                diaActual = diaActual.substring(0, 1).toUpperCase() + diaActual.substring(1); // Capitalizamos
                query = "SELECT id, nombre, dia_semana FROM rutinas WHERE usuario_id = ? AND (dia_semana = ? OR dia_semana = 'Todos los días')";
                args = new String[]{String.valueOf(usuarioId), diaActual};
                break;
            case 1: // Toda la semana
                query = "SELECT id, nombre, dia_semana FROM rutinas WHERE usuario_id = ?";
                args = new String[]{String.valueOf(usuarioId)};
                break;
            case 2: // Solo "Todos los días"
                query = "SELECT id, nombre, dia_semana FROM rutinas WHERE usuario_id = ? AND dia_semana = 'Todos los días'";
                args = new String[]{String.valueOf(usuarioId)};
                break;
            default:
                return; // Si algo va mal, no seguimos
        }

        // Ejecutamos la consulta y rellenamos la lista
        Cursor cursor = db.rawQuery(query, args);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String nombre = cursor.getString(1);
            String dia = cursor.getString(2);
            lista.add(new Rutina(id, nombre, dia));
        }
        cursor.close(); // Cerramos el cursor

        // Adaptador para mostrar las rutinas en el ListView
        ArrayAdapter<Rutina> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, lista);
        listView.setAdapter(adapter); // Asignamos el adaptador
    }
}
