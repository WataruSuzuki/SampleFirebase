package com.devjchankchan.samplefirebase;

import android.app.ProgressDialog;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.storage.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "(・∀・)";

    static final String email = "youremail";
    static final String password = "yourpassword";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference imageRef;
    private File galleryFile;
    private String imagePath;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        setupAuthentic();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        signInUser();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void setupAuthentic() {
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    private void signInUser() {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                        } else {
                            fileDownload();
                        }
                    }
                });
    }

    private void fileDownload() {
        // Prepare to download file.
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://example-devjchankchan.appspot.com/");
        imageRef = storageRef.child("neko.png");

        // ダウンロード中のダイアログ
        progress = new ProgressDialog(MainActivity.this);

        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setMessage("Downloading...");
        progress.show();

        // ギャラリー用に内部ストレージにフォルダを作成
        String firebaseImageDir = Environment.getExternalStorageDirectory().getPath() + "/firebase";
        File file = new File(firebaseImageDir);
        // ディレクトリ初期作成
        if (!file.exists()) {
            if (file.mkdir() == false) {
                Log.i(TAG, "Can't create directory");
                return;
            }
        }

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        imagePath = firebaseImageDir + "/" + sf.format(cal.getTime()) + ".png";
        galleryFile = new File(imagePath);

        imageRef.getFile(galleryFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        // ダウンロード中の表示解除
                        progress.dismiss();

                        // ギャラリーに反映
                        MediaScannerConnection.scanFile(
                                MainActivity.this,
                                new String[] { imagePath },
                                new String[] { "image/png" },
                                null);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG,"Fail to download");
                Log.i(TAG,e.getMessage());
                progress.dismiss();
                Toast.makeText(MainActivity.this,"Fail to download",Toast.LENGTH_LONG).show();
            }
        });
    }
}

