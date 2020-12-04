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
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.mlkit.vision.face.Face
import com.teamC.komok.R
import com.teamC.komok.utils.DrawUtils

class ChooseFaceAdapter(
    val context: Context,
    private val bitmap: Bitmap,
    private val faces: MutableList<Face>,
    private val select: MutableList<Int>,
    private val textDialog: TextView?,
    private val chooseMin: Int=1
    ): RecyclerView.Adapter<ChooseFaceAdapter.ViewHolder>() {

    private val drawUtils = DrawUtils()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        return ViewHolder(inflater.inflate(R.layout.recycle_face_select, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bmp = drawUtils.cropShapeFaces(bitmap, faces[position])
        val filter = PorterDuffColorFilter(Color.argb(155, 185, 185, 185), PorterDuff.Mode.SRC_ATOP)

        if (select[position] == 0) {
            holder.imageFace.colorFilter = filter
        } else {
            holder.imageFace.colorFilter = null
        }
        Glide.with(context)
            .load(bmp)
            .into(holder.imageFace)

        holder.cardFace.setOnClickListener {
            var n = 0
            if (select[position] == 0) {
                n = 1
                select[position] = 1
                holder.imageFace.colorFilter = null
            } else {
                if (select.sum() != chooseMin) {
                    n = 1
                    select[position] = 0
                    holder.imageFace.colorFilter = filter
                } else {
                    Toast.makeText(context, "Need at least $chooseMin face", Toast.LENGTH_SHORT).show()
                }
            }

            if (n == 1) {
                Glide.with(context)
                    .load(bmp)
                    .into(holder.imageFace)
                if (textDialog != null) {
                    textDialog.text = "[${faces.size} FACE] [${select.sum()} SELECTED]"
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return select.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardFace: CardView = itemView.findViewById(R.id.card_face)
        val imageFace: ImageView = itemView.findViewById(R.id.image_face)
    }
}