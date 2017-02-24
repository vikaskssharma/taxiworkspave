package hertz.hertz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import hertz.hertz.R;
import hertz.hertz.helpers.AppConstants;


public class CRegistrationActivity extends BaseActivity {

    @Bind(R.id.etFistName) EditText etFirstName;
    @Bind(R.id.etLastName) EditText etLastName;
    @Bind(R.id.etAge) EditText etAge;
    @Bind(R.id.etMobile) EditText etMobile;
    @Bind(R.id.etEmail) EditText etEmail;
    @Bind(R.id.etPassword) EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btnRegister)
    public void registration() {
        String firstName = etFirstName.getText().toString();
        String lastName = etLastName.getText().toString();
        String mobile = etMobile.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String strAge = etAge.getText().toString();
        int age = (strAge.trim().isEmpty()) ? 0 : Integer.parseInt(strAge);

        /** validations */
        if (!isNetworkAvailable()) {
            showSweetDialog(AppConstants.ERR_CONNECTION, "error", false, null, null);
        } else if (firstName.isEmpty()) {
            setError(etFirstName, AppConstants.WARN_FIELD_REQUIRED);
        } else if (lastName.isEmpty()) {
            setError(etLastName, AppConstants.WARN_FIELD_REQUIRED);
        } else if (mobile.isEmpty()) {
            setError(etMobile, AppConstants.WARN_FIELD_REQUIRED);
        } else if (email.isEmpty()) {
            setError(etEmail, AppConstants.WARN_FIELD_REQUIRED);
        } else if (password.isEmpty()) {
            setError(etPassword, AppConstants.WARN_FIELD_REQUIRED);
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            setError(etEmail, AppConstants.WARN_INVALID_EMAIL_FORMAT);
        } else {
            /** Create new parse user */
            ParseUser user = new ParseUser();
            user.setEmail(email);
            user.setUsername(email);
            user.setPassword(password);
            user.put("firstName", firstName);
            user.put("lastName", lastName);
            user.put("mobileNo", mobile);
            user.put("age",age);
            user.put("userRole","user");
            showCustomProgress(AppConstants.LOAD_CREATE_ACCOUNT);
            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    dismissCustomProgress();
                    if (e == null) {
                        new SweetAlertDialog(CRegistrationActivity.this,SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Hertz")
                                .setContentText(AppConstants.OK_ACCOUNT_CREATED)
                                .setConfirmText("Close")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        ParseUser.logOut();
                                        sweetAlertDialog.dismiss();
                                        startActivity(new Intent(CRegistrationActivity.this, CLoginActivity.class));
                                        animateToLeft(CRegistrationActivity.this);
                                        finish();
                                    }
                                }).show();
                    } else {
                        if (e.getCode() == ParseException.EMAIL_TAKEN) {
                            showSweetDialog(AppConstants.ERR_CREATE_ACCOUNT,"error",false, null, null);
                        } else {
                            showSweetDialog(e.getMessage(),"error",false, null, null);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        animateToRight(this);
    }
}
