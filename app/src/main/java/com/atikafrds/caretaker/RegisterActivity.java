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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText registerFullname, registerEmail, registerPassword, registerConfirmPassword,
        registerPhoneNumber;
    private RadioGroup registerRadioGroup;
    private Button registerButton;
    private TextView loginTextButton;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerFullname = (EditText) findViewById(R.id.registerFullname);
        registerEmail = (EditText) findViewById(R.id.registerEmail);
        registerPassword = (EditText) findViewById(R.id.registerPassword);
        registerConfirmPassword = (EditText) findViewById(R.id.registerConfirmPassword);
        registerPhoneNumber = (EditText) findViewById(R.id.registerPhoneNumber);

        registerRadioGroup = (RadioGroup) findViewById(R.id.registerRoleRadioGroup);

        registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(this);

        loginTextButton = (TextView) findViewById(R.id.loginTextButton);
        loginTextButton.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
    }

    @Override
    public void onClick(View view) {
        if (view == registerButton) {
            if (registerRadioGroup.getCheckedRadioButtonId() == R.id.registerDeviceUserRole) {
                registerUser(UserRole.DEVICE_USER);
            } else {
                registerUser(UserRole.CARETAKER);
            }
        }

        if (view == loginTextButton) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        }
    }

    private void registerUser(final UserRole userRole) {
        final String fullname = registerFullname.getText().toString();
        final String email = registerEmail.getText().toString();
        final String password = registerPassword.getText().toString();
        final String confirmPassword = registerConfirmPassword.getText().toString();
        final String phoneNumber = registerPhoneNumber.getText().toString();

        if (TextUtils.isEmpty(fullname)) {
            Toast.makeText(this, "Please enter full name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.equals(password, confirmPassword)) {
            Toast.makeText(this, "Confirm password does not match", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Registering user...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    if (userRole == UserRole.DEVICE_USER) {
                        databaseReference = FirebaseDatabase.getInstance().getReference("users");
                    } else {
                        databaseReference = FirebaseDatabase.getInstance().getReference("caretakers");
                    }
                    String key = databaseReference.push().getKey();
                    User user = new User(firebaseUser.getUid(), fullname, email, phoneNumber, "", 0, 0);
                    databaseReference.child(key).setValue(user);
                    if (userRole == UserRole.DEVICE_USER) {
                        Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                        intent.putExtra("currentUserId", firebaseUser.getUid());
                        intent.putExtra("partnerId", "");
                        intent.putExtra("userRole", userRole.toString());
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(getApplicationContext(), CaretakerActivity.class);
                        intent.putExtra("currentUserId", firebaseUser.getUid());
                        intent.putExtra("partnerId", "");
                        intent.putExtra("userRole", userRole.toString());
                        startActivity(intent);
                    }
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Could not register. Please try again", Toast.LENGTH_SHORT).show();
                }
                    }
            });
    }
}
