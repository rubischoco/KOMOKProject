package com.teamC.komok.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.mlkit.vision.face.Face
import com.teamC.komok.R
import com.teamC.komok.retrofit.GalleryResponse
import com.teamC.komok.utils.DrawUtils

class MixFaceAdapter2(
    val context: Context,
    private val bitmap: Bitmap,
    private val faces: MutableList<Face>,
    private val select: MutableList<Int>,
    private val apiGallery: MutableList<GalleryResponse>,
    private val mixList: MutableList<Pair<Face, Int>>,
    private val imagePreview: ImageView
    ): RecyclerView.Adapter<MixFaceAdapter2.ViewHolder>() {

    private val drawUtils = DrawUtils()
    private val apiBitmap: MutableList<Bitmap> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        return ViewHolder(inflater.inflate(R.layout.recycle_mix_select, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (apiGallery.size > 0) {
            Glide.with(context)
                .asBitmap()
                .load(apiGallery[position].link)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        holder.imageMix.setImageBitmap(resource)
//                        apiBitmap.add(position, resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }

        holder.cardMix.setOnClickListener {
//            Toast.makeText(context, apiBitmap.toString(), Toast.LENGTH_SHORT).show()
//            val face = drawUtils.getSelectedFaces(faces, select)
//            mixList.add(Pair(face[0], apiBitmap[position]))
//            val bmp = drawUtils.drawMixFace(context, bitmap, mixList)
//            Glide.with(context)
//                .load(bmp)
//                .into(imagePreview)
        }
    }

    override fun getItemCount(): Int {
        return apiGallery.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardMix: CardView = itemView.findViewById(R.id.card_mix)
        val imageMix: ImageView = itemView.findViewById(R.id.image_mix)
    }
}