package com.example.entrenafacilapp;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;


public class HistorialActivity extends AppCompatActivity {


    DBHelper dbHelper;


    ListView listViewHistorial;


    int usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);


        dbHelper = new DBHelper(this);


        listViewHistorial = findViewById(R.id.listViewHistorial);


        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        usuarioId = prefs.getInt("usuario_id", -1);


        cargarHistorial();
    }


    private void cargarHistorial() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();


        Cursor cursor = db.rawQuery(
                "SELECT rutinas.nombre, progreso.fecha FROM progreso " +
                        "JOIN rutinas ON progreso.rutina_id = rutinas.id " +
                        "WHERE progreso.usuario_id = ? " +
                        "ORDER BY progreso.fecha DESC",
                new String[]{String.valueOf(usuarioId)}
        );


        ArrayList<String> lista = new ArrayList<>();


        while (cursor.moveToNext()) {
            String nombre = cursor.getString(0); // Nombre de la rutina
            String fecha = cursor.getString(1);  // Fecha de realización
            lista.add(fecha + " - " + nombre);   // Formato: "2024-05-27 - Rutina Piernas"
        }

        cursor.close();


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, lista);


        listViewHistorial.setAdapter(adapter);
    }
}
