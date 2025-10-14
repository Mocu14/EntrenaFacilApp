package com.example.entrenafacilapp;

import android.content.SharedPreferences; // Para acceder a los datos de sesión guardados
import android.database.Cursor;           // Para manejar resultados de consultas
import android.database.sqlite.SQLiteDatabase; // Para usar la base de datos SQLite
import android.os.Bundle;
import android.widget.TextView;           // Para mostrar datos al usuario

import androidx.appcompat.app.AppCompatActivity;

public class PerfilActivity extends AppCompatActivity {

    // Declaramos los TextView para mostrar los datos del usuario
    TextView tvEdad, tvPeso, tvAltura, tvSexo;

    // Objeto para acceder a la base de datos
    DBHelper dbHelper;

    // Variable que guardará el ID del usuario actual
    int usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);                 // Llamamos al método padre
        setContentView(R.layout.activity_perfil);           // Cargamos el layout con los elementos visuales

        // Enlazamos las variables con los elementos del layout
        tvEdad = findViewById(R.id.tvEdad);
        tvPeso = findViewById(R.id.tvPeso);
        tvAltura = findViewById(R.id.tvAltura);
        tvSexo = findViewById(R.id.tvSexo);

        // Inicializamos la base de datos
        dbHelper = new DBHelper(this);

        // Obtenemos el ID del usuario guardado en las preferencias (sesión activa)
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        usuarioId = prefs.getInt("usuario_id", -1); // -1 si no hay usuario guardado

        // Llamamos al método que carga los datos desde la base de datos
        cargarDatosUsuario();
    }

    // Este método obtiene los datos del usuario desde SQLite y los muestra
    private void cargarDatosUsuario() {
        // Abrimos la base de datos en modo lectura
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Ejecutamos una consulta SQL para obtener los campos de ese usuario
        Cursor cursor = db.rawQuery(
                "SELECT edad, peso, altura, sexo FROM usuarios WHERE id = ?",
                new String[]{String.valueOf(usuarioId)} // Reemplaza el ? por el ID
        );

        // Si hay datos (es decir, si encontró al usuario)
        if (cursor.moveToFirst()) {
            // Asignamos los valores a los TextView
            tvEdad.setText("Edad: " + cursor.getInt(0));
            tvPeso.setText("Peso: " + cursor.getDouble(1) + " kg");
            tvAltura.setText("Altura: " + cursor.getDouble(2) + " cm");
            tvSexo.setText("Sexo: " + cursor.getString(3));
        }

        // Cerramos el cursor para liberar recursos
        cursor.close();
    }
}
