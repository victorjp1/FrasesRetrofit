package cf.victorlopez.frasesretrofit.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cf.victorlopez.frasesretrofit.exceptions.UserNotExistException;
import cf.victorlopez.frasesretrofit.models.Usuario;

/**
 * Copyright 2019 See AUTHORS file.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * PruebaSQLite
 *
 * @author Germán Gascón
 * @version 0.1, 2019-01-28
 * @since 0.1
 **/

public class UsuariosSQLiteHelper extends SQLiteOpenHelper {

    private static UsuariosSQLiteHelper sInstance;
    private static final String DB_NAME = "Usuarios.db";
    private static final int DB_VERSION = 1;

    String sqlCreate = "CREATE TABLE usuarios (username TEXT PRIMARY KEY, " +
            "password TEXT NOT null, admin INTEGER NOT null);";


    public static synchronized UsuariosSQLiteHelper getInstance(Context context) {
        if(sInstance == null) {
            // Usamos el contexto de la aplicación para asegurarnos que no se pierde
            // el contexto, por ejemplo de una Activity.
            sInstance = new UsuariosSQLiteHelper(context.getApplicationContext());
        }
        return  sInstance;
    }

    //Definimos el constructor privado para asegurarnos que no lo utilice nadie desde fuera
    //Así forzamos a utilizar getInstance()
    private UsuariosSQLiteHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String password = Hash.getHash("admin");
        String sqlInsert = "INSERT INTO usuarios (username, password, admin) " +
                "VALUES ('admin',?, 1);";
        //Sólo se ejecuta si la base de datos no existe
        db.execSQL(sqlCreate);
        db.execSQL(sqlInsert,new String[]{password});
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Aquí irán las sentencias de actualización de la base de datos
    }

    /**
     * Método para obtener un usuario
     * @param username Nombre de usuario
     * @param password Contraseña
     * @return Usuario si lo encuentra
     */
    public Usuario getUsuario(String username, String password) throws UserNotExistException {
        SQLiteDatabase db = getReadableDatabase();
        String pEncoded = Hash.getHash(password);
        String query = "SELECT username, password, admin FROM usuarios WHERE username=? AND password=?";
        Cursor c = db.rawQuery(query, new String[]{username, pEncoded});
        try{
            c.moveToFirst();
            return new Usuario(c.getString(0), c.getString(1), c.getInt(2) > 0);
        }catch (CursorIndexOutOfBoundsException ciobe){
            throw new UserNotExistException("El nombre de usuario o contraseña son incorretos");
        }
    }


    // A partir de aquí no uso estos métodos, lo he dejado pensando que puede llegar a ser útil


    /**
     * Método que obtiene todos los usuarios de la DB
     * @return Array de usuarios
     */
    public Usuario[] getUsuarios() {
        Usuario[] usuarios = null;
        int i = 0;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT username, password FROM usuarios", null);
        if(c.moveToFirst()) {
            usuarios  = new Usuario[c.getCount()];
            do {
                String username = c.getString(0);
                String password = c.getString(1);
                Boolean admin = c.getInt(2) > 0;
                usuarios[i] = new Usuario(username, password, admin);
                i++;
            } while(c.moveToNext());
        }
        return usuarios;
    }

    public boolean deleteUsuario(String username) {
        SQLiteDatabase db = getReadableDatabase();
        String[] args = new String[] {username};
        return db.delete("usuarios", "username=?", args) == 1;
    }

    /**
     * Método para añadir un usuario
     * @param username Nombre del usuario
     * @param password Contraseña del usuario
     * @param admin Determina si tiene permisos de administrador o no
     * @return true si se ha creado correctamente, false si hubo algún error
     */
    public boolean addUsuario(String username, String password, boolean admin) {
        SQLiteDatabase db = getWritableDatabase();
        int administrador = admin ? 0 : 1;
        String pEncoded = Hash.getHash(password);
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", pEncoded);
        values.put("admin", administrador);
        //El método insert devuelve el identificador de la fila insertada o
        //-1 en caso de que se haya producido un error
        return db.insert("usuarios",null, values) != -1;
    }

    /**
     * Método para modificar el usuario
     * @param username Usuario a modificar
     * @param password Contraseña a modificar
     * @param admin Permisos de administrador
     * @return devuelve un boolean
     */
    public boolean updateUsuario(String username, String password, boolean admin) {
        SQLiteDatabase db = getWritableDatabase();
        String pEncoded = Hash.getHash(password);
        String[] args = new String[] {username};
        ContentValues values = new ContentValues();
        values.put("password", pEncoded);
        values.put("admin", admin);
        return db.update("usuarios", values, "username=?", args) == 1;
    }
}

