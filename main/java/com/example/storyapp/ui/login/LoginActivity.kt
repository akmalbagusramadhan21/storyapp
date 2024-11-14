package com.example.storyapp.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.storyapp.R
import com.example.storyapp.data.request.LoginRequest
import com.example.storyapp.ui.main.ViewModelFactory
import com.example.storyapp.ui.main.MainActivity
import com.example.storyapp.ui.register.RegisterActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar

    private val loginViewModel: LoginViewModel by viewModels(){
        ViewModelFactory.getInstance(this)
    }


    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        progressBar = findViewById(R.id.progressBar)

        val passwordEditText = findViewById<TextInputEditText>(R.id.passwordEditText)
        val passwordLayout = findViewById<TextInputLayout>(R.id.passwordEditTextLayout)

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                val password = charSequence.toString()

                if (password.length < 8) {
                    passwordLayout.error = getString(R.string.error_password)  // Menampilkan error
                } else {
                    passwordLayout.error = null
                }
            }

            override fun afterTextChanged(editable: Editable?) {}
        })

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            val email = findViewById<TextInputEditText>(R.id.emailEditText).text.toString()
            val password = findViewById<TextInputEditText>(R.id.passwordEditText).text.toString()
            passwordLayout.error = null

            if (email.isNotEmpty() && password.isNotEmpty()) {
                showLoading(true)
                loginViewModel.login(LoginRequest(email, password), onSuccess = {
                    showLoading(false)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }, onError = { errorMessage ->
                    showLoading(false)
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                })
            } else {
                Toast.makeText(this, "Email dan Password harus diisi", Toast.LENGTH_SHORT).show()
            }
        }

        loginViewModel.getSession().observe(this) { user ->
            if (user.isLogin) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        findViewById<TextView>(R.id.registerTextView).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
    private fun showLoading(isLoading: Boolean) {
        val animation = if (isLoading) {
            AlphaAnimation(0f, 1f).apply {
                duration = 300
            }
        } else {
            AlphaAnimation(1f, 0f).apply {
                duration = 300
            }
        }
        progressBar.startAnimation(animation)
        progressBar.visibility = if (isLoading) ProgressBar.VISIBLE else ProgressBar.GONE
    }
}