package com.yusudhanwinwin.winwin.Fragments;

import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.yusudhanwinwin.winwin.Models.Question;
import com.yusudhanwinwin.winwin.R;
import com.yusudhanwinwin.winwin.databinding.FragmentHomeBinding;
import com.yusudhanwinwin.winwin.databinding.FragmentQuizBinding;

import java.util.ArrayList;
import java.util.List;

public class QuizFragment extends Fragment {

    FragmentQuizBinding binding;
    private TextView textViewTimer, textViewQuestion, textViewOptionA, textViewOptionB, textViewOptionC, textViewOptionD;
    private Button buttonNextQuestion;
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int totalCoinsEarned = 0;
    private boolean answered = false;
    private int secondsRemaining = 20; // Başlangıçta 20 saniye
    private CountDownTimer timer;
    FirebaseStorage storage;
    FirebaseUser user;
    DatabaseReference reference;
    FirebaseDatabase database;
    FirebaseAuth auth;

    public QuizFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        binding = FragmentQuizBinding.inflate(inflater, container, false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        user = auth.getCurrentUser();
        reference = database.getReference("Users").child(user.getUid());

        View view = inflater.inflate(R.layout.fragment_quiz, container, false);
        textViewTimer = view.findViewById(R.id.textViewTimer);
        textViewQuestion = view.findViewById(R.id.textViewQuestion);
        textViewOptionA = view.findViewById(R.id.textViewOptionA);
        textViewOptionB = view.findViewById(R.id.textViewOptionB);
        textViewOptionC = view.findViewById(R.id.textViewOptionC);
        textViewOptionD = view.findViewById(R.id.textViewOptionD);
        buttonNextQuestion = view.findViewById(R.id.buttonNextQuestion);

        fillQuestionData();
        startTimer();

        buttonNextQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kullanıcının seçtiği şıkkı alalım (1, 2, 3, 4 olarak belirlediğiniz değerlere göre)
                int userSelectedOption = -1;

                // Kullanıcının seçtiği şıkkı belirleyin ve userSelectedOption değişkenine ata
                // Örneğin, kullanıcı A şıkkını seçtiyse userSelectedOption = 1, B ise userSelectedOption = 2 vb.
                // Burada kullanıcının seçtiği şıkkı almak için gerekli kodu ekleyin.

                // Kullanıcının verdiği cevabı kontrol edelim
                checkUserAnswer(userSelectedOption);
                currentQuestionIndex++;

                if (currentQuestionIndex == questions.size()) {
                    // Quiz tamamlandı, sonuçları gönder ve fragment_quiz'den fragment_home'a geçiş yap
                    Bundle result = new Bundle();
                    result.putInt("updatedCoins", totalCoinsEarned);
                    getParentFragmentManager().setFragmentResult("quizResult", result);

                    getActivity().getSupportFragmentManager().popBackStack();
                } else {
                    // Bir sonraki soruyu göster ve süreyi başlat
                    showQuestion(questions.get(currentQuestionIndex));
                    startTimer();
                }
            }
        });


        showQuestion(questions.get(currentQuestionIndex));
        startTimer();

        return binding.getRoot();
    }
    private void fillQuestionData() {
        questions = new ArrayList<>();
        // Örnek soru eklemeleri
        questions.add(new Question("Soru 1?", "Cevap A", "Cevap B", "Cevap C", "Cevap D", 1));
        questions.add(new Question("Soru 2?", "Cevap A", "Cevap B", "Cevap C", "Cevap D", 3));
        questions.add(new Question("Soru 3?", "Cevap A", "Cevap B", "Cevap C", "Cevap D", 2));
        questions.add(new Question("Soru 4?", "Cevap A", "Cevap B", "Cevap C", "Cevap D", 4));
        questions.add(new Question("Soru 5?", "Cevap A", "Cevap B", "Cevap C", "Cevap D", 4));
        questions.add(new Question("Soru 6?", "Cevap A", "Cevap B", "Cevap C", "Cevap D", 1));
        questions.add(new Question("Soru 7?", "Cevap A", "Cevap B", "Cevap C", "Cevap D", 2));
        questions.add(new Question("Soru 8?", "Cevap A", "Cevap B", "Cevap C", "Cevap D", 3));
        questions.add(new Question("Soru 9?", "Cevap A", "Cevap B", "Cevap C", "Cevap D", 3));
        questions.add(new Question("Soru 10?", "Cevap A", "Cevap B", "Cevap C", "Cevap D", 1));
        // Daha fazla soru ekleyebilirsiniz
    }
    private void showQuestion(Question question) {
        textViewQuestion.setText(question.getQuestion());
        textViewOptionA.setText("A) " + question.getOptionA());
        textViewOptionB.setText("B) " + question.getOptionB());
        textViewOptionC.setText("C) " + question.getOptionC());
        textViewOptionD.setText("D) " + question.getOptionD());
    }
    private void checkUserAnswer(int userSelectedOption) {
        Question currentQuestion = questions.get(currentQuestionIndex);
        if (userSelectedOption == currentQuestion.getCorrectOption()) {
            totalCoinsEarned += 50;
        }
    }
    // fragment_quiz içindeki startTimer() metodunu güncelle
    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }
        secondsRemaining = 20;
        timer = new CountDownTimer(20000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                secondsRemaining--;
                textViewTimer.setText("Timer: " + secondsRemaining);
            }

            @Override
            public void onFinish() {
                // Süre dolduğunda yapılacak işlemler buraya yazılır
                // Örnek olarak, kullanıcı süre dolmadan cevaplamadıysa işlem yapabilirsiniz.

                // Süre dolduğunda kullanıcının cevap verdiği durumu kontrol edelim
                if (!answered) {
                    // Kullanıcı süre dolmadan cevap vermedi, işlem yapabilirsiniz.
                    // Örneğin, soruya yanıt verilmediğini belirten bir mesaj gösterin.
                    Toast.makeText(getContext(), "Time's up! You didn't answer the question.", Toast.LENGTH_SHORT).show();
                    // Burada istediğiniz işlemleri yapabilirsiniz, örneğin puanlama veya veri güncelleme işlemleri.
                }

                Bundle result = new Bundle();
                result.putInt("updatedCoins", totalCoinsEarned);
                getParentFragmentManager().setFragmentResult("quizResult", result);

                getActivity().getSupportFragmentManager().popBackStack();
            }
        }.start();
    }

    // Veritabanındaki coinleri güncelleyen fonksiyon
    /*private void updateCoinsInDatabase(int newCoins) {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Users").child(user.getUid());

        reference.child("coins").setValue(newCoins)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getContext(), "Coins updated successfully!", Toast.LENGTH_SHORT).show();
                        ((HomeFragment) getParentFragment()).setCoinQue(newCoins);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to update coins!", Toast.LENGTH_SHORT).show();
                    }
                });
    }*/

    // fragment_quiz içindeki onDestroyView() metodunu güncelle
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // QuizFragment kapatıldığında veritabanındaki coins değerini güncelliyoruz
        reference.child("coins").setValue(totalCoinsEarned)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Başarılı bir şekilde güncellendi
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Güncelleme başarısız oldu
                    }
                });
    }

}

