package com.yusudhanwinwin.winwin.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yusudhanwinwin.winwin.MainActivity;
import com.yusudhanwinwin.winwin.Models.UserModel;
import com.yusudhanwinwin.winwin.R;
import com.yusudhanwinwin.winwin.databinding.ActivitySignUpBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    ActivitySignUpBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseUser user;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //Objects.requireNonNull(getSupportActionBar()).hide();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        user = auth.getCurrentUser();

        dialog = new ProgressDialog(SignUpActivity.this);
        dialog.setTitle("Creating Account");
        dialog.setMessage("We are creating your account, please wait.");

        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();

                auth.createUserWithEmailAndPassword(binding.edtEmail.getText().toString(),binding.edtPassword.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                dialog.dismiss();
                                if (task.isSuccessful()) {
                                    FirebaseUser user = auth.getCurrentUser();

                                    String email = binding.edtEmail.getText().toString();
                                    String refer = email.substring(0, email.lastIndexOf("@"));
                                    String referCode = refer.replace(".", "");

                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("name", binding.edtName.getText().toString());
                                    map.put("mobileNumber", binding.edtMobileNumber.getText().toString());
                                    map.put("email", binding.edtEmail.getText().toString());
                                    map.put("password", binding.edtPassword.getText().toString());
                                    map.put("profile", "https://firebasestorage.googleapis.com/v0/b/win-win-6a323.appspot.com/o/user.png?alt=media&token=8204d8f0-0968-4430-95a8-007892275d19");
                                    map.put("referCode", referCode);
                                    map.put("coins", 20);
                                    map.put("spins", 4);

                                    Date date = Calendar.getInstance().getTime();
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(date);

                                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                                    Date previousDate = calendar.getTime();

                                    String dateString = dateFormat.format(previousDate);

                                    // Veritabanına verileri eklemek yerine önce verileri alıp sonra güncelleme işlemi yapalım
                                    database.getReference().child("Users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                // Veritabanında veri varsa, yeni verileri alalım ve güncelleme yapalım
                                                UserModel model = snapshot.getValue(UserModel.class);

                                                // Yeni verileri mevcut verilere ekleyelim
                                                map.put("coins", model.getCoins() + 20);
                                                map.put("spins", model.getSpins() + 4);

                                                // Veritabanını güncelleyelim
                                                database.getReference().child("Users").child(user.getUid()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            // Güncelleme başarılıysa, yeni bir tarih değeri ekleyelim
                                                            database.getReference().child("Daily Check").child(user.getUid()).child("date").setValue(dateString);
                                                            // MainActivity'ye yönlendirelim
                                                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                            Toast.makeText(SignUpActivity.this, "Created", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            // Güncelleme başarısızsa hata mesajı gösterelim
                                                            Toast.makeText(SignUpActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            } else {
                                                // Veritabanında veri yoksa, yeni verileri ekleyelim
                                                database.getReference().child("Users").child(user.getUid()).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            // Yeni bir tarih değeri ekleyelim
                                                            database.getReference().child("Daily Check").child(user.getUid()).child("date").setValue(dateString);
                                                            // MainActivity'ye yönlendirelim
                                                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                            Toast.makeText(SignUpActivity.this, "Created", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            // Verileri eklerken hata oluşursa hata mesajı gösterelim
                                                            Toast.makeText(SignUpActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(SignUpActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    // Oturum açarken hata oluştu
                                    dialog.dismiss();
                                    Toast.makeText(SignUpActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}