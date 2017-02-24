package hertz.hertz.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.parse.ParsePush;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hertz.hertz.R;
import hertz.hertz.activities.BaseActivity;
import hertz.hertz.adapters.ChatAdapter;
import hertz.hertz.helpers.AppConstants;
import hertz.hertz.model.Chat;

/**
 * Created by rsbulanon on 11/17/15.
 */
public class ChatDialogFragment extends DialogFragment {

    @Bind(R.id.tvHeader) TextView tvHeader;
    @Bind(R.id.etMessage) EditText etMessage;
    @Bind(R.id.lvChat) ListView lvChat;
    private View view;
    private BaseActivity activity;
    private String recipient;
    private String room;
    private ArrayList<Chat> chats = new ArrayList<>();
    private OnDismissListener onDismissListener;
    private String header;

    public static ChatDialogFragment newInstance(String room, String recipient, String header) {
        ChatDialogFragment frag = new ChatDialogFragment();
        frag.recipient = recipient;
        frag.header = header;
        frag.room = room;
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_chat, null);
        ButterKnife.bind(this, view);
        activity = (BaseActivity)getActivity();
        tvHeader.setText(header);
        lvChat.setAdapter(new ChatAdapter(getActivity(), chats));
        listenToRoom(room);
        final Dialog mDialog = new Dialog(getActivity());
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        return mDialog;
    }

    @OnClick(R.id.btnSend)
    public void sendChat() {
        if (activity.isNetworkAvailable()) {
            if (!etMessage.getText().toString().isEmpty()) {
                Log.d("push", "push sent to --> " + recipient);
                final JSONObject data = new JSONObject();
                final JSONObject conv = new JSONObject();
                try {
                    /** chat data */
                    conv.put("room",room);
                    conv.put("message",etMessage.getText().toString());
                    conv.put("sender", (ParseUser.getCurrentUser().getString("userRole").equals("driver") ? "D" : "C")
                                            + ParseUser.getCurrentUser().getObjectId());
                    conv.put("senderName",ParseUser.getCurrentUser().getString("firstName")+ " "
                                    + ParseUser.getCurrentUser().getString("lastName"));
                    conv.put("timestamp", Calendar.getInstance().getTime().toString());
                    data.put("json", conv);
                    Chat chat = new Chat(room, recipient, etMessage.getText().toString(),
                            activity.getSDFWithTime().format(Calendar.getInstance().getTime()));
                    AppConstants.FIREBASE.child("Chat").child(room).push().setValue(chat);

                    /** push notification message */
                    ParsePush push = new ParsePush();
                    push.setChannel(recipient);
                    try {
                        data.put("alert", "You received message from " +
                                ParseUser.getCurrentUser().getString("firstName")
                                + " " + ParseUser.getCurrentUser().getString("lastName"));
                        push.setData(data);
                        push.sendInBackground();
                        Log.d("push","push successfully sent!");
                    } catch (JSONException e) {
                        Log.d("push","failed to send push --> " + e.getMessage());
                        e.printStackTrace();
                    }
                    etMessage.setText("");
                } catch (JSONException e) {
                    Log.d("push","error in creating json --> " + e.toString());
                    e.printStackTrace();
                }
            } else {
                activity.setError(etMessage,AppConstants.WARN_PLEASE_ENTER_YOUR_MESSAGE);
            }
        } else {
            activity.showSweetDialog(AppConstants.ERR_CONNECTION, "error");
        }
    }

    public void listenToRoom(String room) {
        AppConstants.FIREBASE.child("Chat").child(room).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                chats.add(dataSnapshot.getValue(Chat.class));
                ((BaseAdapter)lvChat.getAdapter()).notifyDataSetChanged();
                lvChat.post(new Runnable() {
                    @Override
                    public void run() {
                        lvChat.setSelection(lvChat.getAdapter().getCount() - 1);
                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public interface OnDismissListener {
        void onDismiss();
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        onDismissListener.onDismiss();
        super.onDismiss(dialog);
        Log.d("push","on dismiss");
    }
}
