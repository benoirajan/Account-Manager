package ben.e;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ben.e.database.tables.Users;
import ben.e.settings.SettingsActivity;
import ben.e.ui.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    public static int NAV = R.id.transactions;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.transactions, R.id.category, R.id.accounts)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String uid = getSharedPreferences("", 0).getString(Users.UID, "fart");
        switch (item.getItemId()) {
            case R.id.category:
                startActivity(new Intent(this, AddEditCategory.class)
                        .putExtra(Users.UID, uid));
                break;
            case R.id.logout:
                getSharedPreferences("", 0).edit()
                        .putString(Users.UID, "")
                        .apply();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (NAV != R.id.transactions)
            navController.navigate(NAV);
    }
}