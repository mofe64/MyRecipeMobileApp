package com.nubari.favdish.view.activities

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.*
import com.nubari.favdish.R
import com.nubari.favdish.databinding.ActivityMainBinding
import com.nubari.favdish.model.notification.NotificationWorker
import com.nubari.favdish.utils.Constants
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // retrieve our bottom nav bar from our binding
        val navView: BottomNavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_all_dishes,
                R.id.navigation_favorite_dishes,
                R.id.navigation_random_dish
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        if (intent.hasExtra(Constants.NOTIFICATION_ID)) {
            val id = intent.getIntExtra(Constants.NOTIFICATION_ID, 0)
            binding.navView.selectedItemId = R.id.navigation_random_dish
        }
        startWork()
    }

    /**
     *
     * This method is called whenever the user chooses to navigate Up within your application's
     * activity hierarchy from the action bar.
     * onSupportNavigateUp comes from AppCompatActivity.
     * we override the method in the same activity where you define your NavHostFragment
     * which is our MainActivity. we override it so that the NavigationUI
     * can correctly support the up navigation or even the drawer layout menu.
     * AppCompatActivity and NavigationUI are two independent components,
     * so you override the method in order to connect the two
     * Essentially we use it when we are using the systems app bar, so as to connect it
     * with Navigation UI
     * If we are providing our own tool bar and set it up with nav controller
     * Eg
     *
     * val navController = findNavController(R.id.nav_host_fragment)
     * val appBarConfiguration = AppBarConfiguration(navController.graph)
     * val toolbar : Toolbar = findViewById(R.id.toolbar)
     * toolbar.setupWithNavController(navController, appBarConfiguration)

     * In such an instance we don't need to override the  onSupportNavigationUp method as
     * Navigation will automatically handle the click events.
     * **/

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, null)
    }

    fun hideBottomNavigationView() {
        // clear any running animations
        binding.navView.clearAnimation()
        // run another animation which translates nav bar off screen
        binding.navView.animate()
            .translationY(binding.navView.height.toFloat())
            .duration = 300
        binding.navView.visibility = View.GONE
    }

    fun showBottomNavigationView() {
        // clear any running animations
        binding.navView.clearAnimation()
        // run another animation which translates nav bar on screen
        binding.navView.animate()
            .translationY(0f)
            .duration = 300
        binding.navView.visibility = View.VISIBLE
    }

    private fun createConstraints() = Constraints
        .Builder()
        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
        .setRequiresBatteryNotLow(true)
        .setRequiresCharging(false)
        .build()

    private fun createWorkRequest() =
        PeriodicWorkRequestBuilder<NotificationWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(createConstraints())
            .build()

    private fun startWork() {
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "fav dish notify work",
                ExistingPeriodicWorkPolicy.KEEP,
                createWorkRequest()
            )
    }
}
