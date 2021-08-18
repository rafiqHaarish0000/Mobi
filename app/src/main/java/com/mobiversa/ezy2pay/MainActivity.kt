package com.mobiversa.ezy2pay

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mobiversa.ezy2pay.base.BaseActivity
import com.mobiversa.ezy2pay.databinding.ActivityMainBinding
import com.mobiversa.ezy2pay.utils.*
import com.mobiversa.ezy2pay.utils.PreferenceHelper.set
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.min

internal val TAG = MainActivity::class.java.canonicalName

class MainActivity : BaseActivity(), ConnectivityReceiver.ConnectivityReceiverListener {

    private var snackBar: Snackbar? = null

    // TODO: 09-08-2021
    /**  Vignesh Selvam
     * binding enabled */
    private lateinit var _binding: ActivityMainBinding
    private val binding: ActivityMainBinding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DataBindingUtil.setContentView(this@MainActivity, R.layout.activity_main)
        setContentView(binding.root)

        // replaced with data binding
//        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_history,
                R.id.navigation_notifications,
                R.id.navigation_settings
            )
        )
        /*val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_history, R.id.navigation_notifications,
                R.id.navigation_chat, R.id.navigation_settings
            )
        )*/
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navigationView.setupWithNavController(navController)

        //Network Connection Check
        registerReceiver(
            ConnectivityReceiver(),
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
        LocationService.init(this)

        val serverVersion = getLoginResponse(applicationContext).appVersion
        val versionName = BuildConfig.VERSION_NAME

//        if (compareVersions(versionName,serverVersion) >= 0) {
//        } else {
//            //Update Available
//            showUpdatePrompt()
//        }

        val current = Calendar.getInstance().time

        val sdfComp = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val currentLogin = sdfComp.format(current)
//        val currentLogin = "2020-06-17"
        val prefs: SharedPreferences = PreferenceHelper.defaultPrefs(applicationContext)


        // TODO: 09-08-2021
        /* Vignesh Selvam
        * Disabled old prompt
        * */
        val lastLogin = prefs.getString(Constants.LastLogin, "")
        if (lastLogin.equals("")) {
            prefs[Constants.LastLogin] = currentLogin
            if (getLoginResponse(applicationContext).hostType.equals(
                    "U",
                    true
                ) && Constants.EzyMoto.equals("EZYLINK", true) && getProductList()[0].isEnable
            )

            /*   Vignesh Selvam 06 / 08 / 2021
            * old referral disabled
            *  */
                showReferralPrompt()
        } else {
            when (DateFormatter.DateComparison(currentLogin, lastLogin)) {
                "before" -> {
                    if (getLoginResponse(applicationContext).hostType.equals(
                            "U",
                            true
                        ) && Constants.EzyMoto.equals(
                            "EZYLINK",
                            true
                        ) && getProductList()[0].isEnable
                    )
                    /*   Vignesh Selvam 06 / 08 / 2021
                    * old referral disabled *
                    *  */
                        showReferralPrompt()
                }
                else -> {
                    if (compareVersions(versionName, serverVersion) >= 0) {
                    } else {
                        //Update Available
                        showUpdatePrompt()
                    }
                }
            }
        }
        prefs[Constants.LastLogin] = currentLogin

        Log.e("ProductName", Constants.EzyMoto)

        binding.navigationView.itemIconTintList = null

        // TODO: 09-08-2021
        /*  Vignesh Selvam
        * New referral prompt added */
        checkReferralPrompt()
    }

    private fun checkReferralPrompt() {


        val lastTimeStamp =
            appSession().getSession(
                Constants.Preferences.KEY_LAST_TIME_STAMP,
                0L,
                this@MainActivity
            )

        val currentTimeStamp = System.currentTimeMillis()

        // default show the prompt for the first time
        if (lastTimeStamp == 0L) {
            appSession().saveSession(
                Constants.Preferences.KEY_LAST_TIME_STAMP,
                System.currentTimeMillis(), this@MainActivity
            )
            showPromotionAlert()
        }
        // else check the last time stamp and current time stamp is the difference is equal or greater than 12 hours then display the prompt
        else {

            // if the lastTimeStamp is more or equal to 12 Hrs
            if (abs(TimeUnit.MILLISECONDS.toHours(currentTimeStamp - lastTimeStamp)) >= 12) {

                // save current time stamp and display the prompt
                appSession().saveSession(
                    Constants.Preferences.KEY_LAST_TIME_STAMP,
                    currentTimeStamp, this@MainActivity
                )
                showPromotionAlert()
            }
        }
    }

    private fun showPromotionAlert() {
        Log.i(TAG, "showPromotionAlert: show")

        val inflater = this.layoutInflater
        val layout: View = inflater.inflate(R.layout.alert_referel_new, null)

        val builder = AlertDialog.Builder(this@MainActivity)
        builder.apply {
            setView(layout)
            setCancelable(true)
        }

        val alertDialog = builder.create()
        alertDialog.let {
            it.window!!.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        }

        val imageView = layout.findViewById<ImageView>(R.id.image_view)
        val closeButton = layout.findViewById<ImageButton>(R.id.image_button_close)

        imageView.setOnClickListener {
            // internal web view
            val chromeTabBuilder = CustomTabsIntent.Builder()
            val customTabsIntent = chromeTabBuilder.build()
            customTabsIntent.launchUrl(this@MainActivity, Uri.parse(Constants.Links.ASPIRASIA_URL))
        }
        closeButton.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        showNetworkMessage(isConnected)
    }

    private fun showNetworkMessage(isConnected: Boolean) {
        if (!isConnected) {
            snackBar = Snackbar.make(
                findViewById(R.id.container),
                "You are offline",
                Snackbar.LENGTH_LONG
            ) //Assume "rootLayout" as the root layout of every activity.
            snackBar?.duration = BaseTransientBottomBar.LENGTH_INDEFINITE
            snackBar?.show()

            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        } else {
            snackBar?.dismiss()
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    override fun onResume() {
        super.onResume()
        ConnectivityReceiver.connectivityReceiverListener = this
    }

    override fun onBackPressed() {
        showExitAlert("Exit Alert", "Are you sure you want to exit from Mobiversa?")
    }

    private fun showUpdatePrompt() {
        lateinit var mAlertDialog: AlertDialog

        val inflater = this.layoutInflater
        val alertLayout: View = inflater.inflate(R.layout.alert_update, null)
        val positiveBtn = alertLayout.findViewById<View>(R.id.positive_btn) as Button
        val negativeBtn = alertLayout.findViewById<View>(R.id.negative_btn) as Button
        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setView(alertLayout)
        alert.setCancelable(false)

        positiveBtn.setOnClickListener {
            val appPackageName =
                "com.mobiversa.ezy2pay"// getPackageName() from Context or Activity object

            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$appPackageName")
                    )
                )
            } catch (anfe: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                    )
                )
            }
        }

        negativeBtn.setOnClickListener {
            mAlertDialog.dismiss()
        }

        mAlertDialog = alert.create()
        mAlertDialog.show()
        //startActivity(new Intent(GoogleMaps.this, VoidAuthActivity.class).putExtra("trensID", transId));
    }

    @Deprecated("not in use now, will be removed in the future updates")
    private fun showReferralPrompt() {


        val inflater = this.layoutInflater
        val alertLayout: View = inflater.inflate(R.layout.alert_referal_main, null)
        val mBuilder = this.let {
            AlertDialog.Builder(it)
                .setView(alertLayout)
                .setCancelable(false)
        }


        val positiveBtn = alertLayout.findViewById<View>(R.id.referal_txt) as TextView
        val negativeBtn = alertLayout.findViewById<View>(R.id.alert_close_button) as ImageView

        val mAlertDialog = mBuilder.create()

        positiveBtn.setOnClickListener {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data =
                Uri.parse("https://gomobi.io/referral/?mid=${getLoginResponse(applicationContext).motoMid}")
            startActivity(openURL)
            mAlertDialog.dismiss()
        }

        negativeBtn.setOnClickListener {
            mAlertDialog.dismiss()
        }

        mAlertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        mAlertDialog.window?.setBackgroundDrawable(ColorDrawable(this.resources.getColor(android.R.color.transparent)))
        val dialogWindow = mAlertDialog.window
        dialogWindow?.setGravity(Gravity.CENTER)

        // TODO: 11-08-2021
        /*   Vignesh Selvam
        *   disabled to create will removed in future update   */

//        mAlertDialog = mBuilder.show()
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val components1 = v1.split("\\.").toTypedArray()
        val components2 = v2.split("\\.").toTypedArray()
        val length = min(components1.size, components2.size)
        for (i in 0 until length) {
            val result = components1[i].compareTo(components2[i])
            if (result != 0) {
                return result
            }
        }
        return components1.size.compareTo(components2.size)
    }
}

