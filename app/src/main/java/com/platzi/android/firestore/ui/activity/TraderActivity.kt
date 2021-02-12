package com.platzi.android.firestore.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.platzi.android.firestore.R
import com.platzi.android.firestore.adapter.CryptosAdapter
import com.platzi.android.firestore.adapter.CryptosAdapterListener
import com.platzi.android.firestore.model.Crypto
import com.platzi.android.firestore.model.User
import com.platzi.android.firestore.network.Callback
import com.platzi.android.firestore.network.FirestroeService
import com.platzi.android.firestore.network.RealTimeDataListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_trader.*
import java.lang.Exception


/**
 * @author Santiago Carrillo
 * 2/14/19.
 */
class TraderActivity : AppCompatActivity(), CryptosAdapterListener {

    lateinit var firestroeService: FirestroeService

    private val cryptosAdapter: CryptosAdapter = CryptosAdapter(this)

    private var userName: String? = null

    private var user: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trader)
        firestroeService = FirestroeService(FirebaseFirestore.getInstance())

        userName = intent.extras!![USERNAME_KEY]!!.toString()
        usernameTextView.text = userName

        configureRecyclerView()
        loadCryptos()

        fab.setOnClickListener { view ->
            Snackbar.make(view, getString(R.string.generating_new_cryptos), Snackbar.LENGTH_SHORT)
                .setAction("Info", null).show()
            generateCryptoCurrenciesRandom()
        }

    }

    private fun generateCryptoCurrenciesRandom() {
        for (crypto in cryptosAdapter.cryptoList) {
            val amount = (1..10).random()
            crypto.available += amount
            firestroeService.updateCrypto(crypto)
        }
    }

    private fun loadCryptos() {
        firestroeService.getCryptos(object : Callback<List<Crypto>> {
            override fun onSuccess(cryptoList: List<Crypto>?) {
                firestroeService.findUserById(userName!!, object : Callback<User> {
                    override fun onSuccess(result: User?) {
                        user = result
                        if (user!!.cryptosList == null) {
                            val userCryptoList = mutableListOf<Crypto>()

                            for (crypto in cryptoList!!) {
                                val cryptoUser = Crypto()
                                cryptoUser.name = crypto.name
                                cryptoUser.available = crypto.available
                                cryptoUser.imageUrl = crypto.imageUrl
                                userCryptoList.add(cryptoUser)
                            }
                            user!!.cryptosList = userCryptoList
                            firestroeService.updateUser(user!!, null)
                        }

                        loadUserCrypto()
                        addRealTimeDatabeseListeners(user!!, cryptoList!!)

                    }

                    override fun onFailed(exception: Exception) {
                        showGeneralServerErrorMessage()
                    }

                })

                this@TraderActivity.runOnUiThread {
                    cryptosAdapter.cryptoList = cryptoList!!
                    cryptosAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailed(exception: Exception) {
                Log.e("TraderActivity", "error loading cryptos", exception)
                showGeneralServerErrorMessage()
            }

        })
    }

    private fun addRealTimeDatabeseListeners(user: User, cryptoList: List<Crypto>) {
        firestroeService.listenForUpdate(user, object : RealTimeDataListener<User>{

            override fun onDataChange(updatedData: User) {
                this@TraderActivity.user = updatedData
                loadUserCrypto()
            }

            override fun onError(exception: Exception) {
                showGeneralServerErrorMessage()
            }

        })

        firestroeService.listenForUpdates(cryptoList, object : RealTimeDataListener<Crypto> {
            override fun onDataChange(updatedData: Crypto) {
                var pos = 0;
                for (crypto in cryptosAdapter.cryptoList){
                    if (crypto.name.equals(updatedData.name)) {
                        crypto.available = updatedData.available
                        cryptosAdapter.notifyItemChanged(pos)
                        pos ++
                    }
                }
            }

            override fun onError(exception: Exception) {
                showGeneralServerErrorMessage()
            }

        })

    }

    private fun loadUserCrypto() {
        runOnUiThread {
            if (user != null && user!!.cryptosList != null) {
                infoPanel.removeAllViews()
                for (crypto in user!!.cryptosList!!) {
                    addUserCryptoInfoRow(crypto)
                }
            }
        }
    }

    private fun addUserCryptoInfoRow(crypto: Crypto) {
        val view = LayoutInflater.from(this).inflate(R.layout.coin_info, infoPanel, false)
        view.findViewById<TextView>(R.id.coinLabel).text =
            getString(R.string.coin_info, crypto.name, crypto.available.toString())

        Picasso.get().load(crypto.imageUrl).into(view.findViewById<ImageView>(R.id.coinIcon))

        infoPanel.addView(view)
    }

    private fun configureRecyclerView() {
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = cryptosAdapter
    }

    fun showGeneralServerErrorMessage() {
        Snackbar.make(
            fab,
            getString(R.string.error_while_connecting_to_the_server),
            Snackbar.LENGTH_LONG
        )
            .setAction("Info", null).show()
    }

    override fun onBuyCryptoClicked(crypto: Crypto) {
        var flag = false
        if (crypto.available > 0) {
            for (userCrypto in user!!.cryptosList!!) {
                if (userCrypto.name == crypto.name) {
                    userCrypto.available += 1
                    flag = true
                    break
                }
            }
            if(!flag){
                val cryptoUser = Crypto()
                cryptoUser.name = crypto.name
                cryptoUser.available = 1
                cryptoUser.imageUrl = crypto.imageUrl
                user!!.cryptosList!!.toMutableList().add(cryptoUser)
            }

            crypto.available--

            firestroeService.updateUser(user!!, null)
            firestroeService.updateCrypto(crypto)
        }
    }
}