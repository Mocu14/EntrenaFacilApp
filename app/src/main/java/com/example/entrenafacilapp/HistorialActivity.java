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
 * Activity encargada de mostrar el historial de entrenamientos del usuario.
 *
 * A partir de la tabla "progreso" se recuperan todas las rutinas que el usuario
 * ha marcado como completadas, junto con la fecha en la que fueron realizadas.
 *
 * Esta funcionalidad permite llevar un seguimiento real del entrenamiento,
 * lo que aporta valor funcional a la aplicación EntrenaFácil.
 */
public class HistorialActivity extends AppCompatActivity {

    // Helper de acceso a la base de datos
    DBHelper dbHelper;

    // Lista visual donde se muestra el historial
    ListView listViewHistorial;

    // Identificador del usuario logueado
    int usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        // Inicialización del helper de la base de datos
        dbHelper = new DBHelper(this);

        // Enlace con el ListView del layout
        listViewHistorial = findViewById(R.id.listViewHistorial);

        // Obtención del usuario desde la sesión
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        usuarioId = prefs.getInt("usuario_id", -1);

        // Carga del historial al iniciar la pantalla
        cargarHistorial();
    }

    /**
     * Recupera desde la base de datos todas las rutinas completadas
     * por el usuario actual y las muestra en formato de lista.
     *
     * Se realiza una consulta JOIN entre las tablas "progreso" y "rutinas"
     * para obtener el nombre de la rutina junto con la fecha de realización.
     */
    private void cargarHistorial() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Consulta SQL que obtiene el nombre de la rutina y la fecha de realización
        Cursor cursor = db.rawQuery(
                "SELECT rutinas.nombre, progreso.fecha FROM progreso " +
                        "JOIN rutinas ON progreso.rutina_id = rutinas.id " +
                        "WHERE progreso.usuario_id = ? " +
                        "ORDER BY progreso.fecha DESC",
                new String[]{String.valueOf(usuarioId)}
        );

        ArrayList<String> lista = new ArrayList<>();

        // Se recorren todos los resultados de la consulta
        while (cursor.moveToNext()) {
            String nombre = cursor.getString(0); // Nombre de la rutina
            String fecha = cursor.getString(1);  // Fecha de realización

            // Se construye un texto legible para el usuario
            lista.add(fecha + " - " + nombre);
        }

        cursor.close();

        // Adaptador simple para mostrar el historial en el ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                lista
        );

        listViewHistorial.setAdapter(adapter);
    }
}
