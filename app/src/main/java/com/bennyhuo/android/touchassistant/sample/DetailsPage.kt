package com.bennyhuo.android.touchassistant.sample

import android.view.LayoutInflater
import android.view.View
import com.bennyhuo.android.touchassistant.page.Page
import kotlinx.android.synthetic.main.page_detail.view.*

/**
 * Created by benny on 5/24/17.
 */
class DetailsPage() : Page() {

    companion object {
        var count = 0
    }

    override val view: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.page_detail, null)
    }

    init {
        count++
    }

    override fun onEnter() {
        super.onEnter()

        view.apply {
            button.setOnClickListener {
                showPage(MainPage::class)
            }

            content.text = "Count: $count"
        }
    }

    override fun onExit() {
        super.onExit()
    }

    override fun onRelease() {
        super.onRelease()
        count--
    }

}