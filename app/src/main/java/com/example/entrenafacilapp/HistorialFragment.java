package com.example.entrenafacilapp;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

/**
 * Fragmento encargado de mostrar el historial de entrenamiento del usuario.
 *
 * Este fragmento consulta la base de datos local para recuperar todas las rutinas
 * que el usuario ha marcado como completadas, junto con la fecha de realización.
 * Los datos se muestran en una lista, permitiendo al usuario consultar su progreso.
 */
public class HistorialFragment extends Fragment {

    // ListView donde se mostrará el historial
    ListView listViewHistorial;

    // Helper para acceder a la base de datos SQLite
    DBHelper dbHelper;

    // Identificador del usuario autenticado
    int usuarioId;

    /**
     * Se ejecuta cuando se crea la vista del fragmento.
     * Aquí se inicializan los componentes visuales y se cargan los datos.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Se carga el layout del fragmento
        View view = inflater.inflate(R.layout.fragment_historial, container, false);

        // Se obtiene la referencia al ListView
        listViewHistorial = view.findViewById(R.id.listViewHistorial);

        // Se inicializa el helper de base de datos
        dbHelper = new DBHelper(getContext());

        // Se recupera el usuario desde las preferencias de sesión
        SharedPreferences prefs = requireContext().getSharedPreferences("sesion", getContext().MODE_PRIVATE);
        usuarioId = prefs.getInt("usuario_id", -1);

        // Se cargan los datos del historial
        cargarHistorial();

        return view;
    }

    /**
     * Obtiene de la base de datos todas las rutinas realizadas por el usuario.
     *
     * Para ello se utiliza una consulta JOIN entre las tablas "progreso" y "rutinas",
     * lo que permite mostrar el nombre de la rutina junto con la fecha en que fue completada.
     */
    private void cargarHistorial() {

        // Se abre la base de datos en modo lectura
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Lista donde se almacenarán los resultados
        ArrayList<String> historial = new ArrayList<>();

        // Consulta SQL que relaciona progreso con rutinas
        String query = "SELECT r.nombre, p.fecha FROM progreso p " +
                "JOIN rutinas r ON r.id = p.rutina_id " +
                "WHERE p.usuario_id = ? ORDER BY p.fecha DESC";

        // Se ejecuta la consulta usando el ID del usuario
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(usuarioId)});

        // Se recorren los resultados y se formatea cada registro
        while (cursor.moveToNext()) {
            String nombre = cursor.getString(0);
            String fecha = cursor.getString(1);
            historial.add(fecha + " - " + nombre);
        }

        cursor.close();

        // Se crea un adaptador para mostrar el historial en el ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                historial
        );

        listViewHistorial.setAdapter(adapter);
    }
}
