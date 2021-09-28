package ir.vebko.www.vebkoarremoteassistance.nodejs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ir.vebko.www.vebkoarremoteassistance.R;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    // ... constructor and member variables
    private List<Contact> mContacts;
    private NodejsActivity nodejsActivity;
    private Dialog dialog;
    private Button btnAggreeDialog;
    private Button btnCncelDialog;
    private EditText edtCustomName;
    private int recyclerClickPosition;

    // Pass in the contact array into the constructor
    public ContactsAdapter(List<Contact> contacts, NodejsActivity nodejsActivity) {
        mContacts = contacts;
        this.nodejsActivity = nodejsActivity;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_contact, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);

        dialog = new Dialog(nodejsActivity); // Context, this, etc.
        dialog.setContentView(R.layout.dialog_layout);
        btnAggreeDialog = dialog.findViewById(R.id.btnAggreeDialog);
        btnCncelDialog = dialog.findViewById(R.id.btnCncelDialog);
        edtCustomName = dialog.findViewById(R.id.edtCustomName);

        btnAggreeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nodejsActivity.addContactCustomName(edtCustomName.getText().toString(), recyclerClickPosition);
                dialog.dismiss();
            }
        });

        btnCncelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ContactsAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        Contact contact = mContacts.get(position);

        // Set item views based on your views and data model
        TextView textView = holder.nameTextView;
        textView.setText(contact.getName());

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nodejsActivity.addDestinationUniueIdToTextView(position);
            }
        });

        TextView txtAddCustomName = holder.txtAddCustomName;
        if (contact.getMyCustomName() != null) {
            if (!contact.getMyCustomName().equals("null")){
                txtAddCustomName.setText(contact.getMyCustomName());
            }
        }

        Button button = holder.btnAddCustomName;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                recyclerClickPosition = position;
            }
        });
//        button.setText(contact.isOnline() ? "Message" : "Offline");
//        button.setEnabled(contact.isOnline());
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public TextView txtAddCustomName;
        public Button btnAddCustomName;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.contact_name);
            txtAddCustomName = itemView.findViewById(R.id.txtAddCustomName);
            btnAddCustomName = (Button) itemView.findViewById(R.id.btnAddCustomName);
        }
    }
}