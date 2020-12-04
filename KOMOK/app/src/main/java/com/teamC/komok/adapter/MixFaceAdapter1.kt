package com.teamC.komok.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.mlkit.vision.face.Face
import com.teamC.komok.MixActivity
import com.teamC.komok.R
import com.teamC.komok.utils.DrawUtils

class MixFaceAdapter1(
    val context: Context,
    private val bitmap: Bitmap,
    private val faces: MutableList<Face>,
    private val recyclerSelf: RecyclerView?,
    private val layoutNext: LinearLayout?
    ): RecyclerView.Adapter<MixFaceAdapter1.ViewHolder>() {

    private val drawUtils = DrawUtils()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        return ViewHolder(inflater.inflate(R.layout.recycle_face_select, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bmp = drawUtils.cropShapeFaces(bitmap, faces[position])
        Glide.with(context)
            .load(bmp)
            .into(holder.imageSelect)

        holder.cardSelect.setOnClickListener {
            if (layoutNext != null) {
                (context as MixActivity).mixFace1(layoutNext, position)
                if (recyclerSelf != null) {
                    recyclerSelf.visibility = View.GONE
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return faces.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardSelect: CardView = itemView.findViewById(R.id.card_face)
        val imageSelect: ImageView = itemView.findViewById(R.id.image_face)
    }
}