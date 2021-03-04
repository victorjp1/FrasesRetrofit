package cf.victorlopez.frasesretrofit.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.google.android.material.navigation.NavigationView;

import cf.victorlopez.frasesretrofit.R;
import cf.victorlopez.frasesretrofit.alarms.AlarmFraseManager;
import cf.victorlopez.frasesretrofit.db.UsuariosSQLiteHelper;
import cf.victorlopez.frasesretrofit.dialogs.DialogLogin;
import cf.victorlopez.frasesretrofit.enums.Modo;
import cf.victorlopez.frasesretrofit.exceptions.UserNotExistException;
import cf.victorlopez.frasesretrofit.fragments.FragmentAutor;
import cf.victorlopez.frasesretrofit.fragments.FragmentCategoria;
import cf.victorlopez.frasesretrofit.fragments.FragmentConsultar;
import cf.victorlopez.frasesretrofit.fragments.FragmentDetalle;
import cf.victorlopez.frasesretrofit.fragments.FragmentFrase;
import cf.victorlopez.frasesretrofit.models.Usuario;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DialogLogin.DialogListener {

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private TextView tvUsuarioNav;
    private UsuariosSQLiteHelper sqLiteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Si el usuario tiene la notificacion activada en preferencias
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean notificar = prefs.getBoolean("notificacion", true);
        if (notificar){
            AlarmFraseManager alarmManager = new AlarmFraseManager(this);
            //Si no existe ninguna alarma ponemos una
            if (!alarmManager.hasAlarm()){
                alarmManager.setAlarm();
            }
        }

        //Obtenemos DB usuarios
        sqLiteHelper = UsuariosSQLiteHelper.getInstance(getApplicationContext());

        //Mostramos la frase del día
        putFraseDelDia();

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //NavigationDrawer
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Navigation View
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        tvUsuarioNav = headerView.findViewById(R.id.tvUsuarioNav);

        Usuario user = getUser(savedInstanceState);

        putUsuario(user);

    }

    /**
     * Método que comprueba si existe un usuario
     * @param savedInstanceState Bundle que puede contener datos
     * @return Usuario en caso de que exista o null
     */
    public Usuario getUser(Bundle savedInstanceState){
        //Comprovamos si existe un usuario
        Usuario user = null;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                user = (Usuario)extras.getSerializable("usuario");
            }
        } else {
            user = (Usuario) savedInstanceState.getSerializable("usuario");
        }
        return user;
    }

    /**
     * Método para poner la información del usuario en el Navigation Drawer
     * @param user Usuario a evaluar
     */
    public void putUsuario(Usuario user){
        String noUser = "Anónimo";
        if (user != null){
            tvUsuarioNav.setText(user.getUsername());
            changeEnableNavigation(user.isAdmin());
        }else{
            changeEnableNavigation(false);
            tvUsuarioNav.setText(noUser);

        }
    }

    /**
     * Método que es ejecutado cuando se pulsa atrás cuando estamos en el menú
     */
    @Override
    public void onBackPressed() {
        //Si el usuario pulsa el botón atrás mientras está mostrándose el menú del NavigationView,
        //hacemos que se cierre dicho menú, ya que el comportamiento por defecto es cerrar la
        //Activity.
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Inflamos el xml de Toolbar
     * @param menu Menu al que inflarla
     * @return True si se ha podido inflar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflamos el menú de la ActionBar
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    /**
     * Método que se ejecuta cuando se selecciona una opción de la Toolbar
     * @param item Item pulsado
     * @return True si se ha ido bien
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Listener para la selección de opciones en el NavigationDrawer
     * @param item Item seleccionado
     * @return Si ha ido bien la operación devolvemos true
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment f = new Fragment();
        Bundle args = new Bundle();
        switch (item.getItemId()){
            case R.id.nav_login:
                DialogLogin dialog = new DialogLogin();
                dialog.show(getSupportFragmentManager(), "Diálogo login");
                break;
            case R.id.nav_frase_del_dia:
                f = new FragmentDetalle();
                args.putSerializable(FragmentDetalle.FRASE, null);
                break;
            case R.id.nav_consulta_frase:
                f = new FragmentConsultar();
                args.putSerializable(FragmentConsultar.BUSCAR_POR, FragmentConsultar.BUSCAR_FRASES);
                break;
            case R.id.nav_consulta_frase_autor:
                f = new FragmentConsultar();
                args.putString(FragmentConsultar.BUSCAR_POR, FragmentConsultar.BUSCAR_POR_AUTOR);
                break;
            case R.id.nav_consulta_frase_categoria:
                f = new FragmentConsultar();
                args.putString(FragmentConsultar.BUSCAR_POR, FragmentConsultar.BUSCAR_POR_CATEGORIA);
                break;
            case R.id.nav_add_frase:
                f = new FragmentFrase();
                args.putSerializable(Modo.CODE_MODE, Modo.ADD);
                break;
            case R.id.nav_add_categoria:
                f = new FragmentCategoria();
                args.putSerializable(Modo.CODE_MODE, Modo.ADD);
                break;
            case R.id.nav_add_autor:
                f = new FragmentAutor();
                args.putSerializable(Modo.CODE_MODE, Modo.ADD);
                break;
            case R.id.nav_modificar_categoria:
                f = new FragmentCategoria();
                args.putSerializable(Modo.CODE_MODE, Modo.UPDATE);
                break;
            case R.id.nav_modificar_frase:
                f = new FragmentFrase();
                args.putSerializable(Modo.CODE_MODE, Modo.UPDATE);
                break;
            case R.id.nav_modificar_autor:
                f = new FragmentAutor();
                args.putSerializable(Modo.CODE_MODE, Modo.UPDATE);
                break;
            case R.id.nav_eliminar_categoria:
                f = new FragmentCategoria();
                args.putSerializable(Modo.CODE_MODE, Modo.DELETE);
                break;
            case R.id.nav_eliminar_autor:
                f = new FragmentAutor();
                args.putSerializable(Modo.CODE_MODE, Modo.DELETE);
                break;
            case R.id.nav_eliminar_frase:
                f = new FragmentFrase();
                args.putSerializable(Modo.CODE_MODE, Modo.DELETE);
                break;
        }
        f.setArguments(args);
        //Borramos el fragment de detalle por si se ha viajado directamente desde el detalle
        //de una frase del recycler view de frases, frases por autor o frases por categoría
        getSupportFragmentManager().popBackStack("lista", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, f).commit();
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Método para habilitar o deshabilitar las opciones de admin del Navigation Drawer
     * @param visible Valor al cual deben estar
     */
    public void changeEnableNavigation(boolean visible){
        Menu menuNav = navigationView.getMenu();
        menuNav.findItem(R.id.nav_add_autor).setVisible(visible);
        menuNav.findItem(R.id.nav_add_frase).setVisible(visible);
        menuNav.findItem(R.id.nav_add_categoria).setVisible(visible);
        menuNav.findItem(R.id.nav_modificar_frase).setVisible(visible);
        menuNav.findItem(R.id.nav_modificar_autor).setVisible(visible);
        menuNav.findItem(R.id.nav_modificar_categoria).setVisible(visible);
        menuNav.findItem(R.id.nav_eliminar_categoria).setVisible(visible);
        menuNav.findItem(R.id.nav_eliminar_autor).setVisible(visible);
        menuNav.findItem(R.id.nav_eliminar_frase).setVisible(visible);
    }

    /**
     * Método que sustituye el fragment actual por el fragment Detalle con la frase del día
     */
    public void putFraseDelDia(){
        //Volvemos a mostrar la frase del día
        FragmentDetalle f = new FragmentDetalle();
        Bundle args = new Bundle();
        args.putSerializable(FragmentDetalle.FRASE, null);
        f.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,f).commit();
    }

    /**
     * Método de la interfaz que nos permite recibir información
     * del diálogo de Login
     * @param username Nombre de usuario
     * @param password Contraseña
     */
    @Override
    public void applyTexts(String username, String password) {
        if (username != null && password != null) {
            try {
                Usuario user = sqLiteHelper.getUsuario(username, password);
                putUsuario(user);
            } catch (UserNotExistException unee) {
                Toast.makeText(this, unee.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        putFraseDelDia();
    }
}