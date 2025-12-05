package org.koin.compose.application

import androidx.compose.runtime.RememberObserver
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatform

@KoinInternalApi
class CompositionKoinApplicationLoader(
    val koinApplication: KoinApplication,
) : RememberObserver {

    var koin : Koin? = null

    init {
        start()
    }

    override fun onAbandoned() {
        koin?.logger?.warn("CompositionKoinApplicationLoader - onAbandoned")
        stop()
    }

    override fun onForgotten() {
        koin?.logger?.debug("CompositionKoinApplicationLoader - onForgotten")
        //don"t stop here, premature
    }

    override fun onRemembered() {
        start()
    }

    private fun start() {
        if (KoinPlatform.getKoinOrNull() == null){
            try {
                koin = startKoin(koinApplication).koin
                koin!!.logger.debug("$this -> attach Koin instance $koin")
            } catch (e: Exception) {
                error("Can't start Koin from Compose context - $e")
            }
        } else {
            koin = KoinPlatform.getKoin()
            koin!!.logger.debug("$this -> re-attach Koin instance $koin")
        }
    }

    private fun stop() {
        koin?.logger?.warn("CompositionKoinApplicationLoader - stop")
        koin = null
        stopKoin()
    }
}