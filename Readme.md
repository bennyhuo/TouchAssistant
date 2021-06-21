[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

# TouchAssistant

A simple Touch Assistant implementation like the one in iOS which can be used as an entrance of debug tools or pages.

# How to use

## Import

I have deployed these modules to maven central, you may add this in your build.gradle:

```
implementation("com.bennyhuo:touch-assistant:1.2")
```

### SNAPSHOT

If you want to try the dev version, add this to your build.gradle:

```
repositories {
    maven {
        url "https://oss.sonatype.org/service/local/repositories/snapshots/"
    }
}

dependencies {
    implementation("com.bennyhuo:touch-assistant:1.3-SNAPSHOT")
}
```

## Usages

It's easy to use, just create an instance of TouchAssistant and invoke method `show` to enable the touch assistant:

``` kotlin
val touchAssistant = TouchAssistant(context, MainPage::class.java)
// show touch assistant
touchAssistant.show()
// dismiss touch assistant
touchAssistant.dismiss()
```

MainPage is a class derived from Page, which will be shown in the touch assistant's content view.

```
class MainPage(pageContext: PageContext) : Page(pageContext) {

    override val view: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.page_main, null)
    }

    init {
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
```

You should implement the property `view` to decide what to show in the page. If you want to navigate to another page, just invoke `show(AnotherPage::class)` or `show(anotherPageInstance)`. We will cache the pages in a stack so that when back key pressed, current page will be popped and the previous page will be shown.

