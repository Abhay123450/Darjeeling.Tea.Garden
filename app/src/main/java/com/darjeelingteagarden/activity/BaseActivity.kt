package com.darjeelingteagarden.activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        super.setContentView(layoutResID)
        val rootView = findViewById<ViewGroup>(android.R.id.content)
        handleSystemBarInsets(rootView)
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        val rootView = findViewById<ViewGroup>(android.R.id.content)
        handleSystemBarInsets(rootView)
    }

    private fun handleSystemBarInsets(rootView: ViewGroup) {
        // Find if a BottomNavigationView exists in the layout
        val bottomNavView = findBottomNavigationView(rootView)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // If a BottomNavigationView exists, it will handle the bottom inset.
            // So, we only apply top, left, and right padding to the root.
            if (bottomNavView != null) {
                rootView.setPadding(insets.left, insets.top, insets.right, 0)
            } else {
                // For activities without a BottomNavigationView, apply all insets.
                rootView.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            }

            windowInsets
        }
    }

    // Helper function to recursively search for a BottomNavigationView
    private fun findBottomNavigationView(viewGroup: ViewGroup): BottomNavigationView? {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is BottomNavigationView) {
                return child
            } else if (child is ViewGroup) {
                val result = findBottomNavigationView(child)
                if (result != null) {
                    return result
                }
            }
        }
        return null
    }
}
