package com.xnethub.xnet_hub_auth.auth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hbb20.CountryCodePicker;
import com.xnethub.xnet_hub_auth.R;

import java.util.HashMap;
import java.util.Map;

public class RegistrationFragment extends Fragment {

    private EditText etName, etEmail, etMobile, etPassword;
    private ImageView ivProfile;
    private Button btnRegister;
    private TextView tvLogin;
    private CountryCodePicker countryCodePicker;
    
    private Uri imageUri;
    private FirebaseAuth mAuth;
    private FirebaseAuth centralAuth;
    private FirebaseFirestore mFirestore;
    private StorageReference mStorage;
    private ProgressDialog progressDialog;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        try {
            centralAuth = FirebaseAuth.getInstance(FirebaseApp.getInstance("XnetHubAuth"));
            mFirestore = FirebaseFirestore.getInstance(FirebaseApp.getInstance("XnetHubAuth"));
            mStorage = FirebaseStorage.getInstance(FirebaseApp.getInstance("XnetHubAuth")).getReference();
        } catch (Exception e) {
            centralAuth = FirebaseAuth.getInstance();
            mFirestore = FirebaseFirestore.getInstance();
            mStorage = FirebaseStorage.getInstance().getReference();
        }

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Creating account...");
        progressDialog.setCancelable(false);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        ivProfile.setImageURI(imageUri);
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration, container, false);

        ivProfile = view.findViewById(R.id.iv_profile);
        etName = view.findViewById(R.id.et_name);
        etEmail = view.findViewById(R.id.et_email);
        etMobile = view.findViewById(R.id.et_mobile);
        etPassword = view.findViewById(R.id.et_password);
        btnRegister = view.findViewById(R.id.btn_register);
        tvLogin = view.findViewById(R.id.tv_login);
        countryCodePicker = view.findViewById(R.id.country_code_picker);
        countryCodePicker.registerCarrierNumberEditText(etMobile);

        if (getArguments() != null) {
            String phoneToFill = getArguments().getString("phone_number");
            if (phoneToFill != null && !phoneToFill.isEmpty()) {
                countryCodePicker.setFullNumber(phoneToFill);
            }
        }

        ivProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        tvLogin.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).showLogin();
            }
        });

        btnRegister.setOnClickListener(v -> registerUser());

        return view;
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = countryCodePicker.getFullNumberWithPlus();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || 
            TextUtils.isEmpty(etMobile.getText().toString().trim()) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            return;
        }

        if (!countryCodePicker.isValidFullNumber()) {
            etMobile.setError("Enter a valid mobile number");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (imageUri == null) {
            Toast.makeText(getContext(), "Please select a profile image", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Checking credentials...");
        progressDialog.show();

        mFirestore.collection("registered_emails").document(email).get()
                .addOnCompleteListener(taskEmail -> {
                    if (taskEmail.isSuccessful()) {
                        if (taskEmail.getResult() != null && taskEmail.getResult().exists()) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "This email is already registered. Please login.", Toast.LENGTH_LONG).show();
                        } else {
                            mFirestore.collection("registered_mobiles").document(mobile).get()
                                    .addOnCompleteListener(taskMobile -> {
                                        if (taskMobile.isSuccessful()) {
                                            if (taskMobile.getResult() != null && taskMobile.getResult().exists()) {
                                                progressDialog.dismiss();
                                                Toast.makeText(getContext(), "This number is already used in another account.", Toast.LENGTH_LONG).show();
                                            } else {
                                                progressDialog.setMessage("Creating account...");
                                                centralAuth.createUserWithEmailAndPassword(email, password)
                                                        .addOnCompleteListener(task -> {
                                                            if (task.isSuccessful()) {
                                                                progressDialog.setMessage("Verifying with Central Auth...");
                                                                AuthManager.signInWithCentralAuth(getContext(), new AuthManager.AuthCallback() {
                                                                    @Override
                                                                    public void onSuccess() {
                                                                        uploadImageAndSaveData(name, email, mobile);
                                                                    }
                                                                    @Override
                                                                    public void onError(String error) {
                                                                        progressDialog.dismiss();
                                                                        Toast.makeText(getContext(), "Central Auth Failed: " + error, Toast.LENGTH_LONG).show();
                                                                    }
                                                                });
                                                            } else {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(getContext(), "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                            }
                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(getContext(), "Database error: " + taskMobile.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Database error: " + taskEmail.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadImageAndSaveData(String name, String email, String mobile) {
        progressDialog.setMessage("Uploading image...");
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Authentication session missing. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference fileRef = mStorage.child("profile_images/" + uid + ".jpg");

        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String imageUrl = uri.toString();
            saveUserInfo(uid, name, email, mobile, imageUrl);
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        })).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveUserInfo(String uid, String name, String email, String mobile, String imageUrl) {
        progressDialog.setMessage("Saving user info...");
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("mobile", mobile);
        userMap.put("profileImageUrl", imageUrl);
        userMap.put("isMobileVerified", false);
        userMap.put("registeredAt", FieldValue.serverTimestamp());
        userMap.put("registeredSource", requireContext().getPackageName());

        Map<String, Object> idMap = new HashMap<>();
        idMap.put("uid", uid);

        WriteBatch batch = mFirestore.batch();
        batch.set(mFirestore.collection("users").document(uid), userMap);
        batch.set(mFirestore.collection("registered_emails").document(email), idMap);
        batch.set(mFirestore.collection("registered_mobiles").document(mobile), idMap);

        batch.commit()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        mAuth.signOut();
                        try {
                            FirebaseAuth.getInstance(FirebaseApp.getInstance("XnetHubAuth")).signOut();
                        } catch (Exception e) {
                            // Ignored
                        }
                        
                        Toast.makeText(getContext(), "Registration successful! Please log in to continue.", Toast.LENGTH_LONG).show();
                        if (getActivity() instanceof AuthActivity) {
                            ((AuthActivity) getActivity()).showLogin();
                        }
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(getContext(), "Database Failed: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
