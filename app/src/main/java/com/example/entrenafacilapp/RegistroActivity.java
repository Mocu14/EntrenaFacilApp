package com.example.entrenafacilapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class RegistroActivity extends AppCompatActivity {

    // Elementos del formulario
    EditText etUsuario, etContrasena, etEdad, etPeso, etAltura;
    Spinner spinnerSexo;
    ImageView ivFotoPerfil;
    Button btnRegistrar;

    // Base de datos
    DBHelper dbHelper;

    // Variables para la imagen seleccionada
    Uri imagenSeleccionada;
    private static final int PICK_IMAGE = 100;
    String rutaLocalImagen = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Asociamos cada elemento del layout con su variable
        etUsuario = findViewById(R.id.etUsuario);
        etContrasena = findViewById(R.id.etContrasena);
        etEdad = findViewById(R.id.etEdad);
        etPeso = findViewById(R.id.etPeso);
        etAltura = findViewById(R.id.etAltura);
        spinnerSexo = findViewById(R.id.spinnerSexo);
        ivFotoPerfil = findViewById(R.id.ivFotoPerfil);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        dbHelper = new DBHelper(this); // Inicializamos la base de datos

        // Rellenamos el Spinner con opciones de sexo
        ArrayAdapter<String> adapterSexo = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Hombre", "Mujer", "Otro"});
        adapterSexo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSexo.setAdapter(adapterSexo);

        // Cuando se hace click en la imagen, abrimos la galería
        ivFotoPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE); // Código de respuesta
        });

        // Al pulsar el botón de registrar, llamamos al método
        btnRegistrar.setOnClickListener(v -> registrarUsuario());
    }

    // Este método recibe el resultado cuando se selecciona la imagen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Si todo fue bien y hay imagen
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && data != null) {
            imagenSeleccionada = data.getData(); // Obtenemos la URI de la imagen
            ivFotoPerfil.setImageURI(imagenSeleccionada); // Mostramos la imagen en pantalla

            // Guardamos la imagen en almacenamiento interno
            rutaLocalImagen = copiarImagenLocal(imagenSeleccionada);
            if (rutaLocalImagen != null) {
                Log.d("RutaImagen", "Guardada en: " + rutaLocalImagen);
                Toast.makeText(this, "Imagen guardada correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Este método guarda la imagen seleccionada en el almacenamiento interno
    private String copiarImagenLocal(Uri uriOrigen) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uriOrigen);
            File archivoDestino = new File(getFilesDir(), "foto_perfil_" + System.currentTimeMillis() + ".jpg");
            OutputStream outputStream = new FileOutputStream(archivoDestino);

            byte[] buffer = new byte[1024]; // Para copiar datos
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            return archivoDestino.getAbsolutePath(); // Devolvemos la ruta local
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Método que guarda el usuario en la base de datos
    private void registrarUsuario() {
        // Recogemos todos los datos del formulario
        String usuario = etUsuario.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();
        String edad = etEdad.getText().toString().trim();
        String peso = etPeso.getText().toString().trim();
        String altura = etAltura.getText().toString().trim();
        String sexo = spinnerSexo.getSelectedItem().toString();

        // Validamos campos obligatorios
        if (usuario.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Usuario y contraseña obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insertamos en la base de datos
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", usuario);
        values.put("contrasena", contrasena);
        values.put("edad", edad);
        values.put("peso", peso);
        values.put("altura", altura);
        values.put("sexo", sexo);
        values.put("foto_perfil", rutaLocalImagen); // Ruta de imagen guardada

        // Ejecutamos el insert
        long id = db.insert("usuarios", null, values);

        // Si se insertó correctamente, creamos rutinas por defecto
        if (id != -1) {
            insertarRutinasPredefinidas((int) id);
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Error al registrar", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para insertar rutinas básicas automáticamente
    private void insertarRutinasPredefinidas(int usuarioId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Insertamos una rutina por cada día de la semana
        insertarRutina(db, usuarioId, "Cardio", "30 min de cinta + bicicleta", "Resistencia", 45, "Lunes");
        insertarRutina(db, usuarioId, "Piernas", "Sentadillas, zancadas y prensa", "Fuerza", 60, "Martes");
        insertarRutina(db, usuarioId, "Espalda", "Dominadas, remo con barra", "Fuerza", 50, "Miércoles");
        insertarRutina(db, usuarioId, "Pecho", "Press banca, aperturas, fondos", "Fuerza", 55, "Jueves");
        insertarRutina(db, usuarioId, "Hombros", "Elevaciones laterales y press militar", "Fuerza", 50, "Viernes");
        insertarRutina(db, usuarioId, "Core", "Abdominales, plancha, giros rusos", "Estabilidad", 40, "Sábado");
        insertarRutina(db, usuarioId, "Full Body", "Circuito de cuerpo completo", "Mixto", 60, "Domingo");

        // Una rutina diaria general
        insertarRutina(db, usuarioId, "Estiramientos", "Estiramiento general post-entreno", "Recuperación", 15, "Todos los días");
    }

    // Método que inserta una rutina individual
    private void insertarRutina(SQLiteDatabase db, int usuarioId, String nombre, String descripcion, String tipo, int duracion, String dia) {
        ContentValues rutina = new ContentValues();
        rutina.put("usuario_id", usuarioId);
        rutina.put("nombre", nombre);
        rutina.put("descripcion", descripcion);
        rutina.put("tipo", tipo);
        rutina.put("duracion", duracion);
        rutina.put("dia_semana", dia);
        db.insert("rutinas", null, rutina); // Insertamos en la tabla
    }
}
