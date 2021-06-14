package com.bennyhuo.android.touchassistant.sample

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.bennyhuo.android.touchassistant.page.Page
import kotlinx.android.synthetic.main.page_main.view.*

/**
 * Created by benny on 5/24/17.
 */
class MainPage() : Page() {

    override val view: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.page_main, null)
    }

    override fun onCreate() {
        view.apply {
            button.setOnClickListener {
                Log.d("MainPage", "clicked, input value: ${input.text}")

                showPage(DetailsPage::class)
            }

            switcher.setOnCheckedChangeListener { buttonView, isChecked ->
                Log.d("MainPage", "checked: $isChecked")
            }
        }
    }

}