package com.GamEducation.snake


import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Random

import kotlin.math.pow
import kotlin.math.sqrt


class MainActivity : Activity(),SharedPreferencesUpdateListener {
    var localJogo = "new_game";
    var orderedItemOrder = 0
    var corretaOuVista = false;
    var tentativa = 0
    var delayMillis = 30L // Update snake position every 100 milliseconds
    var scorex = 0
    var currentDirection = "right" // Start moving right by default
    var codigoAcesso = "";
    var codigo_acesso = 0;
    val correct = 0
    val percentage = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("DadosGuardadosPeloJogo", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "codigo_acesso") {
                // Check if the "codigo_acesso" preference has changed
                val newCodigoAcesso = sharedPreferences.getInt("codigo_acesso", 0)

                if (newCodigoAcesso != 0) {
                    // "codigo_acesso" has changed, recreate the activity
                    editor.apply() // Save any other changes to preferences
                    recreate() // Recreate the activity
                }
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        val serializedData = delayMillis
        codigo_acesso = sharedPreferences.getInt("codigo_acesso", 0)
        localJogo = sharedPreferences.getString("localJogo","new_game").toString()


        if (codigo_acesso == 0) {
            setContentView(R.layout.activity_codigo_acesso)


            val codigoAcessoEditText = findViewById<EditText>(R.id.editTextCodigoAcesso)

            val submitButtonCodigo = findViewById<Button>(R.id.buttonSubmit)

            submitButtonCodigo.setOnClickListener {
                codigoAcesso = codigoAcessoEditText.text.toString()
                codigo_acesso = 0;
                codigo_acesso = codigoAcesso.toInt();

                // Perform network operations in an AsyncTask
                VerificaCodigoAcessoAsyncTask().execute(codigo_acesso)


                if (localJogo == "start_game") {
                    val sharedPreferences =
                        getSharedPreferences("DadosGuardadosPeloJogo", Context.MODE_PRIVATE)

                    sharedPreferences.edit().putInt("codigo_acesso", codigo_acesso).apply()

                    recreate()
                } else {
                    recreate()

                }
            }
        } else{
            setContentView(R.layout.activity_main)

        val board = findViewById<RelativeLayout>(R.id.board)
        val border = findViewById<RelativeLayout>(R.id.relativeLayout)
        val lilu = findViewById<LinearLayout>(R.id.lilu)
        val upButton = findViewById<Button>(R.id.up)
        val downButton = findViewById<Button>(R.id.down)
        val leftButton = findViewById<Button>(R.id.left)
        val rightButton = findViewById<Button>(R.id.right)

        val newgame = findViewById<Button>(R.id.new_game)
        val resume = findViewById<Button>(R.id.resume)
        val playagain = findViewById<Button>(R.id.playagain)
        val continueButton = findViewById<Button>(R.id.continueGame)
        val score = findViewById<Button>(R.id.score)
        val score2 = findViewById<Button>(R.id.score2)
        val meat = ImageView(this)
        val snake = ImageView(this)
        val snakeSegments =
            mutableListOf(snake) // Keep track of the position of each snake segment
        val handler = Handler()
            val logOffButton = findViewById<Button>(R.id.logOffButton)

            logOffButton.setOnClickListener {
                // Clear the saved variables in SharedPreferences
                val sharedPreferences = getSharedPreferences("DadosGuardadosPeloJogo", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                sharedPreferences.edit().putInt("codigo_acesso",0)
                editor.clear()
                editor.apply()

                // Restart the activity
                restartActivity()
            }



            val sharedPreferences = getSharedPreferences("DadosGuardadosPeloJogo", Context.MODE_PRIVATE)
            val correta = sharedPreferences.getInt("correct", 0)
            val percentage = sharedPreferences.getFloat("percentage", 0.0f)
            localJogo = sharedPreferences.getString("localJogo","new_game").toString()
            if (localJogo == "resume_game" && correta ==  1  ){
                resume.visibility = View.VISIBLE
                resume.performClick()
            } else{
                resume.visibility = View.INVISIBLE
            }
            sharedPreferences.edit().remove("correct")
            sharedPreferences.edit().remove("percentage")
        continueButton.visibility = View.INVISIBLE
        board.visibility = View.INVISIBLE
        playagain.visibility = View.INVISIBLE
        score.visibility = View.INVISIBLE
        score2.visibility = View.INVISIBLE

        newgame.setOnClickListener {
            // Clear the board before adding elements
            board.removeAllViews()

            sharedPreferences.edit().putInt("codigo_acesso",codigo_acesso).apply()

            logOffButton.visibility = View.INVISIBLE
            board.visibility = View.VISIBLE
            newgame.visibility = View.INVISIBLE
            score2.visibility = View.VISIBLE
            resume.visibility = View.INVISIBLE

            snake.setImageResource(R.drawable.snake)
            snake.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            board.addView(snake)
            snakeSegments.add(snake) // Add the new snake segment to the list


            var snakeX = snake.x
            var snakeY = snake.y







            meat.setImageResource(R.drawable.food)
            meat.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            board.addView(meat)

            val random = Random() // create a Random object
            val randomX =
                random.nextInt(801) - 400 // generate a random x-coordinate between -400 and 400
            val randomY =
                random.nextInt(801) - 400 // generate a random y-coordinate between -400 and 400


            meat.x = randomX.toFloat()
            meat.y = randomY.toFloat()





            fun checkSnakeBodyCollision() {
                val newHeadX =
                    snake.x + if (currentDirection == "left") -10 else if (currentDirection == "right") 10 else 0
                val newHeadY =
                    snake.y + if (currentDirection == "up") -10 else if (currentDirection == "down") 10 else 0
                for (i in 0 until snakeSegments.size) {
                    val segment = snakeSegments[i]
                    val segmentX = segment.x
                    val segmentY = segment.y


                    if (newHeadX == segmentX && newHeadY == segmentY) { // Check if the head collides with a segment
                        // Handle collision, e.g., game over logic
                        border.setBackgroundColor(getResources().getColor(R.color.red))
                        playagain.visibility = View.VISIBLE
                        currentDirection = "pause"
                        lilu.visibility = View.INVISIBLE

                        score.text = "your score is  " + scorex.toString()
                        score.visibility = View.VISIBLE
                        score2.visibility = View.INVISIBLE
                        return
                    }
                }


            }


            fun checkFoodCollision() {
                val distanceThreshold = 50

                val distance = sqrt((snake.x - meat.x).pow(2) + (snake.y - meat.y).pow(2))

                if (distance < distanceThreshold) { // Check if the distance between the snake head and the meat is less than the threshold

                    val newSnake =
                        ImageView(this) // Create a new ImageView for the additional snake segment
                    newSnake.setImageResource(R.drawable.snake)
                    newSnake.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    board.addView(newSnake)

                    snakeSegments.add(newSnake) // Add the new snake segment to the list

                    val randomX =
                        random.nextInt(801) - -100
                    val randomY =
                        random.nextInt(801) - -100


                    meat.x = randomX.toFloat()
                    meat.y = randomY.toFloat()


                    delayMillis-- // Reduce delay value by 1
                    scorex++
                    val sharedPreferences =
                        getSharedPreferences("DadosGuardadosPeloJogo", Context.MODE_PRIVATE)
                    val serializedData = delayMillis

                    if (delayMillis == 30L || scorex == 0) {
                        // Do nothing or apply changes
                    } else {
                        sharedPreferences.edit().putLong("currentDirection", delayMillis).apply()
                        sharedPreferences.edit().putLong("delayMillis", delayMillis).apply()
                        sharedPreferences.edit().putInt("score", scorex).apply()
                        sharedPreferences.edit().putInt("orderedItemOrder", orderedItemOrder)
                            .apply()
                    }

                    score2.text = "score : " + scorex.toString() // Update delay text view


                }
            }


            val runnable = object : Runnable {
                override fun run() {

                    for (i in snakeSegments.size - 1 downTo 1) { // Update the position of each snake segment except for the head
                        snakeSegments[i].x = snakeSegments[i - 1].x
                        snakeSegments[i].y = snakeSegments[i - 1].y
                    }


                    when (currentDirection) {
                        "up" -> {
                            snakeY -= 10
                            if (snakeY < -490) { // Check if the ImageView goes off the top of the board
                                snakeY = -490f
                                border.setBackgroundColor(getResources().getColor(R.color.red))
                                playagain.visibility = View.VISIBLE
                                currentDirection = "pause"
                                lilu.visibility = View.INVISIBLE
                                continueButton.visibility = View.VISIBLE
                                score.text =
                                    "your score is  " + scorex.toString() // Update delay text view
                                score.visibility = View.VISIBLE
                                score2.visibility = View.INVISIBLE


                            }

                            snake.translationY = snakeY
                        }

                        "down" -> {
                            snakeY += 10
                            val maxY =
                                board.height / 2 - snake.height + 30 // Calculate the maximum y coordinate
                            if (snakeY > maxY) { // Check if the ImageView goes off the bottom of the board
                                snakeY = maxY.toFloat()
                                border.setBackgroundColor(getResources().getColor(R.color.red))
                                playagain.visibility = View.VISIBLE
                                continueButton.visibility = View.VISIBLE
                                currentDirection = "pause"
                                lilu.visibility = View.INVISIBLE

                                score.text =
                                    "your score is  " + scorex.toString() // Update delay text view
                                score.visibility = View.VISIBLE
                                score2.visibility = View.INVISIBLE


                            }
                            snake.translationY = snakeY
                        }

                        "left" -> {
                            snakeX -= 10
                            if (snakeX < -490) { // Check if the ImageView goes off the top of the board
                                snakeX = -490f
                                border.setBackgroundColor(getResources().getColor(R.color.red))
                                playagain.visibility = View.VISIBLE
                                continueButton.visibility = View.VISIBLE
                                currentDirection = "pause"
                                lilu.visibility = View.INVISIBLE
                                score.text =
                                    "your score is  " + scorex.toString() // Update delay text view
                                score.visibility = View.VISIBLE
                                score2.visibility = View.INVISIBLE


                            }
                            snake.translationX = snakeX
                        }

                        "right" -> {
                            snakeX += 10
                            val maxX =
                                board.height / 2 - snake.height + 30 // Calculate the maximum y coordinate
                            if (snakeX > maxX) { // Check if the ImageView goes off the bottom of the board
                                snakeX = maxX.toFloat()
                                border.setBackgroundColor(getResources().getColor(R.color.red))
                                playagain.visibility = View.VISIBLE
                                continueButton.visibility = View.VISIBLE
                                currentDirection = "pause"
                                lilu.visibility = View.INVISIBLE

                                score.text =
                                    "your score is  " + scorex.toString() // Update delay text view
                                score.visibility = View.VISIBLE
                                score2.visibility = View.INVISIBLE


                            }
                            snake.translationX = snakeX
                        }

                        "pause" -> {
                            snakeX += 0
                            snake.translationX = snakeX
                            resume.visibility = View.VISIBLE
                        }
                    }
                    checkSnakeBodyCollision()
                    checkFoodCollision()
                    handler.postDelayed(this, delayMillis)
                }
            }

            handler.postDelayed(runnable, delayMillis)


        }

        fun checkSnakeBodyCollision() {
            val newHeadX =
                snake.x + if (currentDirection == "left") -10 else if (currentDirection == "right") 10 else 0
            val newHeadY =
                snake.y + if (currentDirection == "up") -10 else if (currentDirection == "down") 10 else 0
            for (i in 0 until snakeSegments.size) {
                val segment = snakeSegments[i]
                val segmentX = segment.x
                val segmentY = segment.y


                if (newHeadX == segmentX && newHeadY == segmentY) { // Check if the head collides with a segment
                    // Handle collision, e.g., game over logic
                    border.setBackgroundColor(getResources().getColor(R.color.red))
                    playagain.visibility = View.VISIBLE
                    currentDirection = "pause"
                    lilu.visibility = View.INVISIBLE

                    score.text = "your score is  " + scorex.toString()
                    score.visibility = View.VISIBLE
                    score2.visibility = View.INVISIBLE
                    return
                }
            }
            val sharedPreferences =
                getSharedPreferences("DadosGuardadosPeloJogo", Context.MODE_PRIVATE)
            val serializedData = delayMillis

            if (delayMillis == 30L || scorex == 0) {

            } else {
                sharedPreferences.edit().putLong("delayMillis", delayMillis)
                sharedPreferences.edit().putInt("score", scorex)
            }
        }


        fun checkFoodCollision() {
            val distanceThreshold = 50

            val distance = sqrt((snake.x - meat.x).pow(2) + (snake.y - meat.y).pow(2))

            if (distance < distanceThreshold) { // Check if the distance between the snake head and the meat is less than the threshold

                val newSnake =
                    ImageView(this@MainActivity) // Create a new ImageView for the additional snake segment
                newSnake.setImageResource(R.drawable.snake)
                newSnake.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                board.addView(newSnake)

                snakeSegments.add(newSnake) // Add the new snake segment to the list
                val random = Random()
                val randomX = random.nextInt(801) - -100
                val randomY = random.nextInt(801) - -100


                meat.x = randomX.toFloat()
                meat.y = randomY.toFloat()


                delayMillis-- // Reduce delay value by 1
                scorex++
                val sharedPreferences =
                    getSharedPreferences("DadosGuardadosPeloJogo", Context.MODE_PRIVATE)
                val serializedData = delayMillis

                if (delayMillis == 30L || scorex == 0) {

                } else {
                    sharedPreferences.edit().putLong("delayMillis", delayMillis)
                    sharedPreferences.edit().putInt("score", scorex)
                }
                score2.text = "score : " + scorex.toString() // Update delay text view


            }
        }

        fun handleGameOver() {
            border.setBackgroundColor(getResources().getColor(R.color.red))
            playagain.visibility = View.VISIBLE
            currentDirection = "pause"
            lilu.visibility = View.INVISIBLE
            continueButton.visibility = View.VISIBLE
            score.text = "your score is  $scorex"
            score.visibility = View.VISIBLE
            score2.visibility = View.INVISIBLE
        }

        val runnable = object : Runnable {
            var snakeX = snake.x
            var snakeY = snake.y

            override fun run() {

                if (currentDirection != "pause") {
                    when (currentDirection) {
                        "up" -> {
                            snakeY -= 10
                            if (snakeY < -490) {
                                snakeY = -490f
                                handleGameOver()
                            }
                            snake.translationY = snakeY
                        }

                        "down" -> {
                            snakeY += 10
                            val maxY = board.height / 2 - snake.height + 30
                            if (snakeY > maxY) {
                                snakeY = maxY.toFloat()
                                handleGameOver()
                            }
                            snake.translationY = snakeY
                        }

                        "left" -> {
                            snakeX -= 10
                            if (snakeX < -490) {
                                snakeX = -490f
                                handleGameOver()
                            }
                            snake.translationX = snakeX
                        }

                        "right" -> {
                            snakeX += 10
                            val maxX = board.height / 2 - snake.height + 30
                            if (snakeX > maxX) {
                                snakeX = maxX.toFloat()
                                handleGameOver()
                            }
                            snake.translationX = snakeX
                        }
                    }
                    for (i in snakeSegments.size - 1 downTo 1) {
                        snakeSegments[i].x = snakeSegments[i - 1].x
                        snakeSegments[i].y = snakeSegments[i - 1].y
                    }


                    checkSnakeBodyCollision()
                    checkFoodCollision()
                }
                handler.postDelayed(this, delayMillis)
            }
        }



        resume.setOnClickListener {
            // Clear the board before adding elements
            board.removeAllViews()
            val sharedPreferences =
                getSharedPreferences("DadosGuardadosPeloJogo", Context.MODE_PRIVATE)
            val savedCurrentDirection = sharedPreferences.getString("currentDirection", "pause")
            val savedDelayMillis = sharedPreferences.getLong("delayMillis", 30L)
            val savedScore = sharedPreferences.getInt("score", 0)
            logOffButton.visibility = View.INVISIBLE
            currentDirection = savedCurrentDirection.toString()
            delayMillis = savedDelayMillis
            scorex = savedScore
            board.visibility = View.VISIBLE
            newgame.visibility = View.INVISIBLE
            score2.visibility = View.VISIBLE
            resume.visibility = View.INVISIBLE
            score2.setText(scorex.toString())
            snake.setImageResource(R.drawable.snake)
            snake.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val newSnake =
                ImageView(this) // Create a new ImageView for the additional snake segment
            newSnake.setImageResource(R.drawable.snake)
            newSnake.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )


            var snakeX = snake.x
            var snakeY = snake.y



            val logOffButton = findViewById<Button>(R.id.logOffButton)

            logOffButton.setOnClickListener {
                // Clear the saved variables in SharedPreferences
                val sharedPreferences = getSharedPreferences("DadosGuardadosPeloJogo", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                sharedPreferences.edit().putInt("codigo_acesso",0).apply()
                editor.clear()
                editor.apply()

                // Restart the activity
                restartActivity()
            }



            meat.setImageResource(R.drawable.food)
            meat.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )


            val random = Random() // create a Random object
            val randomX =
                random.nextInt(801) - 400 // generate a random x-coordinate between -400 and 400
            val randomY =
                random.nextInt(801) - 400 // generate a random y-coordinate between -400 and 400


            meat.x = randomX.toFloat()
            meat.y = randomY.toFloat()


            board.addView(snake)
            for (i in 0 until scorex) {
                snakeSegments.add(newSnake) // Add the new snake segment to the list
            }
            board.addView(meat)
            // Restart the game logic
            board.visibility = View.VISIBLE
            newgame.visibility = View.INVISIBLE
            resume.visibility = View.INVISIBLE

            // Start the game runnable
            handler.postDelayed(runnable, delayMillis)
            score2.text = "score : $scorex"
        }

// Set button onClickListeners to update the currentDirection variable when pressed
        upButton.setOnClickListener {
            if (currentDirection != "down") { // Only allow change to up if not moving down
                currentDirection = "up"
            }
        }
        downButton.setOnClickListener {
            if (currentDirection != "up") { // Only allow change to down if not moving up
                currentDirection = "down"
            }
        }
        leftButton.setOnClickListener {
            if (currentDirection != "right") { // Only allow change to left if not moving right
                currentDirection = "left"
            }
        }
        rightButton.setOnClickListener {
            if (currentDirection != "left") { // Only allow change to right if not moving left
                currentDirection = "right"
            }
        }


        playagain.setOnClickListener {
            val sharedPreferences = getSharedPreferences("DadosGuardadosPeloJogo", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("score",0)
            editor.putLong("delayMillis",30L)
            editor.putString("localJogo","start_game")
            editor.putInt("correct",0)
            editor.putFloat("percentage",0f)
            editor.apply()
            recreate()
        }
        continueButton.setOnClickListener {
            showContinueDialog()
        }
    }
    }
    private fun restartActivity() {
        val intent = Intent(this, this::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun showContinueDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Continue?")
        builder.setMessage("Do you want to continue from where you left off?")
        builder.setPositiveButton("Yes") { _, _ ->
            // Continue the game logic here
            // You might need to reset the game state and continue the handler
            // based on the current state of the game.
            orderedItemOrder ++
            localJogo = "resume_game"
            returnToGamEducation(orderedItemOrder)


        }
        builder.setNegativeButton("No") { _, _ ->
            // Start over logic here
            val sharedPreferences = getSharedPreferences("DadosGuardadosPeloJogo", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("score",0)
            editor.putLong("delayMillis",30L)
            editor.putString("localJogo","start_game")
            editor.putInt("correct",0)
            editor.putFloat("percentage",0f)
            editor.apply()
            recreate() // This restarts the activity

        }
        builder.show()
    }

    private fun returnToGamEducation(orderedItemOrder: Int) {

        val sharedPreferences = getSharedPreferences("DadosGuardadosPeloJogo", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("currentDirection", currentDirection)
        editor.putLong("delayMillis", delayMillis)
        editor.putInt("score", scorex)

        editor.apply()


        setContentView(R.layout.activity_gam_education)


        val webView = findViewById<WebView>(R.id.webView)

        // Enable JavaScript if needed
        webView.settings.javaScriptEnabled = true
        // Load a web page or HTML content

        localJogo = "resume_game"
        codigoAcesso = codigo_acesso.toString()
        GetPerguntaOrContentAsyncTask().execute(codigoAcesso, localJogo)


    }
    private inner class GetPerguntaOrContentAsyncTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String?): String {
            val codigoAcesso = params[0]
            val localJogo = params[1]

            val serverUrl = "http://10.0.2.2:80/framework/getCodigoAcesso.php?codigoAcesso=$codigoAcesso&identificacaoJogo=$localJogo"

            try {
                val url = URL(serverUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                return response.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                return "Error: ${e.message}"
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            try {

                val jsonResponse = JSONObject(result)

                // Check if the JSON response contains a redirectURL
                if (jsonResponse.has("redirectURL")) {
                    val redirectURL = "http://10.0.2.2:80/framework/"+ jsonResponse.getString("redirectURL")

                    // Load the redirect URL in the WebView
                    val webView = findViewById<WebView>(R.id.webView)

                    webView.settings.javaScriptEnabled = true

                    val jsInterface =
                        GamEducationInterface(this@MainActivity)
                    webView.addJavascriptInterface(jsInterface, "AndroidInterface");
                    webView.loadUrl(redirectURL)
                } else {
                    // Handle the case where there is no redirectURL in the response
                    // You can display an error message or take appropriate action
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle JSON parsing or other exceptions here
            }
        }
    }

    // Method to close the WebView
    fun closeWebView() {
        val webView = findViewById<WebView>(R.id.webView)
        runOnUiThread {
            if (webView != null) {
                webView.loadData("", "text/html", "utf-8")
                webView.clearCache(true)
                webView.clearHistory()
                webView.destroy()
            }
        }
    }
    inner class VerificaCodigoAcessoAsyncTask : AsyncTask<Int, Void, String>() {

        override fun doInBackground(vararg params: Int?): String {
            val codigo_acesso = params[0] ?: return "Error: Missing codigo_acesso"

            try {
                val serverUrl = "http://10.0.2.2:80/framework/verifica_codigo_acesso.php?codigo_acesso=$codigo_acesso"
                val url = URL(serverUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                return response.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("YourAppName", "Error: ${e.message}", e)
                return "Error: ${e.message}"
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            if (result == "codigo_acesso_exists") {
                // Update UI or perform actions here...
                localJogo = "start_game"
                val sharedPreferences = getSharedPreferences("DadosGuardadosPeloJogo", Context.MODE_PRIVATE)

                sharedPreferences.edit().putString("localJogo",localJogo).apply()
                (this@MainActivity as SharedPreferencesUpdateListener).onSharedPreferencesUpdateComplete()

            } else {
                // Update UI or perform actions here...
                restartActivity()
            }
        }
    }

    override fun onSharedPreferencesUpdateComplete() {
        recreate()
    }


}
interface SharedPreferencesUpdateListener {
    fun onSharedPreferencesUpdateComplete()
}