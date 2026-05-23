package com.printxpress.app.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.printxpress.app.R;
import com.printxpress.app.databinding.FragmentLearnMoreBinding;

public class LearnMoreFragment extends Fragment {

    private FragmentLearnMoreBinding binding;

    // Contact details — update as needed
    private static final String WHATSAPP_NUMBER = "+94714678735";
    private static final String EMAIL_ADDRESS   = "printxpress@gmail.com";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLearnMoreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ── FAQ toggles ──────────────────────────────────────────────────────
        setupFaq(binding.faq1, binding.faq1Answer, binding.faq1Arrow);
        setupFaq(binding.faq2, binding.faq2Answer, binding.faq2Arrow);
        setupFaq(binding.faq3, binding.faq3Answer, binding.faq3Arrow);
        setupFaq(binding.faq4, binding.faq4Answer, binding.faq4Arrow);

        // ── WhatsApp ─────────────────────────────────────────────────────────
        binding.btnWhatsApp.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://wa.me/" + WHATSAPP_NUMBER.replace("+", "")));
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://wa.me/" + WHATSAPP_NUMBER.replace("+", "")));
                startActivity(intent);
            }
        });

        // ── Email support ────────────────────────────────────────────────────
        binding.btnEmail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + EMAIL_ADDRESS));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Design Support - PrintXpress");
            startActivity(Intent.createChooser(intent, "Send Email"));
        });
    }

    /** Expand / collapse a FAQ item */
    private void setupFaq(LinearLayout faqRow, TextView answerView, TextView arrowView) {
        faqRow.setOnClickListener(v -> {
            if (answerView.getVisibility() == View.GONE) {
                answerView.setVisibility(View.VISIBLE);
                arrowView.setText("▲");
            } else {
                answerView.setVisibility(View.GONE);
                arrowView.setText("▼");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}