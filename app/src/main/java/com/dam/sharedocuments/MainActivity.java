package com.dam.sharedocuments;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.TokenWatcher;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.dam.sharedocuments.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    // View Binding
    private ActivityMainBinding binding;

    private Uri imageUri = null;

    private String textToShare = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Binding de la vue
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Gestion du clic pour ajouter une image
        binding.ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PickImage();
            }
        });

        // Gestion du clic sur share text
        binding.btnShareText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToShare = binding.tvMessage.getText().toString().trim();
                // Vérif si le champ est vide ou non
                if (TextUtils.isEmpty(textToShare)) {
                    showToast("Le message est vide");
                } else {
                    shareText();
                }
            }
        });

        binding.btnShareImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri == null) {
                    showToast("Il faut choisir une image");
                } else {
                    shareImage();
                }
            }
        });

        binding.btnShareBoth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get message
                textToShare = binding.tvMessage.getText().toString().trim();
                // Vérif si le champ est vide ou non et si il y a une image
                if (TextUtils.isEmpty(textToShare)) {
                    showToast("Le message est vide");
                // Get Image
                } else if (imageUri == null) {
                    showToast("Il faut choisir une image");
                } else {
                    shareBoth();
                }
            }
        });
    }

    public void PickImage() {
        // Intent pour sélectionner une image
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLaunsher.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLaunsher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // Gestion du retour de la sélection
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Image Picked
                        showToast("Image picked from gallery");
                        // Get Image
                        Intent data = result.getData();
                        imageUri = data.getData();
                        // Set image to imageView
                        binding.ivImage.setImageURI(imageUri);
                    } else {
                        // Canceled
                        showToast("Cancelled ...");
                    }
                }
            });

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void shareText() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Objet du message"); // Pour le partage par emails
        shareIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
        startActivity(Intent.createChooser(shareIntent, "Partager via"));
    }

    private void shareImage() {
        Uri localImageUri = getContentUri();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Objet du message"); // Pour le partage par emails
        shareIntent.putExtra(Intent.EXTRA_STREAM, localImageUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Partager via"));
    }

    private void shareBoth() {
        Uri localImageUri = getContentUri();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Objet du message"); // Pour le partage par emails
        shareIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
        shareIntent.putExtra(Intent.EXTRA_STREAM, localImageUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Partager via"));
    }

    private Uri getContentUri() {
        Bitmap bitmap = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageUri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            }
        } catch (Exception e) {
            showToast("" + e.getMessage());
        }
        // Si on envoi depuis drawable
        /* BitmapDrawable bitmapDrawable = (BitmapDrawable) binding.ivImage.getDrawable();
        bitmap = bitmapDrawable.getBitmap(); */

        File imagesFolder = new File(getCacheDir(), "images");
        Uri contentUri = null;
        try {
            imagesFolder.mkdirs(); // On cré les dossier s'il n'existe pas
            File file = new File(imagesFolder, "shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
            stream.flush();
            stream.close();
            contentUri = FileProvider.getUriForFile(this, "com.dam.sharedocuments.fileprovider", file);
        } catch (Exception e) {
            showToast("" + e.getMessage());
        }

        return contentUri;
    }


}