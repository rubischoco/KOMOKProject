package com.teamC.komok.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.mlkit.vision.face.Face
import com.teamC.komok.R
import com.teamC.komok.utils.DrawUtils

class DuplicateFaceAdapter2(
    val context: Context,
    private val bitmap: Bitmap,
    private val faces: MutableList<Face>,
    private val duplicateFace: Int
//    private val layoutSelf: LinearLayout?,
//    private val layoutNext: LinearLayout?
    ): RecyclerView.Adapter<DuplicateFaceAdapter2.ViewHolder>() {

    private val drawUtils = DrawUtils()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        return ViewHolder(inflater.inflate(R.layout.recycle_mix_select, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // dapatkan gambar wajah
        val bmp = drawUtils.cropShapeFaces(bitmap, faces[position])
        // filter abu-abu untuk gambar
        val filter = PorterDuffColorFilter(Color.argb(155, 185, 185, 185), PorterDuff.Mode.SRC_ATOP)

        // jika gambar wajah terpilih (ter-filter)
        if (position == duplicateFace) { holder.imageSelect.colorFilter = filter }
        // jika gambar wajah tidak terpilih (tanpa filter)
        else { holder.imageSelect.colorFilter = null }
        // tampilkan gambar wajah
        Glide.with(context)
            .load(bmp)
            .into(holder.imageSelect)

        // tombol gambar wajah
        holder.cardSelect.setOnClickListener {
            Toast.makeText(context, "Coming soon! face setting", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return faces.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardSelect: CardView = itemView.findViewById(R.id.card_mix)
        val imageSelect: ImageView = itemView.findViewById(R.id.image_mix)
    }
}