package com.college.shinecart.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService {

    // Stores email -> {otp, expiry}
    private final Map<String, OtpEntry> otpStore = new HashMap<>();

    private static final int OTP_EXPIRY_MINUTES = 10;

    // Generate and store OTP
    public String generateOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStore.put(email.toLowerCase(), new OtpEntry(
                otp, LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)
        ));
        return otp;
    }

    // Verify OTP
    public boolean verifyOtp(String email, String otp) {
        OtpEntry entry = otpStore.get(email.toLowerCase());
        if (entry == null) return false;
        if (LocalDateTime.now().isAfter(entry.expiry)) {
            otpStore.remove(email.toLowerCase());
            return false;
        }
        return entry.otp.equals(otp);
    }

    // Clear OTP after use
    public void clearOtp(String email) {
        otpStore.remove(email.toLowerCase());
    }

    // Inner class to hold OTP and expiry
    private static class OtpEntry {
        String otp;
        LocalDateTime expiry;

        OtpEntry(String otp, LocalDateTime expiry) {
            this.otp = otp;
            this.expiry = expiry;
        }
    }
}