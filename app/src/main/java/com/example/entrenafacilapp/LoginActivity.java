package com.example.entrenafacilapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    // Declaramos los elementos de la interfaz
    EditText etUsuario, etContrasena;
    Button btnLogin, btnIrRegistro;

    // Referencia a la base de datos
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Cargamos el layout

        // Asociamos las variables con los elementos del layout
        etUsuario = findViewById(R.id.etUsuario);
        etContrasena = findViewById(R.id.etContrasena);
        btnLogin = findViewById(R.id.btnLogin);
        btnIrRegistro = findViewById(R.id.btnIrRegistro);

        // Instanciamos el helper de la base de datos
        dbHelper = new DBHelper(this);

        // Configuramos la acción del botón "Iniciar sesión"
        btnLogin.setOnClickListener(v -> login());

        // Botón para ir al registro
        btnIrRegistro.setOnClickListener(v -> {
            // Abrimos la pantalla de registro
            startActivity(new Intent(this, RegistroActivity.class));
        });
    }

    // Método que se llama al pulsar el botón de login
    private void login() {
        // Obtenemos los datos ingresados
        String usuario = etUsuario.getText().toString();
        String contrasena = etContrasena.getText().toString();

        // Accedemos a la base de datos en modo lectura
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Hacemos una consulta para buscar al usuario con esa contraseña
        Cursor cursor = db.rawQuery(
                "SELECT id FROM usuarios WHERE nombre=? AND contrasena=?",
                new String[]{usuario, contrasena}
        );

        // Si encontramos el usuario
        if (cursor.moveToFirst()) {
            int userId = cursor.getInt(0); // Obtenemos el ID del usuario

            // Guardamos el ID en SharedPreferences para mantener sesión iniciada
            SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
            prefs.edit().putInt("usuario_id", userId).apply();

            // Mostramos mensaje y vamos al menú principal
            Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish(); // Cerramos esta pantalla
        } else {
            // Si no se encuentra el usuario, mostramos error
            Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
        }

        cursor.close(); // Cerramos el cursor
    }
}
