package org.bubenheimer

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.StrictMode
import android.util.Log
import java.util.concurrent.atomic.AtomicInteger

class BoundService : Service() {
    init {
        println("BoundService.init()")
    }

    override fun onCreate() {
        println("BoundService.onCreate()")
        super.onCreate()
    }

    override fun onDestroy() {
        println("BoundService.onDestroy()")
        super.onDestroy()

        triggerGC()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int) =
        throw UnsupportedOperationException()

    override fun onBind(intent: Intent): IBinder {
        println("BoundService.onBind()")
        val binder = CustomBinder()
        binder.attachInterface(binder, "cq17qwchqu")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        println("BoundService.onUnbind()")
        return super.onUnbind(intent)
    }

    protected fun finalize() {
        println("BoundService.finalize()")
    }

    private companion object {
        init {
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .setClassInstanceLimit(CustomBinder::class.java, 1)
                    .penaltyLog()
                    .build()
            )

        }
    }
}

class CustomBinder : Binder(), IInterface {
    private val bigalloc = ByteArray(5_000_000)

    init {
        println("CustomBinder.init")

        val total = bindersTotal.incrementAndGet()
        val delta = bindersDelta.incrementAndGet()
        println("CustomBinders total - delta: $total - $delta")
    }

    override fun asBinder() = this

    protected fun finalize() {
        println("CustomBinder.finalize()")

        bindersDelta.decrementAndGet()

        // Random code to keep bigalloc allocated
        Log.d("CustomBinder", "bigalloc size " + bigalloc.size)
    }

    private companion object {
        val bindersTotal = AtomicInteger()
        val bindersDelta = AtomicInteger()
    }
}

fun triggerGC() {
    Thread {
        val runtime = Runtime.getRuntime()
        runtime.gc()
        Thread.sleep(500L)
        runtime.runFinalization()
        runtime.gc()
    }.start()
}
