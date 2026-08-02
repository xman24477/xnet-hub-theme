package com.xnethub.xnet_hub_auth.auth;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hbb20.CountryCodePicker;
import com.xnethub.xnet_hub_auth.R;
import com.xnethub.xnet_hub_auth.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseAuth centralAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private ActivityResultLauncher<Intent> dialogImagePickerLauncher;
    private Uri dialogImageUri;
    private ImageView dialogImageView;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        try {
            centralAuth = FirebaseAuth.getInstance(FirebaseApp.getInstance("XnetHubAuth"));
        } catch (Exception e) {
            centralAuth = FirebaseAuth.getInstance();
        }

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.central_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            Log.d("AuthFlow", "Google Sign-In Intent Result OK.");
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            Log.e("AuthFlow", "Google sign in failed. Code: " + e.getStatusCode(), e);
                            Toast.makeText(getContext(), "Google Sign In Failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        dialogImagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        dialogImageUri = result.getData().getData();
                        if (dialogImageView != null) {
                            dialogImageView.setImageURI(dialogImageUri);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        EditText etEmail = view.findViewById(R.id.et_email);
        EditText etPassword = view.findViewById(R.id.et_password);
        TextView tvRegister = view.findViewById(R.id.tv_register);
        TextView tvForgot = view.findViewById(R.id.tv_forgot_password);
        Button btnLogin = view.findViewById(R.id.btn_login);
        Button btnGoogle = view.findViewById(R.id.btn_google);

        View layoutPassword = view.findViewById(R.id.layout_password);

        etEmail.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().trim();
                if (input.contains("@")) {
                    layoutPassword.setVisibility(View.VISIBLE);
                    btnLogin.setText("Login");
                } else {
                    layoutPassword.setVisibility(View.GONE);
                    btnLogin.setText("Next");
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        tvRegister.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).showRegistration(null);
            }
        });

        tvForgot.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).showForgotPassword();
            }
        });

        btnLogin.setOnClickListener(v -> {
            String input = etEmail.getText().toString().trim();

            if (TextUtils.isEmpty(input)) {
                etEmail.setError("Email or Mobile Number is required");
                return;
            }

            if (input.contains("@")) {
                String password = etPassword.getText().toString().trim();
                if (TextUtils.isEmpty(password)) {
                    etPassword.setError("Password is required");
                    return;
                }
                
                progressDialog.setMessage("Logging in...");
                progressDialog.show();
                centralAuth.signInWithEmailAndPassword(input, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        progressDialog.setMessage("Verifying with Central Auth...");
                        AuthManager.signInWithCentralAuth(getContext(), new AuthManager.AuthCallback() {
                            @Override
                            public void onSuccess() {
                                progressDialog.dismiss();
                                checkUserRegistrationStatus();
                            }

                            @Override
                            public void onError(String error) {
                                progressDialog.dismiss();
                                Toast.makeText(getContext(), "Central Auth Failed: " + error, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                String formattedMobile = com.xnethub.xnet_hub_auth.utils.DeviceUtils.normalizePhoneNumber(input);
                progressDialog.setMessage("Checking account...");
                progressDialog.show();
                FirebaseFirestore centralDb = getCentralDb();
                centralDb.collection("registered_mobiles").document(formattedMobile)
                        .get()
                        .addOnCompleteListener(task -> {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                if (task.getResult() != null && task.getResult().exists()) {
                                    if (getActivity() instanceof AuthActivity) {
                                        ((AuthActivity) getActivity()).showPhoneVerification(formattedMobile);
                                    }
                                } else {
                                    Toast.makeText(getContext(), "No account found with this number. Please register.", Toast.LENGTH_LONG).show();
                                    if (getActivity() instanceof AuthActivity) {
                                        ((AuthActivity) getActivity()).showRegistration(formattedMobile);
                                    }
                                }
                            } else {
                                Toast.makeText(getContext(), "Database error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        return view;
    }

    private FirebaseFirestore getCentralDb() {
        try {
            return FirebaseFirestore.getInstance(FirebaseApp.getInstance("XnetHubAuth"));
        } catch (Exception e) {
            return FirebaseFirestore.getInstance();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        progressDialog.setMessage("Authenticating with Google...");
        progressDialog.show();
        
        centralAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        progressDialog.setMessage("Verifying with Central Auth...");
                        AuthManager.signInWithCentralAuth(getContext(), new AuthManager.AuthCallback() {
                            @Override
                            public void onSuccess() {
                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                checkUserRegistrationStatus();
                            }

                            @Override
                            public void onError(String error) {
                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                Toast.makeText(getContext(), "Central Auth Failed: " + error, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Exception e = task.getException();
                        Toast.makeText(getContext(), "Authentication Failed: " + (e != null ? e.getMessage() : "Unknown"), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRegistrationStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            getCentralDb().collection("users").document(uid).get()
                    .addOnCompleteListener(task -> {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                Map<String, Object> data = document.getData();
                                if (data != null && !isDataMissing(data)) {
                                    Boolean isVerified = (Boolean) data.get("isMobileVerified");
                                    if (isVerified != null && isVerified) {
                                        SessionManager.handleLoginSession(getActivity(), centralAuth.getCurrentUser(), () -> {
                                            getActivity().setResult(Activity.RESULT_OK);
                                            getActivity().finish();
                                        });
                                    } else {
                                        String mobile = (String) data.get("mobile");
                                        if (getActivity() instanceof AuthActivity) {
                                            ((AuthActivity) getActivity()).showPhoneVerification(mobile);
                                        }
                                    }
                                } else {
                                    showCompleteProfileDialog(data);
                                }
                            } else {
                                saveBasicGoogleProfileAndShowDialog();
                            }
                        } else {
                            Toast.makeText(getContext(), "Database error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void saveBasicGoogleProfileAndShowDialog() {
        FirebaseUser user = centralAuth.getCurrentUser();
        FirebaseUser primaryUser = mAuth.getCurrentUser();
        if (user == null || primaryUser == null) {
            showCompleteProfileDialog(null);
            return;
        }
        
        progressDialog.setMessage("Initializing profile...");
        progressDialog.show();
        
        String uid = primaryUser.getUid();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", user.getDisplayName());
        userMap.put("email", user.getEmail());
        userMap.put("profileImageUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
        userMap.put("mobile", "");
        userMap.put("isMobileVerified", false);
        userMap.put("registeredSource", requireContext().getPackageName());
        userMap.put("registeredAt", FieldValue.serverTimestamp());

        FirebaseFirestore centralDb = getCentralDb();
        WriteBatch batch = centralDb.batch();
        batch.set(centralDb.collection("users").document(uid), userMap);
        
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            Map<String, Object> idMap = new HashMap<>();
            idMap.put("uid", uid);
            batch.set(centralDb.collection("registered_emails").document(user.getEmail()), idMap);
        }
        
        batch.commit().addOnCompleteListener(task2 -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (task2.isSuccessful()) {
                showCompleteProfileDialog(userMap);
            } else {
                Toast.makeText(getContext(), "Failed to initialize profile", Toast.LENGTH_SHORT).show();
                showCompleteProfileDialog(null);
            }
        });
    }

    private boolean isDataMissing(Map<String, Object> data) {
        return TextUtils.isEmpty((String) data.get("name")) ||
               TextUtils.isEmpty((String) data.get("email")) ||
               TextUtils.isEmpty((String) data.get("mobile")) ||
               TextUtils.isEmpty((String) data.get("profileImageUrl"));
    }

    private void showCompleteProfileDialog(Map<String, Object> existingData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(false);
        
        View view = getLayoutInflater().inflate(R.layout.dialog_update_profile, null);
        dialogImageView = view.findViewById(R.id.iv_update_profile);
        EditText etName = view.findViewById(R.id.et_update_name);
        EditText etEmail = view.findViewById(R.id.et_update_email);
        LinearLayout layoutMobile = view.findViewById(R.id.layout_update_mobile);
        EditText etMobile = view.findViewById(R.id.et_update_mobile);
        CountryCodePicker ccp = view.findViewById(R.id.update_ccp);
        ccp.registerCarrierNumberEditText(etMobile);

        FirebaseUser user = centralAuth.getCurrentUser();
        
        String name = existingData != null && !TextUtils.isEmpty((String) existingData.get("name")) ? (String) existingData.get("name") : (user != null ? user.getDisplayName() : "");
        String email = existingData != null && !TextUtils.isEmpty((String) existingData.get("email")) ? (String) existingData.get("email") : (user != null ? user.getEmail() : "");
        String photoUrl = existingData != null && !TextUtils.isEmpty((String) existingData.get("profileImageUrl")) ? (String) existingData.get("profileImageUrl") : (user != null && user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");

        boolean nameMissing = TextUtils.isEmpty(name);
        boolean emailMissing = TextUtils.isEmpty(email);
        boolean mobileMissing = existingData == null || TextUtils.isEmpty((String) existingData.get("mobile"));
        boolean imageMissing = TextUtils.isEmpty(photoUrl);

        if (nameMissing) etName.setVisibility(View.VISIBLE);
        if (emailMissing) etEmail.setVisibility(View.VISIBLE);
        if (mobileMissing) layoutMobile.setVisibility(View.VISIBLE);
        if (imageMissing) dialogImageView.setVisibility(View.VISIBLE);

        dialogImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            dialogImagePickerLauncher.launch(intent);
        });

        builder.setView(view);
        builder.setPositiveButton("Save", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String inputName = etName.getText().toString().trim();
            String inputEmail = etEmail.getText().toString().trim();
            String mobile = ccp.getFullNumberWithPlus();

            if (nameMissing && TextUtils.isEmpty(inputName)) {
                etName.setError("Name is required");
                return;
            }
            if (emailMissing && !android.util.Patterns.EMAIL_ADDRESS.matcher(inputEmail).matches()) {
                etEmail.setError("Valid email is required");
                return;
            }
            if (mobileMissing && !ccp.isValidFullNumber()) {
                etMobile.setError("Valid mobile number is required");
                return;
            }
            if (imageMissing && dialogImageUri == null) {
                Toast.makeText(getContext(), "Profile image is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mobileMissing) {
                progressDialog.setMessage("Checking mobile number...");
                progressDialog.show();
                getCentralDb().collection("registered_mobiles").document(mobile).get()
                        .addOnCompleteListener(task -> {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                if (task.getResult() != null && task.getResult().exists()) {
                                    Toast.makeText(getContext(), "This number is already used in another account.", Toast.LENGTH_LONG).show();
                                } else {
                                    dialog.dismiss();
                                    saveUserInfo(nameMissing ? inputName : name, 
                                                emailMissing ? inputEmail : email, 
                                                mobile, 
                                                imageMissing, photoUrl);
                                }
                            } else {
                                Toast.makeText(getContext(), "Database error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                dialog.dismiss();
                saveUserInfo(nameMissing ? inputName : name, 
                            emailMissing ? inputEmail : email, 
                            mobile, 
                            imageMissing, photoUrl);
            }
        });
    }

    private void saveUserInfo(String name, String email, String mobile, boolean uploadImage, String fallbackImage) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        String uid = user.getUid();
        
        progressDialog.setMessage("Saving profile...");
        progressDialog.show();

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("mobile", mobile);

        if (uploadImage && dialogImageUri != null) {
            StorageReference tempRef;
            try {
                tempRef = FirebaseStorage.getInstance(FirebaseApp.getInstance("XnetHubAuth")).getReference().child("profile_images/" + uid + ".jpg");
            } catch (Exception e) {
                tempRef = FirebaseStorage.getInstance().getReference().child("profile_images/" + uid + ".jpg");
            }
            final StorageReference fileRef = tempRef;
            fileRef.putFile(dialogImageUri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                userMap.put("profileImageUrl", uri.toString());
                finalSave(uid, userMap);
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Failed to get URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            })).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            userMap.put("profileImageUrl", fallbackImage);
            finalSave(uid, userMap);
        }
    }

    private void finalSave(String uid, Map<String, Object> userMap) {
        userMap.put("isMobileVerified", false);
        
        String mobile = (String) userMap.get("mobile");
        String email = (String) userMap.get("email");
        
        Map<String, Object> idMap = new HashMap<>();
        idMap.put("uid", uid);

        FirebaseFirestore mFirestore = getCentralDb();
        WriteBatch batch = mFirestore.batch();
        
        batch.set(mFirestore.collection("users").document(uid), userMap, SetOptions.merge());
        if (mobile != null && !mobile.isEmpty()) {
            batch.set(mFirestore.collection("registered_mobiles").document(mobile), idMap);
        }
        if (email != null && !email.isEmpty()) {
            batch.set(mFirestore.collection("registered_emails").document(email), idMap);
        }

        batch.commit()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        if (getActivity() instanceof AuthActivity) {
                            ((AuthActivity) getActivity()).showPhoneVerification(mobile);
                        }
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown";
                        Toast.makeText(getContext(), "Database Failed: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
