package com.teamC.komok

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.teamC.komok.fragment.HelpCropFragment
import com.teamC.komok.fragment.HelpMixFragment
import com.teamC.komok.fragment.HelpSwapFragment
import kotlinx.android.synthetic.main.activity_help.*

class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        var curFragment= intent.getIntExtra("helpFragment", 0)
        val helpFragment = listOf(
            HelpSwapFragment(),
            HelpCropFragment(),
            HelpMixFragment()
        )
        changeFragment(helpFragment, curFragment)

        button_back.setOnClickListener {
            finish()
        }

        button_left.setOnClickListener {
            curFragment = changeFragment(helpFragment, curFragment, "<")
        }

        button_right.setOnClickListener {
            curFragment = changeFragment(helpFragment, curFragment, ">")
        }
    }

    private fun changeFragment(fragments: List<Fragment>, curFragment: Int, mode: String=""): Int {
        val start = 0                   // list start
        val end = fragments.size-1      // list end/length
        val nFragment = when (mode) {
            "<" -> if (curFragment-1 >= start) {curFragment-1} else {end}
            ">" -> if (curFragment+1 <= end) {curFragment+1} else {start}
            else -> curFragment
        }

        supportFragmentManager.beginTransaction().apply{
            replace(R.id.fragment_help, fragments[nFragment])
            commit()
        }

        return nFragment
    }
}