package com.college.shinecart.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Your existing simple email method - KEEP THIS
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("ShineCart — Password Reset OTP");

            String htmlContent =
                    "<!DOCTYPE html>" +
                            "<html lang='en'>" +
                            "<head>" +
                            "<meta charset='UTF-8'>" +
                            "<style>" +
                            "* { margin: 0; padding: 0; box-sizing: border-box; }" +
                            "body { font-family: 'Segoe UI', Arial, sans-serif;" +
                            "  background-color: #f5f5f5; padding: 20px; }" +
                            ".email-wrapper { max-width: 600px; margin: 0 auto;" +
                            "  background: white; border-radius: 12px;" +
                            "  box-shadow: 0 4px 12px rgba(0,0,0,0.1); overflow: hidden; }" +
                            ".header { background: linear-gradient(135deg," +
                            "  #8B7355 0%, #6B5A45 100%); color: white;" +
                            "  padding: 40px 30px; text-align: center; }" +
                            ".header h1 { font-size: 26px; font-weight: 600; }" +
                            ".header p { margin-top: 8px; font-size: 14px;" +
                            "  opacity: 0.9; }" +
                            ".content { padding: 40px 30px; }" +
                            ".greeting { font-size: 18px; color: #333;" +
                            "  margin-bottom: 15px; font-weight: 600; }" +
                            ".message { color: #666; line-height: 1.6;" +
                            "  margin-bottom: 25px; font-size: 14px; }" +
                            ".otp-box { background: #f8f9fa; border-radius: 12px;" +
                            "  padding: 30px; text-align: center; margin: 25px 0;" +
                            "  border: 2px dashed #8B7355; }" +
                            ".otp-label { font-size: 14px; color: #666;" +
                            "  margin-bottom: 12px; }" +
                            ".otp-code { font-size: 48px; font-weight: 700;" +
                            "  color: #8B7355; letter-spacing: 12px;" +
                            "  font-family: monospace; }" +
                            ".expiry { font-size: 12px; color: #999;" +
                            "  margin-top: 12px; }" +
                            ".warning { background: #fff3e0; border-left: 4px solid" +
                            "  #ff9800; padding: 15px; margin: 20px 0;" +
                            "  border-radius: 4px; font-size: 13px; color: #e65100; }" +
                            ".footer { background: #f8f9fa; padding: 25px;" +
                            "  text-align: center; }" +
                            ".footer-logo { font-size: 22px; font-weight: bold;" +
                            "  color: #8B7355; margin-bottom: 8px; }" +
                            ".footer p { color: #999; font-size: 12px;" +
                            "  line-height: 1.6; }" +
                            "</style>" +
                            "</head>" +
                            "<body>" +
                            "<div class='email-wrapper'>" +
                            "  <div class='header'>" +
                            "    <h1>Password Reset</h1>" +
                            "    <p>ShineCart Account Security</p>" +
                            "  </div>" +
                            "  <div class='content'>" +
                            "    <div class='greeting'>Hello!</div>" +
                            "    <p class='message'>We received a request to reset" +
                            "      your ShineCart account password. Use the OTP" +
                            "      below to proceed with your password reset.</p>" +
                            "    <div class='otp-box'>" +
                            "      <div class='otp-label'>Your One Time Password</div>" +
                            "      <div class='otp-code'>" + otp + "</div>" +
                            "      <div class='expiry'>⏱ This OTP expires in" +
                            "        <strong>10 minutes</strong></div>" +
                            "    </div>" +
                            "    <div class='warning'>" +
                            "      ⚠ If you did not request a password reset," +
                            "      please ignore this email. Your account remains" +
                            "      safe and no changes have been made." +
                            "    </div>" +
                            "  </div>" +
                            "  <div class='footer'>" +
                            "    <div class='footer-logo'>ShineCart</div>" +
                            "    <p>© 2026 ShineCart. All rights reserved.</p>" +
                            "    <p style='margin-top: 10px; font-size: 11px;'>" +
                            "      This is an automated email." +
                            "      Please do not reply to this message.</p>" +
                            "  </div>" +
                            "</div>" +
                            "</body>" +
                            "</html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("✓ OTP email sent to: " + toEmail);

        } catch (MessagingException e) {
            System.err.println("✗ Failed to send OTP email to: " + toEmail);
            e.printStackTrace();
        }
    }

    // NEW: Order confirmation email with beautiful HTML template
    public void sendOrderConfirmationEmail(
            String customerEmail,
            String customerName,
            String orderNumber,
            String razorpayPaymentId,
            String razorpayOrderId,
            Double totalAmount,
            String orderItems
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(customerEmail);
            helper.setSubject("✓ Payment Successful - ShineCart Order #" + orderNumber);

            String htmlContent = generateOrderEmailHTML(
                    customerName,
                    orderNumber,
                    razorpayPaymentId,
                    razorpayOrderId,
                    totalAmount,
                    orderItems
            );

            helper.setText(htmlContent, true); // true = HTML email

            mailSender.send(message);
            System.out.println("✓ Order confirmation email sent to: " + customerEmail);

        } catch (MessagingException e) {
            System.err.println("✗ Failed to send email to: " + customerEmail);
            e.printStackTrace();
            // Don't throw exception - we don't want email failure to fail the payment
        }
    }

    private String generateOrderEmailHTML(
            String customerName,
            String orderNumber,
            String razorpayPaymentId,
            String razorpayOrderId,
            Double totalAmount,
            String orderItems
    ) {
        return "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <style>" +
                "        * { margin: 0; padding: 0; box-sizing: border-box; }" +
                "        body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f5f5f5; padding: 20px; }" +
                "        .email-wrapper { max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }" +
                "        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 40px 30px; text-align: center; }" +
                "        .success-icon { font-size: 64px; margin-bottom: 15px; animation: scaleIn 0.5s ease-out; }" +
                "        @keyframes scaleIn { from { transform: scale(0); } to { transform: scale(1); } }" +
                "        .header h1 { margin: 0; font-size: 28px; font-weight: 600; }" +
                "        .header p { margin: 10px 0 0 0; font-size: 14px; opacity: 0.9; }" +
                "        .content { padding: 40px 30px; }" +
                "        .greeting { font-size: 20px; color: #333; margin-bottom: 10px; font-weight: 600; }" +
                "        .message { color: #666; line-height: 1.6; margin-bottom: 25px; }" +
                "        .order-card { background: #f8f9fa; border-radius: 8px; padding: 25px; margin: 25px 0; border-left: 5px solid #667eea; }" +
                "        .order-card h3 { color: #333; margin-bottom: 20px; font-size: 18px; display: flex; align-items: center; }" +
                "        .order-card h3:before { content: '📦'; margin-right: 10px; font-size: 20px; }" +
                "        .detail-row { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid #e0e0e0; }" +
                "        .detail-row:last-child { border-bottom: none; }" +
                "        .label { font-weight: 600; color: #555; font-size: 14px; }" +
                "        .value { color: #333; font-size: 14px; text-align: right; max-width: 60%; word-break: break-word; }" +
                "        .amount-highlight { background: white; padding: 20px; margin-top: 20px; border-radius: 8px; border: 2px solid #667eea; text-align: center; }" +
                "        .amount-label { font-size: 14px; color: #666; margin-bottom: 5px; }" +
                "        .amount { font-size: 32px; color: #667eea; font-weight: 700; }" +
                "        .info-box { background: #e3f2fd; border-left: 4px solid #2196F3; padding: 15px; margin: 20px 0; border-radius: 4px; }" +
                "        .info-box p { color: #1976D2; margin: 0; font-size: 14px; }" +
                "        .footer { background: #f8f9fa; padding: 30px; text-align: center; }" +
                "        .footer-logo { font-size: 24px; font-weight: bold; background: linear-gradient(135deg, #667eea, #764ba2); -webkit-background-clip: text; -webkit-text-fill-color: transparent; margin-bottom: 10px; }" +
                "        .footer p { color: #999; font-size: 13px; line-height: 1.6; margin: 5px 0; }" +
                "        .divider { height: 1px; background: linear-gradient(to right, transparent, #ddd, transparent); margin: 20px 0; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='email-wrapper'>" +
                "        <div class='header'>" +
                "            <div class='success-icon'>✓</div>" +
                "            <h1>Payment Successful!</h1>" +
                "            <p>Your order has been confirmed</p>" +
                "        </div>" +
                "        " +
                "        <div class='content'>" +
                "            <div class='greeting'>Hello " + customerName + "!</div>" +
                "            <p class='message'>Thank you for shopping with ShineCart! Your payment has been successfully processed and your order is confirmed. We're excited to get your items ready for shipment.</p>" +
                "            " +
                "            <div class='order-card'>" +
                "                <h3>Order Details</h3>" +
                "                <div class='detail-row'>" +
                "                    <span class='label'>Order Number : </span>" +
                "                    <span class='value'><strong>" + orderNumber + "</strong></span>" +
                "                </div>" +
                "                <div class='detail-row'>" +
                "                    <span class='label'>Payment ID : </span>" +
                "                    <span class='value'>" + razorpayPaymentId + "</span>" +
                "                </div>" +
                "                <div class='detail-row'>" +
                "                    <span class='label'>Transaction ID : </span>" +
                "                    <span class='value'>" + razorpayOrderId + "</span>" +
                "                </div>" +
                "                <div class='detail-row'>" +
                "                    <span class='label'>Items Ordered : </span>" +
                "                    <span class='value'>" + orderItems + "</span>" +
                "                </div>" +
                "                " +
                "                <div class='amount-highlight'>" +
                "                    <div class='amount-label'>Total Amount Paid : </div>" +
                "                    <div class='amount'>₹" + String.format("%.2f", totalAmount) + "</div>" +
                "                </div>" +
                "            </div>" +
                "            "  +
                "            " +
                "            <div class='divider'></div>" +
                "            " +
                "            <p class='message' style='margin-bottom: 0;'>If you have any questions about your order, please don't hesitate to contact our support team. We're here to help!</p>" +
                "        </div>" +
                "        " +
                "        <div class='footer'>" +
                "            <div class='footer-logo'>ShineCart</div>" +
                "            <p><strong>Thank you for choosing us!</strong></p>" +
                "            <p>© 2024 ShineCart. All rights reserved.</p>" +
                "            <p style='margin-top: 15px; font-size: 11px;'>This is an automated email. Please do not reply to this message.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}