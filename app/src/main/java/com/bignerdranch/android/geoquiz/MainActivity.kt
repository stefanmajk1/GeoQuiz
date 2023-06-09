package com.bignerdranch.android.geoquiz

import Question
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.bignerdranch.android.geoquiz.databinding.ActivityMainBinding


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val quizViewModel: QuizViewModel by viewModels()

    private val cheatLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
// Handle the result
        if(result.resultCode == Activity.RESULT_OK){
            quizViewModel.isCheater =
                result.data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
        }
    }


    //    private val questionBank = listOf(
//        Question(R.string.question_australia, true),
//        Question(R.string.question_ocean, true),
//        Question(R.string.question_mideast, false),
//        Question(R.string.question_africa, false),
//        Question(R.string.question_americas, true),
//        Question(R.string.question_asia, true)
//    )
//
//    private var currentIndex = 0
    private val answers = ArrayList<String>()
    // private var correctAnswers = 0

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        Log.d(TAG, "onCreate(Bundle?) called")
//        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "Got a QuizViewModel: $quizViewModel")

        binding.trueButton.setOnClickListener{view: View->
            checkAnswer(true)
            isAnswered()
        }

        binding.falseButton.setOnClickListener{view: View->
            checkAnswer(false)
            isAnswered()
        }


        binding.prevButton.setOnClickListener{
            answers.remove(answers.last())
            if(quizViewModel.currentIndex == 0){
                quizViewModel.questionBank.size

            }else
                quizViewModel.moveToBack()
            if(quizViewModel.currentIndex ==0)
                binding.prevButton.isEnabled = false
            refreshButtons()
            updateQuestion()
        }
        binding.nextButton.setOnClickListener{

            binding.prevButton.isEnabled = true
            refreshButtons()
            nextQuestion()
        }

        binding.cheatButton.setOnClickListener{
            // val intent = Intent(this, CheatActivity::class.java)

            val answerIsTrue = quizViewModel.currentQuestionAnswer
            val intent = CheatActivity.newIntent(this@MainActivity, answerIsTrue)

            //  startActivity(intent)
            cheatLauncher.launch(intent)
        }

        binding.questionTextView.setOnClickListener{
            nextQuestion()
        }

        if(quizViewModel.currentIndex ==0 || quizViewModel.currentIndex == 1)
            binding.prevButton.isEnabled = false

        updateQuestion()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            blurCheatButton()
        }
    }


    //After the user answers a question, disable True and False buttons
    private fun isAnswered(){
        binding.trueButton.isEnabled = false
        binding.falseButton.isEnabled = false
    }

    //After the user presses the Next Button enable them
    private fun refreshButtons(){
        binding.trueButton.isEnabled = true
        binding.falseButton.isEnabled = true
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called ")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
    }

    override fun onStop(){
        super.onStop()
        Log.d(TAG, "onStop() called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
    }

    private fun nextQuestion(){

        //currentIndex = (currentIndex+1) % questionBank.size
        quizViewModel.moveToNext()

        updateQuestion()
        if(quizViewModel.currentIndex == 0){
            var number: Int = 0
            binding.prevButton.isEnabled = false
            for(item in answers.indices){
                if(answers[item] == "true"){
                    number= number +1
                }
            }
            var percent_correct = (number.toDouble() *100 )
            percent_correct = percent_correct / quizViewModel.questionBank.size
            val string_per ="The percentage of correct answers is: "+ percent_correct.toString() + "%"
            Toast.makeText(this,  string_per, Toast.LENGTH_LONG).show()
            answers.clear()
        }
        if(quizViewModel.currentIndex != answers.size){
            answers.add(" ")
        }

    }

    private fun updateQuestion(){
        // val questionTextResId = questionBank[currentIndex].textResId
        val questionTextResId = quizViewModel.currentQuestionText
        binding.questionTextView.setText(questionTextResId)
    }

    private fun checkAnswer(userAnswer: Boolean){
        // val correctAnswer = quizViewModel.questionBank[quizViewModel.currentIndex].answer
        val correctAnswer = quizViewModel.currentQuestionAnswer
//        val messageResId = if(userAnswer == correctAnswer){
//            R.string.correct_toast
//        }else{
//            R.string.incorrect_toast
//        }

        val messageResId = when{
            quizViewModel.isCheater -> R.string.judgment_toast
            userAnswer ==correctAnswer-> R.string.correct_toast
            else -> R.string.incorrect_toast
        }

        if(userAnswer == correctAnswer){
            answers.add("true")
        }else{
            answers.add("false")
        }

        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()
    }
    @RequiresApi(Build.VERSION_CODES.S)
    private fun blurCheatButton(){
        val effect = RenderEffect.createBlurEffect(
            10.0f,
            10.0f,
            Shader.TileMode.CLAMP
        )
        binding.cheatButton.setRenderEffect(effect)
    }
}