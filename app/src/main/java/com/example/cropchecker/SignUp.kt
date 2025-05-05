package com.example.cropchecker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cropchecker.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUp : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        binding.textLogin.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
        }
        binding.buttonSignup.setOnClickListener {
            val email = binding.emialET.text.toString()
            val pass = binding.passEt.text.toString()
            val confirmpass = binding.passCEt.text.toString()



            if (email.isNotEmpty() && pass.isNotEmpty() && confirmpass.isNotEmpty()) {

                if (pass == confirmpass) {
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val intent = Intent(this, SignIn::class.java)
                            startActivity(intent)
                            Toast.makeText(this, "Account Created", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_LONG).show()
                        }
                    }

                } else {
                    Toast.makeText(this, "Confirm password is Incorrect", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed", Toast.LENGTH_LONG).show()
            }

        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}