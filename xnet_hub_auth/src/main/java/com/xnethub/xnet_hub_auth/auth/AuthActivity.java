package com.xnethub.xnet_hub_auth.auth;

import android.os.Bundle;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.xnethub.xnet_hub_auth.R;
import com.xnethub.xnet_hub_theme.XnetBaseActivity;

public class AuthActivity extends XnetBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Apply edge-to-edge window insets to container
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.auth_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (savedInstanceState == null) {
            showLogin();
        }
    }

    public void showLogin() {
        replaceFragment(new LoginFragment(), false);
    }

    public void showRegistration(String phoneNumber) {
        RegistrationFragment fragment = new RegistrationFragment();
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            Bundle args = new Bundle();
            args.putString("phone_number", phoneNumber);
            fragment.setArguments(args);
        }
        replaceFragment(fragment, true);
    }

    public void showForgotPassword() {
        replaceFragment(new ForgotFragment(), true);
    }

    public void showPhoneVerification(String phoneNumber) {
        showPhoneVerification(phoneNumber, false);
    }

    public void showPhoneVerification(String phoneNumber, boolean isPasswordReset) {
        PhoneVerifyFragment fragment = new PhoneVerifyFragment();
        Bundle args = new Bundle();
        args.putString("phone_number", phoneNumber);
        args.putBoolean("is_password_reset", isPasswordReset);
        fragment.setArguments(args);
        replaceFragment(fragment, true);
    }

    private void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.auth_container, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }
}
