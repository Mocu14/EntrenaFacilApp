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

/**
 * Activity encargada de la autenticación de usuarios.
 *
 * Permite al usuario introducir sus credenciales, validarlas contra la base
 * de datos local y crear una sesión persistente mediante SharedPreferences.
 */
public class LoginActivity extends AppCompatActivity {

    // Campos de entrada y botones de la interfaz
    EditText etUsuario, etContrasena;
    Button btnLogin, btnIrRegistro;

    // Helper para acceso a la base de datos
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Enlace entre variables y elementos del layout
        etUsuario = findViewById(R.id.etUsuario);
        etContrasena = findViewById(R.id.etContrasena);
        btnLogin = findViewById(R.id.btnLogin);
        btnIrRegistro = findViewById(R.id.btnIrRegistro);

        // Inicialización del helper de la base de datos
        dbHelper = new DBHelper(this);

        // Acción del botón de inicio de sesión
        btnLogin.setOnClickListener(v -> login());

        // Botón que redirige al formulario de registro
        btnIrRegistro.setOnClickListener(v -> {
            startActivity(new Intent(this, RegistroActivity.class));
        });
    }

    /**
     * Realiza el proceso de autenticación del usuario.
     *
     * Comprueba que las credenciales introducidas existen en la base de datos.
     * Si son válidas, se guarda el ID del usuario en SharedPreferences
     * para mantener la sesión activa durante el uso de la aplicación.
     */
    private void login() {

        String usuario = etUsuario.getText().toString();
        String contrasena = etContrasena.getText().toString();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id FROM usuarios WHERE nombre=? AND contrasena=?",
                new String[]{usuario, contrasena}
        );

        if (cursor.moveToFirst()) {
            int userId = cursor.getInt(0);

            // Se guarda el ID del usuario en la sesión
            SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
            prefs.edit().putInt("usuario_id", userId).apply();

            Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show();

            // Se accede al menú principal de la aplicación
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
    }
}
