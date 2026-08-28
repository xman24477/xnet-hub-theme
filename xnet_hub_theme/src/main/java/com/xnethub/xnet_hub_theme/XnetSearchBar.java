package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * XnetSearchBar
 *
 * Official Xnet Hub Theme Library Custom Cyber Search Bar.
 * Built with the exact 6-sided Cut-Corner Hexagon Shell contour matching XnetSpinner.
 *
 * Features:
 *   - Cyber Theme : Hexagonal cut-corner shell (bg_xnet_spinner_shell) + Rajdhani font
 *   - Classic Theme : Rounded shell (bg_xnet_spinner_shell_classic)
 *   - Dynamic Right Action Icon State Machine:
 *       1. IDLE (Empty Text)        → Magnifying Glass Search Icon 🔍
 *       2. TYPING (Active Typing)   → Enter / Search Submit Arrow Icon ↵
 *       3. SUBMITTED (Query Active) → Clear / Cross (X) Icon ✕
 *
 * Usage in XML:
 *   <com.xnethub.xnet_hub_theme.XnetSearchBar
 *       android:id="@+id/searchBar"
 *       android:layout_width="match_parent"
 *       android:layout_height="56dp"
 *       android:hint="Search apps..." />
 */
public class XnetSearchBar extends LinearLayout {

    public interface OnSearchListener {
        void onSearch(String query);
        void onClear();
    }

    public enum SearchState {
        IDLE,       // Empty text -> shows Search icon
        TYPING,     // Text entered -> shows Enter/Submit arrow icon
        SUBMITTED   // Query submitted -> shows Clear X icon
    }

    private AppCompatEditText mEditText;
    private AppCompatImageView mActionButton;

    private SearchState mState = SearchState.IDLE;
    private OnSearchListener mListener;

    private boolean mIsCyberTheme = false;
    private int mAccentColor = 0xFF00FFCC;
    private int mTextColorPrimary = Color.WHITE;
    private int mTextColorSecondary = Color.GRAY;

    public XnetSearchBar(@NonNull Context context) {
        this(context, null);
    }

    public XnetSearchBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XnetSearchBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(R.attr.xnetIsCyberTheme, tv, true)) {
            mIsCyberTheme = tv.data != 0;
        }

        if (context.getTheme().resolveAttribute(R.attr.xnetAccentPrimary, tv, true)) {
            mAccentColor = tv.data;
        }

        if (context.getTheme().resolveAttribute(R.attr.xnetTextPrimary, tv, true)) {
            mTextColorPrimary = tv.data;
        }

        if (context.getTheme().resolveAttribute(R.attr.xnetTextSecondary, tv, true)) {
            mTextColorSecondary = tv.data;
        }

        // Apply Dedicated Search Bar Hexagon / Rounded Shell Background
        if (mIsCyberTheme) {
            setBackgroundResource(R.drawable.bg_xnet_search_shell);
        } else {
            setBackgroundResource(R.drawable.bg_xnet_search_shell_classic);
        }

        float density = context.getResources().getDisplayMetrics().density;
        int padH = (int) (16 * density);
        int padV = (int) (2 * density);
        setPadding(padH, padV, (int) (8 * density), padV);
        setMinimumHeight((int) (48 * density));

        // --- Center EditText ---
        mEditText = new AppCompatEditText(context);
        mEditText.setBackground(null);
        mEditText.setPadding(0, 0, 0, 0); // Clear default internal Android padding to prevent text clipping
        mEditText.setIncludeFontPadding(false);
        mEditText.setGravity(Gravity.CENTER_VERTICAL);
        mEditText.setSingleLine(true);
        mEditText.setMaxLines(1);
        mEditText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        mEditText.setTextColor(mTextColorPrimary);
        mEditText.setHintTextColor(mTextColorSecondary);
        mEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);

        // Apply Rajdhani font for Cyber theme
        if (mIsCyberTheme) {
            try {
                Typeface font = context.getResources().getFont(R.font.rajdhani);
                if (font != null) mEditText.setTypeface(font);
            } catch (Exception ignored) {}
        }

        // Read XML hint attribute
        CharSequence xmlHint = "Search...";
        if (attrs != null) {
            int[] attrArray = new int[]{android.R.attr.hint};
            TypedArray ta = context.obtainStyledAttributes(attrs, attrArray);
            CharSequence h = ta.getText(0);
            if (h != null && h.length() > 0) xmlHint = h;
            ta.recycle();
        }
        mEditText.setHint(xmlHint);

        LayoutParams editParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1f);
        editParams.gravity = Gravity.CENTER_VERTICAL;
        mEditText.setLayoutParams(editParams);
        addView(mEditText);

        // --- Right Action Button ---
        mActionButton = new AppCompatImageView(context);
        int iconSize = (int) (32 * density);
        LayoutParams actionParams = new LayoutParams(iconSize, iconSize);
        actionParams.gravity = Gravity.CENTER_VERTICAL;
        actionParams.leftMargin = (int) (4 * density);
        mActionButton.setLayoutParams(actionParams);
        mActionButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        int iconPad = (int) (4 * density);
        mActionButton.setPadding(iconPad, iconPad, iconPad, iconPad);
        addView(mActionButton);

        // --- Listeners & State Binding ---
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    updateState(SearchState.IDLE);
                } else if (mState != SearchState.SUBMITTED) {
                    updateState(SearchState.TYPING);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
               (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                performSearch();
                return true;
            }
            return false;
        });

        mActionButton.setOnClickListener(v -> {
            switch (mState) {
                case TYPING:
                    performSearch();
                    break;
                case SUBMITTED:
                    clearSearch();
                    break;
                case IDLE:
                default:
                    mEditText.requestFocus();
                    break;
            }
        });

        updateState(SearchState.IDLE);
    }

    // -----------------------------------------------------------------------
    // State Management
    // -----------------------------------------------------------------------

    private void updateState(SearchState newState) {
        this.mState = newState;

        switch (newState) {
            case TYPING:
                // Show Enter / Submit Arrow icon (↵)
                mActionButton.setImageResource(android.R.drawable.ic_menu_send);
                mActionButton.setImageTintList(ColorStateList.valueOf(mAccentColor));
                mActionButton.setClickable(true);
                break;

            case SUBMITTED:
                // Show Clear X icon (✕)
                mActionButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                mActionButton.setImageTintList(ColorStateList.valueOf(mTextColorSecondary));
                mActionButton.setClickable(true);
                break;

            case IDLE:
            default:
                // Show Search Magnifying Glass icon (🔍)
                mActionButton.setImageResource(android.R.drawable.ic_menu_search);
                mActionButton.setImageTintList(ColorStateList.valueOf(mTextColorSecondary));
                mActionButton.setClickable(true);
                break;
        }
    }

    public void performSearch() {
        String query = getText();
        if (query.trim().length() > 0) {
            updateState(SearchState.SUBMITTED);
            if (mListener != null) {
                mListener.onSearch(query);
            }
        }
    }

    public void clearSearch() {
        mEditText.setText("");
        updateState(SearchState.IDLE);
        if (mListener != null) {
            mListener.onClear();
        }
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    public void setOnSearchListener(OnSearchListener listener) {
        this.mListener = listener;
    }

    public String getText() {
        return mEditText.getText() != null ? mEditText.getText().toString() : "";
    }

    public void setText(CharSequence text) {
        mEditText.setText(text);
        if (text != null && text.length() > 0) {
            mEditText.setSelection(text.length());
        }
    }

    public void setHint(CharSequence hint) {
        mEditText.setHint(hint);
    }

    public EditText getEditText() {
        return mEditText;
    }
}
