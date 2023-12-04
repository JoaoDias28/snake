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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.compose.material3.Button
import com.gameducation.gameducationlibrary.GamEducationLibrary
import com.GamEducation.snake.skinsClass
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Random

import kotlin.math.pow
import kotlin.math.sqrt


class MainActivity : Activity(),SharedPreferencesUpdateListener {

    var gamEducationLibrary: GamEducationLibrary? = null
    private lateinit var balloon: ImageView
    private lateinit var roulette: skinsClass
    private lateinit var rouletteLayout: LinearLayout
    var resume_game_bool = false;
    var localJogo = "new_game";
    var orderedItemOrder = 0
    var corretaOuVista = false;
    var tentativa = 0
    var delayMillis = 30L // Update snake position every 100 milliseconds
    var scorex = 0
    var currentDirection = "right" // Start moving right by default
    val correct = 0
    val percentage = 0
    private var activePowerup:String? = null
    val powerups = listOf<String>("ReduceVelocity","perderPartesJogador")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gam_education)

        val accessCodeWebView = findViewById<WebView>(R.id.webView)
        gamEducationLibrary = GamEducationLibrary(this, this)

        // Example usage:

        gamEducationLibrary?.showAccessCodeInputPage(accessCodeWebView, object : GamEducationLibrary.AccessCodeCallback {
            override fun onSuccess(accessCode: Boolean) {
                comecarJogo(this@MainActivity)
            }

            override fun onFailure() {
                // Handle failure
            }
        })

    }






    fun comecarJogo(context: Context){

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
        balloon = ImageView(this)
        val meat = ImageView(this)
        val snake = ImageView(this)
        val snakeSegments =
            mutableListOf(snake) // Keep track of the position of each snake segment
        val handler = Handler()
            val logOffButton = findViewById<Button>(R.id.logOffButton)

            gamEducationLibrary?.setupClearButton(logOffButton)



            val sharedPreferences = getSharedPreferences("DadosGuardadosPeloJogo", Context.MODE_PRIVATE)
            val percentage = sharedPreferences.getInt( "QuestionResultFromGamEducation", 0)


            localJogo = sharedPreferences.getString("localJogo","new_game").toString()
        Log.d("MainActivity","localJogo"+localJogo)
            if (localJogo == "resume_game" && percentage == 100 ){
                Log.d("MainActivity","percentage"+percentage.toString())
                resume.visibility = View.VISIBLE
                resume.performClick()
            } else{
                Log.d("MainActivity","percentage"+percentage.toString())
                resume.visibility = View.INVISIBLE
            }
            sharedPreferences.edit().remove("correct")
            sharedPreferences.edit().remove("percentage")
        continueButton.visibility = View.INVISIBLE
        board.visibility = View.INVISIBLE
        playagain.visibility = View.INVISIBLE
        score.visibility = View.INVISIBLE
        score2.visibility = View.INVISIBLE

         rouletteLayout = layoutInflater.inflate(R.layout.layout_roulette, null) as LinearLayout
        var roulette = skinsClass(this,rouletteLayout)

        // Call a method to show the roulette page
        val showRouletteButton = findViewById<Button>(R.id.rouletteButton)
        showRouletteButton.setOnClickListener {
            showRoulettePage()
        }

        var lastBalloonTime = 0L
        val balloonAppearanceInterval = 15 * 1000L

        newgame.setOnClickListener {
            // Clear the board before adding elements
            board.removeAllViews()



            logOffButton.visibility = View.INVISIBLE
            board.visibility = View.VISIBLE
            newgame.visibility = View.INVISIBLE
            score2.visibility = View.VISIBLE
            resume.visibility = View.INVISIBLE
            showRouletteButton.visibility = View.INVISIBLE

            snake.setImageResource(R.drawable.snake)
            snake.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            board.addView(snake)
            snakeSegments.add(snake) // Add the new snake segment to the list


            var snakeX = snake.x
            var snakeY = snake.y




                // Show the balloon
                val balloon = ImageView(this@MainActivity)
                balloon.setImageResource(R.drawable.baseline_coffee_24)
                balloon.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                board.addView(balloon)
                // Set the position of the balloon (you may need to adjust this based on your requirements)
                val random2 = Random()
                val randomX2 = random2.nextInt(801) - 400
                val randomY2 = random2.nextInt(801) - 400

                balloon.x = randomX2.toFloat()
                balloon.y = randomY2.toFloat()

                // Add the balloon to the board




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


            fun checkBalloonCollision(){
                Log.d("checkBallon","cheguei")
                val distanceThreshold = 50


                val distance = sqrt((snake.x - balloon.x).pow(2) + (snake.y - balloon.y).pow(2))
                Log.d("checkBallon","distance "+distance)
                Log.d("checkBallon","balloon.x "+balloon.x)
                Log.d("checkBallon","snake.x "+snake.x)
                if (distance < distanceThreshold) { // Check if the distance between the snake head and the meat is less than the threshold

                    Log.d("checkBallon","distancia menor")
                    setContentView(R.layout.activity_powerups)

                    val backButton = findViewById<Button>(R.id.buttonBack)
                    val card1 = findViewById<CardView>(R.id.card1)
                    val card2 = findViewById<CardView>(R.id.card2)
                    val card3 = findViewById<CardView>(R.id.card3)

                    backButton.setOnClickListener { recreate() }

                    val cardTemplate = LayoutInflater.from(this).inflate(R.layout.card_powerup, null) as LinearLayout
                    val cardImage = cardTemplate.findViewById<ImageView>(R.id.cardImage)
                    val cardTitle = cardTemplate.findViewById<TextView>(R.id.cardTitle)

                    cardImage.setImageResource(R.drawable.baseline_speed_24)
                    cardTitle.setText("Reduz a velocidade do Jogador (min: 0% - max: 60%)")

                    cardTemplate.removeAllViews()
                    card1.addView(cardImage)
                    card1.addView(cardTitle)

                    card1.setOnClickListener {

                        activePowerup = powerups.get(0)
                        setContentView(R.layout.activity_gam_education)

                        val webview:WebView = findViewById(R.id.webView)

                        gamEducationLibrary?.showQuestionPageAndAwait("resume_game",webview,object : GamEducationLibrary.QuestionCallback {
                            override fun onSuccess(result: Int) {
                                Log.d("mainActivity","cheguei ao showQUestion")

                                val percentageReduction = result * 0.6
                                val newDelayMillis = (delayMillis * (1.0 - percentageReduction)).toLong()

                                delayMillis = newDelayMillis
                                val sharedPreferences = getSharedPreferences("DadosGuardadosPeloJogo", Context.MODE_PRIVATE)
                                sharedPreferences.edit().putInt("QuestionResultFromGamEducation",100)
                                sharedPreferences.edit().putLong("delayMillis",delayMillis)

                                sharedPreferences.edit().apply()
                                recreate()

                            }
                        })
                    }


                    val cardTemplate2 = LayoutInflater.from(this).inflate(R.layout.card_powerup, null) as LinearLayout
                    val cardImage2 = cardTemplate2.findViewById<ImageView>(R.id.cardImage)
                    val cardTitle2 = cardTemplate2.findViewById<TextView>(R.id.cardTitle)
                    cardTemplate2.removeAllViews()
                    cardImage2.setImageResource(R.drawable.baseline_coffee_24)
                    cardTitle2.setText("Reduz o tamanho do Jogador pelo numero de partes (min: 0 - max: 5)")

                    card2.addView(cardImage2)
                    card2.addView(cardTitle2)

                    val sharedPreferences =
                        getSharedPreferences("DadosGuardadosPeloJogo", Context.MODE_PRIVATE)

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
                    checkBalloonCollision()
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
        fun checkBalloonCollision(){
            val distanceThreshold = 50
            Log.d("checkBallon","cheguei aqui tmb")
            val distance = sqrt((snake.x - balloon.x).pow(2) + (snake.y - balloon.y).pow(2))

            if (distance < distanceThreshold) { // Check if the distance between the snake head and the meat is less than the threshold
                Log.d("checkBallon","distancia menor")

                setContentView(R.layout.activity_powerups)

                val backButton = findViewById<Button>(R.id.buttonBack)
                val card1 = findViewById<CardView>(R.id.card1)
                val card2 = findViewById<CardView>(R.id.card2)
                val card3 = findViewById<CardView>(R.id.card3)

                backButton.setOnClickListener { recreate() }

                val cardTemplate = LayoutInflater.from(this).inflate(R.layout.card_powerup, null) as LinearLayout
                val cardImage = cardTemplate.findViewById<ImageView>(R.id.cardImage)
                val cardTitle = cardTemplate.findViewById<TextView>(R.id.cardTitle)


                cardImage.setImageResource(R.drawable.baseline_speed_24)
                cardTitle.setText("Reduz a velocidade do Jogador (min: 0% - max: 60%)")
                card1.addView(cardImage)
                card2.addView(cardTitle)

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

        fun handleGameOver() {
            border.setBackgroundColor(getResources().getColor(R.color.red))
            playagain.visibility = View.VISIBLE
            currentDirection = "pause"
            lilu.visibility = View.INVISIBLE
            continueButton.visibility = View.VISIBLE
            score.text = "your score is  $scorex"
            score.visibility = View.VISIBLE
            score2.visibility = View.INVISIBLE
            showRouletteButton.visibility = View.VISIBLE
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

                    checkBalloonCollision()
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
            showRouletteButton.visibility = View.INVISIBLE
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

            val sharedPreferences = getSharedPreferences("DadosGuardadosPeloJogo", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            editor.putString("localJogo",localJogo)
            editor.putString("currentDirection", currentDirection)
            editor.putLong("delayMillis", delayMillis)
            editor.putInt("score", scorex)





            setContentView(R.layout.activity_gam_education)


            val webView = findViewById<WebView>(R.id.webView)



            gamEducationLibrary?.showQuestionPageAndAwait("resume_game",webView, object : GamEducationLibrary.QuestionCallback {
                override fun onSuccess(result: Int) {
                    Log.d("mainActivity","cheguei")
                  editor.putInt("QuestionResultFromGamEducation",result)
                    editor.apply()
                    recreate()
                }
            })


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

    private fun showRoulettePage() {
        // Assuming you have a layout for the roulette page

        setContentView(R.layout.layout_roulette)
        var selectedSkin = 0;
        val rouletteContainer = findViewById<LinearLayout>(R.id.rouletteContainer)
        val spinButton = findViewById<Button>(R.id.spinButton)
        val voltarButton = findViewById<Button>(R.id.voltarButton)
        val wonSkinsContainer = findViewById<LinearLayout>(R.id.wonSkinsContainer)

        val sharedPreferences = getSharedPreferences("DadosGuardadosPeloJogo", Context.MODE_PRIVATE)

        val stringSet = setOf("R.drawable.snake")
        val userStringSet = sharedPreferences.getStringSet("skinsJogador",stringSet)
        val intList: List<Int> = userStringSet!!.map { it.toIntOrNull() ?: 0 }
        updateWonSkins(wonSkinsContainer,intList)
        voltarButton.setOnClickListener {
            recreate()
        }
        // Create an instance of the skinsClass
        roulette = skinsClass(this, findViewById(android.R.id.content))

        if (!this::rouletteLayout.isInitialized) {
            // If the layout is not initialized, create it and add it to the container
            rouletteLayout = roulette.createRouletteLayout()
            rouletteContainer.addView(rouletteLayout)
        }
        roulette.initRecyclerView()
        // Set click listener for the spin button
        spinButton.setOnClickListener {
            // Spin the roulette
            val (selectedSkin, updatedSkinsSet) = roulette.spin()





            Log.d("MainActivity",selectedSkin.toString())

        }

        // Set spin complete listener to handle UI updates after the spin
        roulette.setSpinCompleteListener{
            // Add any logic you need to perform after the spin is complete
            // This will be called when the roulette.completeSpin() is invoked
            setContentView(R.layout.activity_gam_education)


            val webView = findViewById<WebView>(R.id.webView)
            // Save the selected skin to the list of won skins
            gamEducationLibrary?.showQuestionPageAndAwait("resume_game",webView, object : GamEducationLibrary.QuestionCallback {
                override fun onSuccess(result: Int) {
                    Log.d("mainActivity", "cheguei")

                    Log.d("mainActivity", "selectedSkin" + selectedSkin.toString())


                    setContentView(R.layout.activity_prize)

                    val voltarButton = findViewById<Button>(R.id.buttonprize)
                    val imageViewPrize = findViewById<ImageView>(R.id.imageViewprize)
                    val textViewPrize1 = findViewById<TextView>(R.id.textView1prize)
                    val textViewPrize2 = findViewById<TextView>(R.id.textView2prize)

                    textViewPrize1.setText("O seu resultado foi: " + result.toString())
                    voltarButton.setOnClickListener {
                        recreate()
                    }
                    if (result > 50) {
                        textViewPrize2.setText("Parabéns ganhou o seguinte prémio: ")
                        val stringSet = setOf("0")
                        val durationMillis = 2000L
                        val handler = Handler()
                        handler.postDelayed({
                            var sharedPreferences =
                                getSharedPreferences("DadosGuardadosPeloJogo", Context.MODE_PRIVATE)

                            var userStringSet =
                                sharedPreferences.getStringSet("skinsJogador", stringSet)
                            if (userStringSet!!.isNotEmpty()) {
                                // Get the last element from the set
                                val lastSkinString = userStringSet!!.last()

                                var firstSkinString = userStringSet!!.first()
                                // Convert the string back to an integer (assuming it's the drawable resource ID)
                                val firstSkinDrawableId = firstSkinString.toIntOrNull()

                                // If the conversion is successful and the drawable ID is valid, set it to the ImageView
                                if (firstSkinDrawableId != null && firstSkinDrawableId != 0) {

                                    imageViewPrize.setImageResource(firstSkinDrawableId)
                                } else {

                                    // Handle the case where the conversion fails or the drawable ID is not valid
                                    // You might want to set a default image or display an error message
                                    imageViewPrize.setImageResource(R.drawable.snake)
                                }
                            }
                        }, durationMillis)
                    }
                else {
                        textViewPrize2.setText("Não ganhou prémio, tente novamente!")
                }
             }
            })
        }
    }
    private fun updateWonSkins(container: LinearLayout, wonSkins: List<Int>) {
        container.removeAllViews()

        for (wonSkin in wonSkins) {
            val wonSkinImageView = ImageView(this)
            wonSkinImageView.setImageResource(wonSkin)
            container.addView(wonSkinImageView)
        }
    }

    override fun onSharedPreferencesUpdateComplete() {
        recreate()
    }


}
interface SharedPreferencesUpdateListener {
    fun onSharedPreferencesUpdateComplete()
}

data class Skin(val name:String, val imageResource: Int)

