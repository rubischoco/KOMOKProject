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
import com.teamC.komok.MixActivity
import com.teamC.komok.R
import com.teamC.komok.retrofit.GalleryResponse

class MixFaceAdapter2(
    val context: Context,
    private val facePos: Int,
    private val apiGallery: MutableList<GalleryResponse>
    ): RecyclerView.Adapter<MixFaceAdapter2.ViewHolder>() {

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
                        holder.bitmapMix = resource
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }

        holder.cardMix.setOnClickListener {
            holder.bitmapMix?.let { it1 -> (context as MixActivity).mixFace2(facePos, it1) }
        }
    }

    override fun getItemCount(): Int {
        return apiGallery.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardMix: CardView = itemView.findViewById(R.id.card_mix)
        val imageMix: ImageView = itemView.findViewById(R.id.image_mix)
        var bitmapMix: Bitmap? = null
    }
}