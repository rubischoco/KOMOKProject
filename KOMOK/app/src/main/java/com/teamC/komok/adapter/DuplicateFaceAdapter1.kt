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
import com.teamC.komok.R
import com.teamC.komok.SwapActivity
import com.teamC.komok.utils.DrawUtils

class DuplicateFaceAdapter1(
    val context: Context,
    private val bitmap: Bitmap,
    private val faces: MutableList<Face>,
    private val recyclerSelf: RecyclerView?,
    private val layoutNext: LinearLayout?
): RecyclerView.Adapter<DuplicateFaceAdapter1.ViewHolder>() {

    private val drawUtils = DrawUtils()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        return ViewHolder(inflater.inflate(R.layout.recycle_face_select, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // dapatkan dan tampilkan gambar wajah
        val bmp = drawUtils.cropShapeFaces(bitmap, faces[position])
        Glide.with(context)
            .load(bmp)
            .into(holder.imageDuplicate)

        // tombol gambar wajah
        holder.cardDuplicate.setOnClickListener {
            nextRecycler(position)
        }
    }

    override fun getItemCount(): Int {
        return faces.size
    }

    fun nextRecycler(facePos: Int) {
        if (layoutNext != null) {
            (context as SwapActivity).duplicateFace1(layoutNext, facePos, true)
            if (recyclerSelf != null) {
                recyclerSelf.visibility = View.GONE
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardDuplicate: CardView = itemView.findViewById(R.id.card_face)
        val imageDuplicate: ImageView = itemView.findViewById(R.id.image_face)
    }
}