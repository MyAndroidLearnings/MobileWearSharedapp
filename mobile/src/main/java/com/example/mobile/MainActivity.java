package com.example.mobile;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.shareddata.Gesture;
import com.example.shareddata.SharedData;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity implements MessageClient.OnMessageReceivedListener, DataClient.OnDataChangedListener,
        CapabilityClient.OnCapabilityChangedListener {
  Gesture gesture;
  Button checkNodes;
    BroadcastReceiver mReceiver;
    private static final String CAPABILITY_1_NAME = "capability_1";
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkNodes=findViewById(R.id.CheckNodes_Button);
        checkNodes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String node;
                    node=showNodes(CAPABILITY_1_NAME);
                    System.out.println("@@"+node);

                }catch (Exception e)
                {
                    Log.d("@@",e.toString());
                }

            }
        });

    }
    private String showNodes(final String... capabilityNames) {
        try {
            Task<Map<String, CapabilityInfo>> capabilitiesTask =
                    Wearable.getCapabilityClient(this)
                            .getAllCapabilities(CapabilityClient.FILTER_REACHABLE);
            capabilitiesTask.addOnSuccessListener(new
                                                          OnSuccessListener<Map<String, CapabilityInfo>>() {
                                                              @Override
                                                              public void onSuccess(Map<String, CapabilityInfo>
                                                                                            capabilityInfoMap) {
                                                                  Set<Node> nodes = new HashSet<>();
                                                                  if (capabilityInfoMap.isEmpty()) {
                                                                      showDiscoveredNodes(nodes);
                                                                      return;
                                                                  }
                                                                  for (String capabilityName : capabilityNames) {
                                                                      CapabilityInfo capabilityInfo = capabilityInfoMap.get(capabilityName);
                                                                      if (capabilityInfo != null) {
                                                                          nodes.addAll(capabilityInfo.getNodes());
                                                                      }
                                                                  }
                                                                  showDiscoveredNodes(nodes);
                                                              }
                                                          });
        }catch (Exception e)
        {
            Log.d("@@ showNodes",e.toString());
        }

        return null;
    }

    private void showDiscoveredNodes(Set<Node> nodes) {
        List<String> nodesList = new ArrayList<>();
        for (Node node : nodes) {
            nodesList.add(node.getDisplayName());
        }
        Log.d("@@", "Connected Nodes: " + (nodesList.isEmpty()
                ? "No connected device was found for the given capabilities"
                : TextUtils.join(",", nodesList)));
        String msg;
        if (!nodesList.isEmpty()) {
            msg = getString(R.string.connected_nodes, TextUtils.join(", ", nodesList));
        } else {
            msg = getString(R.string.no_device);
        }
        Toast.makeText(MainActivity.this, msg, LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.getMessageClient(this).addListener(this);
        Wearable.getDataClient(this).addListener(this);
        Wearable.getCapabilityClient(this).addListener(this, Uri.parse("wearapp://") ,CapabilityClient.FILTER_REACHABLE);

    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {

    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {

    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        if(messageEvent.getPath().equals("/gesture")){
//            gesture= new Gson().fromJson(String(messageEvent.get()))
            gesture=new Gson().fromJson(new String(messageEvent.getData()),Gesture.class);
            String eventVariable=gesture.getEvent();
            System.out.println("@@ gesture:"+eventVariable);



        }

    }

    public void SendMessage(View view) {
        System.out.println("@@ enter send mesage");
        Log.d("@@", "Generating RPC");
        new StartWearableActivityTask().execute();
    }

    @WorkerThread
    private void sendStartActivityMessage( String node) {
        try {
//            final String nodeName=node;
//            if(!node.isEmpty()) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(getApplicationContext(),nodeName,Toast.LENGTH_SHORT).show();
//                    }
//                });

            Gson gsonObject = new Gson();
            SharedData sharedDataObj = new SharedData("Ios OS");
            byte[] bytes;
            bytes = gsonObject.toJson(sharedDataObj).getBytes();

            Task<Integer> sendMessageTask =
                    Wearable.getMessageClient(this).sendMessage(node, "/shared", bytes);

            try {
                // Block on a task and get the result synchronously (because this is on a background
                // thread).
                Integer result = Tasks.await(sendMessageTask);
                Log.d("@@", "Message sent: " + result);

            } catch (ExecutionException exception) {
                Log.e("@@", "Task failed: " + exception);

            } catch (InterruptedException exception) {
                Log.e("@@", "Interrupt occurred: " + exception);
            }

        }catch (Exception e)
        {
            Log.d("@@Connected",e.toString());
        }

    }

    @WorkerThread
    private ArrayList<String> getNodes() {
//        HashSet<String> results = new HashSet<>();
        ArrayList<String> results = new ArrayList<>();

        Task<List<Node>> nodeListTask =
                Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();

        try {
            // Block on a task and get the result synchronously (because this is on a background
            // thread).
            List<Node> nodes = Tasks.await(nodeListTask);

            for (Node node : nodes) {
                results.add(node.getId());
                results.add(node.getDisplayName());
                System.out.println("@@ node id"+node.getDisplayName()+" " +node.getId());
            }

        } catch (ExecutionException exception) {
            Log.e("@@", "Task failed: " + exception);

        } catch (InterruptedException exception) {
            Log.e("@@", "Interrupt occurred: " + exception);
        }

        return results;
    }
    private class StartWearableActivityTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
          ArrayList<String> nodes=getNodes();
            System.out.println("@@overall node"+nodes);
//            for (String node : nodes) {
                sendStartActivityMessage(nodes.get(0));
                System.out.println("@@nodes"+nodes.get(0));

//            }
            return null;
        }
    }

}
