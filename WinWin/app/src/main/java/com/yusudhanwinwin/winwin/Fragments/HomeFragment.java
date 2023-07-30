package com.yusudhanwinwin.winwin.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.yusudhanwinwin.winwin.Activities.ReferActivity;
import com.yusudhanwinwin.winwin.Activities.ScratchCardActivity;
import com.yusudhanwinwin.winwin.Activities.SpinnerActivity;
import com.yusudhanwinwin.winwin.Models.UserModel;
import com.yusudhanwinwin.winwin.R;
import com.yusudhanwinwin.winwin.databinding.FragmentHomeBinding;

import androidx.fragment.app.FragmentResultListener;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class HomeFragment extends Fragment {
    FragmentHomeBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseUser  user;
    private RewardedAd rewardedAd;
    private int coinQue = 0;
    int userVideoWatchCount = 0;
    private int maxVideoWatchCount = 5;
    private boolean isRewardedAdLoaded = false;
    private Date lastWatchDate = null;
    private int rewardCoins = 50;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
    DatabaseReference userRef;
    private static final String ADMOB_AD_UNIT_ID = "ca-app-pub-5060997619686611~6311505011";

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         binding = FragmentHomeBinding.inflate(inflater, container, false);

        MobileAds.initialize(requireContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                loadRewardedAd();
            }
        });

         auth = FirebaseAuth.getInstance();
         database = FirebaseDatabase.getInstance();
         user = auth.getCurrentUser();

         userRef = database.getReference("Users").child(user.getUid());

         userRef.child("videoWatchCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userVideoWatchCount = snapshot.getValue(Integer.class);
                    checkLastWatchDate();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Veritabanı hatası
            }
        });

         binding.dailyReward.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 final SweetAlertDialog dialog = new SweetAlertDialog(getContext(),SweetAlertDialog.PROGRESS_TYPE);
                 dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DCB6"));
                 dialog.setTitleText("Loading");
                 dialog.setCancelable(false);
                 dialog.show();

                 final Date currentDate = Calendar.getInstance().getTime();
                 final SimpleDateFormat dateFormat =  new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

                 database.getReference().child("Daily Check").child(user.getUid())
                         .addListenerForSingleValueEvent(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot snapshot) {
                                 if(snapshot.exists()){
                                     String dbDateString = snapshot.child("date").getValue(String.class);

                                     try {
                                         assert dbDateString != null;

                                         Date dbDate = dateFormat.parse(dbDateString);

                                         String dates = dateFormat.format(currentDate);
                                         Date date = dateFormat.parse(dates);

                                         if(date.after(dbDate) && date.compareTo(dbDate) != 0){
                                             database.getReference().child("Users").child(user.getUid())
                                                     .addListenerForSingleValueEvent(new ValueEventListener() {
                                                         @Override
                                                         public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                             UserModel model = snapshot.getValue(UserModel.class);

                                                             int currentCoins = (int) model.getCoins();
                                                             int update = currentCoins + 20;
                                                             int spinCoin = model.getSpins();
                                                             int updateSpin = spinCoin + 2;

                                                             HashMap<String,Object> map = new HashMap<>();
                                                             map.put("coins",update);
                                                             map.put("spins",updateSpin);

                                                             database.getReference().child("Users").child(user.getUid())
                                                                     .updateChildren(map);

                                                             Date newDate = Calendar.getInstance().getTime();
                                                             String newDateString = dateFormat.format(newDate);

                                                             HashMap<String,Object> dateMap = new HashMap<>();
                                                             dateMap.put("date",newDateString);

                                                             database.getReference().child("Daily Check").child(FirebaseAuth.getInstance().getUid())
                                                                     .setValue(dateMap)
                                                                     .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                         @Override
                                                                         public void onComplete(@NonNull Task<Void> task) {
                                                                             dialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                                                             dialog.setTitle("Success");
                                                                             dialog.setContentText("Coins added");
                                                                             dialog.setConfirmButton("Dismiss", new SweetAlertDialog.OnSweetClickListener() {
                                                                                 @Override
                                                                                 public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                                                     dialog.dismissWithAnimation();
                                                                                 }
                                                                             }).show();
                                                                         }
                                                                     });

                                                         }

                                                         @Override
                                                         public void onCancelled(@NonNull DatabaseError error) {
                                                             Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                                         }
                                                     });
                                         }
                                         else{
                                             dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                                             dialog.setTitle("Failed");
                                             dialog.setContentText("You have already rewarded, Come back tomorrow.");
                                             dialog.setConfirmButton("Dismiss",null);
                                             dialog.show();
                                         }

                                     } catch (ParseException e) {
                                         throw new RuntimeException(e);
                                     }
                                 }else{
                                     Toast.makeText(getContext(), "data not exist", Toast.LENGTH_SHORT).show();

                                     dialog.changeAlertType(SweetAlertDialog.WARNING_TYPE);
                                     dialog.setTitleText("System Busy");
                                     dialog.setContentText("System is busy, please try later.");
                                     dialog.setConfirmButton("Dismiss", new SweetAlertDialog.OnSweetClickListener() {
                                         @Override
                                         public void onClick(SweetAlertDialog sweetAlertDialog) {
                                             dialog.dismissWithAnimation();
                                         }
                                     });
                                     dialog.dismiss();
                                 }
                             }

                             @Override
                             public void onCancelled(@NonNull DatabaseError error) {
                                 Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                 dialog.dismissWithAnimation();

                             }
                         });
             }
         });

         binding.spinWheel.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(getContext(), SpinnerActivity.class);
                 startActivity(intent);
             }
         });

         binding.referCard.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(getContext(), ReferActivity.class);
                 startActivity(intent);
             }
         });

         binding.scratchCard.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {

                 Intent intent = new Intent(getContext(), ScratchCardActivity.class);
                 startActivity(intent);
             }
         });

        binding.watchVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userVideoWatchCount < maxVideoWatchCount && isRewardedAdLoaded) {
                    watchVideo();
                } else {
                    showNoMoreWatchMessage();
                }
            }
        });

        binding.playQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), QuizFragment.class);
                intent.putExtra("currentCoins", coinQue);
                startActivity(intent);
            }
        });

        getParentFragmentManager().setFragmentResultListener("quizResult", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                if (requestKey.equals("quizResult")) {
                    int updatedCoins = result.getInt("updatedCoins", 0);
                    // Güncel coin değerini alın ve HomeFragment'taki görünümü güncelleyin
                    binding.coins.setText(String.valueOf(updatedCoins));
                }
            }
        });

        database.getReference().child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel model = snapshot.getValue(UserModel.class);

                if(snapshot.exists()){
                    binding.name.setText(model.getName());
                    binding.coins.setText(model.getCoins() + "");

                    Picasso.get()
                            .load(model.getProfile())
                            .placeholder(R.drawable.placeholder)
                            .into(binding.profileImage);
                }else{
                    Toast.makeText(getContext(),"data not exist",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

        return  binding.getRoot();
    }

    private NavController findNavController(){
        NavHostFragment navHostFragment = (NavHostFragment) requireActivity().getSupportFragmentManager().findFragmentById(R.id.nav_graph);
        return navHostFragment.getNavController();
    }
    private void checkLastWatchDate() {
        userRef.child("lastWatchDate").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String dateString = snapshot.getValue(String.class);
                    try {
                        lastWatchDate = dateFormat.parse(dateString);
                        Calendar today = Calendar.getInstance();
                        today.set(Calendar.HOUR_OF_DAY, 0);
                        today.set(Calendar.MINUTE, 0);
                        today.set(Calendar.SECOND, 0);
                        today.set(Calendar.MILLISECOND, 0);
                        Calendar lastWatch = Calendar.getInstance();
                        lastWatch.setTime(lastWatchDate);
                        lastWatch.set(Calendar.HOUR_OF_DAY, 0);
                        lastWatch.set(Calendar.MINUTE, 0);
                        lastWatch.set(Calendar.SECOND, 0);
                        lastWatch.set(Calendar.MILLISECOND, 0);
                        if (today.after(lastWatch)) {
                            // Bir gün geçti, izleme hakkı yeniden açılabilir
                            userVideoWatchCount = 0;
                            updateVideoWatchCount();
                        }
                    } catch (ParseException e) {
                        // Tarih çözümlenemedi, hata durumu
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Veritabanı hatası
            }
        });
    }
    private void updateVideoWatchCount() {
        userRef.child("videoWatchCount").setValue(userVideoWatchCount);
        if (userVideoWatchCount >= maxVideoWatchCount) {
            // İzleme hakkı doldu, reklam yüklenmeyecek
            isRewardedAdLoaded = false;
        } else {
            // İzleme hakkı var, reklam yüklenecek
            isRewardedAdLoaded = true;
        }
    }
    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(requireContext(), ADMOB_AD_UNIT_ID,
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        rewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                    }
                });
    }
    private void watchVideo() {
        if (rewardedAd != null) {
            rewardedAd.show(requireActivity(), new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Kullanıcı ödül kazandı, işlemleri yapabilirsiniz
                    int currentCoins = Integer.parseInt(binding.coins.getText().toString());
                    int updatedCoins = currentCoins + rewardCoins;
                    binding.coins.setText(String.valueOf(updatedCoins));
                }
            });

            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    // Reklam kapatıldığında çalışacak kodlar burada olacak.
                    // Örneğin, reklamı kapattıktan sonra veritabanında kullanıcının izleme hakkını azaltabilirsiniz.
                    userVideoWatchCount++;
                    updateVideoWatchCount();
                }

                // Diğer FullScreenContentCallback metodları burada eklenebilir (isteğe bağlı).
            });
        } else {
            // Reklam yüklenmediyse veya gösterilemediyse burada işlem yapabilirsiniz.
            // Örneğin, kullanıcıya "Reklam yüklenemedi" şeklinde bir Toast mesajı gösterebilirsiniz.
            showNoMoreWatchMessage();
        }
    }
    private void showNoMoreWatchMessage() {
        // Mesajı göster
        Toast.makeText(getContext(), "Your video watch attempts have been used up.", Toast.LENGTH_SHORT).show();
    }

}