package com.example.admin.augscan;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class viewInventoryActivity extends AppCompatActivity {
    Button EditHs;
    ImageButton exportExcel;
    private FirebaseAuth firebaseAuth;
    RecyclerView mrecyclerview;
    DatabaseReference mdatabaseReference;
    private TextView totalnoofitem;
    private int counttotalnoofitem = 0;
    static final Integer WRITE_EXST = 0x3;
    static final Integer READ_EXST = 0x4;
    public Items item;
    List<Items> testList = new ArrayList<>(Arrays.asList(item));
    private ProgressDialog processDialog;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_inventory);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        processDialog = new ProgressDialog(this);

        EditHs = findViewById(R.id.EditHistory);
        EditHs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoUrl("https://docs.google.com/spreadsheets/d/1Sx9jiB_dyDh43RQzbYyKerKMG0An72GYFJCF13znWJ4/edit#gid=0");
            }
        });
        totalnoofitem = findViewById(R.id.totalnoitem);
        firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser users = firebaseAuth.getCurrentUser();
        String finalUser = users.getEmail();
        String resultEmail = finalUser.replace(".", "");
        mdatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child(resultEmail).child("Items");
        mrecyclerview = findViewById(R.id.recyclerViews);
        exportExcel = findViewById(R.id.exportExcel);
        progressBar = findViewById(R.id.progressBars);
        progressBar.setVisibility(View.GONE);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mrecyclerview.setLayoutManager(manager);
        mrecyclerview.setHasFixedSize(true);
        mrecyclerview.setLayoutManager(new LinearLayoutManager(this));
        exportExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE, READ_EXST);
                askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXST);
                ExcelExporter.export(testList);
                finish();
            }
        });

        mdatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    fetchData(dataSnapshot);
                    counttotalnoofitem = (int) dataSnapshot.getChildrenCount();
                    totalnoofitem.setText(Integer.toString(counttotalnoofitem));
                } else {
                    totalnoofitem.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void gotoUrl(String s) {
        Uri uri = Uri.parse(s);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    protected void onStart() {
        super.onStart();
        processDialog.setMessage("................Please Wait.............");
        processDialog.show();
        FirebaseRecyclerAdapter<Items, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Items, UsersViewHolder>
                (Items.class, R.layout.list_layout, UsersViewHolder.class, mdatabaseReference) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Items model, int position) {
                viewHolder.setDetails(
                        getApplicationContext(),
                        model.getItemBarcode(),
                        model.getItemCategory(),
                        model.getItemName(),
                        model.getItemPrice(),
                        model.getItemImg(),
                        model.getItemYear(),
                        model.getItemOrigin(),
                        model.getItemStatus()
                );
                model.setItem(model.getItemName(),
                        model.getItemCategory(),
                        model.getItemPrice(),
                        model.getItemBarcode(),
                        model.getItemImg(),
                        model.getItemYear(),
                        model.getItemOrigin(),
                        model.getItemStatus());
                processDialog.dismiss();
            }
        };
        mrecyclerview.setAdapter(firebaseRecyclerAdapter);
        exportExcel.setEnabled(true);
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(viewInventoryActivity.this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(viewInventoryActivity.this, permission)) {
                ActivityCompat.requestPermissions(viewInventoryActivity.this, new String[]{permission}, requestCode);
            } else {
                ActivityCompat.requestPermissions(viewInventoryActivity.this, new String[]{permission}, requestCode);
            }
            Toast.makeText(this, "Download Failed, Try Again!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Download Success", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Download Success. File in \\Download\\" + ExcelExporter.getFileName(), Toast.LENGTH_LONG).show();
        }
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        private final Items itemList = new Items();
        public void setDetails(
                Context ctx,
                final String itemBarcode,
                String itemCategory,
                String itemName,
                String itemPrice,
                String itemImg,
                String itemYear,
                String itemOrigin,
                String itemStatus
        ) {
            TextView item_barcode = (TextView) mView.findViewById(R.id.viewitembarcode);
            TextView item_name = (TextView) mView.findViewById(R.id.viewitemname);
            TextView item_category = (TextView) mView.findViewById(R.id.viewitemcategory);
            TextView item_price = (TextView) mView.findViewById(R.id.viewitemprice);
            TextView item_year = (TextView) mView.findViewById(R.id.viewItemYear);
            TextView item_origin = (TextView) mView.findViewById(R.id.viewItemOrigin);
            TextView item_status = (TextView) mView.findViewById(R.id.viewItemStatus);
            item_barcode.setText(itemBarcode);
            item_category.setText(itemCategory);
            item_name.setText(itemName);
            item_price.setText(itemPrice);
            item_year.setText(itemYear);
            item_origin.setText(itemOrigin);
            item_status.setText(itemStatus);
            this.itemList.setItem(
                    itemName,
                    itemCategory,
                    itemPrice,
                    itemBarcode,
                    itemImg,
                    itemYear,
                    itemOrigin,
                    itemStatus
            );
            Button editItem = mView.findViewById(R.id.buttonedit);
            editItem.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View view) {
                        Intent i;
                        i = new Intent(mView.getContext(), edititemActivity.class);
                        i.putExtra("itemBarcode", itemList.getItemBarcode());
                        i.putExtra("itemName", itemList.getItemName());
                        i.putExtra("itemCategory", itemList.getItemCategory());
                        i.putExtra("itemPrice", itemList.getItemPrice());
                        i.putExtra("itemYear", itemList.getItemYear());
                        i.putExtra("itemOrigin", itemList.getItemOrigin());
                        i.putExtra("itemStatus", itemList.getItemStatus());
                        view.getContext().startActivity(i);
                  }
            });
            Button btnView = mView.findViewById(R.id.buttonView);
                 btnView.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                       Intent i;
                       i = new Intent(mView.getContext(), ViewImagePopUp.class);
                       i.putExtra("itemBarcode", itemList.getItemBarcode());
                       view.getContext().startActivity(i);
                 }
            });
        }
    }

    private void fetchData(DataSnapshot dataSnapshot)
    {
        testList.clear(); // TODO khoi tao mang rong
        for(DataSnapshot postSnapShot:dataSnapshot.getChildren())
        {
            Items item = postSnapShot.getValue(Items.class);
            testList.add(item);
        }
    }
}
