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
 * Fragmento que muestra el historial de rutinas completadas por el usuario.
 * Se conecta a la base de datos para obtener las rutinas finalizadas y las muestra en una lista.
 */
public class HistorialFragment extends Fragment {

    // Elemento visual que muestra el historial (una lista)
    ListView listViewHistorial;

    // Ayudante para manejar la base de datos SQLite
    DBHelper dbHelper;

    // ID del usuario actual, obtenido desde SharedPreferences
    int usuarioId;

    /**
     * Método que se llama cuando se crea la vista del fragmento
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Se infla (carga) el layout XML del fragmento
        View view = inflater.inflate(R.layout.fragment_historial, container, false);

        // Se referencia el ListView del layout
        listViewHistorial = view.findViewById(R.id.listViewHistorial);

        // Se crea una instancia del DBHelper para acceder a la base de datos
        dbHelper = new DBHelper(getContext());

        // Se obtienen las preferencias guardadas del usuario
        SharedPreferences prefs = requireContext().getSharedPreferences("sesion", getContext().MODE_PRIVATE);
        usuarioId = prefs.getInt("usuario_id", -1); // Se obtiene el ID del usuario

        // Se carga el historial desde la base de datos
        cargarHistorial();

        // Se retorna la vista ya configurada
        return view;
    }

    /**
     * Este método obtiene de la base de datos todas las rutinas completadas por el usuario.
     * Une la tabla de progreso con la de rutinas y ordena por fecha descendente.
     */
    private void cargarHistorial() {
        // Se abre la base de datos en modo lectura
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Lista que almacenará los elementos del historial
        ArrayList<String> historial = new ArrayList<>();

        // Consulta SQL que une las tablas de progreso y rutinas para mostrar nombre y fecha
        String query = "SELECT r.nombre, p.fecha FROM progreso p " +
                "JOIN rutinas r ON r.id = p.rutina_id " +
                "WHERE p.usuario_id = ? ORDER BY p.fecha DESC";

        // Se ejecuta la consulta usando el ID del usuario
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(usuarioId)});

        // Se recorre el cursor para obtener los datos y añadirlos a la lista
        while (cursor.moveToNext()) {
            String nombre = cursor.getString(0); // Nombre de la rutina
            String fecha = cursor.getString(1);  // Fecha de la rutina
            historial.add(fecha + " - " + nombre); // Se añade el elemento a la lista
        }

        // Se cierra el cursor
        cursor.close();

        // Se crea un adaptador para mostrar la lista en el ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, historial);

        // Se asigna el adaptador al ListView
        listViewHistorial.setAdapter(adapter);
    }
}
