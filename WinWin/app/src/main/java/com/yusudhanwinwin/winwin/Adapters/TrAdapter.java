package com.yusudhanwinwin.winwin.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yusudhanwinwin.winwin.Models.TrModel;
import com.yusudhanwinwin.winwin.R;
import com.yusudhanwinwin.winwin.databinding.ItemRedeemBinding;

import java.util.ArrayList;

public class TrAdapter  extends RecyclerView.Adapter<TrAdapter.viewHolder>{

    Context context;
    ArrayList<TrModel> list;

    public TrAdapter(Context context, ArrayList<TrModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_redeem,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

        final TrModel model = list.get(position);

        String status = model.getStatus();
        String coin = model.getCoin();
        String method = model.getPaymentMethode();

        int currentCoin = Integer.parseInt(coin);

        double earn = currentCoin * 0.02;

        holder.binding.paymentMetho.setText(model.getPaymentMethode());
        holder.binding.paymentdetails.setText(model.getPaymentMethode());
        holder.binding.paymentDate.setText(model.getDate());
        holder.binding.earn.setText("(Rs. " + earn + ")");

        if(status.equals("false")){

            holder.binding.btnStatus.setText("Pending");
        }else{

            holder.binding.btnStatus.setText("Success");
            holder.binding.btnStatus.setBackgroundResource(R.drawable.btn_status);

        }

        if(method.equals("Paytm")){

            holder.binding.paymentMethodeLogo.setImageResource(R.drawable.paytm);

        } else if (method.equals("Amazon Gift")) {

            holder.binding.paymentMethodeLogo.setImageResource(R.drawable.amazon);

        } else if (method.equals("Paypal")) {

            holder.binding.paymentMethodeLogo.setImageResource(R.drawable.paypal);

        } else if (method.equals("Google Play Gift")) {

            holder.binding.paymentMethodeLogo.setImageResource(R.drawable.googleplay);

        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public  class viewHolder extends RecyclerView.ViewHolder{

        ItemRedeemBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);

            binding = ItemRedeemBinding.bind(itemView);
        }
    }
}
