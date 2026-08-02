package com.xnethub.xnet_hub_auth.auth;

import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.xnethub.xnet_hub_auth.R;

public class ForgotFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot, container, false);

        EditText etEmail = view.findViewById(R.id.et_email);
        TextView tvBack = view.findViewById(R.id.tv_back_to_login);
        Button btnReset = view.findViewById(R.id.btn_reset);

        tvBack.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).showLogin();
            }
        });

        etEmail.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().trim();
                if (input.contains("@")) {
                    btnReset.setText("Send Reset Link");
                } else {
                    btnReset.setText("Next");
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        btnReset.setOnClickListener(v -> {
            String input = etEmail.getText().toString().trim();

            if (TextUtils.isEmpty(input)) {
                etEmail.setError("Email or Mobile Number is required");
                return;
            }

            FirebaseAuth centralAuth;
            FirebaseFirestore centralDb;
            try {
                centralAuth = FirebaseAuth.getInstance(FirebaseApp.getInstance("XnetHubAuth"));
                centralDb = FirebaseFirestore.getInstance(FirebaseApp.getInstance("XnetHubAuth"));
            } catch (Exception e) {
                centralAuth = FirebaseAuth.getInstance();
                centralDb = FirebaseFirestore.getInstance();
            }

            if (input.contains("@")) {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
                    etEmail.setError("Valid email is required");
                    return;
                }
                centralAuth.sendPasswordResetEmail(input).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Reset link sent to your email", Toast.LENGTH_LONG).show();
                        if (getActivity() instanceof AuthActivity) {
                            ((AuthActivity) getActivity()).showLogin();
                        }
                    } else {
                        Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                String formattedMobile = com.xnethub.xnet_hub_auth.utils.DeviceUtils.normalizePhoneNumber(input);
                centralDb.collection("registered_mobiles")
                        .document(formattedMobile)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null && task.getResult().exists()) {
                                    if (getActivity() instanceof AuthActivity) {
                                        ((AuthActivity) getActivity()).showPhoneVerification(formattedMobile, true);
                                    }
                                } else {
                                    Toast.makeText(getContext(), "No account found with this number.", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getContext(), "Database error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        return view;
    }
}
