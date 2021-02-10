package com.platzi.android.firestore.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.platzi.android.firestore.R
import com.platzi.android.firestore.model.Crypto
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.crypto_row.view.*
import java.util.ArrayList

class CryptosAdapter(val cryptosAdapterListener: CryptosAdapterListener) :
    RecyclerView.Adapter<CryptosAdapter.ViewHolder>() {

    var cryptoList: List<Crypto> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val view =  LayoutInflater.from(parent.context).inflate(R.layout.crypto_row,parent,false)

        return  ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  cryptoList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val crypto = cryptoList[position]

        Picasso.get().load(crypto.imageUrl).into(holder.image)

        holder.textViewNAme.text = crypto.name
        holder.textViewAvailable.text = holder.itemView.context.getString(R.string.available_message, crypto.available.toString())

        holder.buttonBuy.setOnClickListener {
            cryptosAdapterListener.onBuyCryptoClicked(crypto)
        }
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var image = view.findViewById<ImageView>(R.id.image)
        var textViewNAme = view.findViewById<TextView>(R.id.nameTextView)
        var textViewAvailable = view.findViewById<TextView>(R.id.availableTextView)
        var buttonBuy = view.findViewById<Button>(R.id.buyButton)

    }


}