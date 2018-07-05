package jorgeluis.androidsandbox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class StartActivity extends AppCompatActivity implements View.OnClickListener{

    private View mContentView;
    private boolean mVisible;
    private FirebaseAuth mAuth;
    private SignInButton signInButton;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInAccount account;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mContentView = findViewById(R.id.fullscreen_content);

        mGoogleApiClient = Utils.getGoogleApiClient(getString(R.string.default_web_client_id), this);
        mAuth = FirebaseAuth.getInstance();
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }


    private void signIn() {
        signInButton.setEnabled(false);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                account = result.getSignInAccount();

                SharedPreferences sharedPref = getSharedPreferences("android_sandbox", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();

                editor.putString("user_id", account.getId());
                editor.putString("user_email", account.getEmail());
                editor.putString("user_displayname", account.getDisplayName());
                editor.commit();

                firebaseAuthWithGoogle(account);
            } else {

                signInButton.setEnabled(true);
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Dev", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            startMain();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Dev", "signInWithCredential:failure", task.getException());
                            Toast.makeText(StartActivity.this, "Ocurrio un error al intentar Autenticarser, por favor, intente mas tarde.",
                                    Toast.LENGTH_SHORT).show();

                            signInButton.setEnabled(true);
                        }
                    }
                });
    }

    private void startMain(){
        Intent MainActivityIntent = new Intent().setClass(
                StartActivity.this, MainActivity.class
        );
        startActivity(MainActivityIntent);
        finish();
    }

    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            SharedPreferences sharedPref = getSharedPreferences("android_sandbox", Context.MODE_PRIVATE);
            String user_id = sharedPref.getString("user_id", null);

            // Identificamos al usuario para los Reportes Crash
            Crashlytics.setUserIdentifier(currentUser.getEmail());
            startMain();
        }
    }
}
