package hertz.hertz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import hertz.hertz.R;
import hertz.hertz.fragments.ResendForgotDialogFragment;
import hertz.hertz.helpers.AppConstants;
import hertz.hertz.model.User;

public class CLoginActivity extends BaseActivity {

    @Bind(R.id.etEmail) EditText etEmail;
    @Bind(R.id.etPassword) EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_2);
        ButterKnife.bind(this);
        if (ParseUser.getCurrentUser() != null) {
            if (ParseUser.getCurrentUser().getString("userRole").equals("superadmin")) {
                AppConstants.FULL_NAME = "Super Admin";
            } else {
                AppConstants.FULL_NAME = ParseUser.getCurrentUser().getString("firstName") + " " +
                        ParseUser.getCurrentUser().getString("lastName");
            }

            if (ParseUser.getCurrentUser().getString("userRole").equals("driver")) {
                startActivity(new Intent(CLoginActivity.this, DriverDashBoardActivity.class));
            } else if (ParseUser.getCurrentUser().getString("userRole").equals("superadmin")) {
                startActivity(new Intent(CLoginActivity.this, SuperAdminActivity.class));
            } else {
                startActivity(new Intent(CLoginActivity.this, HomeActivity.class));
            }
            showToast("Welcome " + AppConstants.FULL_NAME);
            animateToLeft(CLoginActivity.this);
            finish();
        }
    }

    @OnClick(R.id.btnLogin)
    public void login() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        /** validations */
        if (!isNetworkAvailable()) {
            showSweetDialog(AppConstants.ERR_CONNECTION, "error", false, null, null);
        } else if (email.isEmpty()) {
            setError(etEmail, AppConstants.WARN_FIELD_REQUIRED);
        } else if (password.isEmpty()) {
            setError(etPassword, AppConstants.WARN_FIELD_REQUIRED);
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            setError(etEmail, AppConstants.WARN_INVALID_EMAIL_FORMAT);
        } else {
            /** authenticate user's credentials */
            showCustomProgress(AppConstants.LOAD_LOGIN);
            ParseUser.logInInBackground(email, password, new LogInCallback() {
                @Override
                public void done(final ParseUser user, ParseException e) {
                    dismissCustomProgress();
                    if (e == null) {
                        if (user.getBoolean("emailVerified")) {
                            if (user.getString("userRole").equals("superadmin")) {
                                AppConstants.FULL_NAME = "Super Admin";
                            } else {
                                AppConstants.FULL_NAME = user.getString("firstName") + " " +
                                        user.getString("lastName");
                            }

                            new SweetAlertDialog(CLoginActivity.this,SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Hertz")
                                    .setContentText("Welcome " + AppConstants.FULL_NAME)
                                    .setConfirmText("Close")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            sweetAlertDialog.dismiss();
                                            if (user.getString("userRole").equals("driver")) {
                                                startActivity(new Intent(CLoginActivity.this, DriverDashBoardActivity.class));
                                            } else if (user.getString("userRole").equals("superadmin")) {
                                                startActivity(new Intent(CLoginActivity.this, SuperAdminActivity.class));
                                            } else {
                                                startActivity(new Intent(CLoginActivity.this, HomeActivity.class));
                                            }
                                            animateToLeft(CLoginActivity.this);
                                            finish();
                                        }
                                    }).show();
                        } else {
                            new SweetAlertDialog(CLoginActivity.this,SweetAlertDialog.WARNING_TYPE)
                                    .setTitleText("Hertz")
                                    .setContentText(AppConstants.ERR_EMAIL_NOT_VERIFIED)
                                    .setConfirmText("Resend Verification")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            sweetAlertDialog.dismiss();
                                            final ResendForgotDialogFragment frag = ResendForgotDialogFragment.newInstance("Resend Verification");
                                            frag.setOnRequestListenern(new ResendForgotDialogFragment.OnRequestListener() {
                                                @Override
                                                public void onRequest(String email) {
                                                    frag.dismiss();
                                                    showProgressDialog("Resending email verification link, Please wait...");
                                                    user.setEmail(email);
                                                    user.saveInBackground(new SaveCallback() {
                                                        @Override
                                                        public void done(ParseException e) {
                                                            dismissProgressDialog();
                                                            if (e == null) {
                                                                showSweetDialog("Verification link was successfully sent to your email",
                                                                        "success", false, null, null);
                                                            } else {
                                                                showSweetDialog("Your request failed, Please try again",
                                                                        "error",false,null,null);
                                                            }
                                                            ParseUser.logOut();
                                                        }
                                                    });
                                                }
                                            });
                                            frag.show(getFragmentManager(),"resend");
                                        }
                                    })
                                    .setCancelText("Close")
                                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            ParseUser.logOut();
                                            sweetAlertDialog.dismiss();
                                        }
                                    }).show();
                        }
                    } else {
                        showSweetDialog(e.getMessage(),"error",false,null,null);
                    }
                }
            });
        }
    }

    public interface GetUserCallback {
        public abstract void done(User returnedUser);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        animateToRight(this);
    }

    @OnClick(R.id.tvForgotPassword)
    public void forgotPassword() {
        final ResendForgotDialogFragment fragment = ResendForgotDialogFragment.newInstance("Forgot Password");
        fragment.setOnRequestListenern(new ResendForgotDialogFragment.OnRequestListener() {
            @Override
            public void onRequest(String email) {
                fragment.dismiss();
                showProgressDialog("Requesting for change password, Please wait...");
                ParseUser.requestPasswordResetInBackground(email,
                        new RequestPasswordResetCallback() {
                            public void done(ParseException e) {
                                dismissProgressDialog();
                                if (e == null) {
                                    showSweetDialog("An email was successfully sent with reset instructions",
                                            "success",false,null,null);
                                } else {
                                    showSweetDialog("Your request failed, Please try again",
                                            "error",false,null,null);
                                }
                            }
                        });
            }
        });
        fragment.show(getFragmentManager(),"forgot");
    }
}
