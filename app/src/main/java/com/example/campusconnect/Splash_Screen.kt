package com.example.campusconnect

import android.content.Context
import android.content.Intent

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.graph.authentication.IAuthenticationProvider
import com.microsoft.graph.requests.GraphServiceClient
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.SilentAuthenticationCallback
import com.microsoft.identity.client.exception.MsalException
import java.net.URL
import java.util.concurrent.CompletableFuture


import android.view.View
import androidx.appcompat.app.AppCompatDelegate

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


import com.example.campusconnect.databinding.ActivitySplashScreenBinding
import com.microsoft.graph.models.User
import com.microsoft.identity.client.*

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch



class Splash_Screen : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding

    companion object {
        private val auth: FirebaseAuth = Firebase.auth

        private val SCOPES = arrayOf("Files.Read.All")
//        private var mSingleAccountApp: ISingleAccountPublicClientApplication? = null
        private val TAG: String = Splash_Screen::class.java.simpleName //bala ta3me mn chila later
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val isDarkModeEnabled = sharedPreferences.getBoolean("is_dark_mode_enabled", false)
        val mode = if (isDarkModeEnabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)



        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.overlayView.setVisibility(View.INVISIBLE)
        binding.progressBar.setVisibility(View.INVISIBLE)
        binding.EnterButton.isEnabled=true

        creatingInstanceMicrosoft()


        binding.EnterButton.setOnClickListener {
            MS_Account_Object.mSingleAccountApp?.signIn(this, null, SCOPES, getAuthInteractiveCallback())
        }


//        binding.progressBar.visibility = View.INVISIBLE
//        binding.EnterButton.visibility = View.VISIBLE


    }

    private fun creatingInstanceMicrosoft() {
        PublicClientApplication.createSingleAccountPublicClientApplication(this.applicationContext,
            R.raw.auth_config_single_account,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication?) {
                    MS_Account_Object.mSingleAccountApp = application
                    loadAccount()

                }

                override fun onError(exception: MsalException?) {
                    Log.d(TAG, exception.toString())
                }
            })
    }


    // This is called when logging in for the first time
    // this will open the microsoft login screen and if all is good it will return the token
    private fun getAuthInteractiveCallback(): AuthenticationCallback {
        return object : AuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult?) {
                Log.d(TAG, "Successfully authenticated interactively")
                if (authenticationResult != null) {
                    callGraphAPI(authenticationResult)
                }
            }

            override fun onError(exception: MsalException?) {
                Log.d(TAG, "Authentication failed Interactively: " + exception.toString())
            }

            override fun onCancel() {
                Log.d(TAG, "User cancelled login.")
            }
        }
    }

    // This is called when you are already logged in
    // this will return the token
    private fun getAuthSilentCallback(): SilentAuthenticationCallback {
        return object : SilentAuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult?) {
                Log.d(TAG, "Successfully authenticated silently")
                /* Successfully got a token, use it to call a protected resource - MSGraph */
                binding.EnterButton.isEnabled=false
                binding.overlayView.setVisibility(View.VISIBLE)
                binding.progressBar.setVisibility(View.VISIBLE)
                callGraphAPI(authenticationResult!!)
            }

            override fun onError(exception: MsalException?) {
                Log.d(TAG, "Authentication failed Silently: " + exception.toString())
            }
        }
    }

    private fun loadAccount() {
        MS_Account_Object.mSingleAccountApp?.getCurrentAccountAsync(
            object : ISingleAccountPublicClientApplication.CurrentAccountCallback {
                override fun onAccountLoaded(activeAccount: IAccount?) {
                    if (activeAccount != null) {
//                            println(activeAccount.authority)
                        MS_Account_Object.mSingleAccountApp!!.acquireTokenSilentAsync(
                            SCOPES,
                            "https://login.microsoftonline.com/11a6c59d-5d73-4a47-b23e-f5fcf9bf009b",
                            getAuthSilentCallback()
                        )
                    }

//                        updateUI(activeAccount) // here to change if i loaded the account yaane eza bade ghayir l ui
                }

                override fun onAccountChanged(
                    priorAccount: IAccount?,
                    currentAccount: IAccount?
                ) {
                    Log.d(TAG, "Signed Out.")
                }

                override fun onError(exception: MsalException) {
                    Log.d(TAG, exception.toString())

                }
            }
        )
    }


    // this will return the information about the user from the token
    private fun callGraphAPI(authenticationResult: IAuthenticationResult) {

        val accessToken: String = authenticationResult.accessToken
        val graphClient = GraphServiceClient.builder()
            .authenticationProvider(object : IAuthenticationProvider {
                override fun getAuthorizationTokenAsync(requestUrl: URL): CompletableFuture<String> {
                    Log.d(TAG, "Authenticating request," + requestUrl.toString())
                    val accessTokenFuture: CompletableFuture<String> = CompletableFuture()
                    accessTokenFuture.complete(accessToken)
                    return accessTokenFuture
                }
            }).buildClient()








        GlobalScope.launch(Dispatchers.IO) {
            val result = graphClient.me().buildRequest().get() ?: return@launch
            if ((result.mail==null && result.userPrincipalName == null) || result.id == null) {
                println(result.userPrincipalName)
                println(result.id)
                println("Somehow i entered here")
                MS_Account_Object.mSingleAccountApp!!.signOut()

                GlobalScope.launch(Dispatchers.Main){
                    binding.EnterButton.isEnabled=true
                    binding.overlayView.setVisibility(View.INVISIBLE)
                    binding.progressBar.setVisibility(View.INVISIBLE)

                    Toast.makeText(applicationContext, "Not a valid email", Toast.LENGTH_SHORT).show()
                }
                return@launch
            } else {
                if (result.mail!=null){
                    firebaseAuthUsage(result.mail!!, result.id!!, result.displayName,result)
                }else{
                    firebaseAuthUsage(result.userPrincipalName!!, result.id!!, result.displayName,result)
                }


            }

        }

    }

    private fun firebaseAuthUsage(mail:String, pass:String, name:String?,result:User) {
        println("Entered firebase auth")
        auth.createUserWithEmailAndPassword(mail, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    println("Firebase creation Success")
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    if (result.mail=="ralph.tawil1@net.usj.edu.lb" || result.mail=="wael.ayoub@net.usj.edu.lb"||result.mail=="charbel.hajjmoussa@net.usj.edu.lb" ){
                        intent.putExtra("user","admin")
                    }else{
                        intent.putExtra("user","student")
                    }


                    intent.putExtra("name",result.displayName)


                    startActivity(intent)
                    finish()

                    println("Creation of user successful (Firebase)")
                    return@addOnCompleteListener
                } else {
                    auth.signInWithEmailAndPassword(mail, pass)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                println("Logging of user successful (Firebase)")
                                val intent = Intent(applicationContext, MainActivity::class.java)
                                if (result.mail=="ralph.tawil1@net.usj.edu.lb" || result.mail=="wael.ayoub@net.usj.edu.lb"||result.mail=="charbel.hajjmoussa@net.usj.edu.lb" ){
                                    intent.putExtra("user","admin")
                                }else{
                                    intent.putExtra("user","student")
                                }


                                intent.putExtra("name",result.displayName)


                                startActivity(intent)
                                finish()

                                println("The name that should appear is : " + name)

                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success")
                            } else {
                                println("Entered the worst case")
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.exception)
                                GlobalScope.launch(Dispatchers.IO) {
                                    MS_Account_Object.mSingleAccountApp?.signOut()
                                }
                                Toast.makeText(
                                    baseContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@addOnCompleteListener
                            }
                        }
                    println("Entered now the signing in")
                }
            }

    }
}