package jorgeluis.androidsandbox;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

public class Utils {

    private static GoogleApiClient mGoogleApiClient;
    private static GoogleSignInOptions gso;

    public static GoogleApiClient getGoogleApiClient(String TokenId, FragmentActivity context){
        if(gso==null){
            gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(TokenId)
                    .requestEmail()
                    .build();
        }

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .enableAutoManage(context, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        return mGoogleApiClient;
    }
}
