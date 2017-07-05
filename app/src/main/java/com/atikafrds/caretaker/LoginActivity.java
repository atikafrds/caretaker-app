package com.atikafrds.caretaker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = LoginActivity.class.getSimpleName();

    private EditText loginEmail, loginPassword;
    private RadioGroup loginRadioGroup;
    private Button loginButton;
    private TextView registerTextButton;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    public static final String role = "userRole";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmail = (EditText) findViewById(R.id.loginEmail);
        loginPassword = (EditText) findViewById(R.id.loginPassword);

        loginRadioGroup = (RadioGroup) findViewById(R.id.loginRoleRadioGroup);

        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);

        registerTextButton = (TextView) findViewById(R.id.registerTextButton);
        registerTextButton.setOnClickListener(this);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
    }

    @Override
    public void onClick(View view) {
        if (view == loginButton) {
            if (loginRadioGroup.getCheckedRadioButtonId() == R.id.loginDeviceUserRole) {
                userLogin(UserRole.DEVICE_USER);
            } else {
                userLogin(UserRole.CARETAKER);
            }
        }

        if (view == registerTextButton) {
            startActivity(new Intent(this, RegisterActivity.class));
        }
    }

    private void userLogin(final UserRole userRole) {
        final String email = loginEmail.getText().toString().trim();
        final String password = loginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
            progressDialog.dismiss();

            if (task.isSuccessful()) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    if (userRole == UserRole.DEVICE_USER) {
                        databaseReference = FirebaseDatabase.getInstance().getReference("users");
                    } else {
                        databaseReference = FirebaseDatabase.getInstance().getReference("caretakers");
                    }
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            boolean found = false;
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                if (data.child("id").getValue().toString().equals(user.getUid())) {
                                    found = true;
                                    break;
                                }
                            }

                            if (found) {
                                if (userRole == UserRole.DEVICE_USER) {
                                    startActivity(new Intent(getApplicationContext(), CaretakerActivity.class));
                                } else {
                                    startActivity(new Intent(getApplicationContext(), CaretakerActivity.class));
                                }
                                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString(role, userRole.toString());
                                editor.commit();
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Account is not found", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, "Failed to read database.", databaseError.toException());
                        }
                    });
                }
            } else {
                Toast.makeText(LoginActivity.this, "Log in failed", Toast.LENGTH_SHORT).show();
            }
            }
        });
    }
}