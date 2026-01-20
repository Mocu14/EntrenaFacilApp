package com.example.entrenafacilapp;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity encargada de mostrar la información del perfil del usuario.
 *
 * Obtiene los datos del usuario autenticado desde la base de datos
 * y los muestra en la interfaz de usuario mediante TextViews.
 */
public class PerfilActivity extends AppCompatActivity {

    // TextViews para mostrar los datos del usuario
    TextView tvEdad, tvPeso, tvAltura, tvSexo;

    // Helper para acceso a la base de datos
    DBHelper dbHelper;

    // ID del usuario actual, obtenido de la sesión activa
    int usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);                 // Llamada al método padre
        setContentView(R.layout.activity_perfil);           // Carga del layout visual

        // Enlazamos variables con los elementos del layout
        tvEdad = findViewById(R.id.tvEdad);
        tvPeso = findViewById(R.id.tvPeso);
        tvAltura = findViewById(R.id.tvAltura);
        tvSexo = findViewById(R.id.tvSexo);

        // Inicializamos el helper de la base de datos
        dbHelper = new DBHelper(this);

        // Obtenemos el ID del usuario actualmente logueado desde SharedPreferences
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        usuarioId = prefs.getInt("usuario_id", -1); // -1 si no hay sesión activa

        // Cargamos los datos del usuario desde la base de datos y los mostramos
        cargarDatosUsuario();
    }

    /**
     * Obtiene los datos del usuario desde la base de datos SQLite
     * y los asigna a los TextViews correspondientes.
     */
    private void cargarDatosUsuario() {
        // Abrimos la base de datos en modo lectura
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Consulta SQL para obtener edad, peso, altura y sexo del usuario
        Cursor cursor = db.rawQuery(
                "SELECT edad, peso, altura, sexo FROM usuarios WHERE id = ?",
                new String[]{String.valueOf(usuarioId)} // Sustituye el ? por el ID
        );

        // Si existen datos del usuario
        if (cursor.moveToFirst()) {
            // Asignamos los valores obtenidos a los TextViews
            tvEdad.setText("Edad: " + cursor.getInt(0));
            tvPeso.setText("Peso: " + cursor.getDouble(1) + " kg");
            tvAltura.setText("Altura: " + cursor.getDouble(2) + " cm");
            tvSexo.setText("Sexo: " + cursor.getString(3));
        }

        // Cerramos el cursor para liberar recursos de memoria
        cursor.close();
    }
}
