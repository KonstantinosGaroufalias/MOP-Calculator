package com.example.mop_calculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_MOPCalculator)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 3  // 2F, 3F, Summary

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> ProductionFragment.newInstance("2F")
                    1 -> ProductionFragment.newInstance("3F")
                    2 -> SummaryFragment.newInstance()
                    else -> ProductionFragment.newInstance("2F")
                }
            }
        }
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_2f)
                1 -> getString(R.string.tab_3f)
                2 -> getString(R.string.tab_summary)
                else -> "Tab"
            }
        }.attach()
    }
}
