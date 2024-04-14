package net.castang.esir.progm.terresasutralesactionfun

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.IOException
import java.util.Random
import java.util.UUID


class MultiActivity : ComponentActivity() {
    val numberOfGames = 3
    val scoresList = mutableListOf<Int>()

    private lateinit var bluetoothAdapter : BluetoothAdapter
    private var arrayToShow : Array<String>  = arrayOf()
    private var arrayUUID : Array<String>  = arrayOf()
    private var arrayNAME : Array<String>  = arrayOf()

    private var MY_UUID: UUID = UUID.fromString("d52a4216-0acc-43d7-ba00-d3ffdeecc59b") //TODO chnage this
    private var NAME = "TAAF" //TODO change this
    private val REQUEST_CODE = 1
    private var mHandler: Handler? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi)
        mHandler = Handler()

        //----------------- BLUETOOTH ---------------------
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.getAdapter()
        //Check permission
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent,REQUEST_CODE)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    REQUEST_CODE
                )
                return
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        //____________________________ BUTTON ONCLICK LISTENERS __________________
        //DISCOVERING
        val bouttonDecouverte: Button = findViewById(R.id.button_start_d)
        bouttonDecouverte.setOnClickListener {
            //Check discover permission first
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_CODE
                    )
                    return@setOnClickListener
                }
            }else {
                if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                        arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                        REQUEST_CODE
                    )
                    return@setOnClickListener
                }
            }
            arrayToShow = arrayOf()
            arrayUUID = arrayOf()
            arrayNAME = arrayOf()
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(receiver, filter)
            bluetoothAdapter.startDiscovery()
        }
        //STOP DISCOVERING
        val bouttonStopDecouverte: Button = findViewById(R.id.button_stop_d)
        bouttonStopDecouverte.setOnClickListener {
            bluetoothAdapter.cancelDiscovery()
        }

        //CONNECTION
        val buttonMakeMeDiscoverable: Button = findViewById(R.id.button_make_d)
        buttonMakeMeDiscoverable.setOnClickListener {
            val listView = findViewById<ListView>(R.id.listView)
            val emptyAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
            listView.adapter = emptyAdapter
            val requestCode = 1;
            val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            }
            val threadServerBT = AcceptThread<BluetoothServerSocket>()
            threadServerBT.start()
            startActivityForResult(discoverableIntent, requestCode)
        }
    }

    // ----------- BLUETOOTH CLIENT - 1-----------------
    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device?.name
                    val deviceHardwareAddress = device?.address // MAC address
                    Log.d("BT",deviceName.toString()+" "+deviceHardwareAddress.toString())
                    arrayNAME += deviceHardwareAddress.toString()
                    arrayToShow += deviceName.toString()+"\n"+deviceHardwareAddress.toString()
                    //Show device
                    val listeView : ListView = findViewById(R.id.listView)
                    val adapter = ArrayAdapter(baseContext, android.R.layout.simple_list_item_1, arrayToShow)
                    listeView.adapter = adapter
                    //Device onClickListener
                    listeView.setOnItemClickListener { parent, view, position, id ->
                        //MY_UUID = arrayUUID.get(id.toInt())
                        val nameBT = arrayNAME.get(id.toInt())
                        bluetoothAdapter.cancelDiscovery()
                        Toast.makeText(baseContext, "Connexion à "+nameBT, Toast.LENGTH_SHORT).show()
                        val threadConnectionBT = ConnectThread(bluetoothAdapter.getRemoteDevice(nameBT))
                        threadConnectionBT.start() //Go to Bluetooht Client 2
                    }
                }
            }
        }
    }

    //----------------------- BLUETOOTH CLIENT - 2 ------------------------
    @SuppressLint("MissingPermission")
    private inner class ConnectThread(device: BluetoothDevice) : Thread() {
        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(MY_UUID)
        }
        public override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter?.cancelDiscovery()
            try {
                mmSocket?.let { socket ->
                    // Connect to the remote device through the socket. This call blocks
                    // until it succeeds or throws an exception.
                    socket.connect()
                    // The connection attempt succeeded. Perform work associated with
                    // the connection in a separate thread.
                    manageMyConnectedSocket(socket) //Go to bluetooth client 3
                }
            }catch (e: IOException){
                runOnUiThread(){
                    Toast.makeText(this@MultiActivity,R.string.toast_connection_error,Toast.LENGTH_SHORT).show()
                }
            }
        }

        //-------------- BLUETOOTH CLIENT - 3 ------------------------
        private fun manageMyConnectedSocket(socket: BluetoothSocket) {
            //TODO show user a message of connexion succesfull
            makeAlertDialog(getString(R.string.title_connection_ok),getString(R.string.messsage_connection_ok,socket.remoteDevice.name),false)

            val inputStream = socket.inputStream
            val outputStream = socket.outputStream

            //Sending message
            val messageToSend = "Hello, Bluetooth!"
            try {
                outputStream.write(messageToSend.toByteArray())
                println("Message envoyé: $messageToSend")
            } catch (e: IOException) {
                println("Erreur lors de l'envoi du message: ${e.message}")
            }

            //Receiving message
            val buffer = ByteArray(1024)
            val bytesRead: Int
            try {
                bytesRead = inputStream.read(buffer)
                val receivedMessage = String(buffer, 0, bytesRead)
                println("Message reçu: $receivedMessage")
            } catch (e: IOException) {
                println("Erreur lors de la réception du message: ${e.message}")
            }
        }

        // --------------- OTHERS ---------------------------
        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e("BT", "Could not close the client socket", e)
            }
        }
    }



    //--------------------- BLUETOOTH SERVER -----------------
    @SuppressLint("MissingPermission")
    private inner class AcceptThread<BluetoothServerSocket> : Thread() {
        private val mmServerSocket = bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID)
        override fun run() {
            // Keep listening until exception occurs or a socket is returned.
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e("BT", "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
                    manageMyConnectedSocket(it)
                    mmServerSocket?.close()
                    shouldLoop = false
                }
            }
        }
        private fun manageMyConnectedSocket(it: BluetoothSocket) {
            //When connection suceed, the server start the games
            //TODO start games
            makeAlertDialog(getString(R.string.title_connection_ok),getString(R.string.messsage_connection_ok,it.remoteDevice.name),false)
            //mChatService = new BluetoothChatService(activity, mHandler);
            //mBluetoothService = MyBluetoothService(mHandler!!)
            //mBluetoothService.start()
            val inputStream = it.inputStream
            val outputStream = it.outputStream

            //Sending message
            val messageToSend = "Hello, Bluetooth!"
            try {
                outputStream.write(messageToSend.toByteArray())
                println("Message envoyé: $messageToSend")
            } catch (e: IOException) {
                println("Erreur lors de l'envoi du message: ${e.message}")
            }

            //Receiving message
            val buffer = ByteArray(1024)
            val bytesRead: Int
            try {
                bytesRead = inputStream.read(buffer)
                val receivedMessage = String(buffer, 0, bytesRead)
                println("Message reçu: $receivedMessage")
            } catch (e: IOException) {
                println("Erreur lors de la réception du message: ${e.message}")
            }


        }


        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e("BT", "Could not close the connect socket", e)
            }
        }
    }

    //______________TEST ZONE___________________________

    //------------------------ GAMES FUNCTIONS ----------------------------
    private fun startGames(){
        //--------------------------- GAMES -------------------
        //Pickup games
        val activities = listOf(
            Game1Activity::class.java,
            Game2Activity::class.java,
            Game3Activity::class.java,
            Game4Activity::class.java
            )
        val activitiesToLaunch : MutableList<Class<out ComponentActivity>> = mutableListOf()
        for (i in 1..numberOfGames){
            var activity : Class<out ComponentActivity>
            do {
                val index = Random().nextInt(activities.size)
                activity = activities[index]
            }while (activitiesToLaunch.contains(activity)) //To have only one time each activity
            activitiesToLaunch.add(activity)
        }
        //Start games
        for (i in 1..numberOfGames){
            val intent = Intent(this, activitiesToLaunch.get(i-1))
            startAGame(intent)
        }
    }

    private fun startAGame(intent: Intent){

        startActivityForResult(intent,1)
    }



    private fun endsMultiMode() {
        //End of the games
        val scoreTotal = scoresList.sum()
        Log.d("TEST",scoresList.toString())
        runOnUiThread(){
            MaterialAlertDialogBuilder(this@MultiActivity)
                .setCancelable(false)
                .setTitle(resources.getString(R.string.title_end_game))
                .setMessage(resources.getString(R.string.dialog_end_solo,scoreTotal.toString()))
                .setNeutralButton(resources.getString(R.string.button_seeyou)) { dialog, which ->
                    //Go to menu
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                .show()
        }
        val mediaPlayer = MediaPlayer.create(this, R.raw.music_end_solo)
        mediaPlayer.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val score = data?.getIntExtra("score", 0)
            Log.d("TEST", score.toString())
            if (score != null) {
                scoresList.add(score)
            }
        }
        if (scoresList.size == numberOfGames) {
            //If all the results are here
            endsMultiMode()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't forget to unregister the ACTION_FOUND receiver.
        // unregisterReceiver(receiver)
    }

    private fun makeAlertDialog(title : String,message : String, cancelable : Boolean){
        runOnUiThread(){
            MaterialAlertDialogBuilder(this@MultiActivity)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(cancelable)
                .show()

        }
    }
}

