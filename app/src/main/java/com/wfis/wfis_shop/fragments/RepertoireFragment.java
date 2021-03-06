package com.wfis.wfis_shop.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.squareup.picasso.Picasso;
import com.wfis.wfis_shop.Common.Common;
import com.wfis.wfis_shop.Common.ConfigPayPal;
import com.wfis.wfis_shop.Interface.ItemClickListener;
import com.wfis.wfis_shop.R;
import com.wfis.wfis_shop.ViewHolder.DataViewHolder;
import com.wfis.wfis_shop.ViewHolder.RepertoireViewHolder;
import com.wfis.wfis_shop.ViewHolder.SiteViewHolder;
import com.wfis.wfis_shop.core.BaseFragment;
import com.wfis.wfis_shop.models.DataModel;
import com.wfis.wfis_shop.models.MyTicketsModel;
import com.wfis.wfis_shop.models.RepertoireModel;
import com.wfis.wfis_shop.models.SiteModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class RepertoireFragment extends BaseFragment {

    private FirebaseRecyclerAdapter<RepertoireModel,RepertoireViewHolder> adapter;
    private FirebaseRecyclerAdapter<DataModel, DataViewHolder> adapterDate;
    private FirebaseRecyclerAdapter<SiteModel,SiteViewHolder> adapterSite;
    private View view;
    private ImageView imageView;
    private FirebaseDatabase database;
    private DatabaseReference repertuar;
    private RecyclerView recyclerRepertuar;
    private RecyclerView.LayoutManager layoutManager;
    String chooseFilm,seatId, titleFilm, dataFilm, timeFilm, price, price2;
    private FirebaseAuth mAuth;
    private TextView txtEepertuarPrice, txtRepertuarInfo;
    private Button btnBuyTickets;
    Set<String> set;

    //paypal
    private static PayPalConfiguration confing = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)//use sandbox when test, later change
            .clientId(ConfigPayPal.PAYPAL_CLIENT_ID);
    private static final int PAYPAL_REQUEST_CODE=9999;


    public static RepertoireFragment newInstance() {

        Bundle args = new Bundle();

        RepertoireFragment fragment = new RepertoireFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_repertoire, container, false);

        txtRepertuarInfo = view.findViewById(R.id.repertuarInfo);
        txtEepertuarPrice = view.findViewById(R.id.repertuarPrice);
        btnBuyTickets = view.findViewById(R.id.btnBuyTickets);
        database= FirebaseDatabase.getInstance();
        repertuar = database.getReference(Common.city+"/"+"Repertual");
        imageView = view.findViewById(R.id.LogoImageView);
        recyclerRepertuar=(RecyclerView)view.findViewById(R.id.repertuaRecyclerMenu);
        recyclerRepertuar.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(getContext(),2);
        recyclerRepertuar.setLayoutManager(layoutManager);
        mAuth = FirebaseAuth.getInstance();

        set = new HashSet<String>();

        //int paypal
        Intent intent = new Intent(getActivity(), PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,confing);
        getActivity().startService(intent);


        loadMenu();
        return view;
    }

    private void loadMenu() {
        final Context context = getContext();
        adapter = new FirebaseRecyclerAdapter<RepertoireModel, RepertoireViewHolder>(RepertoireModel.class,R.layout.menu_repertuar,RepertoireViewHolder.class,repertuar) {
            @Override
            protected void populateViewHolder(RepertoireViewHolder viewHolder, final RepertoireModel model, int position) {
                viewHolder.txtMenuName.setText(model.getTytul());
                Picasso.with(getActivity().getBaseContext()).load(model.getUrl()).into(viewHolder.imageView);
                final RepertoireModel clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        chooseFilm = Integer.toString(position+1); // pobranie id filmu i wrzucenie do stringa, +1 bo rekordy w bazie danych zaczynaja sie od 1 a w na liscie od 0
                        imageView.setVisibility(View.VISIBLE);
                        txtRepertuarInfo.setVisibility(View.VISIBLE);
                        titleFilm= model.getTytul();
                        price = model.getPrice();
                        price2=model.getPrice2();
                        //txtRepertuarInfo.setText(model.getOpis());
                        txtRepertuarInfo.setText("Regular ticket: "+price+" $"+ "\nConcession ticket: "+price2+" $"+"\n"+model.getOpis());
                        txtRepertuarInfo.setMovementMethod(new ScrollingMovementMethod());
                        Picasso.with(getActivity().getBaseContext()).load(model.getUrl()).into(imageView);
                        loadDate();

                    }
                });
            }
        };
        recyclerRepertuar.setAdapter(adapter);
    }

    private void loadDate()
    {
        DatabaseReference data = database.getReference(Common.city+"/"+"idData");
        adapterDate = new FirebaseRecyclerAdapter<DataModel, DataViewHolder>(DataModel.class,R.layout.menu_id_data,DataViewHolder.class,data.orderByChild("idTytul").equalTo(chooseFilm)) {//like select from data where idTytuł =
            @Override
            protected void populateViewHolder(DataViewHolder viewHolder, final DataModel model, int position) {
                viewHolder.txtDataName.setText(model.getData());
                viewHolder.txtDataGodzina.setText(model.getGodzina());
                timeFilm=model.getGodzina();
                dataFilm=model.getData();

                final DataModel local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        seatId = "0" +model.getIdMiejsce(); //Integer.toString(position+1);
                        txtRepertuarInfo.setVisibility(View.GONE);
                        loadSites();
                    }
                });
            }
        };
        recyclerRepertuar.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerRepertuar.setAdapter(adapterDate);
    }

    private void loadSites()
    {
        final DatabaseReference data = database.getReference(Common.city+"/"+"idMiejsce/" + seatId );
        btnBuyTickets.setVisibility(View.VISIBLE);
        //txtEepertuarPrice.setVisibility(View.VISIBLE);

        adapterSite = new FirebaseRecyclerAdapter<SiteModel, SiteViewHolder>(SiteModel.class,R.layout.menu_id_data_list,SiteViewHolder.class,data) {
            @Override
            protected void populateViewHolder(final SiteViewHolder viewHolder,  SiteModel model, final int position) {
                viewHolder.textView.setText(Integer.toString(position+1));
                final String status = model.getStatus();
                final String IdKupujacego = model.getIdKupujacego();
                final FirebaseUser currentUser = mAuth.getCurrentUser();
                boolean user=false;
                String position2= Integer.toString(position+1);

                if (currentUser != null)
                    user =model.getIdKupujacego().equals(currentUser.getEmail().toString());
                if( user && model.getStatus().equals("3") ){
                    viewHolder.textView.setBackgroundColor(Color.BLUE);
                    set.add(position2);
                    try {
                        int temp1 = Integer.parseInt(txtEepertuarPrice.getText().toString());
                        int temp2 = Integer.parseInt(price.toString());
                        int temp3=temp1+temp2;
                        txtEepertuarPrice.setText(Integer.toString(temp3));
                    } catch(NumberFormatException e) {
                        Toast.makeText(getContext(),e.toString(),Toast.LENGTH_SHORT).show();
                    }
                }
                else if(model.getStatus().equals("0")) {
                    viewHolder.textView.setBackgroundColor(Color.GREEN);
                }
                else if(model.getStatus().equals("2") && user) {
                    viewHolder.textView.setBackgroundColor(Color.CYAN);
                }
                else if(model.getStatus().equals("2")) {
                    viewHolder.textView.setBackgroundColor(Color.RED);
                }
                else {
                    viewHolder.textView.setBackgroundColor(Color.YELLOW);
                }

                viewHolder.setItemClickListener(new ItemClickListener() {

                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        String position1 = Integer.toString(position+1);
                        if (currentUser != null)
                        {
                            if((status.equals("3")) && IdKupujacego.equals(currentUser.getEmail().toString()) ) {
                                SiteModel model= new SiteModel("0","0","0");
                                data.child(position1).setValue(model);
                                viewHolder.textView.setBackgroundColor(Color.GREEN);
                                Toast.makeText(getContext(),"you canceled the place reservation "+position1,Toast.LENGTH_SHORT).show();



                                int temp1 = Integer.parseInt(txtEepertuarPrice.getText().toString());
                                int temp2 = Integer.parseInt(price.toString());
                                int temp3=temp1-temp2;
                                txtEepertuarPrice.setText(Integer.toString(temp3));

                                set.remove(position1);
                                try
                                {
                                    database.getReference().child(Common.city+"/"+"myTickets/"+currentUser.getUid()+"/"+seatId+position1).removeValue();

                                }
                                catch(Exception e)
                                {
                                    Toast.makeText(getContext(),e.toString(),Toast.LENGTH_SHORT).show();
                                }
                            }
                            else if(status.equals("0")) {

                                SiteModel model= new SiteModel(currentUser.getEmail().toString(),"3","0");
                                data.child(position1).setValue(model);
                                viewHolder.textView.setBackgroundColor(Color.BLUE);
                                Toast.makeText(getContext(),"you made a reservation for place "+position1,Toast.LENGTH_SHORT).show();
                                final MyTicketsModel myTicketsModel = new MyTicketsModel(titleFilm,dataFilm,timeFilm, position1, "Reserved", seatId, price, price2);
                                set.add(position1);
                                try
                                {                                                                           //.push()
                                    database.getReference().child(Common.city+"/"+"myTickets/"+currentUser.getUid()+"/"+seatId+position1).setValue(myTicketsModel);

                                }
                                catch(Exception e)
                                {
                                    Toast.makeText(getContext(),e.toString(),Toast.LENGTH_SHORT).show();
                                }

                            }
                        }
                        else {
                            Toast.makeText(getContext(),"Please log in",Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                btnBuyTickets.setOnClickListener(view -> {
                    if(currentUser!=null)
                    {
                        getNavigation().changeFragment(MyTicketsFragment.newInstance());
                    }
                    else
                    {
                        Toast.makeText(getContext(),"Please log in",Toast.LENGTH_SHORT).show();
                    }

                });

            }
        };
        recyclerRepertuar.setLayoutManager(new GridLayoutManager(getContext(),10));
        recyclerRepertuar.setAdapter(adapterSite);


    }
    private void startPayment(int amount) {
        PayPalPayment payment = new PayPalPayment(new BigDecimal(String.valueOf(amount)),"USD","Theater Payment",PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(getContext(), PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, confing);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT,payment);
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==PAYPAL_REQUEST_CODE)
        {
            if(resultCode== getActivity().RESULT_OK)
            {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null)
                {
                    try
                    {
                        String  paymentDetails =    confirmation.toJSONObject().toString(4);
                        JSONObject jsonObject = new JSONObject(paymentDetails);
                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                    for(String i : set)
                    {
                        Toast.makeText(getContext(),i,Toast.LENGTH_SHORT).show();
                        database.getReference().child(Common.city+"/"+"myTickets/"+mAuth.getCurrentUser().getUid()+"/"+seatId+i).child("status").setValue("Bought");
                        DatabaseReference dataTmp = database.getReference(Common.city+"/"+"idMiejsce/" + seatId );
                        dataTmp.child(i).child("status").setValue("2");
                        txtEepertuarPrice.setText("0");
                    }

                }
            }
            else if (resultCode == Activity.RESULT_CANCELED )
            {
                Toast.makeText(getContext(),"Payments Failed",Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            if (requestCode == PaymentActivity.RESULT_EXTRAS_INVALID)
                Toast.makeText(getContext(), "Payments Invalid", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Repertoire");
        //getActivity().setTitle(getString(R.string.category_repertoire));
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //loadMenu();
    }

}



