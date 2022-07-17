package com.example.admin.augscan;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if(user != null){
            finish();
            startActivity(new Intent(this, dashboardActivity.class));
        }
    }

    public void login (View view) { startActivity(new Intent(this,LoginActivity.class)); }
    public void register (View view)
    {
        startActivity(new Intent(this,RegisterActivity.class));
    }
    public void atTenDanceTracking(View view) { startActivity(new Intent(this,attendancetrackingActivity.class)); }
}
