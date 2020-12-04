package com.teamC.komok.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.mlkit.vision.face.Face
import com.teamC.komok.R
import com.teamC.komok.SwapActivity

class SwapFaceAdapter1(
    val context: Context,
    private val faces: MutableList<Face>,
    private val recyclerSelf: RecyclerView?,
    private val layoutNext: LinearLayout?
): RecyclerView.Adapter<SwapFaceAdapter1.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        return ViewHolder(inflater.inflate(R.layout.recycle_face_select, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textSwap.text = (position+1).toString()

        // tombol gambar wajah
        holder.cardSwap.setOnClickListener {
            nextRecycler(position)
        }
    }

    override fun getItemCount(): Int {
        return faces.size-1
    }

    fun nextRecycler(facePos: Int) {
        if (layoutNext != null) {
            (context as SwapActivity).swapFace1(layoutNext, facePos, true)
            if (recyclerSelf != null) {
                recyclerSelf.visibility = View.GONE
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardSwap: CardView = itemView.findViewById(R.id.card_face)
        val textSwap: TextView = itemView.findViewById(R.id.text_face)
    }
}