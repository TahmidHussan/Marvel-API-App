package com.example.chooseyourownapi

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers
import java.math.BigInteger
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {

    private lateinit var characterImage: ImageView
    private lateinit var characterName: TextView
    private lateinit var characterComics: TextView
    private lateinit var fetchCharacterButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        characterImage = findViewById(R.id.characterImage)
        characterName = findViewById(R.id.characterName)
        characterComics = findViewById(R.id.characterComics)
        fetchCharacterButton = findViewById(R.id.fetchCharacterButton)

        fetchCharacterButton.setOnClickListener {
            fetchCharacter()
        }

        fetchCharacter()
    }

    private var offset = 0

    private fun fetchCharacter() {
        val timestamp = System.currentTimeMillis().toString()
        val publicKey = "f67198abe6021508a178d5e69d380d36"
        val privateKey = "53ee3fba6b0f5fd5767a00d9a89be73ced79795b"
        val hash = stringToMd5(timestamp + privateKey + publicKey)

        offset += 25 // You can adjust this number based on how many characters you want to skip

        val url = "https://gateway.marvel.com/v1/public/characters?ts=$timestamp&apikey=$publicKey&hash=$hash&offset=$offset"

        val client = AsyncHttpClient()
        client.get(url, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON) {
                val results = json.jsonObject.getJSONObject("data").getJSONArray("results")
                if (results.length() > 0) {
                    val character = results.getJSONObject(0) // Fetching the first character
                    val name = character.getString("name")
                    val comicsAvailable = character.getJSONObject("comics").getInt("available")
                    val thumbnail = character.getJSONObject("thumbnail")
                    val imageUrl = thumbnail.getString("path") + "." + thumbnail.getString("extension")

                    runOnUiThread {
                        characterName.text = name
                        characterComics.text = "Comics available: $comicsAvailable"
                        Glide.with(this@MainActivity)
                            .load(imageUrl.replace("http://", "https://"))
                            .into(characterImage)
                    }
                }
            }

            override fun onFailure(statusCode: Int, headers: Headers?, response: String, throwable: Throwable?) {
                Log.d("MarvelError", response ?: "Unknown Error")
            }
        })
    }

    private fun stringToMd5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }
}