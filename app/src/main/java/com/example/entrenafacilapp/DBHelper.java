package com.example.entrenafacilapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "entrenafacil.db";
    public static final int DB_VERSION = 5;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabla de usuarios
        db.execSQL("CREATE TABLE usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, " +
                "contrasena TEXT, " +
                "edad TEXT, " +
                "peso TEXT, " +
                "altura TEXT, " +
                "sexo TEXT, " +
                "foto_perfil TEXT)");

        // Tabla de rutinas
        db.execSQL("CREATE TABLE rutinas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "usuario_id INTEGER, " +
                "nombre TEXT, " +
                "descripcion TEXT, " +
                "tipo TEXT, " +
                "duracion INTEGER, " +
                "dia_semana TEXT, " +
                "fotos_rutina TEXT, " +
                "FOREIGN KEY(usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE)");

        // Tabla de progreso
        db.execSQL("CREATE TABLE progreso (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "usuario_id INTEGER, " +
                "rutina_id INTEGER, " +
                "fecha TEXT, " +
                "FOREIGN KEY(usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(rutina_id) REFERENCES rutinas(id) ON DELETE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE rutinas ADD COLUMN fotos_rutina TEXT");
        }
    }
}
