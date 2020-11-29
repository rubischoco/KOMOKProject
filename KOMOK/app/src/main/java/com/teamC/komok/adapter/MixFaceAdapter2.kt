package com.teamC.komok.adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.mlkit.vision.face.Face
import com.teamC.komok.R
import com.teamC.komok.utils.DrawUtils

class MixFaceAdapter2(
    val context: Context,
    private val bitmap: Bitmap,
    private val faces: MutableList<Face>,
    private val select: MutableList<Int>,
    private val mixList: MutableList<Pair<Face, Int>>,
    private val imagePreview: ImageView
    ): RecyclerView.Adapter<MixFaceAdapter2.ViewHolder>() {

    private val drawUtils = DrawUtils()
    private val imageMix = listOf(R.drawable.meme1, R.drawable.meme2, R.drawable.meme3,
        R.drawable.meme1, R.drawable.meme2, R.drawable.meme3,
        R.drawable.meme1, R.drawable.meme2, R.drawable.meme3)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        return ViewHolder(inflater.inflate(R.layout.recycle_mix_select, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context)
            .load(imageMix[position])
            .into(holder.imageMix)

        holder.cardMix.setOnClickListener {
            val face = drawUtils.getSelectedFaces(faces, select)
            mixList.add(Pair(face[0], imageMix[position]))
            val bmp = drawUtils.drawMixFace(context, bitmap, mixList)
            Glide.with(context)
                .load(bmp)
                .into(imagePreview)
        }
    }

    override fun getItemCount(): Int {
        return imageMix.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardMix: CardView = itemView.findViewById(R.id.card_mix)
        val imageMix: ImageView = itemView.findViewById(R.id.image_mix)
    }
}