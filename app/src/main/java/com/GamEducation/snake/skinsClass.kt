package com.GamEducation.snake

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import kotlin.random.Random

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView


class skinsClass(private val context: Context, private val rootView: View) {
    private var selectedSkin: Int = -1
    private val skins: List<Int> = listOf(
        R.drawable.snake,
        R.drawable.snake_yellow,
        R.drawable.snake_purple,
        R.drawable.snake_orange,
        R.drawable.snake_green,
        R.drawable.snake_yellow,
        R.drawable.snake_blue,
        R.drawable.snake_grey,
        R.drawable.snake_red
    )
    private val wonSkins: MutableList<Int> = mutableListOf()
    private lateinit var adapter: RouletteAdapter
    private var spinCompleteListener: (() -> Unit)? = null
    private lateinit var autoScrollRecyclerView: RecyclerView

    fun getSkinDrawableId(skin: Int): Int {
        return skin // Since your skins are resource IDs, you can directly use them as drawable IDs
    }
    fun spin(): Int {
        val durationMillis = 2000L
        val targetPosition = (0 until skins.size).random()

        // Check if autoScrollRecyclerView is initialized
        if (::autoScrollRecyclerView.isInitialized) {
            // Scroll the AutoScrollRecyclerView
            autoScrollRecyclerView.smoothScrollToPosition(targetPosition)
        }

        val handler = Handler()
        handler.postDelayed({
            selectedSkin = skins[targetPosition]
            spinCompleteListener?.invoke()
        }, durationMillis)

        return selectedSkin
    }

    fun saveWonSkin(skin: Int) {
        wonSkins.add(skin)
    }

    fun getSkinPrize(skinPosition:Int ):String {

        var skin = skins.get(skinPosition)
        return skin.toString()
    }
    fun getWonSkins(): List<Int> {
        return wonSkins.toList()
    }

    fun setSpinCompleteListener(listener: () -> Unit) {
        spinCompleteListener = listener
    }

    fun createRouletteLayout(): LinearLayout {
        val layoutInflater = LayoutInflater.from(context)
        val rouletteLayout = layoutInflater.inflate(R.layout.layout_roulette, null) as LinearLayout
        autoScrollRecyclerView = rouletteLayout.findViewById(R.id.recyclerViewRoulette)
        return rouletteLayout
    }

    fun initRecyclerView() {
        // Assuming R.id.recyclerViewRoulette is the correct ID for your RecyclerView
        autoScrollRecyclerView = rootView.findViewById(R.id.recyclerViewRoulette)

        val layoutManager = RouletteLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        autoScrollRecyclerView.layoutManager = layoutManager

        adapter = RouletteAdapter(skins) // Initialize the adapter
        autoScrollRecyclerView.adapter = adapter
    }
}
class RouletteAdapter(private val originalSkins: List<Int>) :
    RecyclerView.Adapter<RouletteAdapter.ViewHolder>() {

    private val skins: List<Int>

    init {
        // Repeat the original skins to create an infinite dataset
        val repeatedSkins = mutableListOf<Int>()
        repeat(INFINITE_REPEAT_COUNT) {
            repeatedSkins.addAll(originalSkins)
        }
        skins = repeatedSkins
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_roulette, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val skin = skins[position % originalSkins.size] // Use modulo to repeat the original list
        holder.imageView.setImageResource(skin)
    }

    override fun getItemCount(): Int {
        return skins.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.rouletteOptionImageView)
    }

    companion object {
        // Set a large repeat count to create an infinite effect
        private const val INFINITE_REPEAT_COUNT = 1000
    }
}
class RouletteLayoutManager(context: Context?, orientation: Int, reverseLayout: Boolean) :
    LinearLayoutManager(context, orientation, reverseLayout) {

    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State?, position: Int) {
        val smoothScroller = object : LinearSmoothScroller(recyclerView.context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }

            override fun getHorizontalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
    }
}
class AutoScrollRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {

    init {
        val layoutManager = RouletteLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        setLayoutManager(layoutManager)
    }

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        // Override fling to modify the scrolling behavior
        val newVelocityX = (velocityX * 0.6).toInt() // Adjust the fling speed as needed
        return super.fling(newVelocityX, velocityY)
    }
}