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
 * Fragmento principal de la aplicación.
 *
 * Muestra las rutinas del usuario filtradas por día, por semana completa
 * o por aquellas que se realizan todos los días.
 */
public class HomeFragment extends Fragment {

    // Helper de la base de datos
    DBHelper dbHelper;

    // Componentes de la interfaz
    ListView listView;
    Spinner spinnerFiltro;
    Button btnAgregarRutina;

    // Identificador del usuario autenticado
    int usuarioId;

    /**
     * Clase interna que representa una rutina en la lista.
     * Permite asociar el ID de la base de datos con el texto mostrado.
     */
    public static class Rutina {
        public int id;
        public String nombre;
        public String dia;

        public Rutina(int id, String nombre, String dia) {
            this.id = id;
            this.nombre = nombre;
            this.dia = dia;
        }

        /**
         * Define cómo se mostrará la rutina dentro del ListView.
         * Tiene en cuenta si se repite varios días o todos los días.
         */
        @Override
        public String toString() {
            if (dia.equals("Todos los días")) {
                return "Todos los días: " + nombre;
            }

            String[] diasArray = dia.split(",");
            if (diasArray.length > 1) {
                return "Varios días: " + nombre;
            } else {
                return dia + ": " + nombre;
            }
        }
    }

    /**
     * Se ejecuta cuando se crea la vista del fragmento.
     * Inicializa los controles, carga el usuario y configura los filtros.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Se carga el layout del fragmento
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Inicialización de la base de datos
        dbHelper = new DBHelper(getContext());

        // Obtención del usuario desde la sesión
        SharedPreferences prefs = requireContext().getSharedPreferences("sesion", getContext().MODE_PRIVATE);
        usuarioId = prefs.getInt("usuario_id", -1);

        // Enlace con los elementos del layout
        listView = view.findViewById(R.id.listViewRutinas);
        spinnerFiltro = view.findViewById(R.id.spinnerFiltro);
        btnAgregarRutina = view.findViewById(R.id.btnAgregarRutina);

        // Configuración del spinner de filtros
        String[] opciones = {"Día actual", "Toda la semana", "Todos los días"};
        ArrayAdapter<String> filtroAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                opciones
        );
        filtroAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltro.setAdapter(filtroAdapter);

        // Cada vez que cambia el filtro se recarga la lista
        spinnerFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                cargarRutinasFiltradas(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Botón para crear una nueva rutina
        btnAgregarRutina.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddRutinaActivity.class);
            startActivity(intent);
        });

        // Al pulsar una rutina se abre su pantalla de detalle
        listView.setOnItemClickListener((parent, view12, position, id) -> {
            Rutina rutina = (Rutina) parent.getItemAtPosition(position);
            Intent intent = new Intent(getContext(), DetalleRutinaActivity.class);
            intent.putExtra("rutina_id", rutina.id);
            startActivity(intent);
        });

        return view;
    }

    /**
     * Cuando el usuario vuelve al fragmento se recargan las rutinas,
     * por ejemplo tras crear o editar una.
     */
    @Override
    public void onResume() {
        super.onResume();
        int filtroSeleccionado = spinnerFiltro.getSelectedItemPosition();
        cargarRutinasFiltradas(filtroSeleccionado);
    }

    /**
     * Obtiene de la base de datos las rutinas según el filtro aplicado.
     *
     * @param opcion 0 = día actual, 1 = toda la semana, 2 = solo "todos los días"
     */
    private void cargarRutinasFiltradas(int opcion) {

        ArrayList<Rutina> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query;
        String[] args;

        switch (opcion) {
            case 0: // Día actual
                String diaActual = new SimpleDateFormat("EEEE", new Locale("es", "ES")).format(new Date());
                diaActual = diaActual.substring(0, 1).toUpperCase() + diaActual.substring(1);

                query = "SELECT id, nombre, dia_semana FROM rutinas " +
                        "WHERE usuario_id = ? AND (dia_semana LIKE ? OR dia_semana = 'Todos los días')";
                args = new String[]{String.valueOf(usuarioId), "%" + diaActual + "%"};
                break;

            case 1: // Toda la semana
                query = "SELECT id, nombre, dia_semana FROM rutinas WHERE usuario_id = ?";
                args = new String[]{String.valueOf(usuarioId)};
                break;

            case 2: // Solo rutinas diarias
                query = "SELECT id, nombre, dia_semana FROM rutinas " +
                        "WHERE usuario_id = ? AND dia_semana = 'Todos los días'";
                args = new String[]{String.valueOf(usuarioId)};
                break;

            default:
                return;
        }

        Cursor cursor = db.rawQuery(query, args);
        while (cursor.moveToNext()) {
            lista.add(new Rutina(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2)
            ));
        }
        cursor.close();

        // Se muestra la lista de rutinas filtradas
        ArrayAdapter<Rutina> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                lista
        );
        listView.setAdapter(adapter);
    }
}
