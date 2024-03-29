package com.dataflair.librarymanagementapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.dataflair.librarymanagementapp.MainActivity;
import com.dataflair.librarymanagementapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.NotNull;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    GoogleSignInClient mSignInClient;
    FirebaseAuth firebaseAuth;
    ProgressDialog progressBar;
    Button signInButton;
    Spinner spinner;

    String[] roles = {"Admin", "User"};
    String userRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();

        //Progress bar
        progressBar = new ProgressDialog(this);
        progressBar.setTitle("Please Wait...");
        progressBar.setMessage("We are setting Everything for you...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);


        spinner = (Spinner) findViewById(R.id.Spinner);
        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                userRole = roles[i];
                Toast.makeText(getApplicationContext(), roles[i], Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        signInButton = (Button) findViewById(R.id.GoogleSignInBtn);


        //Google Signin Options to get gmail and performa gmail login
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("123453474054-j42f04uq7rnuqqolvvf9hcpo5tmlsr4v.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mSignInClient = GoogleSignIn.getClient(getApplicationContext(), googleSignInOptions);


        //Implementing OnClickListener to perform Login action
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Showing all Gmails
                Intent intent = mSignInClient.getSignInIntent();
                startActivityForResult(intent, 100);

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            Task<GoogleSignInAccount> googleSignInAccountTask = GoogleSignIn
                    .getSignedInAccountFromIntent(data);

            if (googleSignInAccountTask.isSuccessful()) {
                progressBar.show();
                try {
                    GoogleSignInAccount googleSignInAccount = googleSignInAccountTask.getResult(ApiException.class);

                    if (googleSignInAccount != null) {
                        AuthCredential authCredential = GoogleAuthProvider
                                .getCredential(googleSignInAccount.getIdToken(), null);

                        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    //Hashmap to store the userdetails and setting it to fireabse
                                    HashMap<String, Object> user_details = new HashMap<>();

                                    //Accessing the user details from gmail
                                    String id = googleSignInAccount.getId().toString();
                                    String name = googleSignInAccount.getDisplayName().toString();
                                    String mail = googleSignInAccount.getEmail().toString();
                                    String pic = googleSignInAccount.getPhotoUrl().toString();

                                    //storing data in hashmap
                                    user_details.put("id", id);
                                    user_details.put("name", name);
                                    user_details.put("mail", mail);
                                    user_details.put("profilepic", pic);
                                    user_details.put("role", userRole);

                                    //Adding data to firebase
                                    FirebaseDatabase.getInstance().getReference().child("AllUsers").child(id)
                                            .updateChildren(user_details).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                progressBar.cancel();
                                                if (userRole.equals("Admin")) {
                                                    //navigating to the main activity after user successfully registers
                                                    Intent intent = new Intent(getApplicationContext(), AdminActivity.class);
                                                    //Clears older activities and tasks
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                } else {
                                                    //navigating to the main activity after user successfully registers
                                                    Intent intent = new Intent(getApplicationContext(), UserDetailsActivity.class);
                                                    //Clears older activities and tasks
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }

                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }

    }


}