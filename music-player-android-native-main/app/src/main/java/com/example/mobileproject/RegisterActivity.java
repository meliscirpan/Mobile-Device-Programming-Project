package com.example.mobileproject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.scottyab.aescrypt.AESCrypt;

import java.io.ByteArrayOutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class RegisterActivity extends Activity {
    int SELECT_PICTURE = 200;
    ImageView registerImageButton;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    EditText usernameEditText, phoneEditText, emailEditText, passwordEditText, passwordAgainEditText, surnameEditText;
    String passwordSecret = "thisislongandsecretkeynooneknowsthiskeyyeah";
    String imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.register_screen);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPref.edit();
        editor.remove("email").remove("password").apply();
        usernameEditText = findViewById(R.id.name_register);
        surnameEditText = findViewById(R.id.surname_register);
        phoneEditText = findViewById(R.id.phone_register);
        emailEditText = findViewById(R.id.email_register);
        passwordEditText = findViewById(R.id.password_register);
        passwordAgainEditText = findViewById(R.id.password_again);
        Button register = findViewById(R.id.register);
        registerImageButton = findViewById(R.id.image_picker);
        usernameEditText.addTextChangedListener(new TextValidator(usernameEditText) {

            @Override
            public void validate(TextView textView, String text) {
                if (!usernameEditText.getText().toString().matches("^[A-Za-z ]+$")) {
                    usernameEditText.setError(Html.fromHtml("<p>İsim özel karakter içeremez</p><br><br>", Html.FROM_HTML_MODE_COMPACT));
                }
            }
        });
        surnameEditText.addTextChangedListener(new TextValidator(surnameEditText) {

            @Override
            public void validate(TextView textView, String text) {
                if (!surnameEditText.getText().toString().matches("^[A-Za-z]+$")) {
                    surnameEditText.setError(Html.fromHtml("<p>Soyisim özel karakter içeremez</p><br><br>", Html.FROM_HTML_MODE_COMPACT));
                }
            }
        });
        phoneEditText.addTextChangedListener(new TextValidator(phoneEditText) {

            @Override
            public void validate(TextView textView, String text) {
                if (!phoneEditText.getText().toString().matches("^[0-9]{10,13}$")) {
                    phoneEditText.setError(Html.fromHtml("<p>Uygun bir numara giriniz</p><br><br>", Html.FROM_HTML_MODE_COMPACT));
                }
            }
        });
        emailEditText.addTextChangedListener(new TextValidator(emailEditText) {

            @Override
            public void validate(TextView textView, String text) {
                if (!emailEditText.getText().toString().matches("^(.+)@(.+)$")) {
                    emailEditText.setError(Html.fromHtml("<p>Uygun bir email adresi giriniz</p><br><br>", Html.FROM_HTML_MODE_COMPACT));
                }
            }
        });
        passwordEditText.addTextChangedListener(new TextValidator(passwordEditText) {

            @Override
            public void validate(TextView textView, String text) {
                if (passwordEditText.getText().toString().length() < 6) {
                    passwordEditText.setError(Html.fromHtml("<p>Şifreniz en az 6 karakterli olmalı", Html.FROM_HTML_MODE_COMPACT));
                }
            }
        });
        passwordAgainEditText.addTextChangedListener(new TextValidator(passwordAgainEditText) {

            @Override
            public void validate(TextView textView, String text) {
                if (!passwordAgainEditText.getText().toString().equals(passwordEditText.getText().toString())) {
                    passwordAgainEditText.setError(Html.fromHtml("<p>Şifreler aynı olmalı</p><br><br>", Html.FROM_HTML_MODE_COMPACT));
                }
            }
        });
        registerImageButton.setOnClickListener(v -> imageChooser());
        register.setOnClickListener(v -> {
            Boolean result = validation();
            if (result) {
                firebaseAuth.createUserWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
                        .addOnSuccessListener(authResult -> {
                            Map<String, String> userdata = new HashMap<>();
                            userdata.put("name", usernameEditText.getText().toString());
                            userdata.put("surname", surnameEditText.getText().toString());
                            userdata.put("phone", phoneEditText.getText().toString());
                            userdata.put("email", emailEditText.getText().toString());
                            try {
                                userdata.put("password", AESCrypt.encrypt(passwordEditText.getText().toString(), passwordSecret));
                            } catch (GeneralSecurityException e) {
                                throw new RuntimeException(e);
                            }
                            userdata.put("image", encodeToBase64());
                            UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(usernameEditText.getText().toString())
                                    .setPhotoUri(Objects.isNull(imageUri) ? null : Uri.parse(imageUri))
                                    .build();
                            authResult.getUser().updateProfile(userProfileChangeRequest);
                            System.out.println(passwordEditText.getText().toString());
                            editor.putString("email", emailEditText.getText().toString());
                            editor.apply();
                            firebaseFirestore.collection("user_info").document(UUID.randomUUID().toString()).set(userdata).addOnFailureListener(
                                    e -> Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show()
                            ).addOnSuccessListener(
                                    e -> Toast.makeText(getApplicationContext(), "Kayıt Oldunuz!", Toast.LENGTH_SHORT).show()
                            );
                            SendEmail();
                            finish();
                        }).addOnFailureListener(authResult -> {
                            Toast.makeText(getApplicationContext(), authResult.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(getApplicationContext(), "Lütfen alanları doğru girdiğinizden emin olunuz!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String encodeToBase64() {
        Bitmap image;
        if (imageUri == null) {
            image = BitmapFactory.decodeResource(getResources(), R.mipmap.default_avatar_2);
        } else {
            image = ((BitmapDrawable) registerImageButton.getDrawable()).getBitmap();
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void SendEmail() {
        try {
            String stringSenderEmail = "mymusicplayerserdem@gmail.com";
            String stringReceiverEmail = emailEditText.getText().toString();
            String stringPasswordSenderEmail = "serdem963214785";

            String stringHost = "smtp.gmail.com";

            Properties properties = System.getProperties();

            properties.put("mail.smtp.host", stringHost);
            properties.put("mail.smtp.port", "465");
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.auth", "true");

            javax.mail.Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(stringSenderEmail, stringPasswordSenderEmail);
                }
            });

            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(stringReceiverEmail));

            mimeMessage.setSubject("My Music Player Kayıt İşlemi");
            mimeMessage.setText("Merhabalar My Music Player'a başarılı bir şekilde kayıt olmuş bulunmaktasınız!\nSerdem İrdem");

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Transport.send(mimeMessage);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();

        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private boolean validation() {
        if (!usernameEditText.getText().toString().matches("^[A-Za-z ]+$")) {
            return false;
        }
        if (!surnameEditText.getText().toString().matches("^[A-Za-z]+$")) {
            return false;
        }
        if (!phoneEditText.getText().toString().matches("^[0-9]{10,13}$")) {
            return false;
        }
        if (!emailEditText.getText().toString().matches("^(.+)@(.+)$")) {
            return false;
        }
        if (passwordEditText.getText().toString().length() < 6) {
            return false;
        }
        if (!passwordAgainEditText.getText().toString().equals(passwordEditText.getText().toString())) {
            return false;
        }
        return true;
    }

    public abstract class TextValidator implements TextWatcher {
        private final TextView textView;

        public TextValidator(TextView textView) {
            this.textView = textView;
        }

        public abstract void validate(TextView textView, String text);

        @Override
        final public void afterTextChanged(Editable s) {
            String text = textView.getText().toString();
            validate(textView, text);
        }

        @Override
        final public void
        beforeTextChanged(CharSequence s, int start, int count, int after) {
            /* Needs to be implemented, but we are not using it. */
        }

        @Override
        final public void
        onTextChanged(CharSequence s, int start, int before, int count) {
            /* Needs to be implemented, but we are not using it. */
        }
    }

    void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    imageUri = selectedImageUri.toString();
                    registerImageButton.setImageURI(selectedImageUri);
                }
            }
        }
    }
}