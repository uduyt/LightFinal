package com.witcode.light.light.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.witcode.light.light.R;

import org.json.JSONException;
import org.json.JSONObject;

import com.witcode.light.light.backend.LoginTask;
import com.witcode.light.light.backend.MyServerClass;
import com.witcode.light.light.backend.OnTaskCompletedListener;

public class LoginActivity extends AppCompatActivity {
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private TextView tvLogin;
    private Button btLoginButton;
    private LinearLayout llLogin, llLogo;
    private ProfileTracker mProfileTracker;
    private Context mContext = this;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String TAG = "tagg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.setApplicationId(getResources().getString(R.string.facebook_app_id));
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_login);


        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null && Profile.getCurrentProfile() != null) {
            GoToMainActivity();
        } else {
            LoginManager.getInstance().logOut();
            FirebaseAuth.getInstance().signOut();
        }
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

        loginButton = (LoginButton) findViewById(R.id.lb_facebook_login);
        loginButton.setReadPermissions("email", "public_profile");

        btLoginButton = (Button) findViewById(R.id.bt_facebook_login);
        btLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.performClick();
                findViewById(R.id.fl_login_pb).setVisibility(View.VISIBLE);
            }
        });
        // Other app specific specialization
        callbackManager = CallbackManager.Factory.create();
        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(final LoginResult loginResult) {

                if (Profile.getCurrentProfile() == null) {
                    mProfileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(final Profile profile, final Profile profile2) {
                            // profile2 is the new profile
                            Log.v("facebook - profile", profile2.getFirstName());
                            mProfileTracker.stopTracking();
                            handleFacebookAccessToken(AccessToken.getCurrentAccessToken());
                        }
                    };
                    mProfileTracker.startTracking();
                    // no need to call startTracking() on mProfileTracker
                    // because it is called by its constructor, internally.
                } else {
                    handleFacebookAccessToken(AccessToken.getCurrentAccessToken());
                }
            }

            @Override
            public void onCancel() {
                Toast.makeText(mContext, "cancel", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                findViewById(R.id.fl_login_pb).setVisibility(View.GONE);

                if(MyServerClass.isConnected(mContext)){
                    Toast.makeText(mContext, "error al iniciar sesion en facebook", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(mContext, "Necesitas estar conectado a internet para iniciar sesión", Toast.LENGTH_LONG).show();
                }


            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void LogIn() {


        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.v("LoginActivity", response.toString());

                        // Application code
                        try {
                            String email = object.getString("email");
                            String gender = object.getString("gender");

                            new LoginTask(mContext,gender, email, new OnTaskCompletedListener() {
                                @Override
                                public void OnComplete(String result, int resultCode, int resultType) {
                                    Log.v("tagg", "came back with: " + resultCode);
                                    findViewById(R.id.fl_login_pb).setVisibility(View.GONE);
                                    if (resultCode == LoginTask.SUCCESSFUL) {
                                        GoToMainActivity();
                                    } else if (resultCode == LoginTask.NEED_UPDATE) {
                                        FirebaseAuth.getInstance().signOut();
                                        LoginManager.getInstance().logOut();
                                        btLoginButton.setEnabled(true);

                                        MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                                                .title("Hay una nueva versión de la aplicación")
                                                .content("Debes instalar la nueva versión de la app para seguir usándola")
                                                .positiveText("Ir a google play")
                                                .contentColor(Color.parseColor("#ffffff"))
                                                .titleColor(Color.parseColor("#ffffff"))
                                                .backgroundColor(getResources().getColor(R.color.PrimaryDark))
                                                .negativeText("Cancelar")
                                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                                    @Override
                                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                        dialog.dismiss();
                                                    }
                                                })
                                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                    @Override
                                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                                        intent.setData(Uri.parse("market://details?id=com.witcode.light.light"));
                                                        mContext.startActivity(intent);
                                                    }
                                                }).build();
                                        dialog.show();
                                    }
                                }
                            }).execute();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle permissions = new Bundle();
        permissions.putString("fields", "email,gender");
        request.setParameters(permissions);

        request.executeAsync();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        btLoginButton.setEnabled(false);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        LogIn();

                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void GoToMainActivity() {
        if (mAuth.getCurrentUser() == null) {
            Log.d("login", "googlenull");
        }
        if (Profile.getCurrentProfile() == null) {
            Log.d("login", "facebooknull");
        }
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivity(intent);
        finish();
    }
}

