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
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.Random
import java.util.UUID


class MultiActivity : ComponentActivity() {
    val numberOfGames = 3
    val scoresListLocal = mutableListOf<Int>()
    val scoresListRemote = mutableListOf<Int>()

    private lateinit var bluetoothAdapter : BluetoothAdapter
    private var arrayToShow : Array<String>  = arrayOf()
    private var arrayUUID : Array<String>  = arrayOf()
    private var arrayNAME : Array<String>  = arrayOf()

    private var MY_UUID: UUID = UUID.fromString("d52a4216-0acc-43d7-ba00-d3ffdeecc59b") //TODO chnage this
    private var NAME = "TAAF"
    private val REQUEST_CODE = 1

    private lateinit var generalSocket: BluetoothSocket
    private var isServer : Boolean = false

    private val activitiesToLaunch : MutableList<Class<out ComponentActivity>> = mutableListOf()
    private var iteratorOfActivitiesToLaunch = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi)

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
            makeAlertDialog(
                getString(R.string.title_connection_ok),
                getString(R.string.messsage_connection_ok, socket.remoteDevice.name),
                false
            )
            generalSocket = socket

            //Managing message reception
            var boolean = true

            //Wait for receiving message
            val receivingMessage = GlobalScope.async {
                val message = read(socket)
                if (message.contains("START:")){
                    iteratorOfActivitiesToLaunch ++
                    val className = message.substringAfter(":")
                    val newclass = Class.forName(className)
                    val intent = Intent(this@MultiActivity, newclass)
                    startActivityForResult(intent,1)
                }else if(message.contains("SCORE:")){
                    Log.d("DEV",message)
                    val score = message.substringAfter(":").toInt()
                    scoresListRemote.add(score)
                }else if(message.contains("STOP:")){
                    Log.d("DEV",message)
                    val score = message.substringAfter(":").toInt()
                    scoresListRemote.add(score)
                    boolean = false
                    endsMultiMode()
                } else {

                }
            }
            while (boolean){
                receivingMessage.invokeOnCompletion {
                        throwable ->
                    if (throwable != null) {
                    //    println("La coroutine a échoué: $throwable")
                    } else {
                        val response = receivingMessage.getCompleted()
                        boolean = false

                    }
                }
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
                    shouldLoop = false
                }
            }
        }
        private fun manageMyConnectedSocket(socket: BluetoothSocket) {
            //When connection suceed, the server start the games
            makeAlertDialog(
                getString(R.string.title_connection_ok),
                getString(R.string.messsage_connection_ok, socket.remoteDevice.name),
                false
            )
            generalSocket = socket
            startGames()
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

    //______________ BT FUNCTIONS FOR BOTH ___________________________
    fun write(message: String, socket: BluetoothSocket){
        val outputStream = socket.outputStream
        try {
            Log.d("MSG_SENT",message)
            outputStream.write(message.toByteArray())
        } catch (e: IOException) {
            e.message?.let { Log.e("MSG_SENT", it) }
        }
    }
    fun read(socket: BluetoothSocket): String {
        val inputStream = socket.inputStream
        val buffer = ByteArray(1024)
        val bytesRead: Int
        try {
            bytesRead = inputStream.read(buffer)
            return String(buffer, 0, bytesRead)
        } catch (e: IOException) {
            e.message?.let { Log.e("MSG_READ", it) }
            return ("END:999999") //End game if read error
        }
    }

    //------------------------ GAME FUNCTIONS FOR SERVER ----------------------------
    private fun startAGame(intent: Intent){
        iteratorOfActivitiesToLaunch ++
        var message = "START:"+ (intent.component?.className ?: String)
        Log.d("DEV",message)
        write(message,generalSocket)
        startActivityForResult(intent,1)
    }

    private fun startGames(){
        isServer = true
        //Pickup games
        val activities = listOf(
            Game1Activity::class.java,
            Game2Activity::class.java,
            Game3Activity::class.java,
            Game4Activity::class.java,
            Game5Activity::class.java
        )
        for (i in 1..numberOfGames){
            var activity : Class<out ComponentActivity>
            do {
                val index = Random().nextInt(activities.size)
                activity = activities[index]
            }while (activitiesToLaunch.contains(activity)) //To have only one time each activity
            activitiesToLaunch.add(activity)
        }
        goNextGame()
    }

    private fun goNextGame(){
        if (iteratorOfActivitiesToLaunch<numberOfGames){
            //If there is a game to lunch
            val intent = Intent(this,activitiesToLaunch.get(iteratorOfActivitiesToLaunch))
            startAGame(intent)
        }else{
            //If this is the end
            endsMultiMode()
        }
    }


    //______________________ GAME FUNCTIONS FOR BOTH __________________________
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val score = data?.getIntExtra("score", 0)
        if (score != null) {
            runBlocking {
                processScore(score)
            }
        }
    }
    fun processScore(score: Int) {
        GlobalScope.launch {
            // Traitement asynchrone du score
            println("Score traité: $score")
            makeAlertDialog(resources.getString(R.string.title_wait),resources.getString(R.string.dialog_wait_BT),false)
            scoresListLocal.add(score)
            //Manage score and sycroGames for server and send result for client
            if (isServer){
                if (scoresListLocal.size == scoresListRemote.size){
                    //if there is already the socre of the remote game = remote finish game first
                    //can go to next game
                    goNextGame()
                }else{
                    //Wait for the remote to finish and read message
                    val message = read(generalSocket) //Wait for receiving message
                    if (message.contains("SCORE")){
                        //If received score from client, so client finished game
                        val remoteScore = message.substringAfter(":").toInt()
                        scoresListRemote.add(remoteScore)
                        goNextGame()
                    }
                }
            }else{ //If client
                var message = ""
                write("SCORE:"+score,generalSocket)
                message = read(generalSocket) //wait for server
                if (message.contains("START:")){
                    iteratorOfActivitiesToLaunch ++
                    val className = message.substringAfter(":")
                    val newclass = Class.forName(className)
                    val intent = Intent(this@MultiActivity, newclass)
                    startActivityForResult(intent,1)
                }else if(message.contains("SCORE:")){
                    val score = message.substringAfter(":").toInt()
                    scoresListRemote.add(score)
                }else if(message.contains("STOP:")){
                    val score = message.substringAfter(":").toInt()
                    scoresListRemote.add(score)
                    endsMultiMode()
                }
            }
        }
    }
    private fun endsMultiMode() {
        //End of the games
        val scoreTotalLocal = scoresListLocal.sum()
        val scoreTotalRemote = scoresListRemote.sum()
        var message = ""
        var title = ""
        lateinit var music: MediaPlayer

        if (isServer){
            //Stop games on client
            write("STOP:"+scoreTotalLocal,generalSocket)
        }
        if (scoreTotalRemote<=scoreTotalLocal){
            //If the local player win
            title = resources.getString(R.string.titile_end_game_win)
            message = resources.getString(R.string.dialog_end_games_win,scoreTotalLocal.toString(),scoreTotalRemote.toString())
            music = MediaPlayer.create(this,R.raw.music_win)
        }else{
            //If the remote player win
            title = resources.getString(R.string.titile_end_game_loose)
            message = resources.getString(R.string.dialog_end_games_loose,scoreTotalLocal.toString(),scoreTotalRemote.toString())
            music = MediaPlayer.create(this,R.raw.music_loose)
        }
        music.start()
        runOnUiThread(){
            MaterialAlertDialogBuilder(this@MultiActivity)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(resources.getString(R.string.button_seeyou)) { dialog, which ->
                    //Go to menu
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                .show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
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

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent,)
        super.onBackPressed()
    }
}

