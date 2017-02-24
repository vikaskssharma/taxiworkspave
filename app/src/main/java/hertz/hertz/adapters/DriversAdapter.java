package hertz.hertz.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import hertz.hertz.R;
import hertz.hertz.activities.CarManagementActivity;
import hertz.hertz.activities.DriverManagementActivity;
import hertz.hertz.fragments.AddCarDialogFragment;
import hertz.hertz.fragments.AddDriverDialogFragment;
import hertz.hertz.helpers.AppConstants;

/**
 * Created by rsbulanon on 11/23/15.
 */
public class DriversAdapter extends RecyclerView.Adapter<DriversAdapter.ViewHolder> {

    private ArrayList<ParseObject> drivers;
    private Context context;
    private DriverManagementActivity activity;

    public DriversAdapter(Context context, ArrayList<ParseObject> drivers) {
        this.context = context;
        this.drivers = drivers;
        this.activity = (DriverManagementActivity)context;
    }

    @Override
    public int getItemCount() {
        return drivers.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_driver, parent, false);
        ViewHolder  holder = new ViewHolder (v);
        return holder;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDriverName;
        TextView tvMobileNo;
        ImageView ivEdit;
        ImageView ivDelete;

        ViewHolder (View view) {
            super(view);
            tvDriverName = (TextView)view.findViewById(R.id.tvDriverName);
            tvMobileNo = (TextView)view.findViewById(R.id.tvMobileNo);
            ivEdit = (ImageView)view.findViewById(R.id.ivEdit);
            ivDelete = (ImageView)view.findViewById(R.id.ivDelete);
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int i) {
        final ParseObject driver = drivers.get(i).getParseObject("driver");
        final String firstName = driver.getString("firstName");
        final String lastName = driver.getString("lastName");
        final String mobileNo = driver.getString("mobileNo");

        holder.tvDriverName.setText(firstName + " " + lastName);
        holder.tvMobileNo.setText(mobileNo);

        /** edit driver profile */
        holder.ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AddDriverDialogFragment fragment = AddDriverDialogFragment.newInstance(driver);
                fragment.setOnAddDriverListener(new AddDriverDialogFragment.OnAddDriverListener() {

                    @Override
                    public void onAddUpdateStart() {
                        activity.showCustomProgress(AppConstants.LOAD_UPDATE_DRIVE);
                    }

                    @Override
                    public void onNewDriverAdded(ParseUser newDriver) {

                    }

                    @Override
                    public void onDriverRecordUpdated() {
                        activity.dismissCustomProgress();
                        fragment.dismiss();
                        activity.getDrivers();
                    }

                    @Override
                    public void onAddDriverFailed(ParseException e) {
                        activity.dismissCustomProgress();
                        fragment.dismiss();
                        if (e.getCode() == ParseException.EMAIL_TAKEN) {
                            activity.showSweetDialog(AppConstants.ERR_EMAIL_TAKEN, "error");
                        } else {
                            activity.showSweetDialog(e.getMessage(), "error");
                        }
                    }
                });
                fragment.show(activity.getSupportFragmentManager(),"driver");
            }
        });

        /** delete car */
        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity.isNetworkAvailable()) {
                    new SweetAlertDialog(context,SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Hertz")
                            .setContentText("Deleting this record will also vacate the car assigned to this" +
                                    " driver. Are you sure you want to proceed?")
                            .setConfirmText("Yes")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismiss();
                                    activity.showCustomProgress(AppConstants.LOAD_DELETING_DRIVER);
                                    if (driver.getParseObject("car") != null) {
                                        activity.updateCustomProgress(AppConstants.LOAD_VACATING_CAR);
                                        ParseObject car = driver.getParseObject("car");
                                        car.put("assignedTo", "");
                                        car.put("status", "available");
                                        car.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    deleteDriver(driver);
                                                } else {
                                                    activity.showSweetDialog(e.getMessage(), "error");
                                                }
                                            }
                                        });
                                    } else {
                                        deleteDriver(driver);
                                    }
                                }
                            })
                            .setCancelText("No")
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismiss();
                                }
                            })
                            .show();
                } else {
                    activity.showSweetDialog(AppConstants.ERR_CONNECTION, "error");
                }
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private void deleteDriver(ParseObject driver) {
        driver.put("status", "inactive");
        driver.remove("car");
        driver.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                activity.dismissCustomProgress();
                if (e == null) {
                    activity.getDrivers();
                } else {
                    activity.showSweetDialog(e.getMessage(), "error");
                }
            }
        });
    }
}
