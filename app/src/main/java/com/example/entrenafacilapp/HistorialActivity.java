package com.example.entrenafacilapp;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * Actividad que muestra el historial de rutinas completadas por el usuario.
 * Extrae la información de la tabla progreso y la muestra en una lista.
 */
public class HistorialActivity extends AppCompatActivity {

    // Instancia del helper para la base de datos
    DBHelper dbHelper;

    // Lista para mostrar los datos del historial
    ListView listViewHistorial;

    // ID del usuario actual (obtenido desde SharedPreferences)
    int usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial); // Vincula con el layout correspondiente

        // Inicializar la base de datos
        dbHelper = new DBHelper(this);

        // Enlazar la vista del ListView
        listViewHistorial = findViewById(R.id.listViewHistorial);

        // Obtener el ID del usuario desde las preferencias guardadas
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        usuarioId = prefs.getInt("usuario_id", -1);

        // Cargar los datos del historial en la interfaz
        cargarHistorial();
    }

    /**
     * Consulta en la base de datos el historial de rutinas completadas
     * por el usuario y lo muestra en la lista.
     */
    private void cargarHistorial() {
        // Obtener base de datos en modo lectura
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Consulta SQL que une progreso con rutinas para obtener el nombre y la fecha
        Cursor cursor = db.rawQuery(
                "SELECT rutinas.nombre, progreso.fecha FROM progreso " +
                        "JOIN rutinas ON progreso.rutina_id = rutinas.id " +
                        "WHERE progreso.usuario_id = ? " +
                        "ORDER BY progreso.fecha DESC", // Ordena por fecha descendente
                new String[]{String.valueOf(usuarioId)}
        );

        // Lista para almacenar los elementos que se mostrarán
        ArrayList<String> lista = new ArrayList<>();

        // Recorrer los resultados del cursor y formar las líneas de texto
        while (cursor.moveToNext()) {
            String nombre = cursor.getString(0); // Nombre de la rutina
            String fecha = cursor.getString(1);  // Fecha de realización
            lista.add(fecha + " - " + nombre);   // Formato: "2024-05-27 - Rutina Piernas"
        }

        cursor.close(); // Cerrar cursor para liberar recursos

        // Crear un adaptador para mostrar la lista en el ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, lista);

        // Establecer el adaptador en el ListView
        listViewHistorial.setAdapter(adapter);
    }
}
