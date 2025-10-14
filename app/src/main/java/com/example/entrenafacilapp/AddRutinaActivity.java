package com.example.entrenafacilapp;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddRutinaActivity extends AppCompatActivity {

    // Declaración de variables de los elementos de la vista
    EditText etNombre, etDescripcion, etTipo, etDuracion;
    Spinner spinnerDia;
    Button btnGuardar;

    // Instancia del helper para base de datos
    DBHelper dbHelper;

    // Variables de control
    int rutinaId = -1;
    boolean modoEdicion = false;
    int usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rutina); // Establece el layout de la actividad

        // Se inicializa el helper de base de datos
        dbHelper = new DBHelper(this);

        // Se obtiene el ID del usuario que inició sesión previamente
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        usuarioId = prefs.getInt("usuario_id", -1);

        // Se enlazan los elementos del XML con las variables
        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        etTipo = findViewById(R.id.etTipo);
        etDuracion = findViewById(R.id.etDuracion);
        spinnerDia = findViewById(R.id.spinnerDia);
        btnGuardar = findViewById(R.id.btnGuardar);

        // Se crea el array con los días de la semana para el Spinner
        String[] dias = {"Todos los días", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDia.setAdapter(adapter); // Se asigna el adaptador al Spinner

        // Se comprueba si la actividad se abrió en modo edición
        modoEdicion = getIntent().getBooleanExtra("modo_edicion", false);
        if (modoEdicion) {
            rutinaId = getIntent().getIntExtra("rutina_id", -1);
            cargarDatosRutina(rutinaId); // Si es edición, se cargan los datos existentes
        }

        // Se establece el evento al hacer clic en el botón guardar
        btnGuardar.setOnClickListener(view -> guardarRutina());
    }

    // Método que carga los datos de una rutina existente para editarlos
    private void cargarDatosRutina(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nombre, descripcion, tipo, duracion, dia_semana FROM rutinas WHERE id = ?", new String[]{String.valueOf(id)});

        if (cursor.moveToFirst()) {
            etNombre.setText(cursor.getString(0));
            etDescripcion.setText(cursor.getString(1));
            etTipo.setText(cursor.getString(2));
            etDuracion.setText(String.valueOf(cursor.getInt(3)));

            // Seleccionar en el spinner el día correspondiente
            String dia = cursor.getString(4);
            for (int i = 0; i < spinnerDia.getCount(); i++) {
                if (spinnerDia.getItemAtPosition(i).toString().equalsIgnoreCase(dia)) {
                    spinnerDia.setSelection(i);
                    break;
                }
            }
        }
        cursor.close(); // Cerramos el cursor
    }

    // Método para guardar o actualizar una rutina en la base de datos
    private void guardarRutina() {
        // Se recogen los datos de los campos
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String tipo = etTipo.getText().toString().trim();
        String dia = spinnerDia.getSelectedItem().toString();
        int duracion;

        // Se intenta convertir la duración a entero
        try {
            duracion = Integer.parseInt(etDuracion.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Duración inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación de campos obligatorios
        if (nombre.isEmpty() || tipo.isEmpty()) {
            Toast.makeText(this, "Faltan campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Se prepara la base de datos y los datos a insertar
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("usuario_id", usuarioId);
        values.put("nombre", nombre);
        values.put("descripcion", descripcion);
        values.put("tipo", tipo);
        values.put("duracion", duracion);
        values.put("dia_semana", dia);

        // Si estamos editando, actualizamos la rutina
        if (modoEdicion) {
            int filas = db.update("rutinas", values, "id = ?", new String[]{String.valueOf(rutinaId)});
            if (filas > 0) {
                Toast.makeText(this, "Rutina actualizada", Toast.LENGTH_SHORT).show();
                finish(); // Cerramos la actividad
            } else {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Si no es edición, se guarda como nueva
            long result = db.insert("rutinas", null, values);
            if (result != -1) {
                Toast.makeText(this, "Rutina guardada", Toast.LENGTH_SHORT).show();
                finish(); // Cerramos la actividad
            } else {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
