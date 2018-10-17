package ui.anwesome.com.linkedsignalstepindicatorview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import ui.anwesome.com.signalindicatorstepview.SignalIndicatorStepView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SignalIndicatorStepView.create(this)
    }
}
