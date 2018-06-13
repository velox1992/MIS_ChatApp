package com.tigerteam.mischat

import io.underdark.Underdark
import io.underdark.transport.Link
import io.underdark.transport.Transport
import io.underdark.transport.TransportKind
import io.underdark.transport.TransportListener
import io.underdark.util.nslogger.NSLogger
import io.underdark.util.nslogger.NSLoggerAdapter
import java.util.*

class Node(private val activity: MainActivity) : TransportListener {
    private var running: Boolean = false
    private var nodeId: Long = 0
    private val transport: Transport

    val links = ArrayList<Link>()
    var framesCount = 0
        private set

    init {

        do {
            nodeId = Random().nextLong()
        } while (nodeId == 0L)

        if (nodeId < 0)
            nodeId = -nodeId

        configureLogging()

        val kinds = EnumSet.of(TransportKind.BLUETOOTH, TransportKind.WIFI)
        //kinds = EnumSet.of(TransportKind.WIFI);
        //kinds = EnumSet.of(TransportKind.BLUETOOTH);

        this.transport = Underdark.configureTransport(
                234235,
                nodeId,
                this,
                null,
                activity.applicationContext,
                kinds
        )
    }

    private fun configureLogging() {
        val adapter = StaticLoggerBinder.singleton.loggerFactory.getLogger(Node::class.java.name) as NSLoggerAdapter
        adapter.logger = NSLogger(activity.applicationContext)
        adapter.logger.connect("192.168.5.203", 50000)

        Underdark.configureLogging(true)
    }

    fun start() {
        if (running)
            return

        running = true
        transport.start()
    }

    fun stop() {
        if (!running)
            return

        running = false
        transport.stop()
    }

    fun broadcastFrame(frameData: ByteArray) {
        if (links.isEmpty())
            return

        ++framesCount
        activity.refreshFrames()

        for (link in links)
            link.sendFrame(frameData)
    }

    //region TransportListener
    override fun transportNeedsActivity(transport: Transport, callback: TransportListener.ActivityCallback) {
        callback.accept(activity)
    }

    override fun transportLinkConnected(transport: Transport, link: Link) {
        links.add(link)
        activity.refreshPeers()
    }

    override fun transportLinkDisconnected(transport: Transport, link: Link) {
        links.remove(link)
        activity.refreshPeers()

        if (links.isEmpty()) {
            framesCount = 0
            activity.refreshFrames()
        }
    }

    override fun transportLinkDidReceiveFrame(transport: Transport, link: Link, frameData: ByteArray) {
        // Hier wird die empfangene Nachricht ausgewertet
        ++framesCount
        activity.refreshFrames()
        var hMsg = String(frameData, Charsets.UTF_8)
        activity.updateMessage(hMsg)
    }
    //endregion
} // Node