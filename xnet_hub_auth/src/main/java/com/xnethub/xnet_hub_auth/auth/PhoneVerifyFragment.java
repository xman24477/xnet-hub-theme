package com.xnethub.xnet_hub_auth.auth;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.xnethub.xnet_hub_auth.R;
import com.xnethub.xnet_hub_auth.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PhoneVerifyFragment extends Fragment {

    private String phoneNumber;
    private String verificationId;
    private boolean isPasswordReset = false;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private FirebaseAuth centralAuth;
    private FirebaseAuth primaryAuth;

    private EditText etOtp;
    private Button btnVerify;
    private TextView tvResend, tvSubtitle;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        primaryAuth = FirebaseAuth.getInstance();
        try {
            centralAuth = FirebaseAuth.getInstance(FirebaseApp.getInstance("XnetHubAuth"));
        } catch (Exception e) {
            centralAuth = FirebaseAuth.getInstance();
        }

        if (getArguments() != null) {
            phoneNumber = getArguments().getString("phone_number");
            isPasswordReset = getArguments().getBoolean("is_password_reset", false);
        }
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phone_verify, container, false);
        etOtp = view.findViewById(R.id.et_otp);
        btnVerify = view.findViewById(R.id.btn_verify);
        tvResend = view.findViewById(R.id.tv_resend_otp);
        tvSubtitle = view.findViewById(R.id.tv_verify_subtitle);

        if (phoneNumber != null) {
            tvSubtitle.setText("Enter the OTP sent to " + phoneNumber);
            checkAndSendVerificationCode(phoneNumber);
        }

        btnVerify.setOnClickListener(v -> {
            String code = etOtp.getText().toString().trim();
            if (TextUtils.isEmpty(code) || code.length() < 6) {
                etOtp.setError("Enter 6-digit OTP");
                return;
            }
            if (verificationId != null) {
                verifyCode(code);
            }
        });

        tvResend.setOnClickListener(v -> {
            if (phoneNumber != null) {
                resendVerificationCode(phoneNumber, resendToken);
            }
        });

        return view;
    }

    private void checkAndSendVerificationCode(String mobile) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        long lastTime = prefs.getLong("last_otp_time", 0);
        String lastPhone = prefs.getString("last_otp_phone", "");
        long currentTime = System.currentTimeMillis();

        if (lastPhone.equals(mobile) && (currentTime - lastTime) < 120000) {
            long remainingTime = 120000 - (currentTime - lastTime);
            verificationId = prefs.getString("last_verification_id", null);
            Toast.makeText(getContext(), "OTP already sent. Please wait.", Toast.LENGTH_SHORT).show();
            startResendTimer(remainingTime);
        } else {
            sendVerificationCode(mobile);
        }
    }

    private void sendVerificationCode(String mobile) {
        progressDialog.setMessage("Sending OTP...");
        progressDialog.show();
        tvResend.setEnabled(false);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(centralAuth)
                .setPhoneNumber(mobile)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void resendVerificationCode(String mobile, PhoneAuthProvider.ForceResendingToken token) {
        progressDialog.setMessage("Resending OTP...");
        progressDialog.show();
        tvResend.setEnabled(false);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(centralAuth)
                .setPhoneNumber(mobile)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(mCallbacks)
                .setForceResendingToken(token)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
            progressDialog.dismiss();
            String code = credential.getSmsCode();
            if (code != null) {
                etOtp.setText(code);
            }
            Toast.makeText(getContext(), "OTP automatically detected!", Toast.LENGTH_SHORT).show();
            signInWithPhoneAuthCredential(credential);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Verification Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            tvResend.setEnabled(true);
        }

        @Override
        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "OTP Sent Successfully", Toast.LENGTH_SHORT).show();
            PhoneVerifyFragment.this.verificationId = verificationId;
            resendToken = token;

            SharedPreferences prefs = requireActivity().getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
            prefs.edit()
                    .putLong("last_otp_time", System.currentTimeMillis())
                    .putString("last_otp_phone", phoneNumber)
                    .putString("last_verification_id", verificationId)
                    .apply();

            startResendTimer(120000);
        }
    };

    private void startResendTimer(long millisInFuture) {
        new CountDownTimer(millisInFuture, 1000) {
            public void onTick(long millisUntilFinished) {
                if (tvResend != null) {
                    tvResend.setText("Resend OTP in " + millisUntilFinished / 1000 + "s");
                }
            }
            public void onFinish() {
                if (tvResend != null) {
                    tvResend.setText("Resend OTP");
                    tvResend.setEnabled(true);
                }
            }
        }.start();
    }

    private void verifyCode(String code) {
        progressDialog.setMessage("Verifying OTP...");
        progressDialog.show();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        com.google.firebase.auth.FirebaseUser currentUser = centralAuth.getCurrentUser();
        
        if (currentUser != null && !isPasswordReset) {
            currentUser.linkWithCredential(credential)
                    .addOnCompleteListener(requireActivity(), task -> {
                        if (task.isSuccessful()) {
                            progressDialog.setMessage("Verifying with Central Auth...");
                            AuthManager.signInWithCentralAuth(getContext(), new AuthManager.AuthCallback() {
                                @Override
                                public void onSuccess() {
                                    String uid = task.getResult().getUser().getUid();
                                    markMobileAsVerified(uid);
                                }
                                @Override
                                public void onError(String error) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getContext(), "Central Auth Failed: " + error, Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            Exception e = task.getException();
                            Toast.makeText(getContext(), "Verification Failed: " + (e != null ? e.getMessage() : "Unknown"), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            centralAuth.signInWithCredential(credential)
                    .addOnCompleteListener(requireActivity(), task -> {
                        if (task.isSuccessful()) {
                            progressDialog.setMessage("Verifying with Central Auth...");
                            AuthManager.signInWithCentralAuth(getContext(), new AuthManager.AuthCallback() {
                                @Override
                                public void onSuccess() {
                                     if (isPasswordReset) {
                                         showResetPasswordDialog();
                                     } else {
                                         com.google.firebase.auth.FirebaseUser pUser = primaryAuth.getCurrentUser();
                                         com.google.firebase.auth.FirebaseUser cUser = centralAuth.getCurrentUser();
                                         String uid = (pUser != null) ? pUser.getUid() : (cUser != null ? cUser.getUid() : null);
                                         if (uid != null) {
                                             markMobileAsVerified(uid);
                                         } else {
                                             progressDialog.dismiss();
                                             Toast.makeText(getContext(), "Failed to identify user profile", Toast.LENGTH_SHORT).show();
                                         }
                                     }
                                }
                                @Override
                                public void onError(String error) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getContext(), "Central Auth Failed: " + error, Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Verification Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showResetPasswordDialog() {
        progressDialog.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Set New Password");
        builder.setCancelable(false);

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("New Password");
        builder.setView(input);

        builder.setPositiveButton("Reset", (dialog, which) -> {
            String newPassword = input.getText().toString().trim();
            if (newPassword.length() < 6) {
                Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                showResetPasswordDialog();
                return;
            }
            
            progressDialog.setMessage("Updating password...");
            progressDialog.show();
            if (centralAuth.getCurrentUser() != null) {
                centralAuth.getCurrentUser().updatePassword(newPassword).addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Password updated successfully!", Toast.LENGTH_LONG).show();
                        if (getActivity() instanceof AuthActivity) {
                            centralAuth.signOut();
                            primaryAuth.signOut();
                            ((AuthActivity) getActivity()).showLogin();
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to update password: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        builder.show();
    }

    private void markMobileAsVerified(String uid) {
        progressDialog.setMessage("Updating profile...");
        Map<String, Object> updates = new HashMap<>();
        updates.put("mobile", phoneNumber);
        updates.put("isMobileVerified", true);

        FirebaseFirestore db;
        try {
            db = FirebaseFirestore.getInstance(FirebaseApp.getInstance("XnetHubAuth"));
        } catch (Exception e) {
            db = FirebaseFirestore.getInstance();
        }

        db.collection("users").document(uid)
                .set(updates, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        SessionManager.handleLoginSession(getActivity(), centralAuth.getCurrentUser(), () -> {
                            requireActivity().setResult(Activity.RESULT_OK);
                            requireActivity().finish();
                        });
                    } else {
                        Toast.makeText(getContext(), "Failed to update status: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
