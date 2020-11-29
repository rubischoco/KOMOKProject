package com.teamC.komok.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.mlkit.vision.face.Face
import com.teamC.komok.R
import com.teamC.komok.utils.DrawUtils

class MixFaceAdapter1(
    val context: Context,
    private val bitmap: Bitmap,
    private val faces: MutableList<Face>,
    private val select: MutableList<Int>,
    private val recyclerSelf: RecyclerView?,
    private val mixLayout: LinearLayout?
    ): RecyclerView.Adapter<MixFaceAdapter1.ViewHolder>() {

    private val drawUtils = DrawUtils()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        return ViewHolder(inflater.inflate(R.layout.recycle_face_select, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bmp = drawUtils.cropShapeFaces(bitmap, faces[position])
        val filter = PorterDuffColorFilter(Color.argb(155, 185, 185, 185), PorterDuff.Mode.SRC_ATOP)

        holder.imageFace.colorFilter = filter
        Glide.with(context)
            .load(bmp)
            .into(holder.imageFace)

        holder.cardFace.setOnClickListener {
            if (mixLayout != null) {
                select.apply {
                    remove(1)
                    add(0)
                }
                select[position] = 1

                if (recyclerSelf != null) {
                    recyclerSelf.visibility = View.GONE
                }
                mixLayout.visibility = View.VISIBLE
                Glide.with(context)
                    .load(bmp)
                    .into(mixLayout.findViewById(R.id.image_select))
            }
        }
    }

    override fun getItemCount(): Int {
        return faces.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardFace: CardView = itemView.findViewById(R.id.card_face)
        val imageFace: ImageView = itemView.findViewById(R.id.image_face)
    }
}