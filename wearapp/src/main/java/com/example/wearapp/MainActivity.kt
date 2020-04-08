package com.example.wearapp

import android.net.Uri
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.view.View
import com.example.shareddata.Gesture
import com.example.shareddata.SharedData
import com.google.android.gms.wearable.*
import com.google.gson.Gson

class MainActivity : WearableActivity(),MessageClient.OnMessageReceivedListener,DataClient.OnDataChangedListener
,CapabilityClient.OnCapabilityChangedListener{
    var sharedDataObj:SharedData? = null
    var gestureObj:Gesture?=null
    private val CAPABILITY_1_NAME = "capability_1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enables Always-on
        setAmbientEnabled()
    }

    override fun onResume() {
        super.onResume()
        Wearable.getMessageClient(this).addListener(this)
        Wearable.getDataClient(this).addListener(this)
        Wearable.getCapabilityClient(this).addListener(this,Uri.parse("wearapp://"),CapabilityClient.FILTER_REACHABLE)
    }

    override fun onMessageReceived(p0: MessageEvent) {

        if(p0.path.equals("/shared"))
        {
            println("@@"+p0.path)
            sharedDataObj= Gson().fromJson(String(p0.data), SharedData::class.java)
            println("@@"+sharedDataObj)
        }

    }

    override fun onDataChanged(p0: DataEventBuffer) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCapabilityChanged(p0: CapabilityInfo) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun sendMessage(view: View) {
        try {
            gestureObj = Gesture("Click")
            val gson = Gson()
            var connectedNode: String = showNodes(CAPABILITY_1_NAME).toString()
            connectedNode?.forEach { node ->
                val bytes = gson.toJson(gestureObj).toByteArray()
                println("@@bytes"+bytes)
                val sendMessageTask =
                    Wearable.getMessageClient(this).sendMessage(connectedNode, "/gesture", bytes)
                println("@@" + sendMessageTask.toString())
            }
        } catch (e:Exception)
        {
            Log.d("@@",e.toString())
        }
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
}
