package com.example.shareddataapp

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.shareddata.SharedData
import com.google.android.gms.wearable.*
import com.google.gson.Gson

class MainActivity : AppCompatActivity(),DataClient.OnDataChangedListener,MessageClient.OnMessageReceivedListener
,CapabilityClient.OnCapabilityChangedListener{
    var sharedDataObject:SharedData? = null
    private val CAPABILITY_1_NAME = "capability_1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(this)
        Wearable.getMessageClient(this).addListener(this)
        Wearable.getCapabilityClient(this).addListener(this, Uri.parse("wearapp://"),CapabilityClient.FILTER_REACHABLE)
    }

    override fun onDataChanged(p0: DataEventBuffer) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onMessageReceived(p0: MessageEvent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCapabilityChanged(p0: CapabilityInfo) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    private fun showNodes(vararg capabilityNames: String) {
        Wearable.getCapabilityClient(this)
            .getAllCapabilities(CapabilityClient.FILTER_REACHABLE).apply {
                addOnSuccessListener { capabilityInfoMap ->
                    val nodes: Set<Node> = capabilityInfoMap
                        .filter { capabilityNames.contains(it.key) }
                        .flatMap { it.value.nodes }
                        .toSet()
//                    showDiscoveredNodes(nodes)
                }
            }
    }

    fun SendMessage(view: View) {
        try {
            sharedDataObject = SharedData("Android")
            val gson = Gson()
            var connectedNode: String = showNodes(CAPABILITY_1_NAME).toString()
                connectedNode?.forEach { node ->
                val bytes = gson.toJson(sharedDataObject).toByteArray()
                println("@@bytes"+bytes)
                val sendMessageTask =
                    Wearable.getMessageClient(this).sendMessage(connectedNode, "/shareddata", bytes)
                println("@@" + sendMessageTask.toString())
            }
        } catch (e:Exception)
        {
            Log.d("@@",e.toString())
        }
    }
}
