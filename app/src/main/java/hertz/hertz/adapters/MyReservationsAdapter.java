package hertz.hertz.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Calendar;

import hertz.hertz.R;

/**
 * Created by rsbulanon on 12/2/15.
 */
public class MyReservationsAdapter extends RecyclerView.Adapter<MyReservationsAdapter.ViewHolder> {

    private ArrayList<ParseObject> tx;
    private Context context;
    private OnTxClickListener onTxClickListener;
    private String[] months = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug",
                                            "Sep","Oct","Nov","Dec"};

    public MyReservationsAdapter(Context context, ArrayList<ParseObject> tx) {
        this.context = context;
        this.tx = tx;
    }

    @Override
    public int getItemCount() {
        return tx.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_my_reservations, parent, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llReservations;
        TextView tvMonth;
        TextView tvDay;
        TextView tvYear;
        TextView tvBookingId;
        TextView tvFrom;
        TextView tvTo;
        TextView tvStatus;

        ViewHolder(View view) {
            super(view);
            llReservations = (LinearLayout)view.findViewById(R.id.llReservations);
            tvMonth = (TextView)view.findViewById(R.id.tvMonth);
            tvDay = (TextView)view.findViewById(R.id.tvDay);
            tvYear = (TextView)view.findViewById(R.id.tvYear);
            tvBookingId = (TextView)view.findViewById(R.id.tvBookingId);
            tvFrom = (TextView)view.findViewById(R.id.tvFrom);
            tvTo = (TextView)view.findViewById(R.id.tvTo);
            tvStatus = (TextView)view.findViewById(R.id.tvStatus);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int i) {
        final ParseObject obj = tx.get(i);
        Calendar cal = Calendar.getInstance();
        cal.setTime(obj.getCreatedAt());
        holder.tvDay.setText(cal.get(Calendar.DAY_OF_MONTH) + "");
        holder.tvYear.setText(cal.get(Calendar.YEAR) + "");
        holder.tvMonth.setText(months[cal.get(Calendar.MONTH)]);
        holder.tvBookingId.setText(obj.getObjectId());
        holder.tvFrom.setText(obj.getString("from"));
        holder.tvTo.setText(obj.getString("to"));
        holder.tvStatus.setText(obj.getString("status"));
        holder.llReservations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTxClickListener.onSelected(obj);
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public interface OnTxClickListener {
        void onSelected(ParseObject parseObject);
    }

    public void setOnTxClickListener(OnTxClickListener onTxClickListener) {
        this.onTxClickListener = onTxClickListener;
    }
}
