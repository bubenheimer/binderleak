package org.bubenheimer

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock

class MainActivity : Activity() {
    @Volatile
    private var connected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, BoundService::class.java)

        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(
                componentName: ComponentName,
                binder: IBinder
            ) {
                println("onServiceConnected")

                connected = true

                Thread {
                    SystemClock.sleep(1_000L)
                    println("Disconnecting service")
                    unbindService(this)

                    connected = false
                }.start()
            }

            override fun onServiceDisconnected(componentName: ComponentName) {
                println("onServiceDisconnected")
            }
        }

        Thread {
            while (true) {
                if (!connected) {
                    println("Reconnecting service")
                    bindService(intent, serviceConnection, BIND_AUTO_CREATE)
                }

                SystemClock.sleep(2_000L)
            }
        }.start()
    }
}
