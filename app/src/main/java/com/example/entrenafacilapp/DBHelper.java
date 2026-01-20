package com.example.entrenafacilapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Clase encargada de la gestión de la base de datos SQLite.
 *
 * Define:
 *  - La estructura de todas las tablas.
 *  - Las relaciones entre entidades.
 *  - La evolución del esquema de la base de datos mediante control de versiones.
 */
public class DBHelper extends SQLiteOpenHelper {

    // Nombre del archivo físico de la base de datos
    public static final String DB_NAME = "entrenafacil.db";

    // Versión del esquema de la base de datos.
    // Se incrementa cada vez que se modifica la estructura de alguna tabla.
    public static final int DB_VERSION = 5;

    /**
     * Constructor del helper de base de datos.
     */
    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Se ejecuta automáticamente cuando la base de datos se crea por primera vez.
     * Aquí se definen todas las tablas y relaciones del sistema.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // ==========================
        // Tabla USUARIOS
        // ==========================
        // Almacena la información básica de cada usuario registrado en la aplicación.
        db.execSQL("CREATE TABLE usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " + // Identificador único
                "nombre TEXT, " +                         // Nombre de usuario
                "contrasena TEXT, " +                     // Contraseña cifrada o en texto
                "edad TEXT, " +                           // Edad del usuario
                "peso TEXT, " +                           // Peso corporal
                "altura TEXT, " +                         // Altura
                "sexo TEXT, " +                           // Sexo
                "foto_perfil TEXT)");                     // Ruta a la imagen de perfil

        // ==========================
        // Tabla RUTINAS
        // ==========================
        // Almacena las rutinas de entrenamiento creadas por los usuarios.
        // Incluye soporte para múltiples días y múltiples imágenes por rutina.
        db.execSQL("CREATE TABLE rutinas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "usuario_id INTEGER, " +                 // Usuario propietario de la rutina
                "nombre TEXT, " +                         // Nombre de la rutina
                "descripcion TEXT, " +                   // Descripción
                "tipo TEXT, " +                           // Tipo (fuerza, cardio, etc.)
                "duracion INTEGER, " +                   // Duración en minutos
                "dia_semana TEXT, " +                    // Días de la semana (separados por comas)
                "fotos_rutina TEXT, " +                  // Rutas de las imágenes asociadas
                "FOREIGN KEY(usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE)");
        // Si se elimina un usuario, se eliminan sus rutinas automáticamente

        // ==========================
        // Tabla PROGRESO
        // ==========================
        // Registra cuándo un usuario realiza una rutina concreta.
        db.execSQL("CREATE TABLE progreso (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "usuario_id INTEGER, " +                 // Usuario que realiza la rutina
                "rutina_id INTEGER, " +                  // Rutina realizada
                "fecha TEXT, " +                         // Fecha de realización
                "FOREIGN KEY(usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(rutina_id) REFERENCES rutinas(id) ON DELETE CASCADE)");
    }

    /**
     * Se ejecuta automáticamente cuando se incrementa la versión de la base de datos.
     * Permite actualizar el esquema sin perder los datos existentes.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // A partir de la versión 5 se añade soporte para almacenar imágenes en las rutinas
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE rutinas ADD COLUMN fotos_rutina TEXT");
        }
    }
}
