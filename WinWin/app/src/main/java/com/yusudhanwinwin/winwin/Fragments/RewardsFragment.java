package com.yusudhanwinwin.winwin.Fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yusudhanwinwin.winwin.Activities.TrHistoryActivity;
import com.yusudhanwinwin.winwin.Models.UserModel;
import com.yusudhanwinwin.winwin.R;
import com.yusudhanwinwin.winwin.databinding.FragmentRewardsBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class RewardsFragment extends Fragment {

    FragmentRewardsBinding binding;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference reference;
    int currentCoin;
    Dialog dialog;
    ImageView withdLogo;
    TextView withMethods;
    EditText edtPaymentDetails,edtCoins;
    AppCompatButton btnRedeem, btnCancel;
    public RewardsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentRewardsBinding.inflate(inflater, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();

        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.payment_dialog);

        btnRedeem = dialog.findViewById(R.id.btnRedeem);
        btnCancel = dialog.findViewById(R.id.btnCancel);
        edtPaymentDetails = dialog.findViewById(R.id.edtPaymentDetails);
        edtCoins = dialog.findViewById(R.id.edtCoins);
        withdLogo = dialog.findViewById(R.id.withdrawalMethodLogo);
        withMethods = dialog.findViewById(R.id.withdrawalMethod);

        if(dialog.getWindow() != null){
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setCancelable(false);
        }

        binding.paytmRedeem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                withdLogo.setImageResource(R.drawable.paytm);
                withMethods.setText("Paytm");

                dialog.show();
            }
        });

        binding.btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getContext(), TrHistoryActivity.class);
                startActivity(intent);
            }
        });

        binding.amazonRedeem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                withdLogo.setImageResource(R.drawable.amazon);
                withMethods.setText("Amazon Gift");

                dialog.show();
            }
        });

        binding.payPalRedeem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                withdLogo.setImageResource(R.drawable.paypal);
                withMethods.setText("PayPal");

                dialog.show();
            }
        });

        binding.googlePlayRedeem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                withdLogo.setImageResource(R.drawable.googleplay);
                withMethods.setText("Google Pay");

                dialog.show();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnRedeem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String paymentMethods = withMethods.getText().toString();

                redeem(paymentMethods);
            }
        });

        reference.child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel model = snapshot.getValue(UserModel.class);
                binding.progressBig.setProgress(model.getCoins());
                binding.currentCoin.setText(String.valueOf(model.getCoins()));
                binding.currentCoin2.setText(String.valueOf(model.getCoins()));

                currentCoin = Integer.parseInt(String.valueOf(model.getCoins()));
                int requiredCoin = 5000 - currentCoin;

                binding.paytmProg.setProgress(model.getCoins());
                binding.amazonProg.setProgress(model.getCoins());
                binding.paypalProg.setProgress(model.getCoins());
                binding.googlePlayProg.setProgress(model.getCoins());

                if(currentCoin >= 5000){
                    binding.needTexPaytm.setText("Completed");
                    binding.needTexAmazon.setText("Completed");
                    binding.needTexPaypal.setText("Completed");
                    binding.needTexGooglePlay.setText("Completed");

                    binding.needCoinPaytm.setVisibility(View.GONE);
                    binding.needCoinPaypal.setVisibility(View.GONE);
                    binding.needCoinAmazon.setVisibility(View.GONE);
                    binding.needCoinGooglePlay.setVisibility(View.GONE);

                    binding.amazonRedeem.setEnabled(true);
                    binding.payPalRedeem.setEnabled(true);
                    binding.googlePlayRedeem.setEnabled(true);
                    binding.paytmRedeem.setEnabled(true);

                }else{

                    binding.amazonRedeem.setEnabled(false);
                    binding.payPalRedeem.setEnabled(false);
                    binding.googlePlayRedeem.setEnabled(false);
                    binding.paytmRedeem.setEnabled(false);

                    binding.needCoinPaytm.setText(requiredCoin + "");
                    binding.needCoinAmazon.setText(requiredCoin + "");
                    binding.needCoinPaypal.setText(requiredCoin + "");
                    binding.needCoinGooglePlay.setText(requiredCoin + "");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        return binding.getRoot();
    }

    private void redeem(String paymentMethods) {

        String withCoin = edtCoins.getText().toString();
        String paymentDetails = edtPaymentDetails.getText().toString();

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");

        String date = currentDate.format(calendar.getTime());

        HashMap<String,Object> map = new HashMap<>();
        map.put("paymentDetails",paymentDetails);
        map.put("coin",withCoin);
        map.put("paymentMethode",paymentMethods);
        map.put("status","false");
        map.put("date",date);

        reference.child("Redeem").child(user.getUid())
                .push()
                .setValue(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        updateCoin();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateCoin() {

        int withdrawalCoin = Integer.parseInt(edtCoins.getText().toString());
        int updatedCoin = currentCoin - withdrawalCoin;
        
        HashMap<String,Object> map = new HashMap<>();
        map.put("coins",updatedCoin);
        
        reference.child("Users").child(user.getUid())
                .updateChildren(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        
                        if(task.isSuccessful()){

                            dialog.dismiss();

                            Toast.makeText(getContext(), "Congratulations", Toast.LENGTH_SHORT).show();

                        }else {

                            Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();

                        }
                    }
                });
    }
}