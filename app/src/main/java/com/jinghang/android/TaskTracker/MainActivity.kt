package com.jinghang.android.TaskTracker

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import java.util.*

class MainActivity : AppCompatActivity(), TaskListFragment.Callbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (currentFragment == null) {
            val fragment = if (intent.hasExtra("task_id")) {
                val taskId = UUID.fromString(intent.getStringExtra("task_id"))
                TaskFragment.newInstance(taskId)
            } else {
                TaskListFragment.newInstance()
            }

            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }

        if (!isNotificationEnabled()) {
            showEnableNotificationDialog()
        }
    }

    override fun onTaskSelected(taskId: UUID) {
        val fragment = TaskFragment.newInstance(taskId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun isNotificationEnabled(): Boolean {
        val notificationManagerCompat = NotificationManagerCompat.from(this)
        return notificationManagerCompat.areNotificationsEnabled()
    }

    private fun showEnableNotificationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("Task Tracker needs you to enable notifications. Do you want to go to settings?")
            setPositiveButton("Yes") { _, _ ->
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
                startActivity(intent)
            }
            setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            setCancelable(false)
            create()
        }.show()
    }
}
