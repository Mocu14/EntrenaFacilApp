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

/**
 * Activity encargada del registro de nuevos usuarios.
 *
 * Permite al usuario introducir sus datos personales, elegir una foto de perfil
 * y guardar toda la información en la base de datos SQLite. Además, genera
 * rutinas predefinidas automáticamente para cada nuevo usuario.
 */
public class RegistroActivity extends AppCompatActivity {

    // Elementos del formulario de registro
    EditText etUsuario, etContrasena, etEdad, etPeso, etAltura;
    Spinner spinnerSexo;
    ImageView ivFotoPerfil;
    Button btnRegistrar;

    // Helper para acceso a la base de datos
    DBHelper dbHelper;

    // Variables relacionadas con la imagen de perfil
    Uri imagenSeleccionada;
    private static final int PICK_IMAGE = 100;
    String rutaLocalImagen = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Asociamos los elementos visuales con sus variables
        etUsuario = findViewById(R.id.etUsuario);
        etContrasena = findViewById(R.id.etContrasena);
        etEdad = findViewById(R.id.etEdad);
        etPeso = findViewById(R.id.etPeso);
        etAltura = findViewById(R.id.etAltura);
        spinnerSexo = findViewById(R.id.spinnerSexo);
        ivFotoPerfil = findViewById(R.id.ivFotoPerfil);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        // Inicializamos la base de datos
        dbHelper = new DBHelper(this);

        // Configuración del Spinner para seleccionar el sexo
        ArrayAdapter<String> adapterSexo = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Hombre", "Mujer", "Otro"});
        adapterSexo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSexo.setAdapter(adapterSexo);

        // Listener para seleccionar imagen de perfil desde galería
        ivFotoPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE); // Abrimos galería
        });

        // Listener para el botón de registrar usuario
        btnRegistrar.setOnClickListener(v -> registrarUsuario());
    }

    /**
     * Recibe el resultado de la selección de imagen desde la galería.
     *
     * @param requestCode Código de la petición
     * @param resultCode Resultado de la actividad
     * @param data Datos devueltos (URI de la imagen)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Comprobamos que se seleccionó correctamente una imagen
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && data != null) {
            imagenSeleccionada = data.getData(); // URI de la imagen
            ivFotoPerfil.setImageURI(imagenSeleccionada); // Mostramos la imagen

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

    /**
     * Copia la imagen seleccionada al almacenamiento interno de la app.
     *
     * @param uriOrigen URI de la imagen original
     * @return Ruta local de la imagen guardada, o null si hubo error
     */
    private String copiarImagenLocal(Uri uriOrigen) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uriOrigen);
            File archivoDestino = new File(getFilesDir(), "foto_perfil_" + System.currentTimeMillis() + ".jpg");
            OutputStream outputStream = new FileOutputStream(archivoDestino);

            // Copiamos la imagen en bloques
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            return archivoDestino.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Registra un nuevo usuario en la base de datos.
     * Valida los campos obligatorios y crea rutinas predefinidas.
     */
    private void registrarUsuario() {
        // Obtenemos los datos introducidos por el usuario
        String usuario = etUsuario.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();
        String edad = etEdad.getText().toString().trim();
        String peso = etPeso.getText().toString().trim();
        String altura = etAltura.getText().toString().trim();
        String sexo = spinnerSexo.getSelectedItem().toString();

        // Validación básica de campos obligatorios
        if (usuario.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Usuario y contraseña obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insertamos los datos en la tabla usuarios
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", usuario);
        values.put("contrasena", contrasena);
        values.put("edad", edad);
        values.put("peso", peso);
        values.put("altura", altura);
        values.put("sexo", sexo);
        values.put("foto_perfil", rutaLocalImagen);

        long id = db.insert("usuarios", null, values);

        // Si se insertó correctamente, añadimos rutinas predefinidas
        if (id != -1) {
            insertarRutinasPredefinidas((int) id);
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Error al registrar", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Inserta automáticamente rutinas básicas para cada usuario nuevo.
     *
     * @param usuarioId ID del usuario recién registrado
     */
    private void insertarRutinasPredefinidas(int usuarioId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Rutinas predefinidas por día de la semana
        insertarRutina(db, usuarioId, "Cardio", "30 min de cinta + bicicleta", "Resistencia", 45, "Lunes");
        insertarRutina(db, usuarioId, "Piernas", "Sentadillas, zancadas y prensa", "Fuerza", 60, "Martes");
        insertarRutina(db, usuarioId, "Espalda", "Dominadas, remo con barra", "Fuerza", 50, "Miércoles");
        insertarRutina(db, usuarioId, "Pecho", "Press banca, aperturas, fondos", "Fuerza", 55, "Jueves");
        insertarRutina(db, usuarioId, "Hombros", "Elevaciones laterales y press militar", "Fuerza", 50, "Viernes");
        insertarRutina(db, usuarioId, "Core", "Abdominales, plancha, giros rusos", "Estabilidad", 40, "Sábado");
        insertarRutina(db, usuarioId, "Full Body", "Circuito de cuerpo completo", "Mixto", 60, "Domingo");

        // Rutina diaria general
        insertarRutina(db, usuarioId, "Estiramientos", "Estiramiento general post-entreno", "Recuperación", 15, "Todos los días");
    }

    /**
     * Inserta una rutina individual en la base de datos.
     *
     * @param db Base de datos
     * @param usuarioId ID del usuario
     * @param nombre Nombre de la rutina
     * @param descripcion Descripción del ejercicio
     * @param tipo Tipo de rutina
     * @param duracion Duración en minutos
     * @param dia Día de la semana asignado
     */
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
